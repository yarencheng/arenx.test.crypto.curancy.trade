package arenx.test.crypto.curancy.trade.bitfinex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.IntegrationTest;

@Category(IntegrationTest.class)
public class BitfinexExchangeApiIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(BitfinexExchangeApiIntegrationTest.class);

    BitfinexExchangeApi api;

    @Before
    public void start() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

        Method startMethod = BitfinexExchangeApi.class.getDeclaredMethod("start");
        startMethod.setAccessible(true);

        api = new BitfinexExchangeApi();

        startMethod.invoke(api);
    }

    @After
    public void after() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Method stopMethod = BitfinexExchangeApi.class.getDeclaredMethod("stop");
        stopMethod.setAccessible(true);

        stopMethod.invoke(api);
    }

    @Test
    public void receive_2_book_message_in_10_seconds() throws InterruptedException{
        CountDownLatch count = new CountDownLatch(2);

        api.setOrderUpdateListener(bean->{

            count.countDown();
        });

        api.subscribeBook(Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN));

        Assert.assertTrue(count.await(10, TimeUnit.SECONDS));
    }
}
