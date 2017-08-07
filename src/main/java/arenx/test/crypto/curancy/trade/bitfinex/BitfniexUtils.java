package arenx.test.crypto.curancy.trade.bitfinex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.Currency;

public class BitfniexUtils {

    public static final Set<Currency> tZECBTCs = Sets.immutableEnumSet(Currency.ZECASH, Currency.BITCOIN);
    public static final Set<Currency> tETHBTCs = Sets.immutableEnumSet(Currency.ETHEREUM, Currency.BITCOIN);

    public static final List<Currency> tZECBTCl = Collections.unmodifiableList(Arrays.asList(Currency.ZECASH, Currency.BITCOIN));
    public static final List<Currency> tETHBTCl = Collections.unmodifiableList(Arrays.asList(Currency.ETHEREUM, Currency.BITCOIN));

    public static String toSymbol(Set<Currency> currencies){
        if (tZECBTCs.equals(currencies)) {
            return "tZECBTC";
        } else if (tETHBTCs.equals(currencies)) {
            return "tETHBTC";
        } else {
            throw new IllegalArgumentException(String.format("unsupport pair of currency [%s]", currencies));
        }
    }

    public static List<Currency> toCurrencies(String symbol){
        if ("tZECBTC".equals(symbol)) {
            return tZECBTCl;
        } else if ("tETHBTC".equals(symbol)) {
            return tETHBTCl;
        } else {
            throw new IllegalArgumentException("unknown symbol [" + symbol + "]");
        }
    }
}
