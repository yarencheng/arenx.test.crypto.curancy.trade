package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
//		ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
//		PoloniexExchange polo = context.getBean(PoloniexExchange.class);
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM));
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH));



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
//                        session.sendMessage(new TextMessage("{\"command\": \"subscribe\", \"channel\": \"BTC_ETH\"}"));
                    }

                    @Override
                    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                        logger.info("recieve polo essage [{}]", message.getPayload());

                        ObjectMapper ma = new ObjectMapper();

                        JsonNode n = ma.readTree(message.getPayload().toString());

                        if(message.getPayload().toString().length()<200){
                            System.out.println(n);
                        }
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

        wsm.start();

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
