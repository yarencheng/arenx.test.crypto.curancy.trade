package arenx.test.crypto.curancy.trade.bitfinex;

import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import arenx.test.crypto.curancy.trade.antlr4.BitfinexLexer;
import arenx.test.crypto.curancy.trade.antlr4.BitfinexParser;
import arenx.test.crypto.curancy.trade.antlr4.BitfinexParser.ChannelContext;

public class ChannelBean {

    public enum Type{
        DATA, HEART_BEAT
    }

    public Integer id;
    public Type type;
    public List<List<Double>> data;

    public ChannelBean(Integer id, Type type, List<List<Double>> data){
        this.id = id;
        this.type= type;
        this.data = data;
    }

    public ChannelBean(){

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        ChannelBean other = (ChannelBean) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChannelBean [id=" + id + ", type=" + type + ", data=" + data + "]";
    }

    public static ChannelBean parse(String value) {
        CodePointCharStream antlrInputStream = CharStreams.fromString(value);

        BitfinexLexer lexer = new BitfinexLexer(antlrInputStream);
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        BitfinexParser parser = new BitfinexParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());

        try {
            ChannelContext  context = parser.channel();
            ChannelBean bean = context.bean;
            return bean;
        } catch (Throwable e) {
            throw new IllegalArgumentException(String.format("[%s] is not valid.", value), e);
        }
    }
}
