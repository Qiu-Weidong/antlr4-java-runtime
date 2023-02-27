package examples.lexer;

// Generated from java-escape by ANTLR 4.11.1
import runtime.Lexer;
import runtime.CharStream;
import runtime.*;
import runtime.atn.*;
import runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class HelloLexer extends Lexer {
//	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();



	public static final int
		Id=1, Number=2, Add=3, Sub=4, Mul=5, Div=6, WS=7, Comment=8;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] RULE_NAMES = {
			"Digit", "Letter", "Id", "Number", "Add", "Sub", "Mul", "Div", "WS",
			"Comment"
	};

	private static final String[] LITERAL_NAMES = {
			null, null, null, "'+'", "'-'", "'*'", "'/'"
	};

	private static final String[] SYMBOLIC_NAMES = {
			null, "Id", "Number", "Add", "Sub", "Mul", "Div", "WS", "Comment"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(LITERAL_NAMES, SYMBOLIC_NAMES);

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public HelloLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Hello.g4"; }

	@Override
	public String[] getRuleNames() { return RULE_NAMES; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\b@\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u001d"+
		"\b\u0002\n\u0002\f\u0002 \t\u0002\u0001\u0003\u0004\u0003#\b\u0003\u000b"+
		"\u0003\f\u0003$\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t7\b\t\n\t\f\t:\t\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u00018\u0000\n\u0001\u0000\u0003\u0000\u0005"+
		"\u0001\u0007\u0002\t\u0003\u000b\u0004\r\u0005\u000f\u0006\u0011\u0007"+
		"\u0013\b\u0001\u0000\u0003\u0001\u000009\u0003\u0000AZ__az\u0003\u0000"+
		"\t\n\r\r  A\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"+
		"\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"+
		"\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"+
		"\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"+
		"\u0001\u0015\u0001\u0000\u0000\u0000\u0003\u0017\u0001\u0000\u0000\u0000"+
		"\u0005\u0019\u0001\u0000\u0000\u0000\u0007\"\u0001\u0000\u0000\u0000\t"+
		"&\u0001\u0000\u0000\u0000\u000b(\u0001\u0000\u0000\u0000\r*\u0001\u0000"+
		"\u0000\u0000\u000f,\u0001\u0000\u0000\u0000\u0011.\u0001\u0000\u0000\u0000"+
		"\u00132\u0001\u0000\u0000\u0000\u0015\u0016\u0007\u0000\u0000\u0000\u0016"+
		"\u0002\u0001\u0000\u0000\u0000\u0017\u0018\u0007\u0001\u0000\u0000\u0018"+
		"\u0004\u0001\u0000\u0000\u0000\u0019\u001e\u0003\u0003\u0001\u0000\u001a"+
		"\u001d\u0003\u0003\u0001\u0000\u001b\u001d\u0003\u0001\u0000\u0000\u001c"+
		"\u001a\u0001\u0000\u0000\u0000\u001c\u001b\u0001\u0000\u0000\u0000\u001d"+
		" \u0001\u0000\u0000\u0000\u001e\u001c\u0001\u0000\u0000\u0000\u001e\u001f"+
		"\u0001\u0000\u0000\u0000\u001f\u0006\u0001\u0000\u0000\u0000 \u001e\u0001"+
		"\u0000\u0000\u0000!#\u0003\u0001\u0000\u0000\"!\u0001\u0000\u0000\u0000"+
		"#$\u0001\u0000\u0000\u0000$\"\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000"+
		"\u0000%\b\u0001\u0000\u0000\u0000&\'\u0005+\u0000\u0000\'\n\u0001\u0000"+
		"\u0000\u0000()\u0005-\u0000\u0000)\f\u0001\u0000\u0000\u0000*+\u0005*"+
		"\u0000\u0000+\u000e\u0001\u0000\u0000\u0000,-\u0005/\u0000\u0000-\u0010"+
		"\u0001\u0000\u0000\u0000./\u0007\u0002\u0000\u0000/0\u0001\u0000\u0000"+
		"\u000001\u0006\b\u0000\u00001\u0012\u0001\u0000\u0000\u000023\u0005/\u0000"+
		"\u000034\u0005*\u0000\u000048\u0001\u0000\u0000\u000057\t\u0000\u0000"+
		"\u000065\u0001\u0000\u0000\u00007:\u0001\u0000\u0000\u000089\u0001\u0000"+
		"\u0000\u000086\u0001\u0000\u0000\u00009;\u0001\u0000\u0000\u0000:8\u0001"+
		"\u0000\u0000\u0000;<\u0005*\u0000\u0000<=\u0005/\u0000\u0000=>\u0001\u0000"+
		"\u0000\u0000>?\u0006\t\u0001\u0000?\u0014\u0001\u0000\u0000\u0000\u0005"+
		"\u0000\u001c\u001e$8\u0002\u0006\u0000\u0000\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}