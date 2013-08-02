// Generated from FilterTree.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FilterTreeLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__1=1, T__0=2, LINK_TYPE_OR=3, LINK_TYPE_AND=4, EQUAL=5, NOTEQUAL=6, 
		MORE=7, LESS=8, LIKE=9, NUMBER=10, ALIAS=11, FLOAT=12, STRING=13, SPACE=14;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'='", "'!='", "'>'", "'<'", 
		"LIKE", "NUMBER", "ALIAS", "FLOAT", "STRING", "' '"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "EQUAL", "NOTEQUAL", 
		"MORE", "LESS", "LIKE", "DIGIT", "NUMBER", "ALIAS", "FLOAT", "STRING", 
		"HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", "SPACE"
	};


	public FilterTreeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "FilterTree.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 18: SPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void SPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip(); break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\20\u008b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f"+
		"\6\fF\n\f\r\f\16\fG\3\f\5\fK\n\f\3\r\6\rN\n\r\r\r\16\rO\3\r\3\r\3\r\7"+
		"\rU\n\r\f\r\16\rX\13\r\3\16\6\16[\n\16\r\16\16\16\\\3\16\3\16\6\16a\n"+
		"\16\r\16\16\16b\3\17\3\17\3\17\7\17h\n\17\f\17\16\17k\13\17\3\17\3\17"+
		"\3\20\3\20\3\21\3\21\3\21\3\21\5\21u\n\21\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\5\22\u0080\n\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24"+
		"\3\24\3\24\2\25\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23"+
		"\13\1\25\2\1\27\f\1\31\r\1\33\16\1\35\17\1\37\2\1!\2\1#\2\1%\2\1\'\20"+
		"\2\3\2\20\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFff\4\2NNnn\4\2KKkk\4\2"+
		"MMmm\4\2GGgg\5\2C\\aac|\4\2C\\c|\4\2))^^\5\2\62;CHch\n\2$$))^^ddhhppt"+
		"tvv\u0093\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\'\3\2\2\2\3)\3\2\2\2\5+\3\2\2\2\7"+
		"-\3\2\2\2\t\60\3\2\2\2\13\64\3\2\2\2\r\66\3\2\2\2\179\3\2\2\2\21;\3\2"+
		"\2\2\23=\3\2\2\2\25B\3\2\2\2\27J\3\2\2\2\31M\3\2\2\2\33Z\3\2\2\2\35d\3"+
		"\2\2\2\37n\3\2\2\2!t\3\2\2\2#\177\3\2\2\2%\u0081\3\2\2\2\'\u0088\3\2\2"+
		"\2)*\7+\2\2*\4\3\2\2\2+,\7*\2\2,\6\3\2\2\2-.\t\2\2\2./\t\3\2\2/\b\3\2"+
		"\2\2\60\61\t\4\2\2\61\62\t\5\2\2\62\63\t\6\2\2\63\n\3\2\2\2\64\65\7?\2"+
		"\2\65\f\3\2\2\2\66\67\7#\2\2\678\7?\2\28\16\3\2\2\29:\7@\2\2:\20\3\2\2"+
		"\2;<\7>\2\2<\22\3\2\2\2=>\t\7\2\2>?\t\b\2\2?@\t\t\2\2@A\t\n\2\2A\24\3"+
		"\2\2\2BC\4\62;\2C\26\3\2\2\2DF\5\25\13\2ED\3\2\2\2FG\3\2\2\2GE\3\2\2\2"+
		"GH\3\2\2\2HK\3\2\2\2IK\5\33\16\2JE\3\2\2\2JI\3\2\2\2K\30\3\2\2\2LN\t\13"+
		"\2\2ML\3\2\2\2NO\3\2\2\2OM\3\2\2\2OP\3\2\2\2PV\3\2\2\2QU\t\f\2\2RU\5\25"+
		"\13\2SU\7a\2\2TQ\3\2\2\2TR\3\2\2\2TS\3\2\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2"+
		"\2\2W\32\3\2\2\2XV\3\2\2\2Y[\5\25\13\2ZY\3\2\2\2[\\\3\2\2\2\\Z\3\2\2\2"+
		"\\]\3\2\2\2]^\3\2\2\2^`\7\60\2\2_a\5\25\13\2`_\3\2\2\2ab\3\2\2\2b`\3\2"+
		"\2\2bc\3\2\2\2c\34\3\2\2\2di\7)\2\2eh\5!\21\2fh\n\r\2\2ge\3\2\2\2gf\3"+
		"\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2jl\3\2\2\2ki\3\2\2\2lm\7)\2\2m\36"+
		"\3\2\2\2no\t\16\2\2o \3\2\2\2pq\7^\2\2qu\t\17\2\2ru\5%\23\2su\5#\22\2"+
		"tp\3\2\2\2tr\3\2\2\2ts\3\2\2\2u\"\3\2\2\2vw\7^\2\2wx\4\62\65\2xy\4\62"+
		"9\2y\u0080\4\629\2z{\7^\2\2{|\4\629\2|\u0080\4\629\2}~\7^\2\2~\u0080\4"+
		"\629\2\177v\3\2\2\2\177z\3\2\2\2\177}\3\2\2\2\u0080$\3\2\2\2\u0081\u0082"+
		"\7^\2\2\u0082\u0083\7w\2\2\u0083\u0084\5\37\20\2\u0084\u0085\5\37\20\2"+
		"\u0085\u0086\5\37\20\2\u0086\u0087\5\37\20\2\u0087&\3\2\2\2\u0088\u0089"+
		"\7\"\2\2\u0089\u008a\b\24\2\2\u008a(\3\2\2\2\16\2GJOTV\\bgit\177";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}