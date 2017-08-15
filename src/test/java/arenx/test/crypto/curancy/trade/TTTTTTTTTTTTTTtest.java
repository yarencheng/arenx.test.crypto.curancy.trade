package arenx.test.crypto.curancy.trade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class TTTTTTTTTTTTTTtest {

	private static Logger logger = LoggerFactory.getLogger(TTTTTTTTTTTTTTtest.class);

//	@Test
	public void  aaa() throws Exception{
		logger.info("info");
		logger.warn("warn");
		logger.error("error");

		NettyWampClientConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();

		WampClientBuilder builder = new WampClientBuilder();
	    builder
			.withConnectorProvider(connectorProvider)
			.withUri("wss://api.poloniex.com")
			.withRealm("realm1")
			.withInfiniteReconnects()
			.withReconnectInterval(1, TimeUnit.SECONDS);
	    // Create a client through the builder. This will not immediatly start
	    // a connection attempt
	    WampClient client = builder.build();



	    client.statusChanged().subscribe((state)->{
	    	logger.info("Session status changed to " + state);

	    	if (state instanceof WampClient.ConnectedState) {
	    		client
	        	.makeSubscription("BTC_ZEC", Object.class)
	        	.subscribe(
	        		(ticker)->{
	        			logger.info("BTC_ZEC {} ", ticker);
	        		},
	        		(e)->{
	        			logger.info("BTC_ZEC e={} ", e.getMessage());
	        			e.printStackTrace();
	        		});

	    		client
	        	.makeSubscription("BTC_ZEC", Object.class)
	        	.subscribe(
	        		(ticker)->{
	        			logger.info("BTC_ZEC {} ", ticker);
	        		},
	        		(e)->{
	        			logger.info("BTC_ZEC e={} ", e.getMessage());
	        			e.printStackTrace();
	        		});

//	    		client
//	        	.makeSubscription("ticker", String.class)
//	        	.subscribe(
//	        		(ticker)->{
//	        			logger.info("ticker {} ", ticker);
//	        		},
//	        		(e)->{
//	        			logger.info("ticker e={} ", e.getMessage());
//	        			e.printStackTrace();
//	        		});
	    	}
	    });


	    client.open();


        Thread.sleep(10000000);


//        ObjectMapper s;
//        s.arr

	}

	static class Ticker{
		public int id;
		public double last;
		public double lowestAsk;
		public double highestBid;
		public double percentChange;
		public double baseVolume;
		public double quoteVolume;
		public double isFrozen;
		public double high24hr;
		public double low24hr;
	}

//	@Test
	public void sss() throws Exception{

		AtomicReference<Double> polo_high_bid = new AtomicReference<Double>(0.0);
		AtomicReference<Double> polo_low_ask = new AtomicReference<Double>(0.0);
		AtomicReference<Double> bitf_high_bid = new AtomicReference<Double>(0.0);
		AtomicReference<Double> bitf_low_ask = new AtomicReference<Double>(0.0);

		new Thread(()->{

			RestTemplate rest = new RestTemplate();

			while (true) {

				long start = System.currentTimeMillis();

				Map<String, Map<String, Object>> tikcers = rest.getForObject("https://poloniex.com/public?command=returnTicker", Map.class);

//				logger.info("poloniex BTC ZEC: {} {} {}",
//						tikcers.get("BTC_ZEC").get("highestBid"),
//						tikcers.get("BTC_ZEC").get("last"),
//						tikcers.get("BTC_ZEC").get("lowestAsk")
//						);

				polo_high_bid.set(Double.parseDouble((String) tikcers.get("BTC_ZEC").get("highestBid")));
				polo_low_ask.set(Double.parseDouble((String) tikcers.get("BTC_ZEC").get("lowestAsk")));

				long end = System.currentTimeMillis();

				if (end - start < 200) {
					try {
						Thread.sleep(200 - (end - start));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();

		new Thread(()->{

			RestTemplate rest = new RestTemplate();

			while (true) {
//				logger.info("bitfinex");

				long start = System.currentTimeMillis();

				List<List<Object>> tikcers = rest.getForObject("https://api.bitfinex.com/v2/tickers?symbols=tZECBTC", List.class);

				List ticker = tikcers.get(0);

//				logger.info(String.format("bitfinex BTC ZEC: %1.8f %1.8f %1.8f", ticker.get(1), ticker.get(9), ticker.get(3)));

				bitf_high_bid.set((Double) ticker.get(1));
				bitf_low_ask.set((Double) ticker.get(3));

				long end = System.currentTimeMillis();

				if (end - start < 700) {
					try {
						Thread.sleep(700 - (end - start));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();

		Thread.sleep(10000);


		long a = 0, b = 0, all = 0;
		long lastInfo = 0;
		double max = 0;

		while (true) {

			all++;

			if (polo_low_ask.get() / bitf_high_bid.get() < 0.99500625) {
				a++;

				max = Math.max(max, (1 - (polo_low_ask.get() / bitf_high_bid.get())) * 100);

				logger.info("all:{} a:{} b:{}   {} %",
					all, a, b,
					(1 - (polo_low_ask.get() / bitf_high_bid.get())) * 100
					);
			}

			if (bitf_low_ask.get() / polo_high_bid.get() < 0.99500625) {
				b++;

				max = Math.max(max, (1 - (bitf_low_ask.get() / polo_high_bid.get())) * 100);

				logger.info("all:{} a:{} b:{}   {} %", all, a, b,
					(1 - (bitf_low_ask.get() / polo_high_bid.get())) * 100
					);
			}

			if (System.currentTimeMillis() > lastInfo + 10000) {
				logger.info("all:{} a:{} b:{} max:{}", all, a, b, max);
				lastInfo = System.currentTimeMillis();
			}

			Thread.sleep(50);
		}
	}

//	@Test
	public void dd(){
		ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(1);

		ex.scheduleAtFixedRate(()->{

			logger.info("START");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			logger.info("END");

		}, 0, 10, TimeUnit.SECONDS);

		ex.scheduleAtFixedRate(this::adasd, 0, 10, TimeUnit.SECONDS);

		try {
			Thread.sleep(500000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	@Test
	public void adasd() {
		WebSocketClient ws = new JettyWebSocketClient();

		WebSocketConnectionManager wsm = new WebSocketConnectionManager(
			ws,
			new WebSocketHandler(){

				@Override
				public void afterConnectionClosed(WebSocketSession arg0, CloseStatus arg1) throws Exception {
					logger.info("afterConnectionClosed() arg0={} arg1={}", arg0, arg1);
				}

				@Override
				public void afterConnectionEstablished(WebSocketSession arg0) throws Exception {
					logger.info("afterConnectionEstablished() arg0={}", arg0);

//					arg0.sendMessage(new TextMessage("{ \"event\":\"ping\"}"));
					arg0.sendMessage(new TextMessage("{\"event\": \"subscribe\", \"channel\": \"book\", \"symbol\": \"tETHBTC\", \"prec\": \"P0\",  \"freq\": \"F0\",  \"len\": 25 }"));

//					Thread.sleep(1000);

//					arg0.sendMessage(new TextMessage("{\"event\": \"subscribe\", \"channel\": \"book\", \"symbol\": \"tETHBTC\", \"prec\": \"P0\",  \"freq\": \"F0\",  \"len\": 25 }"));

//					Thread.sleep(5);
//					arg0.sendMessage(new TextMessage("{\"event\": \"subscribe\", \"channel\": \"book\", \"symbol\": \"tZECBTC\", \"prec\": \"P0\",  \"freq\": \"F0\",  \"len\": 25 }"));



				}

				boolean isSub = false;
				@Override
				public void handleMessage(WebSocketSession arg0, WebSocketMessage<?> arg1) throws Exception {
//					logger.info("handleMessage() arg0={} arg1={}", arg0, arg1);
//					logger.info("handleMessage() arg1={}", arg1.getPayload().getClass());
					logger.info("handleMessage() arg1={}", arg1.getPayload().toString());

					if (isSub) {
						return;
					}

					isSub = true;

//					arg0.sendMessage(new TextMessage(
//						"{'event': 'subscribe', 'channel': 'book', 'symbol': 'tZECBTC', 'prec': 'P0',  'freq': 'F0',  'len': 25 }"
//						"{\"event\": \"ping\" }"
//					));

//					arg0.sendMessage(new TextMessage("{ \"event\":\"ping\"}"));

				}

				@Override
				public void handleTransportError(WebSocketSession arg0, Throwable arg1) throws Exception {
					logger.info("handleTransportError() arg0={} arg1={}", arg0, arg1);

				}

				@Override
				public boolean supportsPartialMessages() {
					logger.info("supportsPartialMessages() ");
					return false;
				}},
//			"wss://api.poloniex.com:443"
			"wss://api.bitfinex.com/ws/2"
		);

		wsm.start();

		try {
			Thread.sleep(500000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	@Test
//	public void adasssd(){
//
//	    CodePointCharStream antlrInputStream = CharStreams.fromString("[123,[4.56,7,8]]");
////	    CodePointCharStream antlrInputStream = CharStreams.fromString("[123,[4]]");
//	    BitfinexLexer lexer = new BitfinexLexer(antlrInputStream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//        System.out.println();
//
//        BitfinexParser parser = new BitfinexParser(tokens);
//
//        ChannelBean bean = parser.channel().bean;
//
//        System.out.println("bean = " + bean);
//
//        System.out.println();
//
//	}

}
