package arenx.test.crypto.curancy.trade;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

import com.google.common.collect.Sets;

import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

@Configuration
@ComponentScan
@PropertySource("classpath:trade.properties")
public class Application {

	@Bean(name = "monitoredCurrency")
	public Set<Set<Currency>> get(){
	    return new HashSet<>(
	            Arrays.asList(
	                    Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM)
//	                    Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH)
                )
        );
	}

	@Bean(name = "webSocketClient")
	@Scope("prototype")
    public WebSocketClient getWebSocketClient(){
	    org.eclipse.jetty.websocket.client.WebSocketClient c = new org.eclipse.jetty.websocket.client.WebSocketClient();
	    c.getPolicy().setMaxTextMessageSize(1024*1024);
        return new JettyWebSocketClient(c);
    }

	@Bean(name = "wampClient")
    @Scope("prototype")
    public WampClient getWampClient() throws Exception{
	    return new WampClientBuilder()
            .withConnectorProvider(new NettyWampClientConnectorProvider())
            .withUri("wss://api.poloniex.com")
            .withRealm("realm1")
            .withInfiniteReconnects()
            .withReconnectInterval(1, TimeUnit.SECONDS)
            .build();
    }
}
