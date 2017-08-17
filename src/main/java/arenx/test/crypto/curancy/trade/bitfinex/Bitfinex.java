package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
public class Bitfinex extends BaseWebSocketClient{

    private static Logger logger = LoggerFactory.getLogger(Bitfinex.class);

    public static final String Bitfinex = "Bitfinex";

    protected static final Set<Currency> tZECBTCs = Sets.immutableEnumSet(Currency.ZECASH, Currency.BITCOIN);
    protected static final Set<Currency> tETHBTCs = Sets.immutableEnumSet(Currency.ETHEREUM, Currency.BITCOIN);

    protected static final List<Currency> tZECBTCl = Collections.unmodifiableList(Arrays.asList(Currency.ZECASH, Currency.BITCOIN));
    protected static final List<Currency> tETHBTCl = Collections.unmodifiableList(Arrays.asList(Currency.ETHEREUM, Currency.BITCOIN));

    private OrderKey key = new OrderKey(); // reused object
    private SortedMap<OrderKey, Order> orders = new TreeMap<>();
    private SortedMap<Integer, String> subscribedBooks = Collections.synchronizedSortedMap(new TreeMap<>());
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode subscribe = mapper.createObjectNode()
            .put("event", "subscribe")
            .put("channel", "book")
            .put("precision", "P0")
            .put("frequency", "F0")
            .put("length", 25);

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
            throw new BitfinexException(s, e);
        } catch (IOException e) {
            String s = String.format("IO error when parsing [%s]", message);
            logger.error(s, e);
            throw new BitfinexException(s, e);
        }

        switch (node.getNodeType()) {
        case ARRAY:
            handleData(node);
            break;
        case OBJECT:
            handleEvent(node);
            break;
        default:
            String s = String.format("not a valid type [%s]", node.getNodeType());
            logger.error(s);
            throw new BitfinexException(s);
        }

    }

    @Override
    protected URI getURI() {
        return URI.create("wss://api.bitfinex.com/ws/2");
    }

    @PostConstruct
    private void start(){
        monitoredCurrency.forEach(c->subscribeOrder(c));
    }

    protected void subscribeOrder(Set<Currency> currencies){
        Validate.notNull(currencies);
        Validate.isTrue(currencies.size() == 2);

        String symbol = toSymbol(currencies);

        logger.info("subscribe [{}]", symbol);

        subscribe.put("symbol", symbol);

        sendMessage(subscribe.toString());
    }

    protected void handleData(JsonNode node) throws BitfinexException{

        int id = node.get(0).asInt();

        switch (node.get(1).getNodeType()) {
        case ARRAY:
            if (subscribedBooks.containsKey(id)) {
                handleBookData(subscribedBooks.get(id), node.get(1));
                return;
            } else {
                logger.error("TODO {}", node);
                return;
            }
        case STRING:
            break;
        default:
            throw new BitfinexException(String.format("invalid type [%s] of node [%s]", node.get(1).getNodeType(), node));
        }
    }

    protected void handleBookData(String symbol, JsonNode node){
        List<Currency> currencies = toCurrencies(symbol);

        if (!node.get(0).isDouble()) {
            node.forEach(child->handleBookData(symbol, child));
            return;
        }

        double price = node.get(0).asDouble();
        int count = node.get(1).asInt();
        double amount = node.get(2).asDouble();

        Order order = null;

        if (0 == count) {
            if (1 == amount) {
                key.symbol = symbol;
                key.type = Order.Type.BID;
                key.price = price;
            } else if (-1 == amount) {
                key.symbol = symbol;
                key.type = Order.Type.ASK;
                key.price = price;
            } else {
                throw new RuntimeException("invalid data");
            }

            order = orders.remove(key);
            order.setVolume(0.0);

        } else if (0 < count) {
            if (0 < amount) {
                key.symbol = symbol;
                key.type = Order.Type.BID;
                key.price = price;
            } else if (0 > amount) {
                key.symbol = symbol;
                key.type = Order.Type.ASK;
                key.price = price;
            } else {
                throw new RuntimeException("invalid data");
            }

            order = orders.get(key);

            if (null == order) {
                order = new Order();
                order.setExchange(Bitfinex);
                order.setFromCurrency(currencies.get(0));
                order.setToCurrency(currencies.get(1));
                order.setPrice(price);
                order.setType(key.type);
                order.setVolume(0.0);
                orders.put(key.copy(), order);
            }

            order.setVolume(order.getVolume() + Math.abs(amount));

        } else {
            throw new RuntimeException("invalid data");
        }

        Order o = order;
        orderUpdateListeners.forEach(l->l.OnUpdate(o));
    }

    protected void handleEvent(JsonNode node) throws BitfinexException{
        String event = node.get("event").asText();

        if ("info".equals(event)) {
            JsonNode version = node.get("version");
            if (null == version) {
                handleInfo(node.get("code").asInt(), node.get("msg").asText());
            } else {
                handleInfoVersion(version.asInt());
            }

            return;
        } else if ("subscribed".equals(event)) {
            handleSubscribed(node.get("channel").asText(), node.get("chanId").asInt(), node);
            return;
        }
    }

    protected void handleInfoVersion(int version) throws BitfinexException{
        logger.info("Connection version [{}]", version);
    }

    protected void handleInfo(int code, String message) throws BitfinexException{
        switch (code) {
        case 20051:
            logger.error("TODO 20051");
            break;
        case 20060:
            logger.error("TODO 20060");
            break;
        case 20061:
            logger.error("TODO 20061");
            break;
        default:
            throw new BitfinexException(String.format("Unknown code [%d] with message [%s]", code, message));
        }
    }

    protected void handleSubscribed(String channel, int id, JsonNode node){
        if ("book".equals(channel)) {
            handleSubscribedBook(id, node);
            return;
        } else {
            logger.error("TODO {} {} {}", channel, id, node);
        }
    }

    protected void handleSubscribedBook(int id, JsonNode node){
        String symbol = node.get("symbol").asText();
        logger.info("book {} is subscribed", symbol);
        subscribedBooks.put(id, node.get("symbol").asText());
    }

    protected static String toSymbol(Set<Currency> currencies){
        if (tZECBTCs.equals(currencies)) {
            return "tZECBTC";
        } else if (tETHBTCs.equals(currencies)) {
            return "tETHBTC";
        } else {
            throw new IllegalArgumentException(String.format("unsupport pair of currency [%s]", currencies));
        }
    }

    protected static List<Currency> toCurrencies(String symbol){
        if ("tZECBTC".equals(symbol)) {
            return tZECBTCl;
        } else if ("tETHBTC".equals(symbol)) {
            return tETHBTCl;
        } else {
            throw new IllegalArgumentException("unknown symbol [" + symbol + "]");
        }
    }
}
