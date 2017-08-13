package arenx.test.crypto.curancy.trade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
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
import org.springframework.context.annotation.Configuration;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BaseWebSocketClientTest.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
public class BaseWebSocketClientTest {

    private static Logger logger = LoggerFactory.getLogger(BaseWebSocketClientTest.class);

    @Bean
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
    WebSocketSession getWebSocketSession(){

        WebSocketSession session = Mockito.mock(WebSocketSession.class);

        Mockito.when(session.isOpen()).thenReturn(true);

        return session;
    }

    @Bean
    ArgumentCaptor<WebSocketHandler> getWsHandler(){
        return ArgumentCaptor.forClass(WebSocketHandler.class);
    }

    @Bean
    LinkedBlockingDeque<CharSequence> getReceivedMessages(){
        return new LinkedBlockingDeque<>();
    }

    @Bean
    BaseWebSocketClient getBaseWebSocketClient(@Autowired LinkedBlockingDeque<CharSequence> rm) throws URISyntaxException{

        BaseWebSocketClient c = spy(new BaseWebSocketClient(){
            @Override
            protected void onMessageReceive(CharSequence message) {
                rm.add(message);
            }

            @Override
            protected URI getURI() {
                // TODO Auto-generated method stub
                try {
                    return new URI("wss://test.test");
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }});

        return c;
    }

    @Autowired
    BaseWebSocketClient baseWebSocketClient;

    @Autowired
    WebSocketSession session;

    @Autowired
    ArgumentCaptor<WebSocketHandler> wsHandler;

    @Autowired
    LinkedBlockingDeque<CharSequence> receivedMessages;

    @Test
    public void sendMessage() throws IOException{
        baseWebSocketClient.sendMessage("ss");
        verify(session, timeout(10000).times(1)).sendMessage(new TextMessage("ss"));
    }

    @Test
    public void onMessageReceive() throws Exception{
        wsHandler.getValue().handleMessage(session, new TextMessage("aaa"));
        Assert.assertEquals("aaa", receivedMessages.pollLast(10, TimeUnit.SECONDS));
    }
}
