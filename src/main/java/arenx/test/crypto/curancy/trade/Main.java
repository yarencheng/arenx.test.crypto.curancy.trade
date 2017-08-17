package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import arenx.test.crypto.curancy.trade.poloniex.Poloniex;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
//		PoloniexExchange polo = context.getBean(PoloniexExchange.class);
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM));
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH));

//		Bitfinex bit = context.getBean(Bitfinex.class);

		Poloniex polo = context.getBean(Poloniex.class);


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
