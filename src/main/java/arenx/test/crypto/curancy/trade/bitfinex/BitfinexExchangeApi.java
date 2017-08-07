package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.Order;
import arenx.test.crypto.curancy.trade.Order.Type;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Channel;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Event;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Frequency;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Precision;

@Component
@Scope("singleton")
@Lazy
public class BitfinexExchangeApi {

    private static class OrderKey implements Comparable<OrderKey>{

        public String symbol;
        public Order.Type type;
        public double price;

        public OrderKey(String symbol, Type type, double price) {
            super();
            this.symbol = symbol;
            this.type = type;
            this.price = price;
        }

        @Override
        public int compareTo(OrderKey o) {
            int r = Double.compare(price, o.price);

            if (0 != r) {
                return r;
            }

            r = type.compareTo(o.type);

            if (0 != r) {
                return r;
            }

            return symbol.compareTo(o.symbol);
        }

    }

    private static Logger logger = LoggerFactory.getLogger(BitfinexExchangeApi.class);

    private BlockingDeque<TextMessage> sendWsQueue = new LinkedBlockingDeque<>();
    private BlockingDeque<TextMessage> receiveWsQueue = new LinkedBlockingDeque<>();
    private SortedMap<Integer, String> subscribedBooks = Collections.synchronizedSortedMap(new TreeMap<>());
    private SortedMap<OrderKey, Order> orders = new TreeMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean isNeedRestart = new AtomicBoolean(false);
    private AtomicBoolean isStopped = new AtomicBoolean(false);

    private WebSocketConnectionManager wsm;
    private WebSocketSession ws;
    private OrderUpdateListener orderUpdateListener;
    private Runnable reconnectListener;
    private Thread sendWsWorkerThread;
    private Thread receiveWsWorkerThread;
    private Thread watchdogThread;

