package arenx.test.crypto.curancy.trade;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
public class BitfinexExchange implements TickerProviderInterface {

	private static Logger logger = LoggerFactory.getLogger(BitfinexExchange.class);

	@Value("${bitfinex.query.interval}")
	private long queryInterval;

	private RestOperations rest = new RestTemplate();
	private ScheduledExecutorService ex = new ScheduledThreadPoolExecutor(1);
	private Map<Currency, Map<Currency, TickerListenerInterface>> tickerUpdaters = Collections.synchronizedMap(new HashMap<>());
	private Set<String> tickerSymols = Collections.synchronizedSet(new HashSet<>());

	public void start() {
		ex.scheduleAtFixedRate(this::updateTickers, 0, queryInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void addUpdateListener(Currency from, Currency to, TickerListenerInterface listener) {

		if (from == Currency.BITCOIN && to == Currency.ZECASH) {
			tickerSymols.add("tZECBTC");
		} else if (from == Currency.ZECASH && to == Currency.BITCOIN) {
			tickerSymols.add("tZECBTC");
		} else {
			throw new IllegalArgumentException(String.format("no such mapping rule of [%s - %s]", from, to));
		}
		
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
		
		String url = "https://api.bitfinex.com/v2/tickers?symbols=" + tickerSymols.stream().collect(Collectors.joining(","));
		
		List<List<Object>> tikcers = rest.getForObject(url, List.class);
		
		tikcers.parallelStream().forEach(ticker -> {
			Currency[] curs;
			
			try{
				curs = parseTickerName((String) ticker.get(0));
			} catch (IllegalArgumentException e) {
				logger.debug("can't identify currency {}", ticker.get(0));
				return;
			}
			
			Ticker toTicker = new Ticker();
			toTicker.setFrom(curs[0]);
			toTicker.setTo(curs[1]);
			toTicker.setHighestBid((double) ticker.get(1));
			toTicker.setLowestAsk((double) ticker.get(3));
			toTicker.setLast((double) ticker.get(9));
			
			Ticker fromTicker = toTicker.reverse();
			
			if (tickerUpdaters.containsKey(curs[0]) && tickerUpdaters.get(curs[0]).containsKey(curs[1])) {
				tickerUpdaters.get(curs[0]).get(curs[1]).onUpdate(toTicker);;
			}
			
			if (tickerUpdaters.containsKey(curs[1]) && tickerUpdaters.get(curs[1]).containsKey(curs[0])) {
				tickerUpdaters.get(curs[1]).get(curs[0]).onUpdate(fromTicker);;
			}
		});

	}

	private Currency[] parseTickerName(String s){
		if (s.equals("tZECBTC")) {
			return new Currency[]{Currency.ZECASH, Currency.BITCOIN};
		}
		
		throw new IllegalArgumentException("unknow symbol: " + s);
	}

}
