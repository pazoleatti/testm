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
		NOTEQUAL=8, MORE=9, LESS=10, LIKE=11, IS_NULL=12, NUMBER=13, ALIAS=14, 
		FLOAT=15, STRING=16, SPACE=17;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'LOWER'", "'LENGTH'", 
		"'='", "'!='", "'>'", "'<'", "LIKE", "IS_NULL", "NUMBER", "ALIAS", "FLOAT", 
		"STRING", "' '"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "LOWER", "LENGTH", "EQUAL", 
		"NOTEQUAL", "MORE", "LESS", "LIKE", "DIGIT", "IS_NULL", "NUMBER", "ALIAS", 
		"FLOAT", "STRING", "HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", 
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\23\u00a9\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\16\3\16\3\16\6\16[\n\16\r\16\16\16\\\3\16\3\16\3\16\3\16\3"+
		"\16\3\17\6\17e\n\17\r\17\16\17f\3\17\5\17j\n\17\3\20\6\20m\n\20\r\20\16"+
		"\20n\3\20\3\20\3\20\7\20t\n\20\f\20\16\20w\13\20\3\21\6\21z\n\21\r\21"+
		"\16\21{\3\21\3\21\6\21\u0080\n\21\r\21\16\21\u0081\3\22\3\22\7\22\u0086"+
		"\n\22\f\22\16\22\u0089\13\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\5"+
		"\24\u0093\n\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u009e"+
		"\n\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\2\30\3\3\1\5\4"+
		"\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\2\1"+
		"\33\16\1\35\17\1\37\20\1!\21\1#\22\1%\2\1\'\2\1)\2\1+\2\1-\23\2\3\2\21"+
		"\4\2QQqq\4\2TTtt\4\2CCcc\4\2PPpp\4\2FFff\4\2NNnn\4\2KKkk\4\2MMmm\4\2G"+
		"Ggg\4\2UUuu\4\2WWww\5\2C\\aac|\4\2C\\c|\5\2\62;CHch\n\2$$))^^ddhhpptt"+
		"vv\u00b1\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\2-\3\2\2\2\3/\3\2\2\2\5\61\3\2\2\2\7\63\3\2\2\2\t\66\3\2\2\2\13:\3\2"+
		"\2\2\r@\3\2\2\2\17G\3\2\2\2\21I\3\2\2\2\23L\3\2\2\2\25N\3\2\2\2\27P\3"+
		"\2\2\2\31U\3\2\2\2\33W\3\2\2\2\35i\3\2\2\2\37l\3\2\2\2!y\3\2\2\2#\u0083"+
		"\3\2\2\2%\u008c\3\2\2\2\'\u0092\3\2\2\2)\u009d\3\2\2\2+\u009f\3\2\2\2"+
		"-\u00a6\3\2\2\2/\60\7+\2\2\60\4\3\2\2\2\61\62\7*\2\2\62\6\3\2\2\2\63\64"+
		"\t\2\2\2\64\65\t\3\2\2\65\b\3\2\2\2\66\67\t\4\2\2\678\t\5\2\289\t\6\2"+
		"\29\n\3\2\2\2:;\7N\2\2;<\7Q\2\2<=\7Y\2\2=>\7G\2\2>?\7T\2\2?\f\3\2\2\2"+
		"@A\7N\2\2AB\7G\2\2BC\7P\2\2CD\7I\2\2DE\7V\2\2EF\7J\2\2F\16\3\2\2\2GH\7"+
		"?\2\2H\20\3\2\2\2IJ\7#\2\2JK\7?\2\2K\22\3\2\2\2LM\7@\2\2M\24\3\2\2\2N"+
		"O\7>\2\2O\26\3\2\2\2PQ\t\7\2\2QR\t\b\2\2RS\t\t\2\2ST\t\n\2\2T\30\3\2\2"+
		"\2UV\4\62;\2V\32\3\2\2\2WX\t\b\2\2XZ\t\13\2\2Y[\7\"\2\2ZY\3\2\2\2[\\\3"+
		"\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]^\3\2\2\2^_\t\5\2\2_`\t\f\2\2`a\t\7\2\2a"+
		"b\t\7\2\2b\34\3\2\2\2ce\5\31\r\2dc\3\2\2\2ef\3\2\2\2fd\3\2\2\2fg\3\2\2"+
		"\2gj\3\2\2\2hj\5!\21\2id\3\2\2\2ih\3\2\2\2j\36\3\2\2\2km\t\r\2\2lk\3\2"+
		"\2\2mn\3\2\2\2nl\3\2\2\2no\3\2\2\2ou\3\2\2\2pt\t\16\2\2qt\5\31\r\2rt\7"+
		"a\2\2sp\3\2\2\2sq\3\2\2\2sr\3\2\2\2tw\3\2\2\2us\3\2\2\2uv\3\2\2\2v \3"+
		"\2\2\2wu\3\2\2\2xz\5\31\r\2yx\3\2\2\2z{\3\2\2\2{y\3\2\2\2{|\3\2\2\2|}"+
		"\3\2\2\2}\177\7\60\2\2~\u0080\5\31\r\2\177~\3\2\2\2\u0080\u0081\3\2\2"+
		"\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082\"\3\2\2\2\u0083\u0087\7"+
		")\2\2\u0084\u0086\13\2\2\2\u0085\u0084\3\2\2\2\u0086\u0089\3\2\2\2\u0087"+
		"\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008a\3\2\2\2\u0089\u0087\3\2"+
		"\2\2\u008a\u008b\7)\2\2\u008b$\3\2\2\2\u008c\u008d\t\17\2\2\u008d&\3\2"+
		"\2\2\u008e\u008f\7^\2\2\u008f\u0093\t\20\2\2\u0090\u0093\5+\26\2\u0091"+
		"\u0093\5)\25\2\u0092\u008e\3\2\2\2\u0092\u0090\3\2\2\2\u0092\u0091\3\2"+
		"\2\2\u0093(\3\2\2\2\u0094\u0095\7^\2\2\u0095\u0096\4\62\65\2\u0096\u0097"+
		"\4\629\2\u0097\u009e\4\629\2\u0098\u0099\7^\2\2\u0099\u009a\4\629\2\u009a"+
		"\u009e\4\629\2\u009b\u009c\7^\2\2\u009c\u009e\4\629\2\u009d\u0094\3\2"+
		"\2\2\u009d\u0098\3\2\2\2\u009d\u009b\3\2\2\2\u009e*\3\2\2\2\u009f\u00a0"+
		"\7^\2\2\u00a0\u00a1\7w\2\2\u00a1\u00a2\5%\23\2\u00a2\u00a3\5%\23\2\u00a3"+
		"\u00a4\5%\23\2\u00a4\u00a5\5%\23\2\u00a5,\3\2\2\2\u00a6\u00a7\7\"\2\2"+
		"\u00a7\u00a8\b\27\2\2\u00a8.\3\2\2\2\16\2\\finsu{\u0081\u0087\u0092\u009d";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}