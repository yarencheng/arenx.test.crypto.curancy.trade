package arenx.test.crypto.curancy.trade;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseExchange implements TickerProviderInterface {

	private static Logger logger = LoggerFactory.getLogger(BaseExchange.class);

	private Map<Currency, Map<Currency, List<TickerListenerInterface>>> tickerUpdaters = Collections.synchronizedMap(new HashMap<>());
	
	public abstract String getName();

	public void addUpdateListener(Currency from, Currency to, TickerListenerInterface listener) {
		Map<Currency, List<TickerListenerInterface>> m = tickerUpdaters.get(from);

		if (m == null) {
			m = Collections.synchronizedMap(new HashMap<>());
			tickerUpdaters.put(from, m);
		}

		List<TickerListenerInterface> l = m.get(to);

		if (l == null) {
			l = Collections.synchronizedList(new LinkedList<>());
			m.put(to, l);
		}

		l.add(listener);
	}

	protected void update(Ticker ticker) {
		if (tickerUpdaters.containsKey(ticker.getFromCurrency()) && tickerUpdaters.get(ticker.getFromCurrency()).containsKey(ticker.getToCurrency())) {
			tickerUpdaters.get(ticker.getFromCurrency()).get(ticker.getToCurrency()).forEach(updater -> updater.onUpdate(ticker));
		}

		Ticker fromTicker = ticker.reverse();

		if (tickerUpdaters.containsKey(fromTicker.getFromCurrency()) && tickerUpdaters.get(fromTicker.getFromCurrency()).containsKey(fromTicker.getToCurrency())) {
			tickerUpdaters.get(fromTicker.getFromCurrency()).get(fromTicker.getToCurrency()).forEach(updater -> updater.onUpdate(ticker));
		}
	}
}
