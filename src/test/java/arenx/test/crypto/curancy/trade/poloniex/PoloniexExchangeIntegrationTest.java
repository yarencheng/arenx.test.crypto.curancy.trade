package arenx.test.crypto.curancy.trade.poloniex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import arenx.test.crypto.curancy.trade.Application;
import arenx.test.crypto.curancy.trade.IntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
@ComponentScan
public class PoloniexExchangeIntegrationTest {
    private static Logger logger = LoggerFactory.getLogger(PoloniexExchangeIntegrationTest.class);

    @Autowired
    PoloniexExchange api;

    @Before
    public void before(){

    }

    @After
    public void after() {

    }

    @Test
    public void receive_2_book_message_in_10_seconds() throws InterruptedException{
//        CountDownLatch count = new CountDownLatch(2);
//
//        api.setOrderUpdateListener(bean->{
//            logger.debug("receive bean [{}]", bean);
//            count.countDown();
//        });
//
//        api.subscribeOrder(Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN));
//
//        Assert.assertTrue(count.await(10, TimeUnit.SECONDS));
    }
}
