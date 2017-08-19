package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SimpleOrderUpdateListener implements OrderUpdateListener{

    private static Logger logger = LoggerFactory.getLogger(SimpleOrderUpdateListener.class);

    @Override
    public void update(String ex, Action action, Type type, double price, double volume) {
        logger.info("{} {} {} {} {}", ex, action, type, price, volume);
    }

    @Override
    public void removeAll(String ex){
        logger.info("{}", ex);
    }

}
