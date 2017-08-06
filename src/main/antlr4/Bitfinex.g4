grammar Bitfinex;

@header {
package arenx.test.crypto.curancy.trade.antlr4;
import arenx.test.crypto.curancy.trade.bitfinex.ChannelBean;
import java.lang.Double;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
}

channel returns [ChannelBean bean]
	@init {
		$bean = new ChannelBean();
	}
	:	'['
		id					{ $bean.id = Integer.parseInt($id.text); }
		','
		(	records			{ $bean.data = $records.values; $bean.type = ChannelBean.Type.DATA; }
		|	HeartBeat		{ $bean.type = ChannelBean.Type.HEART_BEAT; }
		)
		']'
		EOF
	;

records returns [List<List<Double>> values]
	@init {
		$values = new ArrayList<>();
	}
	:	record				{ $values.add($record.values); }
	|	'['
		(	record			{ $values.add($record.values); }
			','
		)+
		record				{ $values.add($record.values); }
		']'
	;

record returns [List<Double> values]
	@init {
		$values = new ArrayList<>();
	}
	:	'['		
		(	numeric			{ $values.add(Double.parseDouble($numeric.text)); }
			','
		)*
		numeric				{ $values.add(Double.parseDouble($numeric.text)); }
		']'
	;
	
id
	:	integer
	;

numeric			: float_ | integer ;
float_			: Sign? Decimal ('.' Decimal)? ;
integer         : Sign? Decimal ;

Sign			: '-' | '+' ;
Decimal         : (DecimalDigit)* ;
DecimalDigit	: '0'..'9' ;
WS  			: [ \t\r\n]+ -> skip ;
HeartBeat		: '"hb"' ;
