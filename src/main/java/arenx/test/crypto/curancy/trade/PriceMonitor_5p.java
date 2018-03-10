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

//@Component
//@Scope("singleton")
public class PriceMonitor_5p implements OrderChangeListener{

    private static Logger logger = LoggerFactory.getLogger(PriceMonitor_5p.class);

    @Autowired
    private PersistenceManager pm;

    private long lastTime;
    private double allProfit;

    private AtomicBoolean isRun = new AtomicBoolean(true);
    private AtomicBoolean isChange = new AtomicBoolean(false);
    private Runnable worker = ()->{
        while (isRun.get()) {
            if (!isChange.getAndSet(false)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }

            Query maxAskQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.ASK + " && updateMilliSeconds > (p1 * 1.005)");
            maxAskQuery.setResult("max(price)");

            Query bidsQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.BID + " && updateMilliSeconds > p1 && price < (p2 * 0.995)");
            bidsQuery.setResult("price, volume, updateMilliSeconds");
            bidsQuery.declareParameters("long p1");
            bidsQuery.declareVariables("double p2");
            bidsQuery.addSubquery(maxAskQuery, "double p2", null);
            bidsQuery.setOrdering("price ASC");

            Query minBidQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.BID + " && updateMilliSeconds > p1");
            minBidQuery.setResult("min(price)");

            Query asksQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.ASK + " && updateMilliSeconds > p1 && price > p2");
            asksQuery.setResult("price, volume, updateMilliSeconds");
            asksQuery.declareParameters("long p1");
            asksQuery.declareVariables("double p2");
            asksQuery.addSubquery(minBidQuery, "double p2", null);
            asksQuery.setOrdering("price DESC");

            List<Object[]> bids = (List<Object[]>) bidsQuery.execute(lastTime);
            List<Object[]> asks = (List<Object[]>) asksQuery.execute(lastTime);

            if (bids.isEmpty() || asks.isEmpty()) {
                continue;
            }

            long bidAllVolume = 0;
            double bidAllprofit = 0;

            for (Object[] o: bids) {
                bidAllVolume += (double)o[1];
                bidAllprofit += (double)o[0] * (double)o[1];
                lastTime = Math.max(lastTime, (long)o[2]);
            }

            long askAllVolume = 0;
            double askAllprofit = 0;

            for (Object[] o: asks) {
                askAllVolume += (double)o[1];
                askAllprofit += (double)o[0] * (double)o[1];
                lastTime = Math.max(lastTime, (long)o[2]);
            }

            double profit;
            if (bidAllVolume < askAllVolume) {
                profit = bidAllprofit;
            } else {
                profit = askAllprofit;
            }

            profit *= 0.995;
            allProfit += profit;

            logger.info("5% profit: {} / {}", profit, allProfit);
        }
    };
    private Thread thread;


    @Override
    public void afterChange() {
        isChange.set(true);
    }

    @PostConstruct
    private void start() {
        thread = new Thread(worker, "5%.monitor");
        thread.setUncaughtExceptionHandler((t,e)->{
            logger.error("error in thread [" + t.getName() + "]", e);
        });
        thread.start();
    }

    @PreDestroy
    private void stop() throws InterruptedException{
        isRun.set(false);

        logger.info("join thread [{}]", thread.getName());
        thread.join();

        pm.close();
    }
}
