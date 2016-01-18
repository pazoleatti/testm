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
		T__2=1, T__1=2, T__0=3, LINK_TYPE_OR=4, LINK_TYPE_AND=5, LOWER=6, LENGTH=7,
		TO_CHAR=8, TRUNC=9, TO_DATE=10, EQUAL=11, NOTEQUAL=12, MORE=13, LESS=14,
		LIKE=15, NUMBER=16, ALIAS=17, FLOAT=18, STRING=19, IS_NULL=20, SPACE=21;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"'('", "')'", "'.'", "LINK_TYPE_OR", "LINK_TYPE_AND", "'LOWER'", "'LENGTH'",
		"'TO_CHAR'", "'TRUNC'", "'TO_DATE'", "'='", "'!='", "'>'", "'<'", "LIKE",
		"NUMBER", "ALIAS", "FLOAT", "STRING", "IS_NULL", "' '"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "LOWER", "LENGTH",
		"TO_CHAR", "TRUNC", "TO_DATE", "EQUAL", "NOTEQUAL", "MORE", "LESS", "LIKE",
		"DIGIT", "NUMBER", "ALIAS", "FLOAT", "STRING", "IS_NULL", "HEX_DIGIT",
		"ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", "SPACE"
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
		case 25: SPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void SPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip(); break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\27\u00cb\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6"+
		"\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20"+
		"\3\20\3\20\3\21\3\21\3\22\6\22y\n\22\r\22\16\22z\3\22\5\22~\n\22\3\23"+
		"\6\23\u0081\n\23\r\23\16\23\u0082\3\23\3\23\3\23\7\23\u0088\n\23\f\23"+
		"\16\23\u008b\13\23\3\24\6\24\u008e\n\24\r\24\16\24\u008f\3\24\3\24\6\24"+
		"\u0094\n\24\r\24\16\24\u0095\3\25\3\25\3\25\3\25\7\25\u009c\n\25\f\25"+
		"\16\25\u009f\13\25\3\25\3\25\3\26\3\26\3\26\6\26\u00a6\n\26\r\26\16\26"+
		"\u00a7\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\30\5\30\u00b5"+
		"\n\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00c0\n\31\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\2\34\3\3\1\5\4\1\7\5\1\t"+
		"\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1"+
		"\35\20\1\37\21\1!\2\1#\22\1%\23\1\'\24\1)\25\1+\26\1-\2\1/\2\1\61\2\1"+
		"\63\2\1\65\27\2\3\2\22\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFff\4\2NNn"+
		"n\4\2KKkk\4\2MMmm\4\2GGgg\5\2C\\aac|\4\2C\\c|\3\2))\4\2UUuu\4\2WWww\5"+
		"\2\62;CHch\n\2$$))^^ddhhppttvv\u00d4\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23"+
		"\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2"+
		"\2\2\2\65\3\2\2\2\3\67\3\2\2\2\59\3\2\2\2\7;\3\2\2\2\t=\3\2\2\2\13@\3"+
		"\2\2\2\rD\3\2\2\2\17J\3\2\2\2\21Q\3\2\2\2\23Y\3\2\2\2\25_\3\2\2\2\27g"+
		"\3\2\2\2\31i\3\2\2\2\33l\3\2\2\2\35n\3\2\2\2\37p\3\2\2\2!u\3\2\2\2#}\3"+
		"\2\2\2%\u0080\3\2\2\2\'\u008d\3\2\2\2)\u0097\3\2\2\2+\u00a2\3\2\2\2-\u00ae"+
		"\3\2\2\2/\u00b4\3\2\2\2\61\u00bf\3\2\2\2\63\u00c1\3\2\2\2\65\u00c8\3\2"+
		"\2\2\678\7*\2\28\4\3\2\2\29:\7+\2\2:\6\3\2\2\2;<\7\60\2\2<\b\3\2\2\2="+
		">\t\2\2\2>?\t\3\2\2?\n\3\2\2\2@A\t\4\2\2AB\t\5\2\2BC\t\6\2\2C\f\3\2\2"+
		"\2DE\7N\2\2EF\7Q\2\2FG\7Y\2\2GH\7G\2\2HI\7T\2\2I\16\3\2\2\2JK\7N\2\2K"+
		"L\7G\2\2LM\7P\2\2MN\7I\2\2NO\7V\2\2OP\7J\2\2P\20\3\2\2\2QR\7V\2\2RS\7"+
		"Q\2\2ST\7a\2\2TU\7E\2\2UV\7J\2\2VW\7C\2\2WX\7T\2\2X\22\3\2\2\2YZ\7V\2"+
		"\2Z[\7T\2\2[\\\7W\2\2\\]\7P\2\2]^\7E\2\2^\24\3\2\2\2_`\7V\2\2`a\7Q\2\2"+
		"ab\7a\2\2bc\7F\2\2cd\7C\2\2de\7V\2\2ef\7G\2\2f\26\3\2\2\2gh\7?\2\2h\30"+
		"\3\2\2\2ij\7#\2\2jk\7?\2\2k\32\3\2\2\2lm\7@\2\2m\34\3\2\2\2no\7>\2\2o"+
		"\36\3\2\2\2pq\t\7\2\2qr\t\b\2\2rs\t\t\2\2st\t\n\2\2t \3\2\2\2uv\4\62;"+
		"\2v\"\3\2\2\2wy\5!\21\2xw\3\2\2\2yz\3\2\2\2zx\3\2\2\2z{\3\2\2\2{~\3\2"+
		"\2\2|~\5\'\24\2}x\3\2\2\2}|\3\2\2\2~$\3\2\2\2\177\u0081\t\13\2\2\u0080"+
		"\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0083\3\2\2"+
		"\2\u0083\u0089\3\2\2\2\u0084\u0088\t\f\2\2\u0085\u0088\5!\21\2\u0086\u0088"+
		"\7a\2\2\u0087\u0084\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0086\3\2\2\2\u0088"+
		"\u008b\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a&\3\2\2\2"+
		"\u008b\u0089\3\2\2\2\u008c\u008e\5!\21\2\u008d\u008c\3\2\2\2\u008e\u008f"+
		"\3\2\2\2\u008f\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0091\3\2\2\2\u0091"+
		"\u0093\7\60\2\2\u0092\u0094\5!\21\2\u0093\u0092\3\2\2\2\u0094\u0095\3"+
		"\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096(\3\2\2\2\u0097\u009d"+
		"\7)\2\2\u0098\u009c\n\r\2\2\u0099\u009a\7^\2\2\u009a\u009c\7)\2\2\u009b"+
		"\u0098\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u009f\3\2\2\2\u009d\u009b\3\2"+
		"\2\2\u009d\u009e\3\2\2\2\u009e\u00a0\3\2\2\2\u009f\u009d\3\2\2\2\u00a0"+
		"\u00a1\7)\2\2\u00a1*\3\2\2\2\u00a2\u00a3\t\b\2\2\u00a3\u00a5\t\16\2\2"+
		"\u00a4\u00a6\7\"\2\2\u00a5\u00a4\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00a5"+
		"\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00aa\t\5\2\2\u00aa"+
		"\u00ab\t\17\2\2\u00ab\u00ac\t\7\2\2\u00ac\u00ad\t\7\2\2\u00ad,\3\2\2\2"+
		"\u00ae\u00af\t\20\2\2\u00af.\3\2\2\2\u00b0\u00b1\7^\2\2\u00b1\u00b5\t"+
		"\21\2\2\u00b2\u00b5\5\63\32\2\u00b3\u00b5\5\61\31\2\u00b4\u00b0\3\2\2"+
		"\2\u00b4\u00b2\3\2\2\2\u00b4\u00b3\3\2\2\2\u00b5\60\3\2\2\2\u00b6\u00b7"+
		"\7^\2\2\u00b7\u00b8\4\62\65\2\u00b8\u00b9\4\629\2\u00b9\u00c0\4\629\2"+
		"\u00ba\u00bb\7^\2\2\u00bb\u00bc\4\629\2\u00bc\u00c0\4\629\2\u00bd\u00be"+
		"\7^\2\2\u00be\u00c0\4\629\2\u00bf\u00b6\3\2\2\2\u00bf\u00ba\3\2\2\2\u00bf"+
		"\u00bd\3\2\2\2\u00c0\62\3\2\2\2\u00c1\u00c2\7^\2\2\u00c2\u00c3\7w\2\2"+
		"\u00c3\u00c4\5-\27\2\u00c4\u00c5\5-\27\2\u00c5\u00c6\5-\27\2\u00c6\u00c7"+
		"\5-\27\2\u00c7\64\3\2\2\2\u00c8\u00c9\7\"\2\2\u00c9\u00ca\b\33\2\2\u00ca"+
		"\66\3\2\2\2\17\2z}\u0082\u0087\u0089\u008f\u0095\u009b\u009d\u00a7\u00b4"+
		"\u00bf";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}