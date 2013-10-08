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
		T__1=1, T__0=2, LINK_TYPE_OR=3, LINK_TYPE_AND=4, LOWER=5, LENGTH=6, EQUAL=7, 
		NOTEQUAL=8, MORE=9, LESS=10, LIKE=11, NUMBER=12, ALIAS=13, FLOAT=14, STRING=15, 
		IS_NULL=16, SPACE=17;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'LOWER'", "'LENGTH'", 
		"'='", "'!='", "'>'", "'<'", "LIKE", "NUMBER", "ALIAS", "FLOAT", "STRING", 
		"IS_NULL", "' '"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "LOWER", "LENGTH", "EQUAL", 
		"NOTEQUAL", "MORE", "LESS", "LIKE", "DIGIT", "NUMBER", "ALIAS", "FLOAT", 
		"STRING", "IS_NULL", "HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", 
		"SPACE"
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
		case 21: SPACE_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void SPACE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip(); break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\23\u00ab\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\16\6\16Y\n\16\r\16\16\16Z\3\16\5\16^\n\16\3\17\6\17a\n\17\r"+
		"\17\16\17b\3\17\3\17\3\17\7\17h\n\17\f\17\16\17k\13\17\3\20\6\20n\n\20"+
		"\r\20\16\20o\3\20\3\20\6\20t\n\20\r\20\16\20u\3\21\3\21\3\21\3\21\7\21"+
		"|\n\21\f\21\16\21\177\13\21\3\21\3\21\3\22\3\22\3\22\6\22\u0086\n\22\r"+
		"\22\16\22\u0087\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24"+
		"\5\24\u0095\n\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u00a0"+
		"\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\2\30\3\3\1\5\4"+
		"\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\2\1"+
		"\33\16\1\35\17\1\37\20\1!\21\1#\22\1%\2\1\'\2\1)\2\1+\2\1-\23\2\3\2\22"+
		"\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFff\4\2NNnn\4\2KKkk\4\2MMmm\4\2G"+
		"Ggg\5\2C\\aac|\4\2C\\c|\3\2))\4\2UUuu\4\2WWww\5\2\62;CHch\n\2$$))^^dd"+
		"hhppttvv\u00b4\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3"+
		"\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2"+
		"\2\27\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2-\3\2\2\2\3/\3\2\2\2\5\61\3\2\2\2\7\63\3\2\2\2\t\66\3\2\2\2\13:"+
		"\3\2\2\2\r@\3\2\2\2\17G\3\2\2\2\21I\3\2\2\2\23L\3\2\2\2\25N\3\2\2\2\27"+
		"P\3\2\2\2\31U\3\2\2\2\33]\3\2\2\2\35`\3\2\2\2\37m\3\2\2\2!w\3\2\2\2#\u0082"+
		"\3\2\2\2%\u008e\3\2\2\2\'\u0094\3\2\2\2)\u009f\3\2\2\2+\u00a1\3\2\2\2"+
		"-\u00a8\3\2\2\2/\60\7+\2\2\60\4\3\2\2\2\61\62\7*\2\2\62\6\3\2\2\2\63\64"+
		"\t\2\2\2\64\65\t\3\2\2\65\b\3\2\2\2\66\67\t\4\2\2\678\t\5\2\289\t\6\2"+
		"\29\n\3\2\2\2:;\7N\2\2;<\7Q\2\2<=\7Y\2\2=>\7G\2\2>?\7T\2\2?\f\3\2\2\2"+
		"@A\7N\2\2AB\7G\2\2BC\7P\2\2CD\7I\2\2DE\7V\2\2EF\7J\2\2F\16\3\2\2\2GH\7"+
		"?\2\2H\20\3\2\2\2IJ\7#\2\2JK\7?\2\2K\22\3\2\2\2LM\7@\2\2M\24\3\2\2\2N"+
		"O\7>\2\2O\26\3\2\2\2PQ\t\7\2\2QR\t\b\2\2RS\t\t\2\2ST\t\n\2\2T\30\3\2\2"+
		"\2UV\4\62;\2V\32\3\2\2\2WY\5\31\r\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2Z[\3"+
		"\2\2\2[^\3\2\2\2\\^\5\37\20\2]X\3\2\2\2]\\\3\2\2\2^\34\3\2\2\2_a\t\13"+
		"\2\2`_\3\2\2\2ab\3\2\2\2b`\3\2\2\2bc\3\2\2\2ci\3\2\2\2dh\t\f\2\2eh\5\31"+
		"\r\2fh\7a\2\2gd\3\2\2\2ge\3\2\2\2gf\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2"+
		"\2\2j\36\3\2\2\2ki\3\2\2\2ln\5\31\r\2ml\3\2\2\2no\3\2\2\2om\3\2\2\2op"+
		"\3\2\2\2pq\3\2\2\2qs\7\60\2\2rt\5\31\r\2sr\3\2\2\2tu\3\2\2\2us\3\2\2\2"+
		"uv\3\2\2\2v \3\2\2\2w}\7)\2\2x|\n\r\2\2yz\7^\2\2z|\7)\2\2{x\3\2\2\2{y"+
		"\3\2\2\2|\177\3\2\2\2}{\3\2\2\2}~\3\2\2\2~\u0080\3\2\2\2\177}\3\2\2\2"+
		"\u0080\u0081\7)\2\2\u0081\"\3\2\2\2\u0082\u0083\t\b\2\2\u0083\u0085\t"+
		"\16\2\2\u0084\u0086\7\"\2\2\u0085\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087"+
		"\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u008a\t\5"+
		"\2\2\u008a\u008b\t\17\2\2\u008b\u008c\t\7\2\2\u008c\u008d\t\7\2\2\u008d"+
		"$\3\2\2\2\u008e\u008f\t\20\2\2\u008f&\3\2\2\2\u0090\u0091\7^\2\2\u0091"+
		"\u0095\t\21\2\2\u0092\u0095\5+\26\2\u0093\u0095\5)\25\2\u0094\u0090\3"+
		"\2\2\2\u0094\u0092\3\2\2\2\u0094\u0093\3\2\2\2\u0095(\3\2\2\2\u0096\u0097"+
		"\7^\2\2\u0097\u0098\4\62\65\2\u0098\u0099\4\629\2\u0099\u00a0\4\629\2"+
		"\u009a\u009b\7^\2\2\u009b\u009c\4\629\2\u009c\u00a0\4\629\2\u009d\u009e"+
		"\7^\2\2\u009e\u00a0\4\629\2\u009f\u0096\3\2\2\2\u009f\u009a\3\2\2\2\u009f"+
		"\u009d\3\2\2\2\u00a0*\3\2\2\2\u00a1\u00a2\7^\2\2\u00a2\u00a3\7w\2\2\u00a3"+
		"\u00a4\5%\23\2\u00a4\u00a5\5%\23\2\u00a5\u00a6\5%\23\2\u00a6\u00a7\5%"+
		"\23\2\u00a7,\3\2\2\2\u00a8\u00a9\7\"\2\2\u00a9\u00aa\b\27\2\2\u00aa.\3"+
		"\2\2\2\17\2Z]bgiou{}\u0087\u0094\u009f";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}