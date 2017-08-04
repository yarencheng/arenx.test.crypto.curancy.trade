package arenx.test.crypto.curancy.trade;

public class Ticker {
	private Currency fromCurrency;
	private Currency toCurrency;
	private double last;
	private double lowestAsk;		
	private double highestBid;
	
	public Ticker reverse(){
		Ticker ticker = new Ticker();
		ticker.setFromCurrency(toCurrency);
		ticker.setToCurrency(fromCurrency);
		ticker.setHighestBid(1 / lowestAsk);
		ticker.setLowestAsk(1.0 / highestBid);
		ticker.setLast(1 / last);
		
		return ticker;
	}
	
	@Override
	public String toString() {
		return "Ticker [Currency=" + fromCurrency + ", toCurrency=" + toCurrency + ", last=" + last + ", lowestAsk=" + lowestAsk + ", highestBid=" + highestBid + "]";
	}

	public Currency getFromCurrency() {
		return fromCurrency;
	}
	public void setFromCurrency(Currency from) {
		this.fromCurrency = from;
	}
	public Currency getToCurrency() {
		return toCurrency;
	}
	public void setToCurrency(Currency to) {
		this.toCurrency = to;
	}	
	public double getLast() {
		return last;
	}
	public void setLast(double last) {
		this.last = last;
	}
	public double getLowestAsk() {
		return lowestAsk;
	}
	public void setLowestAsk(double lowestAsk) {
		this.lowestAsk = lowestAsk;
	}
	public double getHighestBid() {
		return highestBid;
	}
	public void setHighestBid(double highestBid) {
		this.highestBid = highestBid;
	}
}
