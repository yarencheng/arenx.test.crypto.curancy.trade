package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import arenx.test.crypto.curancy.trade.BaseExchange;
import arenx.test.crypto.curancy.trade.Currency;
import arenx.test.crypto.curancy.trade.Ticker;
import arenx.test.crypto.curancy.trade.TickerListenerInterface;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Channel;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Event;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Frequency;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Precision;

@Component
@Scope("singleton")
@Lazy
public class BitfinexExchange extends BaseExchange {

	private static Logger logger = LoggerFactory.getLogger(BitfinexExchange.class);

	@Value("${bitfinex.query.interval}")
	private long queryInterval;

	@Autowired
	private Set<Set<Currency>> monitoredCurrency;

	private WebSocketConnectionManager wsm;
	private ObjectMapper mapper = new ObjectMapper();
	private LinkedList<WebSocketBean> unhandledBeans = new LinkedList<>();

	private RestOperations rest = new RestTemplate();
	private ScheduledExecutorService ex = new ScheduledThreadPoolExecutor(1);
	private Set<String> tickerSymols = Collections.synchronizedSet(new HashSet<>());

	@PostConstruct
	private void postConstruct() {

		logger.info("create connection to Bitfinex");
		wsm = createWebSocket();
		wsm.start();

		Thread t = new Thread(()->{
		    while (true) {
		        WebSocketBean bean;

		        synchronized (unhandledBeans) {
		            if (unhandledBeans.isEmpty()) {
	                    try {
	                        unhandledBeans.wait(100);
	                    } catch (InterruptedException e) {
	                        logger.error("interrupted", e);
	                    }
	                    continue;
	                }

		            bean = unhandledBeans.pollFirst();
                }

                handle(bean);
		    }
		});
		logger.info("thread [{}] is starting", t.getName());
		t.start();
	}

	@Override
	public String getName(){
		return "Bitfinex";
	}

	private WebSocketConnectionManager createWebSocket(){
		WebSocketClient client = new JettyWebSocketClient();

		return new WebSocketConnectionManager(
				client,
				new WebSocketHandler(){

				    @Override
					public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
						logger.info("connection to Bitfinex is closed({}).", status);
					}

					@Override
					public void afterConnectionEstablished(WebSocketSession session) throws Exception {
						logger.info("connection to Bitfinex is established.");
						addChannels(session);
					}

					@Override
					public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
						logger.debug("recieve message [{}]", message.getPayload());
						logger.error("recieve message [{}]", message.getPayload());

						if (!(message instanceof TextMessage)) {
						    logger.error("an unreconized message [{}]", message);
						    return;
						}

						TextMessage t = (TextMessage)message;

						WebSocketBean bean = mapper.readValue(t.getPayload(), WebSocketBean.class);

						synchronized (unhandledBeans) {
						    unhandledBeans.add(bean);
	                        unhandledBeans.notify();
                        }

//						   expp

//						ExpLexer e;

					}

					@Override
					public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
						logger.error("recieve error from Bitfinex [{}]", e.getMessage());
						logger.debug("recieve error from Bitfinex", e);
					}

					@Override
					public boolean supportsPartialMessages() {
						return false;
					}},
				"wss://api.bitfinex.com/ws/2"
			);
	}

    private void addChannels(WebSocketSession session) {

        monitoredCurrency.stream()
            .map(currencies -> toSymbol(currencies))
            .peek(symbol->logger.info("add book channel of [{}]", symbol))
            .map(symbol->{
                WebSocketBean bean = new WebSocketBean();

                bean.event = Event.SUBSCRIBE;
                bean.channel = Channel.BOOK;
                bean.symbol = symbol;
                bean.precision = Precision.P0;
                bean.frequency = Frequency.F0;
                bean.length = 25;

                return bean;
            })
            .map(bean->{
                try {
                    return mapper.writeValueAsString(bean);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize object to json string", e);
                }
            })
            .map(json->new TextMessage(json))
            .forEach(message->{
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send message to establish channel", e);
                }
            })
            ;
    }

    private void handle(WebSocketBean bean){
        switch (bean.event) {
        case INFO:
            handleInfo(bean);
            break;
        case SUBSCRIBED:
            handleSubscribed(bean);
            break;
        default:
            throw new RuntimeException(String.format("unrecognized event [%s]", bean.event));
        }
    }

    private void handleSubscribed(WebSocketBean bean){

    }

    private void handleInfo(WebSocketBean bean){
        if (null != bean.version) {
            logger.info("using version [{}] of Bitfinex Websocket", bean.version);
            return;
        }

        switch (bean.code) {
        case 20051:
            logger.error("TODO: shoule reconnet");
            System.exit(1);
            break;
        case 20060:
            logger.info("Bitinex is Refreshing data from the Trading Engine.");
            break;

        default:
            logger.error("TODO: shoule unsubscribe/subscribe again all channels");
            System.exit(1);
            break;
        }
    }

	private String toSymbol(Set<Currency> currencies){
	    if (Sets.newHashSet(Currency.ZECASH, Currency.BITCOIN).equals(currencies)) {
	        return "tZECBTC";
	    } else if (Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN).equals(currencies)) {
            return "tETHBTC";
        } else {
	        throw new IllegalArgumentException(String.format("unsupport pair of currency [%s]", currencies));
	    }
    }

	private Set<Currency> fromSymbol(String symbol){
	    if ("tZECBTC".equals(symbol)) {
            return Sets.newHashSet(Currency.ZECASH, Currency.BITCOIN);
        } else if ("tETHBTC".equals(symbol)) {
            return Sets.newHashSet(Currency.ETHEREUM, Currency.BITCOIN);
        } else {
            throw new IllegalArgumentException(String.format("unsupport symbol [%s]", symbol));
        }
    }



	@Override
	public void addUpdateListener(Currency from, Currency to, TickerListenerInterface listener) {

		if (from == Currency.BITCOIN && to == Currency.ZECASH) {
			tickerSymols.add("tZECBTC");
		} else if (from == Currency.ZECASH && to == Currency.BITCOIN) {
			tickerSymols.add("tZECBTC");
		} else {
			throw new IllegalArgumentException(String.format("no such mapping rule of [%s - %s]", from, to));
		}

		super.addUpdateListener(from, to, listener);
	}

	private void updateTickers() {

		String url = "https://api.bitfinex.com/v2/tickers?symbols=" + tickerSymols.stream().collect(Collectors.joining(","));

		List<List<Object>> tikcers = rest.getForObject(url, List.class);

		tikcers.parallelStream().forEach(ticker -> {
			Currency[] curs;

			try{
				curs = parseTickerName((String) ticker.get(0));
			} catch (IllegalArgumentException e) {
				logger.debug("can't identify currency {}", ticker.get(0));
				return;
			}

			Ticker toTicker = new Ticker();
			toTicker.setFromCurrency(curs[0]);
			toTicker.setToCurrency(curs[1]);
			toTicker.setHighestBid((double) ticker.get(1));
			toTicker.setLowestAsk((double) ticker.get(3));
			toTicker.setLast((double) ticker.get(9));

			super.update(toTicker);
		});

	}

	private Currency[] parseTickerName(String s){
		if (s.equals("tZECBTC")) {
			return new Currency[]{Currency.ZECASH, Currency.BITCOIN};
		}

		throw new IllegalArgumentException("unknow symbol: " + s);
	}

}
