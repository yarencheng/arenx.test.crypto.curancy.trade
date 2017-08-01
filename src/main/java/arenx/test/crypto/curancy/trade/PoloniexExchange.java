package arenx.test.crypto.curancy.trade;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class PoloniexExchange implements TickerProviderInterface {

	private static Logger logger = LoggerFactory.getLogger(PoloniexExchange.class);

	@Value("${poloniex.query.interval}")
	private long queryInterval;

	private RestOperations rest = new RestTemplate();
	private ScheduledExecutorService ex = new ScheduledThreadPoolExecutor(1);
	private Map<Currency, Map<Currency, TickerListenerInterface>> tickerUpdaters = Collections.synchronizedMap(new HashMap<>());

	public void start() {
		ex.scheduleAtFixedRate(this::updateTickers, 0, queryInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void addUpdateListener(Currency from, Currency to, TickerListenerInterface listener) {

		Map<Currency, TickerListenerInterface> m = tickerUpdaters.get(from);

		if (m == null) {
			m = Collections.synchronizedMap(new HashMap<>());
			tickerUpdaters.put(from, m);
		}

		if (m.containsKey(to)) {
			logger.warn("ticker listener of [{} - {}] is replaced", from, to);
		}

		m.put(to, listener);
	}

	private void updateTickers() {
		Map<String, Map<String, Object>> tikcers = rest.getForObject("https://poloniex.com/public?command=returnTicker", Map.class);

		logger.info("get {} tickers from the poloniex", tikcers.size());
		
		tikcers.entrySet().parallelStream()
			.forEach((a)->{
				
				logger.debug("a: {}", a);
				
				Currency[] curs;
				
				try{
					curs = parseTickerName(a.getKey());
				} catch (IllegalArgumentException e) {
					logger.debug("can't identify currency {}", a.getKey());
					return;
				}
				
				Ticker toTicker = new Ticker();
				toTicker.setFrom(curs[1]);
				toTicker.setTo(curs[0]);
				toTicker.setHighestBid(Double.parseDouble((String) a.getValue().get("highestBid")));
				toTicker.setLowestAsk(Double.parseDouble((String) a.getValue().get("lowestAsk")));
				toTicker.setLast(Double.parseDouble((String) a.getValue().get("last")));
				
				Ticker fromTicker = toTicker.reverse();
				
				if (tickerUpdaters.containsKey(curs[0]) && tickerUpdaters.get(curs[0]).containsKey(curs[1])) {
					tickerUpdaters.get(curs[0]).get(curs[1]).onUpdate(fromTicker);;
				}
				
				if (tickerUpdaters.containsKey(curs[1]) && tickerUpdaters.get(curs[1]).containsKey(curs[0])) {
					tickerUpdaters.get(curs[1]).get(curs[0]).onUpdate(toTicker);;
				}
			});

	}

	private Currency[] parseTickerName(String s){
		String[] tokens = s.split("_");
		
		Currency[] cur = (Currency[]) Arrays.stream(tokens)
			.map(this::toCurrency)
			.toArray(Currency[]::new);
		
		return cur;
	}
	
	private Currency toCurrency(String s){
		if (s.equals("BTC")) {
			return Currency.BITCOIN;
		} else if (s.equals("ETH")) {
			return Currency.ETHEREUM;
		} else if (s.equals("ZEC")) {
			return Currency.ZECASH;
		} else if (s.equals("XMR")) {
			return Currency.MONERO;
		} else {
			throw new IllegalArgumentException("unknown input: " + s);
		}
	}

}
