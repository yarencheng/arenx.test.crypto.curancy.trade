package arenx.test.crypto.curancy.trade;

public interface TickerProviderInterface {

	public void addUpdateListener(Currency from, Currency to, TickerListenerInterface listener);
}
