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
	private Long id;

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

	public Long getId() {
		return id;
	}

    @Override
    public String toString() {
        return "Order [id=" + id + ", fromCurrency=" + fromCurrency + ", toCurrency=" + toCurrency + ", exchange=" + exchange + ", type=" + type + ", price=" + price + ", volume=" + volume + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
        result = prime * result + ((fromCurrency == null) ? 0 : fromCurrency.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((toCurrency == null) ? 0 : toCurrency.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((volume == null) ? 0 : volume.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (exchange == null) {
            if (other.exchange != null)
                return false;
        } else if (!exchange.equals(other.exchange))
            return false;
        if (fromCurrency != other.fromCurrency)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (toCurrency != other.toCurrency)
            return false;
        if (type != other.type)
            return false;
        if (volume == null) {
            if (other.volume != null)
                return false;
        } else if (!volume.equals(other.volume))
            return false;
        return true;
    }



}
