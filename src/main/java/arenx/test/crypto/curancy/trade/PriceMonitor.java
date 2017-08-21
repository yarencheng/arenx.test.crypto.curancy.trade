package arenx.test.crypto.curancy.trade;

import java.util.Arrays;
import java.util.List;

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

    Query bidsQuery;

    @Override
    public void afterChange() {
        List<Object[]> data = (List<Object[]>) bidsQuery.execute(0);

        for (Object[] o: data) {
            logger.info("data {}", Arrays.toString(o));
        }
    }

    @PostConstruct
    private void start() {

        Query maxAskQuery = pm.newQuery(Order.class, "type == " + OrderType.class + "." + OrderType.ASK + " && updateMilliSeconds > p1");
        maxAskQuery.setResult("max(price)");

        bidsQuery = pm.newQuery(Order.class, "type == " + OrderType.class + "." + OrderType.ASK + " && updateMilliSeconds > p1 && price < p2");
        bidsQuery.declareVariables("long p1, double p2");
        bidsQuery.addSubquery(maxAskQuery, "double p2", null);

//        Query maxBidQuery = pm.newQuery(Order.class, "type == " + OrderType.class + "." + OrderType.BID + " && updateMilliSeconds > p1");
//        maxBidQuery.setResult("min(price)");
    }

    @PreDestroy
    private void stop() throws InterruptedException{

        pm.close();
    }
}
