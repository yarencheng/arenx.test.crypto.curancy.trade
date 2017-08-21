package arenx.test.crypto.curancy.trade;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.PersistenceManager;

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

    @Override
    public void afterChange() {

    }

    @PostConstruct
    private void start() {

    }

    @PreDestroy
    private void stop() throws InterruptedException{

        pm.close();
    }
}
