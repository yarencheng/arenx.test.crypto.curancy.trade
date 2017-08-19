package arenx.test.crypto.curancy.trade;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Bean("fromCurrency")
	public Currency getFromCurrency(){
	    return Currency.ZECASH;
	}

	@Bean("toCurrency")
    public Currency getToCurrency(){
        return Currency.BITCOIN;
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

	@Bean
    @Scope("singleton")
    public PersistenceManagerFactory getPersistenceManagerFactory() throws Exception{
	    JDOPersistenceManagerFactory pmf = (JDOPersistenceManagerFactory) JDOHelper.getPersistenceManagerFactory("h2_memory");

	    PersistenceNucleusContext ctx = pmf.getNucleusContext();

	    Set<String> classNames = Sets.newHashSet(Order.class.getName());

	    Properties props = new Properties();

	    ((SchemaAwareStoreManager)ctx.getStoreManager()).createSchemaForClasses(classNames, props);

        return pmf;
    }

	@Bean
    @Scope("prototype")
    public PersistenceManager getPersistenceManager(@Autowired PersistenceManagerFactory pmf) throws Exception{
        return pmf.getPersistenceManager();
    }
}
