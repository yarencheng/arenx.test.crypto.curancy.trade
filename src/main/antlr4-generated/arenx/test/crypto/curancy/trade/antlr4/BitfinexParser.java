// Generated from Bitfinex.g4 by ANTLR 4.7

package arenx.test.crypto.curancy.trade.antlr4;
import arenx.test.crypto.curancy.trade.bitfinex.ChannelBean;
import java.lang.Double;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BitfinexParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, Sign=5, Decimal=6, DecimalDigit=7, WS=8, 
		HeartBeat=9;
	public static final int
		RULE_channel = 0, RULE_records = 1, RULE_record = 2, RULE_id = 3, RULE_numeric = 4, 
		RULE_float_ = 5, RULE_integer = 6;
	public static final String[] ruleNames = {
		"channel", "records", "record", "id", "numeric", "float_", "integer"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'['", "','", "']'", "'.'", null, null, null, null, "'\"hb\"'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, "Sign", "Decimal", "DecimalDigit", "WS", 
		"HeartBeat"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Bitfinex.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public BitfinexParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ChannelContext extends ParserRuleContext {
		public ChannelBean bean;
		public IdContext id;
		public RecordsContext records;
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode EOF() { return getToken(BitfinexParser.EOF, 0); }
		public RecordsContext records() {
			return getRuleContext(RecordsContext.class,0);
		}
		public TerminalNode HeartBeat() { return getToken(BitfinexParser.HeartBeat, 0); }
		public ChannelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_channel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterChannel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitChannel(this);
		}
	}

	public final ChannelContext channel() throws RecognitionException {
		ChannelContext _localctx = new ChannelContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_channel);

				((ChannelContext)_localctx).bean =  new ChannelBean();
			
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			match(T__0);
			setState(15);
			((ChannelContext)_localctx).id = id();
			 _localctx.bean.id = Integer.parseInt((((ChannelContext)_localctx).id!=null?_input.getText(((ChannelContext)_localctx).id.start,((ChannelContext)_localctx).id.stop):null)); 
			setState(17);
			match(T__1);
			setState(23);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				setState(18);
				((ChannelContext)_localctx).records = records();
				 _localctx.bean.data = ((ChannelContext)_localctx).records.values; _localctx.bean.type = ChannelBean.Type.DATA; 
				}
				break;
			case HeartBeat:
				{
				setState(21);
				match(HeartBeat);
				 _localctx.bean.type = ChannelBean.Type.HEART_BEAT; 
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(25);
			match(T__2);
			setState(26);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RecordsContext extends ParserRuleContext {
		public List<List<Double>> values;
		public RecordContext record;
		public List<RecordContext> record() {
			return getRuleContexts(RecordContext.class);
		}
		public RecordContext record(int i) {
			return getRuleContext(RecordContext.class,i);
		}
		public RecordsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_records; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterRecords(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitRecords(this);
		}
	}

	public final RecordsContext records() throws RecognitionException {
		RecordsContext _localctx = new RecordsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_records);

				((RecordsContext)_localctx).values =  new ArrayList<>();
			
		try {
			int _alt;
			setState(44);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				((RecordsContext)_localctx).record = record();
				 _localctx.values.add(((RecordsContext)_localctx).record.values); 
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				match(T__0);
				setState(36); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(32);
						((RecordsContext)_localctx).record = record();
						 _localctx.values.add(((RecordsContext)_localctx).record.values); 
						setState(34);
						match(T__1);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(38); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(40);
				((RecordsContext)_localctx).record = record();
				 _localctx.values.add(((RecordsContext)_localctx).record.values); 
				setState(42);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RecordContext extends ParserRuleContext {
		public List<Double> values;
		public NumericContext numeric;
		public List<NumericContext> numeric() {
			return getRuleContexts(NumericContext.class);
		}
		public NumericContext numeric(int i) {
			return getRuleContext(NumericContext.class,i);
		}
		public RecordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_record; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterRecord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitRecord(this);
		}
	}

	public final RecordContext record() throws RecognitionException {
		RecordContext _localctx = new RecordContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_record);

				((RecordContext)_localctx).values =  new ArrayList<>();
			
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			match(T__0);
			setState(53);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(47);
					((RecordContext)_localctx).numeric = numeric();
					 _localctx.values.add(Double.parseDouble((((RecordContext)_localctx).numeric!=null?_input.getText(((RecordContext)_localctx).numeric.start,((RecordContext)_localctx).numeric.stop):null))); 
					setState(49);
					match(T__1);
					}
					} 
				}
				setState(55);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(56);
			((RecordContext)_localctx).numeric = numeric();
			 _localctx.values.add(Double.parseDouble((((RecordContext)_localctx).numeric!=null?_input.getText(((RecordContext)_localctx).numeric.start,((RecordContext)_localctx).numeric.stop):null))); 
			setState(58);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitId(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			integer();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericContext extends ParserRuleContext {
		public Float_Context float_() {
			return getRuleContext(Float_Context.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public NumericContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeric; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterNumeric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitNumeric(this);
		}
	}

	public final NumericContext numeric() throws RecognitionException {
		NumericContext _localctx = new NumericContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_numeric);
		try {
			setState(64);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				float_();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				integer();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Float_Context extends ParserRuleContext {
		public List<TerminalNode> Decimal() { return getTokens(BitfinexParser.Decimal); }
		public TerminalNode Decimal(int i) {
			return getToken(BitfinexParser.Decimal, i);
		}
		public TerminalNode Sign() { return getToken(BitfinexParser.Sign, 0); }
		public Float_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterFloat_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitFloat_(this);
		}
	}

	public final Float_Context float_() throws RecognitionException {
		Float_Context _localctx = new Float_Context(_ctx, getState());
		enterRule(_localctx, 10, RULE_float_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Sign) {
				{
				setState(66);
				match(Sign);
				}
			}

			setState(69);
			match(Decimal);
			setState(72);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(70);
				match(T__3);
				setState(71);
				match(Decimal);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerContext extends ParserRuleContext {
		public TerminalNode Decimal() { return getToken(BitfinexParser.Decimal, 0); }
		public TerminalNode Sign() { return getToken(BitfinexParser.Sign, 0); }
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).enterInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BitfinexListener ) ((BitfinexListener)listener).exitInteger(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_integer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Sign) {
				{
				setState(74);
				match(Sign);
				}
			}

			setState(77);
			match(Decimal);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\13R\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\5\2\32\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\6\3\'"+
		"\n\3\r\3\16\3(\3\3\3\3\3\3\3\3\5\3/\n\3\3\4\3\4\3\4\3\4\3\4\7\4\66\n\4"+
		"\f\4\16\49\13\4\3\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6\5\6C\n\6\3\7\5\7F\n\7"+
		"\3\7\3\7\3\7\5\7K\n\7\3\b\5\bN\n\b\3\b\3\b\3\b\2\2\t\2\4\6\b\n\f\16\2"+
		"\2\2R\2\20\3\2\2\2\4.\3\2\2\2\6\60\3\2\2\2\b>\3\2\2\2\nB\3\2\2\2\fE\3"+
		"\2\2\2\16M\3\2\2\2\20\21\7\3\2\2\21\22\5\b\5\2\22\23\b\2\1\2\23\31\7\4"+
		"\2\2\24\25\5\4\3\2\25\26\b\2\1\2\26\32\3\2\2\2\27\30\7\13\2\2\30\32\b"+
		"\2\1\2\31\24\3\2\2\2\31\27\3\2\2\2\32\33\3\2\2\2\33\34\7\5\2\2\34\35\7"+
		"\2\2\3\35\3\3\2\2\2\36\37\5\6\4\2\37 \b\3\1\2 /\3\2\2\2!&\7\3\2\2\"#\5"+
		"\6\4\2#$\b\3\1\2$%\7\4\2\2%\'\3\2\2\2&\"\3\2\2\2\'(\3\2\2\2(&\3\2\2\2"+
		"()\3\2\2\2)*\3\2\2\2*+\5\6\4\2+,\b\3\1\2,-\7\5\2\2-/\3\2\2\2.\36\3\2\2"+
		"\2.!\3\2\2\2/\5\3\2\2\2\60\67\7\3\2\2\61\62\5\n\6\2\62\63\b\4\1\2\63\64"+
		"\7\4\2\2\64\66\3\2\2\2\65\61\3\2\2\2\669\3\2\2\2\67\65\3\2\2\2\678\3\2"+
		"\2\28:\3\2\2\29\67\3\2\2\2:;\5\n\6\2;<\b\4\1\2<=\7\5\2\2=\7\3\2\2\2>?"+
		"\5\16\b\2?\t\3\2\2\2@C\5\f\7\2AC\5\16\b\2B@\3\2\2\2BA\3\2\2\2C\13\3\2"+
		"\2\2DF\7\7\2\2ED\3\2\2\2EF\3\2\2\2FG\3\2\2\2GJ\7\b\2\2HI\7\6\2\2IK\7\b"+
		"\2\2JH\3\2\2\2JK\3\2\2\2K\r\3\2\2\2LN\7\7\2\2ML\3\2\2\2MN\3\2\2\2NO\3"+
		"\2\2\2OP\7\b\2\2P\17\3\2\2\2\n\31(.\67BEJM";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}