package arenx.test.crypto.curancy.trade.bitfinex;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(Include.NON_NULL)
public class WebSocketBean {
    /*
     * {\"event\": \"subscribe\", \"channel\": \"book\", \"symbol\": \"tETHBTC\", \"prec\": \"P0\",  \"freq\": \"F0\",  \"len\"
     * : 25 }
     *
     * {"event":"subscribed","channel":"book","chanId":30245,"symbol":"tETHBTC","prec":"P0","freq":"F0","len":"25","pair":"ETHBTC"}
     *
     * {"event":"info","version":2}
     *
     */

    public enum Event {
        SUBSCRIBE, SUBSCRIBED, INFO;

        @JsonValue
        @Override
        public String toString() {
            switch (this) {
            case SUBSCRIBE:
                return "subscribe";
            case SUBSCRIBED:
                return "subscribed";
            case INFO:
                return "info";
            default:
                throw new IllegalArgumentException(String.format("unknown value [%s]", name()));
            }
        }

        public static Event get(String value) {
            if ("subscribe".equals(value)) {
                return SUBSCRIBE;
            } else if ("subscribed".equals(value)) {
                return SUBSCRIBED;
            } else if ("info".equals(value)) {
                return INFO;
            } else {
                throw new IllegalArgumentException(String.format("unknown value [%s]", value));
            }
        }
    }

    public enum Channel {
        BOOK;

        @JsonValue
        @Override
        public String toString() {
            switch (this) {
            case BOOK:
                return "book";
            default:
                throw new IllegalArgumentException(String.format("unknown value [%s]", name()));
            }
        }

        public static Channel get(String value) {
            if ("book".equals(value)) {
                return BOOK;
            } else {
                throw new IllegalArgumentException(String.format("unknown value [%s]", value));
            }
        }
    }

    public enum Precision {
        P0, P1, P2, P3;

        @Override
        public String toString() {
            switch (this) {
            case P0:
                return "P0";
            case P1:
                return "P1";
            case P2:
                return "P2";
            case P3:
                return "P3";
            default:
                throw new IllegalArgumentException(String.format("unknown value [%s]", name()));
            }
        }

        public static Precision get(String value) {
            if ("P0".equals(value)) {
                return P0;
            } else if ("P1".equals(value)) {
                return P1;
            } else if ("P2".equals(value)) {
                return P2;
            } else if ("P3".equals(value)) {
                return P3;
            } else {
                throw new IllegalArgumentException(String.format("unknown value [%s]", value));
            }
        }
    }

    public enum Frequency {
        F0, F1, F2, F3;

        @Override
        public String toString() {
            switch (this) {
            case F0:
                return "F0";
            case F1:
                return "F1";
            case F2:
                return "F2";
            case F3:
                return "F3";
            default:
                throw new IllegalArgumentException(String.format("unknown value [%s]", name()));
            }
        }

        public static Frequency get(String value) {
            if ("F0".equals(value)) {
                return F0;
            } else if ("F1".equals(value)) {
                return F1;
            } else if ("F2".equals(value)) {
                return F2;
            } else if ("F3".equals(value)) {
                return F3;
            } else {
                throw new IllegalArgumentException(String.format("unknown value [%s]", value));
            }
        }
    }

    @JsonProperty
    public Event event;

    @JsonProperty
    public Channel channel;

    @JsonProperty
    public String symbol;

    @JsonProperty(value="prec")
    public Precision precision;

    @JsonProperty(value="freq")
    public Frequency frequency;

    @JsonProperty(value="len")
    public Integer length;

    @JsonProperty
    public Integer version;

    @JsonProperty
    public Integer code;

    @JsonProperty(value="msg")
    public String message;

    @JsonProperty(value="chanId")
    public Integer channelId;

    @JsonProperty
    public String pair;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((pair == null) ? 0 : pair.hashCode());
        result = prime * result + ((precision == null) ? 0 : precision.hashCode());
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebSocketBean other = (WebSocketBean) obj;
        if (channel != other.channel)
            return false;
        if (channelId == null) {
            if (other.channelId != null)
                return false;
        } else if (!channelId.equals(other.channelId))
            return false;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (event != other.event)
            return false;
        if (frequency != other.frequency)
            return false;
        if (length == null) {
            if (other.length != null)
                return false;
        } else if (!length.equals(other.length))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (pair == null) {
            if (other.pair != null)
                return false;
        } else if (!pair.equals(other.pair))
            return false;
        if (precision != other.precision)
            return false;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WebSocketBean [event=" + event + ", channel=" + channel + ", symbol=" + symbol + ", precision=" + precision + ", frequency=" + frequency + ", length=" + length + ", version=" + version
                + ", code=" + code + ", message=" + message + ", channelId=" + channelId + ", pair=" + pair + "]";
    }



}
