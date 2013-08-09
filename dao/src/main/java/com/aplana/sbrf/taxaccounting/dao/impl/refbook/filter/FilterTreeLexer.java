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
		MORE=7, LESS=8, LIKE=9, IS_NULL=10, NUMBER=11, ALIAS=12, FLOAT=13, STRING=14, 
		SPACE=15;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'='", "'!='", "'>'", "'<'", 
		"LIKE", "IS_NULL", "NUMBER", "ALIAS", "FLOAT", "STRING", "' '"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "EQUAL", "NOTEQUAL", 
		"MORE", "LESS", "LIKE", "DIGIT", "IS_NULL", "NUMBER", "ALIAS", "FLOAT", 
		"STRING", "HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", "SPACE"
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
		case 19: SPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void SPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip(); break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\21\u0099\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\3\f\6\fJ\n\f\r\f\16\fK\3\f\3\f\3\f\3\f\3\f\3\r\6\rT\n\r"+
		"\r\r\16\rU\3\r\5\rY\n\r\3\16\6\16\\\n\16\r\16\16\16]\3\16\3\16\3\16\7"+
		"\16c\n\16\f\16\16\16f\13\16\3\17\6\17i\n\17\r\17\16\17j\3\17\3\17\6\17"+
		"o\n\17\r\17\16\17p\3\20\3\20\3\20\7\20v\n\20\f\20\16\20y\13\20\3\20\3"+
		"\20\3\21\3\21\3\22\3\22\3\22\3\22\5\22\u0083\n\22\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\5\23\u008e\n\23\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\25\3\25\3\25\2\26\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1"+
		"\21\n\1\23\13\1\25\2\1\27\f\1\31\r\1\33\16\1\35\17\1\37\20\1!\2\1#\2\1"+
		"%\2\1\'\2\1)\21\2\3\2\22\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFff\4\2N"+
		"Nnn\4\2KKkk\4\2MMmm\4\2GGgg\4\2UUuu\4\2WWww\5\2C\\aac|\4\2C\\c|\4\2))"+
		"^^\5\2\62;CHch\n\2$$))^^ddhhppttvv\u00a2\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37"+
		"\3\2\2\2\2)\3\2\2\2\3+\3\2\2\2\5-\3\2\2\2\7/\3\2\2\2\t\62\3\2\2\2\13\66"+
		"\3\2\2\2\r8\3\2\2\2\17;\3\2\2\2\21=\3\2\2\2\23?\3\2\2\2\25D\3\2\2\2\27"+
		"F\3\2\2\2\31X\3\2\2\2\33[\3\2\2\2\35h\3\2\2\2\37r\3\2\2\2!|\3\2\2\2#\u0082"+
		"\3\2\2\2%\u008d\3\2\2\2\'\u008f\3\2\2\2)\u0096\3\2\2\2+,\7+\2\2,\4\3\2"+
		"\2\2-.\7*\2\2.\6\3\2\2\2/\60\t\2\2\2\60\61\t\3\2\2\61\b\3\2\2\2\62\63"+
		"\t\4\2\2\63\64\t\5\2\2\64\65\t\6\2\2\65\n\3\2\2\2\66\67\7?\2\2\67\f\3"+
		"\2\2\289\7#\2\29:\7?\2\2:\16\3\2\2\2;<\7@\2\2<\20\3\2\2\2=>\7>\2\2>\22"+
		"\3\2\2\2?@\t\7\2\2@A\t\b\2\2AB\t\t\2\2BC\t\n\2\2C\24\3\2\2\2DE\4\62;\2"+
		"E\26\3\2\2\2FG\t\b\2\2GI\t\13\2\2HJ\7\"\2\2IH\3\2\2\2JK\3\2\2\2KI\3\2"+
		"\2\2KL\3\2\2\2LM\3\2\2\2MN\t\5\2\2NO\t\f\2\2OP\t\7\2\2PQ\t\7\2\2Q\30\3"+
		"\2\2\2RT\5\25\13\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2VY\3\2\2\2W"+
		"Y\5\35\17\2XS\3\2\2\2XW\3\2\2\2Y\32\3\2\2\2Z\\\t\r\2\2[Z\3\2\2\2\\]\3"+
		"\2\2\2][\3\2\2\2]^\3\2\2\2^d\3\2\2\2_c\t\16\2\2`c\5\25\13\2ac\7a\2\2b"+
		"_\3\2\2\2b`\3\2\2\2ba\3\2\2\2cf\3\2\2\2db\3\2\2\2de\3\2\2\2e\34\3\2\2"+
		"\2fd\3\2\2\2gi\5\25\13\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2kl\3\2"+
		"\2\2ln\7\60\2\2mo\5\25\13\2nm\3\2\2\2op\3\2\2\2pn\3\2\2\2pq\3\2\2\2q\36"+
		"\3\2\2\2rw\7)\2\2sv\5#\22\2tv\n\17\2\2us\3\2\2\2ut\3\2\2\2vy\3\2\2\2w"+
		"u\3\2\2\2wx\3\2\2\2xz\3\2\2\2yw\3\2\2\2z{\7)\2\2{ \3\2\2\2|}\t\20\2\2"+
		"}\"\3\2\2\2~\177\7^\2\2\177\u0083\t\21\2\2\u0080\u0083\5\'\24\2\u0081"+
		"\u0083\5%\23\2\u0082~\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0081\3\2\2\2"+
		"\u0083$\3\2\2\2\u0084\u0085\7^\2\2\u0085\u0086\4\62\65\2\u0086\u0087\4"+
		"\629\2\u0087\u008e\4\629\2\u0088\u0089\7^\2\2\u0089\u008a\4\629\2\u008a"+
		"\u008e\4\629\2\u008b\u008c\7^\2\2\u008c\u008e\4\629\2\u008d\u0084\3\2"+
		"\2\2\u008d\u0088\3\2\2\2\u008d\u008b\3\2\2\2\u008e&\3\2\2\2\u008f\u0090"+
		"\7^\2\2\u0090\u0091\7w\2\2\u0091\u0092\5!\21\2\u0092\u0093\5!\21\2\u0093"+
		"\u0094\5!\21\2\u0094\u0095\5!\21\2\u0095(\3\2\2\2\u0096\u0097\7\"\2\2"+
		"\u0097\u0098\b\25\2\2\u0098*\3\2\2\2\17\2KUX]bdjpuw\u0082\u008d";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}