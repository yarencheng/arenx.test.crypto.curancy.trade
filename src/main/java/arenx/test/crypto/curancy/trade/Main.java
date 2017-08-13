package arenx.test.crypto.curancy.trade;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
//		ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
//		PoloniexExchange polo = context.getBean(PoloniexExchange.class);
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM));
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH));


/*
	    org.eclipse.jetty.websocket.client.WebSocketClient c = new org.eclipse.jetty.websocket.client.WebSocketClient();
	    c.getPolicy().setMaxTextMessageSize(1024*1024);

	    WebSocketClient webSocketClient = new JettyWebSocketClient(c);

        WebSocketConnectionManager  wsm = new WebSocketConnectionManager(
                webSocketClient,
                new WebSocketHandler(){

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                        logger.info("connection to polo is closed({}).", status);
                    }

                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        logger.info("connection to polo is established.");
//                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": 1001}"));
//                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": 1002}"));
//                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": 1003}"));
                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": \"BTC_ZEC\"}"));
                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": \"BTC_ETH\"}"));
                    }

                    @Override
                    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                        logger.info("recieve polo essage [{}]", message.getPayload());
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
                        logger.error("recieve error from polo", e);
                    }

                    @Override
                    public boolean supportsPartialMessages() {
                        return false;
                    }},
                "wss://api2.poloniex.com"
            );

        wsm.start();*/

	    WampClient wampClient = new WampClientBuilder()
        .withConnectorProvider(new NettyWampClientConnectorProvider())
        .withUri("wss://api2.poloniex.com")
        .withRealm("realm1")
        .withInfiniteReconnects()
        .withReconnectInterval(1, TimeUnit.SECONDS)
        .build();
	    wampClient.statusChanged().subscribe((WampClient.State status)->{

            if (status instanceof WampClient.ConnectingState) {
                logger.info("is connecting to poloniex [{}]", status);
            } else if (status instanceof WampClient.ConnectedState) {
                logger.info("is connected to poloniex [{}]", status);
            } else if (status instanceof WampClient.DisconnectedState) {
                logger.info("disconnected from poloniex [{}]", status);
            } else {
                String s = String.format("unknown status [%s]", status);
                logger.error(s);
            }
        });

        wampClient.open();


		while (true) {
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
