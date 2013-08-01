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
		MORE=7, LESS=8, LIKE=9, INT=10, ALIAS=11, STR=12, STRING=13, SPACE=14;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"')'", "'('", "LINK_TYPE_OR", "LINK_TYPE_AND", "'='", "'!='", "'>'", "'<'", 
		"LIKE", "INT", "ALIAS", "STR", "STRING", "' '"
	};
	public static final String[] ruleNames = {
		"T__1", "T__0", "LINK_TYPE_OR", "LINK_TYPE_AND", "EQUAL", "NOTEQUAL", 
		"MORE", "LESS", "LIKE", "DIGIT", "INT", "ALIAS", "STR", "STRING", "HEX_DIGIT", 
		"ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", "SPACE"
	};


	public FilterTreeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SqlCondition.g4"; }

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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\20\u0086\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f"+
		"\6\fF\n\f\r\f\16\fG\3\r\6\rK\n\r\r\r\16\rL\3\r\3\r\7\rQ\n\r\f\r\16\rT"+
		"\13\r\3\16\3\16\3\16\7\16Y\n\16\f\16\16\16\\\13\16\3\16\3\16\3\17\3\17"+
		"\3\17\7\17c\n\17\f\17\16\17f\13\17\3\17\3\17\3\20\3\20\3\21\3\21\3\21"+
		"\3\21\5\21p\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22{\n"+
		"\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\2\25\3\3\1\5\4\1"+
		"\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\2\1\27\f\1\31\r\1\33"+
		"\16\1\35\17\1\37\2\1!\2\1#\2\1%\2\1\'\20\2\3\2\20\4\2QQqq\4\2TTtt\4\2"+
		"CCcc\4\2PPpp\4\2FFff\4\2NNnn\4\2KKkk\4\2MMmm\4\2GGgg\4\2C\\c|\5\2\62;"+
		"C\\c|\4\2$$^^\5\2\62;CHch\n\2$$))^^ddhhppttvv\u008c\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\2\'\3\2\2\2\3)\3\2\2\2\5+\3\2\2\2\7-\3\2\2\2\t\60\3\2\2\2\13\64"+
		"\3\2\2\2\r\66\3\2\2\2\179\3\2\2\2\21;\3\2\2\2\23=\3\2\2\2\25B\3\2\2\2"+
		"\27E\3\2\2\2\31J\3\2\2\2\33U\3\2\2\2\35_\3\2\2\2\37i\3\2\2\2!o\3\2\2\2"+
		"#z\3\2\2\2%|\3\2\2\2\'\u0083\3\2\2\2)*\7+\2\2*\4\3\2\2\2+,\7*\2\2,\6\3"+
		"\2\2\2-.\t\2\2\2./\t\3\2\2/\b\3\2\2\2\60\61\t\4\2\2\61\62\t\5\2\2\62\63"+
		"\t\6\2\2\63\n\3\2\2\2\64\65\7?\2\2\65\f\3\2\2\2\66\67\7#\2\2\678\7?\2"+
		"\28\16\3\2\2\29:\7@\2\2:\20\3\2\2\2;<\7>\2\2<\22\3\2\2\2=>\t\7\2\2>?\t"+
		"\b\2\2?@\t\t\2\2@A\t\n\2\2A\24\3\2\2\2BC\4\62;\2C\26\3\2\2\2DF\5\25\13"+
		"\2ED\3\2\2\2FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2H\30\3\2\2\2IK\t\13\2\2JI\3"+
		"\2\2\2KL\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MR\3\2\2\2NQ\t\13\2\2OQ\5\25\13\2"+
		"PN\3\2\2\2PO\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2S\32\3\2\2\2TR\3\2\2"+
		"\2UZ\7)\2\2VY\5\'\24\2WY\t\f\2\2XV\3\2\2\2XW\3\2\2\2Y\\\3\2\2\2ZX\3\2"+
		"\2\2Z[\3\2\2\2[]\3\2\2\2\\Z\3\2\2\2]^\7)\2\2^\34\3\2\2\2_d\7$\2\2`c\5"+
		"!\21\2ac\n\r\2\2b`\3\2\2\2ba\3\2\2\2cf\3\2\2\2db\3\2\2\2de\3\2\2\2eg\3"+
		"\2\2\2fd\3\2\2\2gh\7$\2\2h\36\3\2\2\2ij\t\16\2\2j \3\2\2\2kl\7^\2\2lp"+
		"\t\17\2\2mp\5%\23\2np\5#\22\2ok\3\2\2\2om\3\2\2\2on\3\2\2\2p\"\3\2\2\2"+
		"qr\7^\2\2rs\4\62\65\2st\4\629\2t{\4\629\2uv\7^\2\2vw\4\629\2w{\4\629\2"+
		"xy\7^\2\2y{\4\629\2zq\3\2\2\2zu\3\2\2\2zx\3\2\2\2{$\3\2\2\2|}\7^\2\2}"+
		"~\7w\2\2~\177\5\37\20\2\177\u0080\5\37\20\2\u0080\u0081\5\37\20\2\u0081"+
		"\u0082\5\37\20\2\u0082&\3\2\2\2\u0083\u0084\7\"\2\2\u0084\u0085\b\24\2"+
		"\2\u0085(\3\2\2\2\r\2GLPRXZbdoz";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}