package arenx.test.crypto.curancy.trade.bitfinex;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Channel;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Event;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Frequency;
import arenx.test.crypto.curancy.trade.bitfinex.WebSocketBean.Precision;

public class WebSocketBeanTest {

    @Test
    public void jsonSerialize_subscribe_book() throws JsonProcessingException {
        WebSocketBean bean = new WebSocketBean();

        bean.event = Event.SUBSCRIBE;
        bean.channel = Channel.BOOK;
        bean.symbol = "tETHBTC";
        bean.precision = Precision.P0;
        bean.frequency = Frequency.F0;
        bean.length = 25;

        ObjectMapper mapper = new ObjectMapper();
        String actual = mapper.writeValueAsString(bean);

        String expected = "{\"event\":\"subscribe\",\"channel\":\"book\",\"symbol\":\"tETHBTC\",\"prec\":\"P0\",\"freq\":\"F0\",\"len\":25}";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void jsonDeserialize_subscribed_book() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String json = "{\"event\":\"subscribed\",\"channel\":\"book\",\"chanId\":30245,\"symbol\":\"tETHBTC\",\"prec\":\"P0\",\"freq\":\"F0\",\"len\":\"25\",\"pair\":\"ETHBTC\"}";

        WebSocketBean actual = mapper.readValue(json, WebSocketBean.class);

        WebSocketBean expected = new WebSocketBean();

        expected.event = Event.SUBSCRIBED;
        expected.channel = Channel.BOOK;
        expected.channelId = 30245;
        expected.symbol = "tETHBTC";
        expected.precision = Precision.P0;
        expected.frequency = Frequency.F0;
        expected.length = 25;
        expected.pair = "ETHBTC";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void jsonDeserialize_info_version() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String json = "{\"event\":\"info\",\"version\":2}";

        WebSocketBean actual = mapper.readValue(json, WebSocketBean.class);

        WebSocketBean expected = new WebSocketBean();

        expected.event = Event.INFO;
        expected.version = 2;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void jsonDeserialize_info_code() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String json = "{\"event\":\"info\",\"code\":20051,\"msg\":\"aaaa\"}";

        WebSocketBean actual = mapper.readValue(json, WebSocketBean.class);

        WebSocketBean expected = new WebSocketBean();

        expected.event = Event.INFO;
        expected.code = 20051;
        expected.message = "aaaa";

        Assert.assertEquals(expected, actual);
    }
}
