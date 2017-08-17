package arenx.test.crypto.curancy.trade.poloniex;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.BaseWebSocketClient;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.Order;
import arenx.test.crypto.curancy.trade.Order.OrderKey;
import arenx.test.crypto.curancy.trade.OrderUpdateListener;

@Component
@Scope("prototype")
@Lazy
public class Poloniex extends BaseWebSocketClient{

    private static Logger logger = LoggerFactory.getLogger(Poloniex.class);

    public static final String Poloniex = "Poloniex";

    protected static final Set<Currency> BTC_ZECs = Sets.immutableEnumSet(Currency.ZECASH, Currency.BITCOIN);
    protected static final Set<Currency> BTC_ETHs = Sets.immutableEnumSet(Currency.ETHEREUM, Currency.BITCOIN);

    protected static final List<Currency> BTC_ZECl = Collections.unmodifiableList(Arrays.asList(Currency.ZECASH, Currency.BITCOIN));
    protected static final List<Currency> BTC_ETHl = Collections.unmodifiableList(Arrays.asList(Currency.ETHEREUM, Currency.BITCOIN));

    private OrderKey key = new OrderKey(); // reused object
    private SortedMap<OrderKey, Order> orders = new TreeMap<>();
    private Map<Integer, String> channelToSymbol = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode subscribe = mapper.createObjectNode()
            .put("command", "subscribe");

    @Autowired
    private List<OrderUpdateListener> orderUpdateListeners;

    @Autowired
    private Set<Set<Currency>> monitoredCurrency;

    @Override
    protected void onMessageReceive(String message) throws Exception {

        JsonNode node = null;

        try {
            node = mapper.readTree(message);
        } catch (JsonProcessingException e) {
            String s = String.format("[%s] is not a json", message);
            logger.error(s, e);
            throw new PoloniexException(s, e);
        } catch (IOException e) {
            String s = String.format("IO error when parsing [%s]", message);
            logger.error(s, e);
            throw new PoloniexException(s, e);
        }

        int channelId = node.get(0).asInt();

        switch (channelId) {
        case 1010:
            /**
             * 1010 seems to be a heart beat.
             * It was send from server per second if there is no data
             */
            return;
        default:
        }

        long serial = node.get(1).asLong();
        ArrayNode array = (ArrayNode) node.get(2);

        handleChannel(channelId, serial, array);
    }

    @Override
    protected URI getURI() {
        return URI.create("wss://api2.poloniex.com/");
    }

    @PostConstruct
    private void start() throws PoloniexException{
        for (Set<Currency> c: monitoredCurrency) {
            subscribeOrder(c);
        }
    }

    protected void subscribeOrder(Set<Currency> currencies) throws PoloniexException{
        Validate.notNull(currencies);
        Validate.isTrue(currencies.size() == 2);

        String symbol = toSymbol(currencies);

        logger.info("subscribe [{}]", symbol);

        subscribe.put("channel", symbol);

        sendMessage(subscribe.toString());
    }

    protected static String toSymbol(Set<Currency> currencies) throws PoloniexException{
        if (BTC_ZECs.equals(currencies)) {
            return "BTC_ZEC";
        } else if (BTC_ETHs.equals(currencies)) {
            return "BTC_ETH";
        } else {
            throw new PoloniexException(String.format("unsupport pair of currency [%s]", currencies));
        }
    }

    protected static List<Currency> toCurrencies(String symbol) throws PoloniexException{
        if ("BTC_ZEC".equals(symbol)) {
            return BTC_ZECl;
        } else if ("BTC_ETH".equals(symbol)) {
            return BTC_ETHl;
        } else {
            throw new PoloniexException("unknown symbol [" + symbol + "]");
        }
    }

    protected void handleChannel(int id, long serial, ArrayNode array) throws PoloniexException{
        for (JsonNode node: array) {
            String type = node.get(0).asText();
            if ("o".equals(type)) {
                handleTypeO(id, node);
            } else if ("t".equals(type)) {
                handleTypeT(id, node);
            } else if ("i".equals(type)) {
                handleTypeI(id, node);
            } else {
                throw new PoloniexException("unknown type [" + type + "]");
            }
        }
    }

    protected void handleTypeI(int id, JsonNode node) throws PoloniexException{
        JsonNode data = node.get(1);
        String symbol = data.get("currencyPair").asText();
        List<Currency> currencies = toCurrencies(symbol);
        ArrayNode orderBook = (ArrayNode) data.get("orderBook");
        ObjectNode bids = (ObjectNode) orderBook.get(0);
        ObjectNode asks = (ObjectNode) orderBook.get(1);

        channelToSymbol.put(id, symbol);

        // remove old orders
        orders = orders.entrySet().stream()
            .filter(e->!e.getKey().symbol.equals(symbol))
            .collect(Collectors.toMap(
                    e->e.getKey(),
                    e->e.getValue(),
                    (a,b)->{
                        logger.error("dup key [{}] [{}]", a, b);
                        return a;
                    },
                    TreeMap::new
            ));

        for (Iterator<Entry<String, JsonNode>> iterator = bids.fields(); iterator.hasNext();){
            Entry<String, JsonNode> e = iterator.next();
            double price = Double.parseDouble(e.getKey());

            Order order = new Order();
            order.setExchange(Poloniex);
            order.setFromCurrency(currencies.get(0));
            order.setToCurrency(currencies.get(1));
            order.setPrice(price);
            order.setType(Order.Type.BID);
            order.setVolume(e.getValue().asDouble());

            Order.OrderKey key = new Order.OrderKey(symbol, Order.Type.BID, price);
            orders.put(key, order);

            order.setUpdateNanoSeconds(System.nanoTime());
            orderUpdateListeners.forEach(u->u.OnUpdate(order));
        }

        for (Iterator<Entry<String, JsonNode>> iterator = asks.fields(); iterator.hasNext();){
            Entry<String, JsonNode> e = iterator.next();
            double price = Double.parseDouble(e.getKey());

            Order order = new Order();
            order.setExchange(Poloniex);
            order.setFromCurrency(currencies.get(0));
            order.setToCurrency(currencies.get(1));
            order.setPrice(price);
            order.setType(Order.Type.ASK);
            order.setVolume(e.getValue().asDouble());
            order.setUpdateNanoSeconds(System.nanoTime());

            Order.OrderKey key = new Order.OrderKey(symbol, Order.Type.ASK, price);
            orders.put(key, order);

            orderUpdateListeners.forEach(u->u.OnUpdate(order));
        }
    }

    protected void handleTypeO(int id, JsonNode node) throws PoloniexException{
        String symbol = channelToSymbol.get(id);
        int type = node.get(1).asInt();
        double price = node.get(2).asDouble();
        double volume = node.get(3).asDouble();

        key.symbol = symbol;
        key.price = price;
        key.type = 0 == type ? Order.Type.BID : Order.Type.ASK;

        Order order = null;

        if (0.0 == volume) {
            order = orders.remove(key);
        } else {
            order = orders.get(key);
        }

        if (null == order && 0.0 != volume) {
            List<Currency> currencies = toCurrencies(symbol);

            order = new Order();
            order.setExchange(Poloniex);
            order.setFromCurrency(currencies.get(0));
            order.setToCurrency(currencies.get(1));
            order.setPrice(price);
            order.setType(Order.Type.ASK);

            orders.put(key.copy(), order);
        }

        order.setUpdateNanoSeconds(System.nanoTime());
        order.setVolume(volume);

        Order o = order;
        orderUpdateListeners.forEach(u->u.OnUpdate(o));
    }

    protected void handleTypeT(int id, JsonNode node){
        // trade
    }
}
