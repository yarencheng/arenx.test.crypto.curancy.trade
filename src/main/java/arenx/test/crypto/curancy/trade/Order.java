package arenx.test.crypto.curancy.trade;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Order {

	public static class OrderKey implements Comparable<OrderKey>{

	    public String exchange;
        public OrderType type;
        public double price;

        public OrderKey(){

        }

        public OrderKey(String exchange, OrderType type, double price) {
            this.exchange = exchange;
            this.type = type;
            this.price = price;
        }

        @Override
        public int compareTo(OrderKey o) {
            int r = Double.compare(price, o.price);

            if (0 != r) {
                return r;
            }

            r = type.compareTo(o.type);

            if (0 != r) {
                return r;
            }

            return exchange.compareTo(o.exchange);
        }

        public OrderKey copy(){
            return new OrderKey(exchange, type, price);
        }

    }



	public Order(String exchange, OrderType type, Double price, Double volume, Long updateMilliSeconds) {
        this.exchange = exchange;
        this.type = type;
        this.price = price;
        this.updateMilliSeconds = updateMilliSeconds;
        this.volume = volume;
    }

    @PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	@Column(allowsNull="false")
	private String exchange;

	@Persistent
	@Column(allowsNull="false")
	private OrderType type;

	@Persistent
	@Column(allowsNull="false")
	private Double price;

	@Persistent
	@Column(allowsNull="false")
	private Long updateMilliSeconds;

    @Persistent
    @Column(allowsNull="false")
    private Double volume;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getUpdateMilliSeconds() {
        return updateMilliSeconds;
    }

    public void setUpdateMilliSeconds(Long updateMilliSeconds) {
        this.updateMilliSeconds = updateMilliSeconds;
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
        return "Order [id=" + id + ", exchange=" + exchange + ", type=" + type + ", price=" + price + ", updateMilliSeconds=" + updateMilliSeconds + ", volume=" + volume + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((updateMilliSeconds == null) ? 0 : updateMilliSeconds.hashCode());
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
        if (type != other.type)
            return false;
        if (updateMilliSeconds == null) {
            if (other.updateMilliSeconds != null)
                return false;
        } else if (!updateMilliSeconds.equals(other.updateMilliSeconds))
            return false;
        if (volume == null) {
            if (other.volume != null)
                return false;
        } else if (!volume.equals(other.volume))
            return false;
        return true;
    }






}
