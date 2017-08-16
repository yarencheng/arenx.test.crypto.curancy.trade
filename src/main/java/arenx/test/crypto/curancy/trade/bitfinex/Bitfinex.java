package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import arenx.test.crypto.curancy.trade.BaseWebSocketClient;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.Order;
import arenx.test.crypto.curancy.trade.Order.OrderKey;
import arenx.test.crypto.curancy.trade.OrderUpdateListener;

@Component
@Scope("prototype")
public class Bitfinex extends BaseWebSocketClient{

    private static Logger logger = LoggerFactory.getLogger(Bitfinex.class);

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

    @Override
    protected void onMessageReceive(String message) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.info("receive message from Bitfinex [{}]", message);
        }

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

    public void subscribeOrder(Set<Currency> currencies){
        Validate.notNull(currencies);
        Validate.isTrue(currencies.size() == 2);

        String symbol = BitfniexUtils.toSymbol(currencies);

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
        List<Currency> currencies = BitfniexUtils.toCurrencies(symbol);

        if (!node.get(0).isDouble()) {
            node.forEach(child->handleBookData(symbol, child));
            return;
        }

        double price = node.get(0).asDouble();
        int count = node.get(1).asInt();
        double amount = node.get(2).asDouble();

        OrderKey key = null;
        Order order = null;

        if (0 == count) {
            if (1 == amount) {
                key = new OrderKey(symbol, Order.Type.BID, price);
            } else if (-1 == amount) {
                key = new OrderKey(symbol, Order.Type.ASK, price);
            } else {
                throw new RuntimeException("invalid data");
            }

            order = orders.remove(key);
            order.setVolume(0.0);

        } else if (0 < count) {
            if (0 < amount) {
                key = new OrderKey(symbol, Order.Type.BID, price);
            } else if (0 > amount) {
                key = new OrderKey(symbol, Order.Type.ASK, price);
            } else {
                throw new RuntimeException("invalid data");
            }

            order = orders.get(key);

            if (null == order) {
                order = new Order();
                order.setExchange(BitfniexUtils.Bitfniex);
                order.setFromCurrency(currencies.get(0));
                order.setToCurrency(currencies.get(1));
                order.setPrice(price);
                order.setType(key.type);
                order.setVolume(0.0);
                orders.put(key, order);
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
}
