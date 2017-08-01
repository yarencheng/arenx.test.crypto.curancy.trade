package arenx.test.crypto.curancy.trade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import rx.Subscription;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class PoloTest {

	private static Logger logger = LoggerFactory.getLogger(PoloTest.class);
	
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
	    		
//	    		client
//	        	.makeSubscription("ticker", Object.class)
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
        
        
        Thread.sleep(100000);
        
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
				logger.info("polo");
				
				long start = System.currentTimeMillis();
				
				Map<String, Map<String, Object>> tikcers = rest.getForObject("https://poloniex.com/public?command=returnTicker", Map.class);			

				logger.info("poloniex BTC ZEC: {} {} {}", 
						tikcers.get("BTC_ZEC").get("highestBid"),
						tikcers.get("BTC_ZEC").get("last"), 
						tikcers.get("BTC_ZEC").get("lowestAsk")
						);
				
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
				
				logger.info(String.format("bitfinex BTC ZEC: %1.8f %1.8f %1.8f", ticker.get(1), ticker.get(9), ticker.get(3)));
				
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
			
			if (polo_low_ask.get() / bitf_high_bid.get() < 0.9999) {
				a++;
				
				max = Math.max(max, (1 - (polo_low_ask.get() / bitf_high_bid.get())) * 100);
				
				logger.info("all:{} a:{} b:{}   {} %", 
					all, a, b, 
					(1 - (polo_low_ask.get() / bitf_high_bid.get())) * 100
					);
			}
			
			if (bitf_low_ask.get() / polo_high_bid.get() < 0.9999) {
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
	
	 public void adasd(){
		
	}

}
