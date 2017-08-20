package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

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

import arenx.test.crypto.curancy.trade.BaseWebSocketClient;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.OrderType;
import arenx.test.crypto.curancy.trade.OrderUpdater;

@Component
@Scope("prototype")
@Lazy
public class Bitfinex extends BaseWebSocketClient{

    protected static class Symbol{
        public Symbol(String symbol, Currency fromCurrency, Currency toCurrency) {
            this.symbol = symbol;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }
        public String symbol;
        public Currency fromCurrency;
        public Currency toCurrency;
    }

    private static Logger logger = LoggerFactory.getLogger(Bitfinex.class);

    public static final String Bitfinex = "Bitfinex";

    protected static final List<Symbol> symbols = Arrays.asList(
            new Symbol("tZECBTC", Currency.ZECASH, Currency.BITCOIN),
            new Symbol("tZECBTC", Currency.ETHEREUM, Currency.BITCOIN)
            );

    private boolean reversSymbol;
    private int channelId = -1;
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode subscribe = mapper.createObjectNode()
            .put("event", "subscribe")
            .put("channel", "book")
            .put("precision", "P0")
            .put("frequency", "F0")
            .put("length", 25);

    @Autowired
    private List<OrderUpdater> orderUpdaters;

    @Autowired
    private Currency fromCurrency;

    @Autowired
    private Currency toCurrency;

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
    private void start() throws BitfinexException{
        Optional<String> from  = symbols.stream()
                .filter(s->s.fromCurrency.equals(fromCurrency))
                .filter(s->s.toCurrency.equals(toCurrency))
                .map(s->s.symbol)
                .findFirst();

        Optional<String> to  = symbols.stream()
                .filter(s->s.fromCurrency.equals(toCurrency))
                .filter(s->s.toCurrency.equals(fromCurrency))
                .map(s->s.symbol)
                .findFirst();

        if (from.isPresent()) {
            reversSymbol = false;
            subscribe.put("symbol", from.get());
            logger.info("subscribe [{}]; not reverse", from.get());
        } else if (to.isPresent()) {
            reversSymbol = true;
            subscribe.put("symbol", to.get());
            logger.info("subscribe [{}]; reverse", to.get());
        } else {
            throw new BitfinexException(String.format("can't fond symbol for [%s - %s]", fromCurrency, toCurrency));
        }

        sendMessage(subscribe.toString());
    }

    protected void handleData(JsonNode node) throws BitfinexException{

        int id = node.get(0).asInt();

        switch (node.get(1).getNodeType()) {
        case ARRAY:
            if (id != channelId) {
                throw new BitfinexException(String.format("unknown channel ID [{}]", id));
            }
            handleBookData(node.get(1));
            return;
        case STRING:
            break;
        default:
            throw new BitfinexException(String.format("invalid type [%s] of node [%s]", node.get(1).getNodeType(), node));
        }
    }

    protected void handleBookData(JsonNode node){

        if (!node.get(0).isDouble()) {
            node.forEach(child->handleBookData(child));
            return;
        }

        OrderUpdater.Action action;
        OrderType type;
        double price = node.get(0).asDouble();
        int count = node.get(1).asInt();
        double volume = node.get(2).asDouble();

        if (0 == count) {
            if (1 == volume) {
                action = OrderUpdater.Action.REMOVE;
                type = reversSymbol ? OrderType.BID : OrderType.ASK;
                volume = 0;
            } else if (-1 == volume) {
                action = OrderUpdater.Action.REMOVE;
                type = reversSymbol ? OrderType.ASK : OrderType.BID;
                volume = 0;
            } else {
                throw new RuntimeException("invalid data");
            }

        } else if (0 < count) {
            action = OrderUpdater.Action.UPDATE;
            if (0 < volume) {
                type = reversSymbol ? OrderType.BID : OrderType.ASK;
            } else if (0 > volume) {
                type = reversSymbol ? OrderType.ASK : OrderType.BID;
                volume = -volume;
            } else {
                throw new RuntimeException("invalid data");
            }

        } else {
            throw new RuntimeException("invalid data");
        }

        price = reversSymbol ? (1/price) : price;
        volume = reversSymbol ? (volume/price) : volume;

        for (OrderUpdater updater: orderUpdaters) {
            updater.update(Bitfinex, action, type, price, volume);
        }
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

    protected void handleSubscribed(String channel, int id, JsonNode node) throws BitfinexException{
        if ("book".equals(channel)) {
            handleSubscribedBook(id, node);
            return;
        } else {
            logger.error("TODO {} {} {}", channel, id, node);
        }
    }

    protected void handleSubscribedBook(int id, JsonNode node) throws BitfinexException{
        if (channelId != -1) {
            throw new BitfinexException(String.format("Receive second channel ID. Don't subscribe twice."));
        }
        String symbol = node.get("symbol").asText();
        channelId = id;
        logger.info("book [{} - {}] is subscribed", id, symbol);
    }
}
