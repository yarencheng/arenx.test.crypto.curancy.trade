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
    public void OnUpdate(Order bean) {
        logger.info("order: {}", bean);
    }

}