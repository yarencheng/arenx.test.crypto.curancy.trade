package arenx.test.crypto.curancy.trade;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class PMF {

	private static PersistenceManagerFactory pmf = null;

	public static PersistenceManager getPersistenceManager() {
		if (null == pmf) {
			pmf = JDOHelper.getPersistenceManagerFactory("test");
		}
		return pmf.getPersistenceManager();
	}
}
