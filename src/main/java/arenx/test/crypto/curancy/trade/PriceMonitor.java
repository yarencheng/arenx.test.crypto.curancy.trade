package arenx.test.crypto.curancy.trade;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    private long lastTime;
    private BufferedWriter  writer;

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

            Query maxAskQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.ASK + " && updateMilliSeconds > p1");
            maxAskQuery.setResult("max(price)");

            Query bidsQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.BID + " && updateMilliSeconds > p1 && price < p2");
            bidsQuery.declareParameters("long p1");
            bidsQuery.declareVariables("double p2");
            bidsQuery.addSubquery(maxAskQuery, "double p2", null);
            bidsQuery.setOrdering("price ASC");

            Query minBidQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.BID + " && updateMilliSeconds > p1");
            minBidQuery.setResult("min(price)");

            Query asksQuery = pm.newQuery(Order.class, "type == " + OrderType.class.getName() + "." + OrderType.ASK + " && updateMilliSeconds > p1 && price > p2");
            asksQuery.declareParameters("long p1");
            asksQuery.declareVariables("double p2");
            asksQuery.addSubquery(minBidQuery, "double p2", null);
            asksQuery.setOrdering("price DESC");

            List<Order> bids = (List<Order>) bidsQuery.execute(lastTime);
            List<Order> asks = (List<Order>) asksQuery.execute(lastTime);

            if (bids.isEmpty() || asks.isEmpty()) {
                continue;
            }

            List<Order> all = new ArrayList<>(bids.size() + asks.size());
            all.addAll(bids);
            all.addAll(asks);

            lastTime = all.stream().mapToLong(o->o.getUpdateMilliSeconds()).max().getAsLong();

            try {
                for (Order o : all) {
                    String line = lastTime + '\t' + o.getExchange() + '\t' + o.getType() + '\t' + o.getPrice() + '\t' + o.getVolume();
                    writer.write(line);
                    writer.newLine();
                    logger.info(line);
                }
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    };
    private Thread thread;


    @Override
    public void afterChange() {
        isChange.set(true);
    }

    @PostConstruct
    private void start() throws IOException {

        writer = new BufferedWriter(new FileWriter("profit.txt"));

        thread = new Thread(worker, "0%.monitor");
        thread.setUncaughtExceptionHandler((t,e)->{
            logger.error("error in thread [" + t.getName() + "]", e);
        });
        thread.start();
    }

    @PreDestroy
    private void stop() throws InterruptedException, IOException{
        isRun.set(false);

        logger.info("join thread [{}]", thread.getName());
        thread.join();

        pm.close();

        writer.flush();
        writer.close();
    }
}
