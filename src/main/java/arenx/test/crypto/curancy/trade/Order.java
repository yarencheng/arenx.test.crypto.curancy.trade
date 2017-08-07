package arenx.test.crypto.curancy.trade;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Order {

	public enum Type{
		ASK, BID;
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private long id;

	@Persistent
	@Column(allowsNull="false")
	private Currency fromCurrency;

	@Persistent
	@Column(allowsNull="false")
	private Currency toCurrency;

	@Persistent
	@Column(allowsNull="false")
	private String exchange;

	@Persistent
	@Column(allowsNull="false")
	private Type type;

	@Persistent
	@Column(allowsNull="false")
	private Double price;

	@Persistent
	@Column(allowsNull="false")
	private Double volume;

	public Currency getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(Currency fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public Currency getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(Currency toCurrency) {
		this.toCurrency = toCurrency;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public long getId() {
		return id;
	}

    @Override
    public String toString() {
        return "Order [id=" + id + ", fromCurrency=" + fromCurrency + ", toCurrency=" + toCurrency + ", exchange=" + exchange + ", type=" + type + ", price=" + price + ", volume=" + volume + "]";
    }



}
