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
		TO_CHAR=8, EQUAL=9, NOTEQUAL=10, MORE=11, LESS=12, LIKE=13, NUMBER=14, 
		ALIAS=15, FLOAT=16, STRING=17, IS_NULL=18, SPACE=19;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'.'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'LOWER'", "'LENGTH'", 
		"'TO_CHAR'", "'='", "'!='", "'>'", "'<'", "LIKE", "NUMBER", "ALIAS", "FLOAT", 
		"STRING", "IS_NULL", "' '"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "LOWER", "LENGTH", 
		"TO_CHAR", "EQUAL", "NOTEQUAL", "MORE", "LESS", "LIKE", "DIGIT", "NUMBER", 
		"ALIAS", "FLOAT", "STRING", "IS_NULL", "HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", 
		"UNICODE_ESC", "SPACE"
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
		case 23: SPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void SPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip(); break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\25\u00b9\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17"+
		"\3\17\3\20\6\20g\n\20\r\20\16\20h\3\20\5\20l\n\20\3\21\6\21o\n\21\r\21"+
		"\16\21p\3\21\3\21\3\21\7\21v\n\21\f\21\16\21y\13\21\3\22\6\22|\n\22\r"+
		"\22\16\22}\3\22\3\22\6\22\u0082\n\22\r\22\16\22\u0083\3\23\3\23\3\23\3"+
		"\23\7\23\u008a\n\23\f\23\16\23\u008d\13\23\3\23\3\23\3\24\3\24\3\24\6"+
		"\24\u0094\n\24\r\24\16\24\u0095\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\26"+
		"\3\26\3\26\3\26\5\26\u00a3\n\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\5\27\u00ae\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31"+
		"\2\32\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f"+
		"\1\27\r\1\31\16\1\33\17\1\35\2\1\37\20\1!\21\1#\22\1%\23\1\'\24\1)\2\1"+
		"+\2\1-\2\1/\2\1\61\25\2\3\2\22\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFf"+
		"f\4\2NNnn\4\2KKkk\4\2MMmm\4\2GGgg\5\2C\\aac|\4\2C\\c|\3\2))\4\2UUuu\4"+
		"\2WWww\5\2\62;CHch\n\2$$))^^ddhhppttvv\u00c2\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2\61\3\2\2"+
		"\2\3\63\3\2\2\2\5\65\3\2\2\2\7\67\3\2\2\2\t9\3\2\2\2\13<\3\2\2\2\r@\3"+
		"\2\2\2\17F\3\2\2\2\21M\3\2\2\2\23U\3\2\2\2\25W\3\2\2\2\27Z\3\2\2\2\31"+
		"\\\3\2\2\2\33^\3\2\2\2\35c\3\2\2\2\37k\3\2\2\2!n\3\2\2\2#{\3\2\2\2%\u0085"+
		"\3\2\2\2\'\u0090\3\2\2\2)\u009c\3\2\2\2+\u00a2\3\2\2\2-\u00ad\3\2\2\2"+
		"/\u00af\3\2\2\2\61\u00b6\3\2\2\2\63\64\7+\2\2\64\4\3\2\2\2\65\66\7\60"+
		"\2\2\66\6\3\2\2\2\678\7*\2\28\b\3\2\2\29:\t\2\2\2:;\t\3\2\2;\n\3\2\2\2"+
		"<=\t\4\2\2=>\t\5\2\2>?\t\6\2\2?\f\3\2\2\2@A\7N\2\2AB\7Q\2\2BC\7Y\2\2C"+
		"D\7G\2\2DE\7T\2\2E\16\3\2\2\2FG\7N\2\2GH\7G\2\2HI\7P\2\2IJ\7I\2\2JK\7"+
		"V\2\2KL\7J\2\2L\20\3\2\2\2MN\7V\2\2NO\7Q\2\2OP\7a\2\2PQ\7E\2\2QR\7J\2"+
		"\2RS\7C\2\2ST\7T\2\2T\22\3\2\2\2UV\7?\2\2V\24\3\2\2\2WX\7#\2\2XY\7?\2"+
		"\2Y\26\3\2\2\2Z[\7@\2\2[\30\3\2\2\2\\]\7>\2\2]\32\3\2\2\2^_\t\7\2\2_`"+
		"\t\b\2\2`a\t\t\2\2ab\t\n\2\2b\34\3\2\2\2cd\4\62;\2d\36\3\2\2\2eg\5\35"+
		"\17\2fe\3\2\2\2gh\3\2\2\2hf\3\2\2\2hi\3\2\2\2il\3\2\2\2jl\5#\22\2kf\3"+
		"\2\2\2kj\3\2\2\2l \3\2\2\2mo\t\13\2\2nm\3\2\2\2op\3\2\2\2pn\3\2\2\2pq"+
		"\3\2\2\2qw\3\2\2\2rv\t\f\2\2sv\5\35\17\2tv\7a\2\2ur\3\2\2\2us\3\2\2\2"+
		"ut\3\2\2\2vy\3\2\2\2wu\3\2\2\2wx\3\2\2\2x\"\3\2\2\2yw\3\2\2\2z|\5\35\17"+
		"\2{z\3\2\2\2|}\3\2\2\2}{\3\2\2\2}~\3\2\2\2~\177\3\2\2\2\177\u0081\7\60"+
		"\2\2\u0080\u0082\5\35\17\2\u0081\u0080\3\2\2\2\u0082\u0083\3\2\2\2\u0083"+
		"\u0081\3\2\2\2\u0083\u0084\3\2\2\2\u0084$\3\2\2\2\u0085\u008b\7)\2\2\u0086"+
		"\u008a\n\r\2\2\u0087\u0088\7^\2\2\u0088\u008a\7)\2\2\u0089\u0086\3\2\2"+
		"\2\u0089\u0087\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008c"+
		"\3\2\2\2\u008c\u008e\3\2\2\2\u008d\u008b\3\2\2\2\u008e\u008f\7)\2\2\u008f"+
		"&\3\2\2\2\u0090\u0091\t\b\2\2\u0091\u0093\t\16\2\2\u0092\u0094\7\"\2\2"+
		"\u0093\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096"+
		"\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\t\5\2\2\u0098\u0099\t\17\2\2"+
		"\u0099\u009a\t\7\2\2\u009a\u009b\t\7\2\2\u009b(\3\2\2\2\u009c\u009d\t"+
		"\20\2\2\u009d*\3\2\2\2\u009e\u009f\7^\2\2\u009f\u00a3\t\21\2\2\u00a0\u00a3"+
		"\5/\30\2\u00a1\u00a3\5-\27\2\u00a2\u009e\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2"+
		"\u00a1\3\2\2\2\u00a3,\3\2\2\2\u00a4\u00a5\7^\2\2\u00a5\u00a6\4\62\65\2"+
		"\u00a6\u00a7\4\629\2\u00a7\u00ae\4\629\2\u00a8\u00a9\7^\2\2\u00a9\u00aa"+
		"\4\629\2\u00aa\u00ae\4\629\2\u00ab\u00ac\7^\2\2\u00ac\u00ae\4\629\2\u00ad"+
		"\u00a4\3\2\2\2\u00ad\u00a8\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ae.\3\2\2\2"+
		"\u00af\u00b0\7^\2\2\u00b0\u00b1\7w\2\2\u00b1\u00b2\5)\25\2\u00b2\u00b3"+
		"\5)\25\2\u00b3\u00b4\5)\25\2\u00b4\u00b5\5)\25\2\u00b5\60\3\2\2\2\u00b6"+
		"\u00b7\7\"\2\2\u00b7\u00b8\b\31\2\2\u00b8\62\3\2\2\2\17\2hkpuw}\u0083"+
		"\u0089\u008b\u0095\u00a2\u00ad";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}