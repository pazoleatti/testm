package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import org.antlr.v4.runtime.*;
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
		T__2=1, T__1=2, T__0=3, LINK_TYPE_OR=4, LINK_TYPE_AND=5, LOWER=6, LENGTH=7, 
		EQUAL=8, NOTEQUAL=9, MORE=10, LESS=11, LIKE=12, NUMBER=13, ALIAS=14, FLOAT=15, 
		STRING=16, IS_NULL=17, SPACE=18;
	public static final String[] tokenNames = {
		"<INVALID>", "')'", "'.'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'LOWER'", 
		"'LENGTH'", "'='", "'!='", "'>'", "'<'", "LIKE", "NUMBER", "ALIAS", "FLOAT", 
		"STRING", "IS_NULL", "' '"
	};
	public static final int
		RULE_query = 0, RULE_condition = 1, RULE_link_type = 2, RULE_expr = 3, 
		RULE_operand = 4, RULE_simpleoperand = 5, RULE_funcwrap = 6, RULE_functype = 7, 
		RULE_strtype = 8, RULE_operand_type = 9, RULE_number = 10, RULE_alias = 11, 
		RULE_eAlias = 12, RULE_internlAlias = 13, RULE_externalAlias = 14, RULE_string = 15;
	public static final String[] ruleNames = {
		"query", "condition", "link_type", "expr", "operand", "simpleoperand", 
		"funcwrap", "functype", "strtype", "operand_type", "number", "alias", 
		"eAlias", "internlAlias", "externalAlias", "string"
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
			setState(33); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(32); condition();
				}
				}
				setState(35); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 3) | (1L << LINK_TYPE_OR) | (1L << LINK_TYPE_AND) | (1L << LOWER) | (1L << LENGTH) | (1L << NUMBER) | (1L << ALIAS) | (1L << STRING))) != 0) );
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
			setState(52);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new NobraketsContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(38);
				_la = _input.LA(1);
				if (_la==LINK_TYPE_OR || _la==LINK_TYPE_AND) {
					{
					setState(37); link_type();
					}
				}

				setState(40); expr();
				}
				break;

			case 2:
				_localctx = new WithbraketsContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(42);
				_la = _input.LA(1);
				if (_la==LINK_TYPE_OR || _la==LINK_TYPE_AND) {
					{
					setState(41); link_type();
					}
				}

				setState(44); match(3);
				setState(46); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(45); condition();
					}
					}
					setState(48); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 3) | (1L << LINK_TYPE_OR) | (1L << LINK_TYPE_AND) | (1L << LOWER) | (1L << LENGTH) | (1L << NUMBER) | (1L << ALIAS) | (1L << STRING))) != 0) );
				setState(50); match(1);
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
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitLink_type(this);
		}
	}

	public final Link_typeContext link_type() throws RecognitionException {
		Link_typeContext _localctx = new Link_typeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_link_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
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
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StandartExprContext extends ExprContext {
		public OperandContext operand(int i) {
			return getRuleContext(OperandContext.class,i);
		}
		public Operand_typeContext operand_type() {
			return getRuleContext(Operand_typeContext.class,0);
		}
		public List<OperandContext> operand() {
			return getRuleContexts(OperandContext.class);
		}
		public StandartExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterStandartExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitStandartExpr(this);
		}
	}
	public static class IsNullExprContext extends ExprContext {
		public TerminalNode IS_NULL() { return getToken(FilterTreeParser.IS_NULL, 0); }
		public SimpleoperandContext simpleoperand() {
			return getRuleContext(SimpleoperandContext.class,0);
		}
		public IsNullExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterIsNullExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitIsNullExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_expr);
		try {
			setState(63);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				_localctx = new StandartExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(56); operand();
				setState(57); operand_type();
				setState(58); operand();
				}
				break;

			case 2:
				_localctx = new IsNullExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(60); simpleoperand();
				setState(61); match(IS_NULL);
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

	public static class OperandContext extends ParserRuleContext {
		public SimpleoperandContext simpleoperand() {
			return getRuleContext(SimpleoperandContext.class,0);
		}
		public FuncwrapContext funcwrap() {
			return getRuleContext(FuncwrapContext.class,0);
		}
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
		try {
			setState(67);
			switch (_input.LA(1)) {
			case LOWER:
			case LENGTH:
				enterOuterAlt(_localctx, 1);
				{
				setState(65); funcwrap();
				}
				break;
			case NUMBER:
			case ALIAS:
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(66); simpleoperand();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class SimpleoperandContext extends ParserRuleContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public SimpleoperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleoperand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterSimpleoperand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitSimpleoperand(this);
		}
	}

	public final SimpleoperandContext simpleoperand() throws RecognitionException {
		SimpleoperandContext _localctx = new SimpleoperandContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_simpleoperand);
		try {
			setState(72);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(69); number();
				}
				break;
			case ALIAS:
				enterOuterAlt(_localctx, 2);
				{
				setState(70); alias();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(71); string();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class FuncwrapContext extends ParserRuleContext {
		public FunctypeContext functype() {
			return getRuleContext(FunctypeContext.class,0);
		}
		public StrtypeContext strtype() {
			return getRuleContext(StrtypeContext.class,0);
		}
		public FuncwrapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcwrap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterFuncwrap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitFuncwrap(this);
		}
	}

	public final FuncwrapContext funcwrap() throws RecognitionException {
		FuncwrapContext _localctx = new FuncwrapContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_funcwrap);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74); functype();
			setState(75); match(3);
			setState(76); strtype();
			setState(77); match(1);
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

	public static class FunctypeContext extends ParserRuleContext {
		public TerminalNode LOWER() { return getToken(FilterTreeParser.LOWER, 0); }
		public TerminalNode LENGTH() { return getToken(FilterTreeParser.LENGTH, 0); }
		public FunctypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functype; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterFunctype(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitFunctype(this);
		}
	}

	public final FunctypeContext functype() throws RecognitionException {
		FunctypeContext _localctx = new FunctypeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_functype);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			_la = _input.LA(1);
			if ( !(_la==LOWER || _la==LENGTH) ) {
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

	public static class StrtypeContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public StrtypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strtype; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterStrtype(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitStrtype(this);
		}
	}

	public final StrtypeContext strtype() throws RecognitionException {
		StrtypeContext _localctx = new StrtypeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_strtype);
		try {
			setState(83);
			switch (_input.LA(1)) {
			case ALIAS:
				enterOuterAlt(_localctx, 1);
				{
				setState(81); alias();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(82); string();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
		enterRule(_localctx, 18, RULE_operand_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
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

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(FilterTreeParser.NUMBER, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitNumber(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87); match(NUMBER);
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

	public static class AliasContext extends ParserRuleContext {
		public EAliasContext eAlias() {
			return getRuleContext(EAliasContext.class,0);
		}
		public InternlAliasContext internlAlias() {
			return getRuleContext(InternlAliasContext.class,0);
		}
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitAlias(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_alias);
		try {
			setState(91);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(89); internlAlias();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(90); eAlias();
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

	public static class EAliasContext extends ParserRuleContext {
		public List<ExternalAliasContext> externalAlias() {
			return getRuleContexts(ExternalAliasContext.class);
		}
		public TerminalNode ALIAS() { return getToken(FilterTreeParser.ALIAS, 0); }
		public ExternalAliasContext externalAlias(int i) {
			return getRuleContext(ExternalAliasContext.class,i);
		}
		public EAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterEAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitEAlias(this);
		}
	}

	public final EAliasContext eAlias() throws RecognitionException {
		EAliasContext _localctx = new EAliasContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_eAlias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93); match(ALIAS);
			setState(96); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(94); match(2);
				setState(95); externalAlias();
				}
				}
				setState(98); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==2 );
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

	public static class InternlAliasContext extends ParserRuleContext {
		public TerminalNode ALIAS() { return getToken(FilterTreeParser.ALIAS, 0); }
		public InternlAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_internlAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterInternlAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitInternlAlias(this);
		}
	}

	public final InternlAliasContext internlAlias() throws RecognitionException {
		InternlAliasContext _localctx = new InternlAliasContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_internlAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100); match(ALIAS);
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

	public static class ExternalAliasContext extends ParserRuleContext {
		public TerminalNode ALIAS() { return getToken(FilterTreeParser.ALIAS, 0); }
		public ExternalAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externalAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterExternalAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitExternalAlias(this);
		}
	}

	public final ExternalAliasContext externalAlias() throws RecognitionException {
		ExternalAliasContext _localctx = new ExternalAliasContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_externalAlias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102); match(ALIAS);
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

	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(FilterTreeParser.STRING, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FilterTreeListener ) ((FilterTreeListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104); match(STRING);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\24m\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\6\2$\n\2\r"+
		"\2\16\2%\3\3\5\3)\n\3\3\3\3\3\5\3-\n\3\3\3\3\3\6\3\61\n\3\r\3\16\3\62"+
		"\3\3\3\3\5\3\67\n\3\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5B\n\5\3\6\3"+
		"\6\5\6F\n\6\3\7\3\7\3\7\5\7K\n\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\5"+
		"\nV\n\n\3\13\3\13\3\f\3\f\3\r\3\r\5\r^\n\r\3\16\3\16\3\16\6\16c\n\16\r"+
		"\16\16\16d\3\17\3\17\3\20\3\20\3\21\3\21\3\21\2\22\2\4\6\b\n\f\16\20\22"+
		"\24\26\30\32\34\36 \2\5\3\2\6\7\3\2\b\t\3\2\n\16h\2#\3\2\2\2\4\66\3\2"+
		"\2\2\68\3\2\2\2\bA\3\2\2\2\nE\3\2\2\2\fJ\3\2\2\2\16L\3\2\2\2\20Q\3\2\2"+
		"\2\22U\3\2\2\2\24W\3\2\2\2\26Y\3\2\2\2\30]\3\2\2\2\32_\3\2\2\2\34f\3\2"+
		"\2\2\36h\3\2\2\2 j\3\2\2\2\"$\5\4\3\2#\"\3\2\2\2$%\3\2\2\2%#\3\2\2\2%"+
		"&\3\2\2\2&\3\3\2\2\2\')\5\6\4\2(\'\3\2\2\2()\3\2\2\2)*\3\2\2\2*\67\5\b"+
		"\5\2+-\5\6\4\2,+\3\2\2\2,-\3\2\2\2-.\3\2\2\2.\60\7\5\2\2/\61\5\4\3\2\60"+
		"/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63\64\3\2\2\2\64\65"+
		"\7\3\2\2\65\67\3\2\2\2\66(\3\2\2\2\66,\3\2\2\2\67\5\3\2\2\289\t\2\2\2"+
		"9\7\3\2\2\2:;\5\n\6\2;<\5\24\13\2<=\5\n\6\2=B\3\2\2\2>?\5\f\7\2?@\7\23"+
		"\2\2@B\3\2\2\2A:\3\2\2\2A>\3\2\2\2B\t\3\2\2\2CF\5\16\b\2DF\5\f\7\2EC\3"+
		"\2\2\2ED\3\2\2\2F\13\3\2\2\2GK\5\26\f\2HK\5\30\r\2IK\5 \21\2JG\3\2\2\2"+
		"JH\3\2\2\2JI\3\2\2\2K\r\3\2\2\2LM\5\20\t\2MN\7\5\2\2NO\5\22\n\2OP\7\3"+
		"\2\2P\17\3\2\2\2QR\t\3\2\2R\21\3\2\2\2SV\5\30\r\2TV\5 \21\2US\3\2\2\2"+
		"UT\3\2\2\2V\23\3\2\2\2WX\t\4\2\2X\25\3\2\2\2YZ\7\17\2\2Z\27\3\2\2\2[^"+
		"\5\34\17\2\\^\5\32\16\2][\3\2\2\2]\\\3\2\2\2^\31\3\2\2\2_b\7\20\2\2`a"+
		"\7\4\2\2ac\5\36\20\2b`\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2e\33\3\2\2"+
		"\2fg\7\20\2\2g\35\3\2\2\2hi\7\20\2\2i\37\3\2\2\2jk\7\22\2\2k!\3\2\2\2"+
		"\r%(,\62\66AEJU]d";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}