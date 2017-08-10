package arenx.test.crypto.curancy.trade.bitfinex;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BitfinexExchangeApiTest.class })
@Configuration
@ComponentScan
public class BitfinexExchangeApiTest {

    private static Logger logger = LoggerFactory.getLogger(BitfinexExchangeApiTest.class);

    @Bean
    public WebSocketClient getWebSocketClient() {

        WebSocketClient client = mock(WebSocketClient.class);
        ListenableFuture<WebSocketSession> future = mock(ListenableFuture.class);
        WebSocketSession session = mock(WebSocketSession.class);

        Mockito.when(session.isOpen()).thenReturn(true);

        Mockito.doAnswer(new Answer<ListenableFuture<WebSocketSession> >(){

            @Override
            public ListenableFuture<WebSocketSession> answer(InvocationOnMock invocation) throws Throwable {

                WebSocketHandler ha = invocation.getArgumentAt(0, WebSocketHandler.class);

                ha.afterConnectionEstablished(session);

                return future;
            }}).when(client).doHandshake(any(WebSocketHandler.class), any(WebSocketHttpHeaders.class), any(URI.class));

        Mockito.doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                ListenableFutureCallback<WebSocketSession> fn = invocation.getArgumentAt(0, ListenableFutureCallback.class);

                fn.onSuccess(session);

                return null;
            }}).when(future).addCallback(any(ListenableFutureCallback.class));

        return client;
    }

    @Autowired
    BitfinexExchangeApi api;



    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Test
    public void aaaa() throws InterruptedException {

    }
}
