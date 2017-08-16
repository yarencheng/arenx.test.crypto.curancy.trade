package arenx.test.crypto.curancy.trade;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OrderTest {

	PersistenceManager pm;

	@Before
	public void before(){
		pm = JDOHelper.getPersistenceManagerFactory("h2_memory").getPersistenceManager();
	}

	@After
	public void after(){
	}

	@Test
	public void sss(){

		try{
			Order o = new Order();

			o.setExchange("ssss");
			o.setFromCurrency(Currency.BITCOIN);
			o.setToCurrency(Currency.BITCOIN);
			o.setPrice(123.0);
			o.setType(Order.Type.ASK);
			o.setVolume(23.0);
			o.setUpdateNanoSeconds(System.nanoTime());

			pm.makePersistent(o);
		} finally {
			pm.close();
		}

	}
}
