package arenx.test.crypto.curancy.trade;

public class Ticker {
	private Currency from;
	private Currency to;
	private double last;
	private double lowestAsk;		
	private double highestBid;
	
	public Ticker reverse(){
		Ticker ticker = new Ticker();
		ticker.setFrom(to);
		ticker.setTo(from);
		ticker.setHighestBid(1 / lowestAsk);
		ticker.setLowestAsk(1.0 / highestBid);
		ticker.setLast(1 / last);
		
		return ticker;
	}
	
	@Override
	public String toString() {
		return "Ticker [from=" + from + ", to=" + to + ", last=" + last + ", lowestAsk=" + lowestAsk + ", highestBid=" + highestBid + "]";
	}

	public Currency getFrom() {
		return from;
	}
	public void setFrom(Currency from) {
		this.from = from;
	}
	public Currency getTo() {
		return to;
	}
	public void setTo(Currency to) {
		this.to = to;
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
