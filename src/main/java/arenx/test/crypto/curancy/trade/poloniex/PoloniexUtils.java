package arenx.test.crypto.curancy.trade.poloniex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.Currency;

public class PoloniexUtils {

    public static final Set<Currency> BTC_ZECs = Sets.immutableEnumSet(Currency.ZECASH, Currency.BITCOIN);
    public static final Set<Currency> BTC_ETHs = Sets.immutableEnumSet(Currency.ETHEREUM, Currency.BITCOIN);

    public static final List<Currency> BTC_ZECl = Collections.unmodifiableList(Arrays.asList(Currency.ZECASH, Currency.BITCOIN));
    public static final List<Currency> BTC_ETHl = Collections.unmodifiableList(Arrays.asList(Currency.ETHEREUM, Currency.BITCOIN));

    public static String toSymbol(Set<Currency> currencies){
        if (BTC_ZECs.equals(currencies)) {
            return "BTC_ZEC";
        } else if (BTC_ETHs.equals(currencies)) {
            return "BTC_ET";
        } else {
            throw new IllegalArgumentException(String.format("unsupport pair of currency [%s]", currencies));
        }
    }

    public static List<Currency> toCurrencies(String symbol){
        if ("BTC_ZEC".equals(symbol)) {
            return BTC_ZECl;
        } else if ("BTC_ETH".equals(symbol)) {
            return BTC_ETHl;
        } else {
            throw new IllegalArgumentException("unknown symbol [" + symbol + "]");
        }
    }
}
