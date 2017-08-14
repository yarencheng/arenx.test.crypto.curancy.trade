package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.bitfinex.Bitfinex;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
//		PoloniexExchange polo = context.getBean(PoloniexExchange.class);
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM));
//		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH));

		Bitfinex polo = context.getBean(Bitfinex.class);
		polo.subscribeOrder(Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM));



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
