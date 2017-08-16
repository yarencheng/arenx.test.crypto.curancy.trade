package arenx.test.crypto.curancy.trade;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;

@Component
@Scope("prototype")
public abstract class BaseWebSocketClient {

    public enum Status{
        IDLE,
        PRE_START,
        STARTED,
        RECONNECT,
        PRE_STOP,
        STOPPED
    }

    private static Logger logger = LoggerFactory.getLogger(BaseWebSocketClient.class);

    @Autowired
    private WebSocketClient webSocketClient;

    private BlockingDeque<TextMessage> sendWsQueue = new LinkedBlockingDeque<>();
    private BlockingDeque<TextMessage> receiveWsQueue = new LinkedBlockingDeque<>();
    private List<Throwable> uncaughtExceptions = Collections.synchronizedList(new ArrayList<>());
    private AtomicReference<WebSocketConnectionManager> wsm = new AtomicReference<>();
    private AtomicReference<WebSocketSession> ws = new AtomicReference<>();
    private volatile Status status = Status.IDLE;
    private AtomicReferenceFieldUpdater<BaseWebSocketClient, Status> atomicStatus = AtomicReferenceFieldUpdater.newUpdater(BaseWebSocketClient.class, Status.class, "status");
    private Thread sendWsWorkerThread;
    private Thread receiveWsWorkerThread;

    private Runnable sendWsWorker = ()->{

        logger.info("Worker starts to send message to [{}]", getURI());

        while (wsm.get().isRunning()) {

            TextMessage send = null;

            try {
                if (null == ws.get()) {
                    Thread.sleep(1000);
                    continue;
                }
                send = sendWsQueue.pollLast(1, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }

            if (null == send) {
                continue;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("send [{}]", send.getPayload());
            }

            try {
                ws.get().sendMessage(send);
            } catch (IOException e) {
                String message = String.format("Failed to send message [%s] to [%s].", send.getPayload(), getURI());
                logger.error(message, e);
                throw new RuntimeException("message", e);
            }
        }

        logger.info("Worker stop to send message to [{}]", getURI());
    };

    private Runnable receiveWsWorker = ()->{

        logger.info("Worker starts to receive message from [{}]", getURI());

        while (wsm.get().isRunning()) {
            TextMessage receive = null;

            try {
                receive = receiveWsQueue.pollLast(1, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }


            if (null == receive) {
                continue;
            }

            try {
                onMessageReceive(receive.getPayload());
            } catch (Throwable e) {
                String message = String.format("Failed to handle message [%s] from [%s].", receive.getPayload(), getURI());
                logger.error(message, e);
                throw new RuntimeException("message", e);
            }
        }

        logger.info("Worker stops to receive message from [{}]", getURI());
    };

    protected BaseWebSocketClient(){
        atomicStatus.set(this, Status.IDLE);
    }

    protected abstract void onMessageReceive(String message) throws Exception;
    protected abstract URI getURI();

    protected void sendMessage(CharSequence message) {

        if (!wsm.get().isRunning()) {
            String m = String.format("Can't send a message [%s] to closed client", message);
            logger.debug(m);
            throw new RuntimeException(m);
        }

        sendWsQueue.add(new TextMessage(message));
    }

    @PostConstruct
    private void start() throws WebSocketClientException, InterruptedException {
        if (!atomicStatus.compareAndSet(this, Status.IDLE, Status.PRE_START)) {
            throw new WebSocketClientException(String.format("Can't start client when status is not idle instead of ", atomicStatus.get(this)));
        }

        startInternal();

        atomicStatus.set(this, Status.STARTED);
    }

    @PreDestroy
    private void stop() throws WebSocketClientException{
        if (!atomicStatus.compareAndSet(this, Status.STARTED, Status.PRE_STOP)) {
            throw new WebSocketClientException(String.format("Can't start client when status is not started instead of ", atomicStatus.get(this)));
        }

        logger.info("stop client of [{}]", getURI());

        wsm.get().stop();

        atomicStatus.set(this, Status.STOPPED);
    }

    protected void reconnect() throws WebSocketClientException, InterruptedException{
        if (!atomicStatus.compareAndSet(this, Status.STARTED, Status.RECONNECT)) {
            throw new WebSocketClientException(String.format("Can't reconnect client when status is not started instead of ", atomicStatus.get(this)));
        }

        logger.info("reconnect client of [{}]", getURI());

        startInternal();

        atomicStatus.set(this, Status.STARTED);
    }

    private void startInternal() throws WebSocketClientException, InterruptedException{
        if (null != wsm.get()) {
            logger.info("Stop WebSocketConnectionManager");
            wsm.get().stop();
        }

        if (null != sendWsWorkerThread) {
            logger.info("join thread [{}]", sendWsWorkerThread.getName());
            sendWsWorkerThread.join();
        }

        if (null != receiveWsWorkerThread) {
            logger.info("join thread [{}]", receiveWsWorkerThread.getName());
            receiveWsWorkerThread.join();
        }

        logger.info("start client of [{}]", getURI());

        WebSocketConnectionManager w = createWebSocketConnectionManager();
        w.start();
        wsm.set(w);

        sendWsWorkerThread = new Thread(sendWsWorker, String.format("SendWorker[%s]",getURI()));
        sendWsWorkerThread.setUncaughtExceptionHandler((thread, e)->{
            logger.error("something was wrong in [" + thread.getName() + "]", e);
            uncaughtExceptions.add(e);
        });

        receiveWsWorkerThread = new Thread(receiveWsWorker, String.format("ReceiveWorker[%s]",getURI()));
        receiveWsWorkerThread.setUncaughtExceptionHandler((thread, e)->{
            logger.error("something was wrong in [" + thread.getName() + "]", e);
            uncaughtExceptions.add(e);
        });

        sendWsWorkerThread.start();
        receiveWsWorkerThread.start();
    }

    private WebSocketConnectionManager createWebSocketConnectionManager(){
        WebSocketConnectionManager w = new WebSocketConnectionManager(
                webSocketClient,
                new WebSocketHandler(){

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                        logger.info("connection to [{}] is closed.", getURI());
                    }

                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        logger.info("connection to [{}] is established.", getURI());
                        ws.set(session);
                    }

                    @Override
                    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                        logger.debug("Receive message [{}]", message.getPayload());
                        TextMessage m = (TextMessage) message;
                        receiveWsQueue.push(m);
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
                        logger.error("get error [{}]", e.getMessage());
                        uncaughtExceptions.add(e);
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                        return false;
                    }},
                getURI().toString()
            );
        return w;
    }
}
