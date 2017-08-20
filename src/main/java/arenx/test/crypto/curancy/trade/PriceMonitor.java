package arenx.test.crypto.curancy.trade;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class PriceMonitor implements OrderChangeListener{

    private static Logger logger = LoggerFactory.getLogger(PriceMonitor.class);

    @Autowired
    private PersistenceManager pm;

    private Thread workerThread;
    private AtomicBoolean isRun = new AtomicBoolean(true);
    private AtomicBoolean isChange = new AtomicBoolean(false);


    private double profitThreshold = 0.000;
    private double profit;
    private long lasttime;

    private Runnable worker = ()->{
        while (isRun.get()) {

            if (!isChange.get()) {
                try {
                    Thread.sleep(0, 100000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }

            isChange.set(false);

            Query query = pm.newQuery(Order.class);
            query.setResult("exchange");
            query.setGrouping("exchange");

            List<String> exchanges = (List<String>) query.execute();

            for (String exchange: exchanges) {
                query = pm.newQuery(Order.class, "type == p1 && exchange == p2");
                query.declareParameters(OrderType.class.getName() + " p1, " + String.class.getName() + " p2");
                query.setOrdering("price DESC");

                List<Order> asks = (List<Order>) query.execute(OrderType.ASK, exchange);

                if (asks.isEmpty()) {
                    continue;
                }

                Order ask = asks.get(0);

                logger.info("ex={} ASK price={} volume={}", exchange, ask.getPrice(), ask.getVolume());
            }

            for (String exchange: exchanges) {
                query = pm.newQuery(Order.class, "type == p1 && exchange == p2");
                query.declareParameters(OrderType.class.getName() + " p1, " + String.class.getName() + " p2");
                query.setOrdering("price ASC");

                List<Order> bids = (List<Order>) query.execute(OrderType.BID, exchange);

                if (bids.isEmpty()) {
                    continue;
                }

                Order bid = bids.get(0);

                logger.info("ex={} BID price={} volume={}", exchange, bid.getPrice(), bid.getVolume());
            }

            for (String exchange: exchanges) {
                query = pm.newQuery(Order.class, "type == p1 && exchange == p2");
                query.declareParameters(OrderType.class.getName() + " p1, " + String.class.getName() + " p2");
                query.setOrdering("price DESC");

                List<Order> asks = (List<Order>) query.execute(OrderType.ASK, exchange);

                if (asks.isEmpty()) {
                    continue;
                }

                Order ask = asks.get(0);

                query = pm.newQuery(Order.class, "type == p1 && price < p2 && exchange != p3 && updateMilliSeconds > p4");
                query.declareParameters(OrderType.class.getName() + " p1, double p2, " + String.class.getName() + " p3, long p4");
                query.setResult("sum(volume * (p2 - price)), max(updateMilliSeconds)");

                Object[] r = (Object[]) query.executeWithArray(OrderType.BID, ask.getPrice() * (1.0-profitThreshold), exchange, lasttime);

                if (null == r || r.length==0 || r[0] == null) {
                    continue;
                }

                profit += (double)r[0];
                lasttime = (long) r[1];
            }

            for (String exchange: exchanges) {
                query = pm.newQuery(Order.class, "type == p1 && exchange == p2");
                query.declareParameters(OrderType.class.getName() + " p1, " + String.class.getName() + " p2");
                query.setOrdering("price DESC");

                List<Order> asks = (List<Order>) query.execute(OrderType.BID, exchange);

                if (asks.isEmpty()) {
                    continue;
                }

                Order ask = asks.get(0);

                query = pm.newQuery(Order.class, "type == p1 && price > p2 && exchange != p3 && updateMilliSeconds > p4");
                query.declareParameters(OrderType.class.getName() + " p1, double p2, " + String.class.getName() + " p3, long p4");
                query.setResult("sum(volume * (price - p2)), max(updateMilliSeconds)");

                Object[] r = (Object[]) query.executeWithArray(OrderType.ASK, ask.getPrice() * (1.0+profitThreshold), exchange, lasttime);

                if (null == r || r.length==0 || r[0] == null) {
                    continue;
                }

                profit += (double)r[0];
                lasttime = (long) r[1];
            }

            logger.info("profit = {}", profit);

            logger.info("========================");

        }
    };

    @Override
    public void afterChange() {
        isChange.set(true);
    }



    @PostConstruct
    private void start() {



        workerThread = new Thread(worker, "PriceMonitor");
        workerThread.setUncaughtExceptionHandler((thread, e)->{
            logger.error("some thing is wrong", e);
        });
        workerThread.start();
    }

    @PreDestroy
    private void stop() throws InterruptedException{
        logger.info("join PriceMonitor thread");
        isRun.set(false);
        workerThread.join();

        pm.close();
    }
}
