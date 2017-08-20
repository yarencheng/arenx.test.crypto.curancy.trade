package arenx.test.crypto.curancy.trade.poloniex;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import arenx.test.crypto.curancy.trade.BaseWebSocketClient;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.OrderType;
import arenx.test.crypto.curancy.trade.OrderUpdater;

@Component
@Scope("prototype")
@Lazy
public class Poloniex extends BaseWebSocketClient{

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

    private static Logger logger = LoggerFactory.getLogger(Poloniex.class);

    public static final String Poloniex = "Poloniex";

    protected static final List<Symbol> symbols = Arrays.asList(
            new Symbol("BTC_ZEC", Currency.ZECASH, Currency.BITCOIN),
            new Symbol("BTC_ZEC", Currency.ETHEREUM, Currency.BITCOIN)
            );

    private boolean reversSymbol;
    private int channelId = -1;
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode subscribe = mapper.createObjectNode()
            .put("command", "subscribe");

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
            subscribe.put("channel", from.get());
            logger.info("subscribe [{}]; not reverse", from.get());
        } else if (to.isPresent()) {
            reversSymbol = true;
            subscribe.put("channel", to.get());
            logger.info("subscribe [{}]; reverse", to.get());
        } else {
            throw new PoloniexException(String.format("can't fond symbol for [%s - %s]", fromCurrency, toCurrency));
        }

        sendMessage(subscribe.toString());
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
        ArrayNode orderBook = (ArrayNode) data.get("orderBook");
        ObjectNode bids = (ObjectNode) orderBook.get(0);
        ObjectNode asks = (ObjectNode) orderBook.get(1);

        for (OrderUpdater updater: orderUpdaters) {
            updater.removeAll(Poloniex);
        }

        for (Iterator<Entry<String, JsonNode>> iterator = bids.fields(); iterator.hasNext();){
            Entry<String, JsonNode> e = iterator.next();
            double price = Double.parseDouble(e.getKey());
            double volume = e.getValue().asDouble();

            price = reversSymbol ? (1/price) : price;
            volume = reversSymbol ? (volume/price) : volume;
            OrderType type = reversSymbol ? OrderType.ASK : OrderType.BID;

            for (OrderUpdater updater: orderUpdaters) {
                updater.update(Poloniex, OrderUpdater.Action.UPDATE, type, price, volume);
            }
        }

        for (Iterator<Entry<String, JsonNode>> iterator = asks.fields(); iterator.hasNext();){
            Entry<String, JsonNode> e = iterator.next();
            double price = Double.parseDouble(e.getKey());
            double volume = e.getValue().asDouble();

            price = reversSymbol ? (1/price) : price;
            volume = reversSymbol ? (volume/price) : volume;
            OrderType type = reversSymbol ? OrderType.BID : OrderType.ASK;

            for (OrderUpdater updater: orderUpdaters) {
                updater.update(Poloniex, OrderUpdater.Action.UPDATE, type, price, volume);
            }
        }
    }

    protected void handleTypeO(int id, JsonNode node) throws PoloniexException{
        int type = node.get(1).asInt();
        double price = node.get(2).asDouble();
        double volume = node.get(3).asDouble();

        OrderUpdater.Action action;
        OrderType oType = 0 == type
                ? reversSymbol ? OrderType.ASK : OrderType.BID
                : reversSymbol ? OrderType.BID : OrderType.ASK;

        if (0.0 == volume) {
            action = OrderUpdater.Action.REMOVE;
        } else {
            action = OrderUpdater.Action.REPLACE;
        }

        price = reversSymbol ? (1/price) : price;
        volume = reversSymbol ? (volume/price) : volume;

        for (OrderUpdater updater: orderUpdaters) {
            updater.update(Poloniex, action, oType, price, volume);
        }

    }

    protected void handleTypeT(int id, JsonNode node){
        // trade
    }
}
