package arenx.test.crypto.curancy.trade;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.common.collect.Sets;

@Configuration
@ComponentScan
@PropertySource("classpath:trade.properties")
public class Application {

	@Bean(name = "monitoredCurrency")
	public Set<Set<Currency>> get(){
		return Sets.newHashSet(
			Sets.newHashSet(Currency.BITCOIN, Currency.ETHEREUM),
			Sets.newHashSet(Currency.BITCOIN, Currency.ZECASH)
		);
	}
}
