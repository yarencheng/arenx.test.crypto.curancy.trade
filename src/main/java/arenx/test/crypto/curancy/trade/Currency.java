package arenx.test.crypto.curancy.trade;

public enum Currency {
	BITCOIN, ETHEREUM, ZECASH, MONERO;
	
	public String toString(){
		switch(this){
		case BITCOIN: return "Bitcoin";
		case ETHEREUM: return "Ethereum";
		case ZECASH: return "Zcash";
		case MONERO: return "Monero";
		default: throw new RuntimeException("unknown currency");		
		}
	}
}
