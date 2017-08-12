package arenx.test.crypto.curancy.trade.poloniex;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import arenx.test.crypto.curancy.trade.ApiInterface;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.OrderUpdateListener;
import ws.wamp.jawampa.WampClient;

@Component
@Scope("singleton")
@Lazy
public class PoloniexExchange implements ApiInterface {

    private static Logger logger = LoggerFactory.getLogger(PoloniexExchange.class);

    @Autowired
    private WampClient wampClient;

    private AtomicBoolean isReady = new AtomicBoolean(false);
    private AtomicBoolean isReconnect = new AtomicBoolean(false);
    private Runnable reconnectListener;
    private OrderUpdateListener oderUpdateListener;

    @Override
    public void subscribeOrder(Set<Currency> currencies) {
        if (!isReady.get()) {
            throw new RuntimeException("client is not ready");
        }

        String symbol = PoloniexUtils.toSymbol(currencies);

        logger.info("subscribe order of [{}]", symbol);

        CountDownLatch isDone = new CountDownLatch(1);

        wampClient
        .makeSubscription(symbol, Object.class)
        .subscribe(
            (ticker)->{
                logger.info("symbol {} ", symbol, ticker);
            },
            (e)->{
                logger.error("Failed to subscribe [{}]", e.getMessage());
                e.printStackTrace();
            },
            ()->{
                logger.info("subscribe order of [{}] sucessfully", symbol);
                isDone.countDown();
            });
    }

    @Override
    public void setOrderUpdateListener(OrderUpdateListener listener) {
        Validate.notNull(listener);
        oderUpdateListener = listener;
    }

    @Override
    public void setReconnectListener(Runnable listener) {
        Validate.notNull(listener);
        reconnectListener = listener;
    }

    @PostConstruct
    private void start() {

        wampClient.statusChanged().subscribe((WampClient.State status)->{

            if (status instanceof WampClient.ConnectingState) {
                logger.info("is connecting to poloniex [{}]", status);
                if (isReconnect.get() && null!= reconnectListener) {
                    reconnectListener.run();
                }
            } else if (status instanceof WampClient.ConnectedState) {
                logger.info("is connected to poloniex [{}]", status);
                isReady.set(true);
            } else if (status instanceof WampClient.DisconnectedState) {
                logger.info("disconnected from poloniex [{}]", status);
                isReconnect.set(true);
                isReady.set(false);
            } else {
                String s = String.format("unknown status [%s]", status);
                logger.error(s);
                throw new RuntimeException(s);
            }
        });

        logger.info("opening WAMP client");
        wampClient.open();
    }

    @PreDestroy
    private void stop(){
        logger.info("closong WAMP client");
        wampClient.close().toBlocking().last();
        logger.info("WAMP client was closed");
    }

}
