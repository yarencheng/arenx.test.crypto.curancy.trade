package arenx.test.crypto.curancy.trade.poloniex;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.OrderUpdateListener;
import ws.wamp.jawampa.WampClient;

@Component
@Scope("prototype")
public class PoloniexExchange {

    private static Logger logger = LoggerFactory.getLogger(PoloniexExchange.class);

    @Autowired
    private WampClient wampClient;

    private AtomicBoolean isReconnect = new AtomicBoolean(false);
    private Runnable reconnectListener;
    private OrderUpdateListener oderUpdateListener;


    public void subscribeOrder(Set<Currency> currencies) {
        String symbol = PoloniexUtils.toSymbol(currencies);

        logger.info("subscribe order of [{}]", symbol);

        CountDownLatch isDone = new CountDownLatch(1);

        wampClient
        .makeSubscription(symbol, Object.class)
        .subscribe(
            (ticker)->{
                logger.info("symbol {} {}", symbol, ticker);
            },
            (e)->{
                logger.error("Failed to subscribe " + symbol, e);
            },
            ()->{
                logger.info("subscribe order of [{}] sucessfully", symbol);
                isDone.countDown();
            });

        try {
            if(!isDone.await(10, TimeUnit.MINUTES)){
                throw new RuntimeException("Failed to subscribe " + symbol);
            }
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }
    }


    public void setOrderUpdateListener(OrderUpdateListener listener) {
        Validate.notNull(listener);
        oderUpdateListener = listener;
    }

    public void setReconnectListener(Runnable listener) {
        Validate.notNull(listener);
        reconnectListener = listener;
    }

    @PostConstruct
    private void start() throws InterruptedException {

        CountDownLatch isConnected = new CountDownLatch(1);

        wampClient.statusChanged().subscribe((WampClient.State status)->{

            if (status instanceof WampClient.ConnectingState) {
                logger.info("is connecting to poloniex [{}]", status);
                if (isReconnect.get() && null!= reconnectListener) {
                    reconnectListener.run();
                }
            } else if (status instanceof WampClient.ConnectedState) {
                logger.info("is connected to poloniex [{}]", status);
                isConnected.countDown();
            } else if (status instanceof WampClient.DisconnectedState) {
                logger.info("disconnected from poloniex [{}]", status);
                isReconnect.set(true);
            } else {
                String s = String.format("unknown status [%s]", status);
                logger.error(s);
                throw new RuntimeException(s);
            }
        });

        logger.info("opening WAMP client");
        wampClient.open();

        if (!isConnected.await(10, TimeUnit.MINUTES)) {
            String s = "Failed to start client. timeout";
            logger.error(s);
            throw new RuntimeException(s);
        }
    }

    @PreDestroy
    private void stop(){
        logger.info("closong WAMP client");
        wampClient.close().toBlocking().last();
        logger.info("WAMP client was closed");
    }

}
