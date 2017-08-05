// Generated from Bitfinex.g4 by ANTLR 4.7

package arenx.test.crypto.curancy.trade.antlr4;
import arenx.test.crypto.curancy.trade.bitfinex.ChannelBean;
import java.lang.Double;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BitfinexParser}.
 */
public interface BitfinexListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#channel}.
	 * @param ctx the parse tree
	 */
	void enterChannel(BitfinexParser.ChannelContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#channel}.
	 * @param ctx the parse tree
	 */
	void exitChannel(BitfinexParser.ChannelContext ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#records}.
	 * @param ctx the parse tree
	 */
	void enterRecords(BitfinexParser.RecordsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#records}.
	 * @param ctx the parse tree
	 */
	void exitRecords(BitfinexParser.RecordsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#record}.
	 * @param ctx the parse tree
	 */
	void enterRecord(BitfinexParser.RecordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#record}.
	 * @param ctx the parse tree
	 */
	void exitRecord(BitfinexParser.RecordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(BitfinexParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(BitfinexParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(BitfinexParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(BitfinexParser.NumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#float_}.
	 * @param ctx the parse tree
	 */
	void enterFloat_(BitfinexParser.Float_Context ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#float_}.
	 * @param ctx the parse tree
	 */
	void exitFloat_(BitfinexParser.Float_Context ctx);
	/**
	 * Enter a parse tree produced by {@link BitfinexParser#integer}.
	 * @param ctx the parse tree
	 */
	void enterInteger(BitfinexParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BitfinexParser#integer}.
	 * @param ctx the parse tree
	 */
	void exitInteger(BitfinexParser.IntegerContext ctx);
}