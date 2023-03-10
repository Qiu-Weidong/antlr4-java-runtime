// Generated from java-escape by ANTLR 4.11.1

package examples.lexer;

import runtime.*;
import runtime.atn.ATN;
import runtime.atn.ATNDeserializer;
import runtime.atn.LexerATNSimulator;
import runtime.atn.PredictionContextCache;
import runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class HelloLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
			new PredictionContextCache();
	public static final int
			ID=1, NUMBER=2, WS=3;
	public static String[] channelNames = {
			"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
			"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
				"Digit", "Letter", "ID", "NUMBER", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
				null, "ID", "NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);



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
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
			"\u0004\u0000\u0003 \u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
					"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
					"\u0007\u0004\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002"+
					"\u0001\u0002\u0001\u0002\u0005\u0002\u0013\b\u0002\n\u0002\f\u0002\u0016"+
					"\t\u0002\u0001\u0003\u0004\u0003\u0019\b\u0003\u000b\u0003\f\u0003\u001a"+
					"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0000\u0000\u0005\u0001"+
					"\u0000\u0003\u0000\u0005\u0001\u0007\u0002\t\u0003\u0001\u0000\u0003\u0001"+
					"\u000009\u0002\u0000AZaz\u0003\u0000\t\n\r\r   \u0000\u0005\u0001\u0000"+
					"\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000"+
					"\u0000\u0001\u000b\u0001\u0000\u0000\u0000\u0003\r\u0001\u0000\u0000\u0000"+
					"\u0005\u000f\u0001\u0000\u0000\u0000\u0007\u0018\u0001\u0000\u0000\u0000"+
					"\t\u001c\u0001\u0000\u0000\u0000\u000b\f\u0007\u0000\u0000\u0000\f\u0002"+
					"\u0001\u0000\u0000\u0000\r\u000e\u0007\u0001\u0000\u0000\u000e\u0004\u0001"+
					"\u0000\u0000\u0000\u000f\u0014\u0003\u0003\u0001\u0000\u0010\u0013\u0003"+
					"\u0001\u0000\u0000\u0011\u0013\u0003\u0003\u0001\u0000\u0012\u0010\u0001"+
					"\u0000\u0000\u0000\u0012\u0011\u0001\u0000\u0000\u0000\u0013\u0016\u0001"+
					"\u0000\u0000\u0000\u0014\u0012\u0001\u0000\u0000\u0000\u0014\u0015\u0001"+
					"\u0000\u0000\u0000\u0015\u0006\u0001\u0000\u0000\u0000\u0016\u0014\u0001"+
					"\u0000\u0000\u0000\u0017\u0019\u0003\u0001\u0000\u0000\u0018\u0017\u0001"+
					"\u0000\u0000\u0000\u0019\u001a\u0001\u0000\u0000\u0000\u001a\u0018\u0001"+
					"\u0000\u0000\u0000\u001a\u001b\u0001\u0000\u0000\u0000\u001b\b\u0001\u0000"+
					"\u0000\u0000\u001c\u001d\u0007\u0002\u0000\u0000\u001d\u001e\u0001\u0000"+
					"\u0000\u0000\u001e\u001f\u0006\u0004\u0000\u0000\u001f\n\u0001\u0000\u0000"+
					"\u0000\u0004\u0000\u0012\u0014\u001a\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}