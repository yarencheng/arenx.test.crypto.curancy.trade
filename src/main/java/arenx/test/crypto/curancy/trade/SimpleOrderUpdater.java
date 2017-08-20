package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SimpleOrderUpdater implements OrderUpdater{

    private static Logger logger = LoggerFactory.getLogger(SimpleOrderUpdater.class);

    @Override
    public void update(String ex, Action action, OrderType type, double price, double volume) {
//        logger.info("{} {} {} {} {}", ex, action, type, price, volume);
    }

    @Override
    public void removeAll(String ex){
//        logger.info("{}", ex);
    }

}
