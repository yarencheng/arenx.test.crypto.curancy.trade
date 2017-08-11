package arenx.test.crypto.curancy.trade.bitfinex;

import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.Order;
import arenx.test.crypto.curancy.trade.Order.Type;
import arenx.test.crypto.curancy.trade.OrderUpdateListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BitfinexExchangeV2Test.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
@ComponentScan
public class BitfinexExchangeV2Test {

    private static Logger logger = LoggerFactory.getLogger(BitfinexExchangeV2Test.class);

    @Bean
    @Scope("singleton")
    public WebSocketClient getWebSocketClient(@Autowired WebSocketSession session, @Autowired ArgumentCaptor<WebSocketHandler> wsHandler) {

        WebSocketClient client = Mockito.mock(WebSocketClient.class);
        ListenableFuture<WebSocketSession> future = Mockito.mock(ListenableFuture.class);

        Mockito.doAnswer(new Answer<ListenableFuture<WebSocketSession> >(){

            @Override
            public ListenableFuture<WebSocketSession> answer(InvocationOnMock invocation) throws Throwable {

                WebSocketHandler ha = invocation.getArgument(0);

                ha.afterConnectionEstablished(session);

                return future;
            }}).when(client).doHandshake(wsHandler.capture(), any(WebSocketHttpHeaders.class), any(URI.class));

        Mockito.doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                ListenableFutureCallback<WebSocketSession> fn = invocation.getArgument(0);

                fn.onSuccess(session);

                return null;
            }}).when(future).addCallback(any(ListenableFutureCallback.class));

        return client;
    }

    @Bean
    @Scope("singleton")
    WebSocketSession getWebSocketSession(){
        WebSocketSession session = Mockito.mock(WebSocketSession.class);

        Mockito.when(session.isOpen()).thenReturn(true);

        return session;
    }

    @Bean
    @Scope("singleton")
    ArgumentCaptor<WebSocketHandler> getWsHandler(){
        return ArgumentCaptor.forClass(WebSocketHandler.class);
    }


    @Autowired
    BitfinexExchangeV2 api;

    @Autowired
    WebSocketSession session;

    @Autowired
    ArgumentCaptor<WebSocketHandler> wsHandler;

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void subscribeOrder() throws InterruptedException, IOException{

        Set<Currency> currencies = BitfniexUtils.tETHBTCs;

        api.subscribeOrder(currencies);

        ArgumentCaptor<TextMessage> actualMessage = ArgumentCaptor.forClass(TextMessage.class);

        Mockito.verify(session, Mockito.timeout(10000).times(1)).sendMessage(actualMessage.capture());

        JsonNode expectJson = new ObjectMapper().readTree("{"
                + "\"event\":\"subscribe\","
                + "\"channel\":\"book\","
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}");

        JsonNode actualJson = new ObjectMapper().readTree(actualMessage.getValue().getPayload());

        Assert.assertEquals(expectJson, actualJson);
    }

    @Test
    public void setOrderUpdateListener() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Order expectedOrder = new Order();
        expectedOrder.setExchange("bitfinex");
        expectedOrder.setFromCurrency(Currency.ETHEREUM);
        expectedOrder.setToCurrency(Currency.BITCOIN);
        expectedOrder.setPrice(222.222);
        expectedOrder.setType(Type.BID);
        expectedOrder.setVolume(444.444);

        Assert.assertEquals(expectedOrder, actualOrder.getValue());
    }

    @Test
    public void setOrderUpdateListener_getId() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertNull(actualOrder.getValue().getId());
    }

    @Test
    public void setOrderUpdateListener_getExchange() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals("bitfinex", actualOrder.getValue().getExchange());
    }

    @Test
    public void setOrderUpdateListener_exchange() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(Currency.ETHEREUM, actualOrder.getValue().getFromCurrency());
    }

    @Test
    public void setOrderUpdateListener_getToCurrency() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(Currency.BITCOIN, actualOrder.getValue().getToCurrency());
    }

    @Test
    public void setOrderUpdateListener_getPrice() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(222.222, actualOrder.getValue().getPrice(), 0);
    }

    @Test
    public void setOrderUpdateListener_getVolume() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(444.444, actualOrder.getValue().getVolume(), 0);
    }

    @Test
    public void setOrderUpdateListener_getType_bid() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(Type.BID, actualOrder.getValue().getType());
    }

    @Test
    public void setOrderUpdateListener_getType_ask() throws Exception{

        OrderUpdateListener callBack = Mockito.mock(OrderUpdateListener.class);
        api.setOrderUpdateListener(callBack);

        wsHandler.getValue().handleMessage(session, new TextMessage("{"
                + "\"event\":\"subscribed\","
                + "\"channel\":\"book\","
                + "\"chanId\":111,"
                + "\"symbol\":\"tETHBTC\","
                + "\"prec\":\"P0\","
                + "\"freq\":\"F0\","
                + "\"len\":25"
                + "}"));

        wsHandler.getValue().handleMessage(session, new TextMessage("[111,[222.222,333,-444.444]]"));

        ArgumentCaptor<Order> actualOrder = ArgumentCaptor.forClass(Order.class);

        Mockito.verify(callBack, Mockito.timeout(10000).times(1)).OnUpdate(actualOrder.capture());

        Assert.assertEquals(Type.ASK, actualOrder.getValue().getType());
    }
}
