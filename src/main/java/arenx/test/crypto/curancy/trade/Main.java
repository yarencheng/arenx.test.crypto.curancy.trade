package arenx.test.crypto.curancy.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import arenx.test.crypto.curancy.trade.bitfinex.BitfinexExchange;

public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);

//		PoloniexExchange polo = context.getBean(PoloniexExchange.class);
//
//		polo.addUpdateListener(Currency.BITCOIN, Currency.ZECASH, (a)->{
//			logger.info("polo {}", a);
//		});

		BitfinexExchange bitf = context.getBean(BitfinexExchange.class);

//		bitf.addUpdateListener(Currency.BITCOIN, Currency.ZECASH, (a)->{
//			logger.info("bitf {}", a);
//		});

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
