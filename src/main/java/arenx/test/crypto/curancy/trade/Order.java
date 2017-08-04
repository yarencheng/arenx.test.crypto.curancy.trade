package arenx.test.crypto.curancy.trade;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table="stock")
public class Order {
	@PrimaryKey
	private long id;

	@Persistent
	private String exchange;

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public long getId() {
		return id;
	}



}
