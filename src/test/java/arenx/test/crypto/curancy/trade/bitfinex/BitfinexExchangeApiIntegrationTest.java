package arenx.test.crypto.curancy.trade.bitfinex;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
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

import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.Application;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.IntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
@ComponentScan
public class BitfinexExchangeApiIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(BitfinexExchangeApiIntegrationTest.class);

    @Autowired
    BitfinexExchangeApi api;

    @Before
    public void before() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    }

    @After
    public void after() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException{
        Thread.sleep(1000); // avoid too many request to bitfinex
    }

    @Test
    public void receive_2_book_message_in_10_seconds() throws InterruptedException{
        CountDownLatch count = new CountDownLatch(2);

        api.setOrderUpdateListener(bean->{
            logger.debug("receive bean [{}]", bean);
            count.countDown();
        });

        api.subscribeBook(Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN));

        Assert.assertTrue(count.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void reconnectListener() throws InterruptedException{
        CountDownLatch reconnect = new CountDownLatch(1);

        api.setReconnectListener(()->{
            reconnect.countDown();
        });

        api.reconnect();

        Assert.assertTrue(reconnect.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void reconnect_then_receive_book_message_in_10_seconds() throws InterruptedException{
        CountDownLatch count1 = new CountDownLatch(1);

        api.setOrderUpdateListener(bean->{
            logger.debug("receive bean [{}]", bean);
            count1.countDown();
        });

        api.subscribeBook(Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN));

        Assert.assertTrue(count1.await(10, TimeUnit.SECONDS));

        CountDownLatch count2 = new CountDownLatch(1);

        api.setReconnectListener(()->{
            try {
                api.subscribeBook(Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            api.setOrderUpdateListener(bean->{
                count2.countDown();
            });
        });

        api.reconnect();

        Assert.assertTrue(count2.await(10, TimeUnit.SECONDS));
    }
}
