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

            {
                Query ask= pm.newQuery(Order.class, "type == p1");
                ask.declareParameters(OrderType.class.getName() + " p1");
                ask.setResult("max(price)");
                ask.setGrouping("exchange");

                List<Object> asks = (List<Object>) ask.execute(OrderType.ASK);

                logger.info("asks {}", asks);
            }

            {
                Query bid= pm.newQuery(Order.class, "type == p1");
                bid.declareParameters(OrderType.class.getName() + " p1");
                bid.setResult("min(price)");
                bid.setGrouping("exchange");

                List<Object> bids = (List<Object>) bid.execute(OrderType.BID);

                logger.info("bids {}", bids);
            }


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
