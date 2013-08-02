// Generated from FilterTree.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterTreeParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__1=1, T__0=2, LINK_TYPE_OR=3, LINK_TYPE_AND=4, EQUAL=5, NOTEQUAL=6, 
		MORE=7, LESS=8, LIKE=9, NUMBER=10, ALIAS=11, FLOAT=12, STRING=13, SPACE=14;
	public static final String[] tokenNames = {
		"<INVALID>", "')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'='", "'!='", 
		"'>'", "'<'", "LIKE", "NUMBER", "ALIAS", "FLOAT", "STRING", "' '"
	};
	public static final int
		RULE_query = 0, RULE_condition = 1, RULE_link_type = 2, RULE_expr = 3, 
		RULE_operand = 4, RULE_operand_type = 5;
	public static final String[] ruleNames = {
		"query", "condition", "link_type", "expr", "operand", "operand_type"
	};

	@Override
	public String getGrammarFileName() { return "FilterTree.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public FilterTreeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class QueryContext extends ParserRuleContext {
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(12); condition();
				}
				}
				setState(15); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << LINK_TYPE_OR) | (1L << LINK_TYPE_AND) | (1L << NUMBER) | (1L << ALIAS) | (1L << STRING))) != 0) );
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

	public static class ConditionContext extends ParserRuleContext {
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
	 
		public ConditionContext() { }
		public void copyFrom(ConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NobraketsContext extends ConditionContext {
		public Link_typeContext link_type() {
			return getRuleContext(Link_typeContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NobraketsContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterNobrakets(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitNobrakets(this);
		}
	}
	public static class WithbraketsContext extends ConditionContext {
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public Link_typeContext link_type() {
			return getRuleContext(Link_typeContext.class,0);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public WithbraketsContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterWithbrakets(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitWithbrakets(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		ConditionContext _localctx = new ConditionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_condition);
		int _la;
		try {
			setState(32);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new NobraketsContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(18);
				_la = _input.LA(1);
				if (_la==LINK_TYPE_OR || _la==LINK_TYPE_AND) {
					{
					setState(17); link_type();
					}
				}

				setState(20); expr();
				}
				break;

			case 2:
				_localctx = new WithbraketsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(22);
				_la = _input.LA(1);
				if (_la==LINK_TYPE_OR || _la==LINK_TYPE_AND) {
					{
					setState(21); link_type();
					}
				}

				setState(24); match(2);
				setState(26); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(25); condition();
					}
					}
					setState(28); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << LINK_TYPE_OR) | (1L << LINK_TYPE_AND) | (1L << NUMBER) | (1L << ALIAS) | (1L << STRING))) != 0) );
				setState(30); match(1);
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

	public static class Link_typeContext extends ParserRuleContext {
		public TerminalNode LINK_TYPE_OR() { return getToken(FilterTreeParser.LINK_TYPE_OR, 0); }
		public TerminalNode LINK_TYPE_AND() { return getToken(FilterTreeParser.LINK_TYPE_AND, 0); }
		public Link_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_link_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterLink_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener) ((FilterTreeListener)listener).exitLink_type(this);
		}
	}

	public final Link_typeContext link_type() throws RecognitionException {
		Link_typeContext _localctx = new Link_typeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_link_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			_la = _input.LA(1);
			if ( !(_la==LINK_TYPE_OR || _la==LINK_TYPE_AND) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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

	public static class ExprContext extends ParserRuleContext {
		public OperandContext operand(int i) {
			return getRuleContext(OperandContext.class,i);
		}
		public Operand_typeContext operand_type() {
			return getRuleContext(Operand_typeContext.class,0);
		}
		public List<OperandContext> operand() {
			return getRuleContexts(OperandContext.class);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36); operand();
			setState(37); operand_type();
			setState(38); operand();
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

	public static class OperandContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(FilterTreeParser.NUMBER, 0); }
		public TerminalNode ALIAS() { return getToken(FilterTreeParser.ALIAS, 0); }
		public TerminalNode STRING() { return getToken(FilterTreeParser.STRING, 0); }
		public OperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterOperand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitOperand(this);
		}
	}

	public final OperandContext operand() throws RecognitionException {
		OperandContext _localctx = new OperandContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_operand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << ALIAS) | (1L << STRING))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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

	public static class Operand_typeContext extends ParserRuleContext {
		public TerminalNode MORE() { return getToken(FilterTreeParser.MORE, 0); }
		public TerminalNode LESS() { return getToken(FilterTreeParser.LESS, 0); }
		public TerminalNode EQUAL() { return getToken(FilterTreeParser.EQUAL, 0); }
		public TerminalNode LIKE() { return getToken(FilterTreeParser.LIKE, 0); }
		public TerminalNode NOTEQUAL() { return getToken(FilterTreeParser.NOTEQUAL, 0); }
		public Operand_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operand_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterOperand_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitOperand_type(this);
		}
	}

	public final Operand_typeContext operand_type() throws RecognitionException {
		Operand_typeContext _localctx = new Operand_typeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_operand_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL) | (1L << NOTEQUAL) | (1L << MORE) | (1L << LESS) | (1L << LIKE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\20/\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\6\2\20\n\2\r\2\16\2\21\3\3\5\3"+
		"\25\n\3\3\3\3\3\5\3\31\n\3\3\3\3\3\6\3\35\n\3\r\3\16\3\36\3\3\3\3\5\3"+
		"#\n\3\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\2\b\2\4\6\b\n\f\2\5"+
		"\3\2\5\6\4\2\f\r\17\17\3\2\7\13-\2\17\3\2\2\2\4\"\3\2\2\2\6$\3\2\2\2\b"+
		"&\3\2\2\2\n*\3\2\2\2\f,\3\2\2\2\16\20\5\4\3\2\17\16\3\2\2\2\20\21\3\2"+
		"\2\2\21\17\3\2\2\2\21\22\3\2\2\2\22\3\3\2\2\2\23\25\5\6\4\2\24\23\3\2"+
		"\2\2\24\25\3\2\2\2\25\26\3\2\2\2\26#\5\b\5\2\27\31\5\6\4\2\30\27\3\2\2"+
		"\2\30\31\3\2\2\2\31\32\3\2\2\2\32\34\7\4\2\2\33\35\5\4\3\2\34\33\3\2\2"+
		"\2\35\36\3\2\2\2\36\34\3\2\2\2\36\37\3\2\2\2\37 \3\2\2\2 !\7\3\2\2!#\3"+
		"\2\2\2\"\24\3\2\2\2\"\30\3\2\2\2#\5\3\2\2\2$%\t\2\2\2%\7\3\2\2\2&\'\5"+
		"\n\6\2\'(\5\f\7\2()\5\n\6\2)\t\3\2\2\2*+\t\3\2\2+\13\3\2\2\2,-\t\4\2\2"+
		"-\r\3\2\2\2\7\21\24\30\36\"";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}