    private Runnable sendWsWorker = ()->{
        while (!isStopped.get()) {
            TextMessage send = null;

            try {
                send = sendWsQueue.pollFirst(1, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }

            if (null == send) {
                continue;
            }

            try {
                ws.sendMessage(send);
            } catch (IOException e) {
                logger.error("Failed to send message [{}]", send.getPayload());
                throw new RuntimeException("Failed to send message", e);
            }
        }

        logger.info("Worker of sending web socket message is stopped");
    };

    private Runnable receiveWsWorker = ()->{
        while (!isStopped.get()) {
            TextMessage receive = null;

            try {
                receive = receiveWsQueue.pollFirst(1, TimeUnit.SECONDS);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (null == receive) {
                continue;
            }

            try {
                handle(receive);
            } catch (Throwable e) {
                logger.error("Failed to handle received message [{}]", receive.getPayload());
                throw new RuntimeException("Failed to send message", e);
            }
        }

        logger.info("Worker of handling web socket message is stopped");
    };

    private Runnable watchdog = ()->{
        while(isStopped.get()){
            if (isNeedRestart.get()) {

                logger.info("Prepare to restart");

                isNeedRestart.set(false);

                disconnect();

                if (null != reconnectListener) {
                    reconnectListener.run();
                }
                connect();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        disconnect();
    };

    public void subscribeBook(Set<Currency> currencies) throws InterruptedException{
        Validate.notNull(currencies);
        Validate.isTrue(currencies.size() == 2);

        String symbol = BitfniexUtils.toSymbol(currencies);

        WebSocketBean bean = new WebSocketBean();

        bean.event = Event.SUBSCRIBE;
        bean.channel = Channel.BOOK;
        bean.symbol = symbol;
        bean.precision = Precision.P0;
        bean.frequency = Frequency.F0;
        bean.length = 25;

        String json;
        try {
            json = mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Fail to serialize bean", e);
        }

        TextMessage t = new TextMessage(json);
        sendWsQueue.push(t);
    }

    public void setOrderUpdateListener(OrderUpdateListener listener){
        Validate.notNull(listener);
        this.orderUpdateListener = listener;
    }

    public void setReconnectListener(Runnable listener){
        Validate.notNull(listener);
        this.reconnectListener = listener;
    }

    @PostConstruct
    private void start() {

        connect();

        logger.info("create watch dog");
        watchdogThread = new Thread(watchdog);
        watchdogThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            stop();
        }));
    }

    @PreDestroy
    private void stop(){
        isStopped.set(true);

        disconnect();
    }

    private void handle(TextMessage receive) throws JsonParseException, JsonMappingException, IOException{

        ChannelBean ch = null;

        try {
            ch = ChannelBean.parse(receive.getPayload());
        } catch (IllegalArgumentException e) {
            // maybe it is not a channel message.
            // do nothing
        }

        if (null != ch) {
            handle(ch);
            return;
        }

        WebSocketBean wsbean = mapper.readValue(receive.getPayload(), WebSocketBean.class);
        handle(wsbean);
    }

    private void handle(ChannelBean ch){
        String symbol = subscribedBooks.get(ch.id);

        List<Currency> currencies = BitfniexUtils.toCurrencies(symbol);

        for (List<Double> l: ch.data) {
            OrderKey key = null;

            double price = l.get(0);
            double count = l.get(1);
            double amount = l.get(2);

            if (0 == count) {
                if (1 == amount) {
                    key = new OrderKey(symbol, Order.Type.BID, price);
                } else if (-1 == amount) {
                    key = new OrderKey(symbol, Order.Type.ASK, price);
                } else {
                    throw new RuntimeException("invalid data");
                }

                orders.remove(key);

            } else if (0 < count) {
                if (0 < amount) {
                    key = new OrderKey(symbol, Order.Type.BID, price);
                } else if (0 > amount) {
                    key = new OrderKey(symbol, Order.Type.ASK, price);
                } else {
                    throw new RuntimeException("invalid data");
                }

                Order old = orders.get(key);

                if (null == old) {
                    old = new Order();
                    old.setExchange("bitfinex");
                    old.setFromCurrency(currencies.get(0));
                    old.setFromCurrency(currencies.get(1));
                    old.setPrice(price);
                    old.setType(key.type);
                    old.setVolume(0.0);
                    orders.put(key, old);
                }

                old.setVolume(old.getVolume() + Math.abs(amount));

                if (null != orderUpdateListener) {
                    orderUpdateListener.OnUpdate(old);
                }

            } else {
                throw new RuntimeException("invalid data");
            }
        }
    }

    private void handle(WebSocketBean bean){
        switch (bean.event) {
        case INFO:
            handleInfo(bean);
            break;
        case SUBSCRIBED:
            handleSubscribed(bean);
            break;
        default:
            throw new RuntimeException(String.format("unrecognized event [%s]", bean.event));
        }
    }

    private void handleSubscribed(WebSocketBean bean){

        logger.error("bean = {}", bean);

        if (bean.channel == Channel.BOOK) {
            subscribedBooks.put(bean.channelId, bean.symbol);
        }
    }

    private void handleInfo(WebSocketBean bean){
        if (null != bean.version) {
            logger.info("using version [{}] of Bitfinex Websocket", bean.version);
            return;
        }

        switch (bean.code) {
        case 20051:
            logger.info("Bitfniex server is restarted. Begine restart procedure");
            isNeedRestart.set(true);
            break;
        case 20060:
            logger.info("Bitinex is Refreshing data from the Trading Engine. Begine restart procedure");
            isNeedRestart.set(true);
            break;
        case 20061:
            logger.info("Bitinex finished Refreshing data from the Trading Engine. Begine restart procedure");
            isNeedRestart.set(true);
            break;
        default:
            logger.error("unknown error force stop");
            isStopped.set(true);
        }
    }



    private void disconnect(){
        if (null != sendWsWorkerThread) {
            logger.info("join sending thread of web socket");
            try {
                sendWsWorkerThread.join();
            } catch (InterruptedException e) {
                new RuntimeException("Was interrupted after joining sendWsWorkerThread", e);
            }
        }

        if (null != receiveWsWorkerThread) {
            logger.info("join sending thread of web socket");
            try {
                receiveWsWorkerThread.join();
            } catch (InterruptedException e) {
                new RuntimeException("Was interrupted after joining receiveWsWorkerThread", e);
            }
        }

        if (null != wsm) {
            CountDownLatch isStop = new CountDownLatch(1);

            logger.info("Stop web socket manager");
            wsm.stop(()->isStop.countDown());

            try {
                isStop.await();
            } catch (InterruptedException e) {
                new RuntimeException("was interrupted when stopping web socket manager", e);
            }
        }

        sendWsQueue.clear();
        receiveWsQueue.clear();
    }

    private void connect(){

        logger.info("create web socket connection");
        startWebSocketConnection();

        logger.info("connected");
    }

    private void startWebSocketConnection(){
        WebSocketClient client = new JettyWebSocketClient();
        CountDownLatch isConnected = new CountDownLatch(1);

        wsm = new WebSocketConnectionManager(
                client,
                new WebSocketHandler(){

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                        logger.info("connection to Bitfinex is closed({}).", status);
                        wsm.stop(()->logger.info("web socket manager is stopped"));
                    }

                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        logger.info("connection to Bitfinex is established.");
                        isConnected.countDown();
                        ws = session;
                    }

                    @Override
                    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                        logger.debug("recieve message [{}]", message.getPayload());
                        TextMessage m = (TextMessage) message;
                        receiveWsQueue.push(m);
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
                        logger.error("recieve error from Bitfinex [{}]", e.getMessage());
                        logger.debug("recieve error from Bitfinex", e);
                        throw new Exception(e);
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                        return false;
                    }},
                "wss://api.bitfinex.com/ws/2"
            );

        wsm.start();

        boolean isReady = false;;
        try {
            isReady = isConnected.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }

        if (!isReady) {
            logger.error("Failed to establish connection to bitfinex. Stop web socket manager.");
            wsm.stop(()->logger.info("web socket manager is stopped"));
            throw new RuntimeException("Failed to create connection to bitfinex");
        }

        sendWsWorkerThread = new Thread(sendWsWorker, "Bitfinex-ws-send-thread");
        sendWsWorkerThread.setUncaughtExceptionHandler((thread, e)->{
            logger.error("something was wrong in [" + thread.getName() + "]", e);
            System.exit(-1);
        });

        logger.info("Start sending thread [{}] of Bitfinex web socket", sendWsWorkerThread.getName());
        sendWsWorkerThread.start();

        receiveWsWorkerThread = new Thread(receiveWsWorker, "Bitfinex-ws-receive-thread");
        sendWsWorkerThread.setUncaughtExceptionHandler((thread, e)->{
            logger.error("something was wrong in [" + thread.getName() + "]", e);
            System.exit(-1);
        });

        logger.info("Start receiving thread [{}] of Bitfinex web socket", receiveWsWorkerThread.getName());
        receiveWsWorkerThread.start();
    }
}
