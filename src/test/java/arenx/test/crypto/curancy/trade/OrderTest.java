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

		Order o = new Order();

		o.setExchange("ssss");

		pm.makePersistent(o);

		pm.close();
	}
}
