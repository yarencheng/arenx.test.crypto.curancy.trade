package arenx.test.crypto.curancy.trade.bitfinex;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ChannelBeanParseTest {

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
            new Object[]{
                    "book update",
                    true,
                    "[123,[123]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(123.0)
                            )
                    )
            },
            new Object[]{
                    "book update",
                    true,
                    "[123,[-123]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(-123.0)
                            )
                    )
            },
            new Object[]{
                    "book update",
                    true,
                    "[123,[+123]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(123.0)
                            )
                    )
            },
            new Object[]{
                    "book update",
                    true,
                    "[123,[0]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(0.0)
                            )
                    )
            },
            new Object[]{
                    "book update",
                    true,
                    "[123,[123.123]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(123.123)
                            )
                    )
            },
            new Object[]{
                    "book update",
                    true,
                    "[123,[11,22]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(11.0,22.0)
                            )
                    )
            },
            new Object[]{
                    "book snapshot",
                    true,
                    "[123,[[11]]]",
                    new ChannelBean(
                            123,
                            Arrays.asList(
                                    Arrays.asList(11.0)
                            )
                    )
            },
            new Object[]{"not valid", false, "ss", null},
            new Object[]{"not valid", false, "[123,[]]", null},
            new Object[]{"not valid", false, "[123,[[]]]", null}
        );
    }

    boolean name;
    boolean isValid;
    String v;
    ChannelBean expected;

    public ChannelBeanParseTest(String name, boolean isValid, String v, ChannelBean expected){
        this.isValid = isValid;
        this.v=v;
        this.expected = expected;
    }

    @Test
    public void parse(){
        if (isValid) {
            ChannelBean actual = ChannelBean.parse(v);
            Assert.assertEquals(expected, actual);
        } else {
            try {
                ChannelBean actual = ChannelBean.parse(v);
                Assert.fail("[" + v + "] is not valid. [" + actual + "]");
            } catch (IllegalArgumentException e) {
                // pass
            }
        }

    }
}
