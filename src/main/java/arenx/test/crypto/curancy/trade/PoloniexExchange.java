package arenx.test.crypto.curancy.trade;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Component
@Scope("singleton")
@Lazy
public class PoloniexExchange extends BaseExchange {

	private static Logger logger = LoggerFactory.getLogger(PoloniexExchange.class);

	@Value("${poloniex.query.interval}")
	private long queryInterval;

	private RestOperations rest = new RestTemplate();
	private ScheduledExecutorService ex = new ScheduledThreadPoolExecutor(1);

	@PostConstruct
	private void postConstruct() {
		ex.scheduleAtFixedRate(this::updateTickers, 0, queryInterval, TimeUnit.MILLISECONDS);
	}

	@Override
    public String getName(){
		return "Poloniex";
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
				toTicker.setFromCurrency(curs[1]);
				toTicker.setToCurrency(curs[0]);
				toTicker.setHighestBid(Double.parseDouble((String) a.getValue().get("highestBid")));
				toTicker.setLowestAsk(Double.parseDouble((String) a.getValue().get("lowestAsk")));
				toTicker.setLast(Double.parseDouble((String) a.getValue().get("last")));

				super.update(toTicker);
			});

	}

	private Currency[] parseTickerName(String s){
		String[] tokens = s.split("_");

		Currency[] cur = Arrays.stream(tokens)
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
