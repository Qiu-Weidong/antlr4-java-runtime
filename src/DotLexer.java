// Generated from java-escape by ANTLR 4.11.1
import runtime.Lexer;
import runtime.CharStream;
import runtime.Token;
import runtime.TokenStream;
import runtime.*;
import runtime.atn.*;
import runtime.dfa.DFA;
import runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class DotLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, STRICT=11, GRAPH=12, DIGRAPH=13, NODE=14, EDGE=15, SUBGRAPH=16, 
		NUMBER=17, STRING=18, ID=19, HTML_STRING=20, COMMENT=21, LINE_COMMENT=22, 
		PREPROC=23, WS=24;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "STRICT", "GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "NUMBER", 
			"DIGIT", "STRING", "ID", "LETTER", "HTML_STRING", "TAG", "COMMENT", "LINE_COMMENT", 
			"PREPROC", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "';'", "'['", "','", "']'", "'='", "'->'", "'--'", 
			"':'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "STRICT", 
			"GRAPH", "DIGRAPH", "NODE", "EDGE", "SUBGRAPH", "NUMBER", "STRING", "ID", 
			"HTML_STRING", "COMMENT", "LINE_COMMENT", "PREPROC", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public DotLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Dot.g4"; }

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
		"\u0004\u0000\u0018\u00e6\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0003\u0010w\b\u0010\u0001\u0010\u0001\u0010\u0004\u0010{\b\u0010"+
		"\u000b\u0010\f\u0010|\u0001\u0010\u0004\u0010\u0080\b\u0010\u000b\u0010"+
		"\f\u0010\u0081\u0001\u0010\u0001\u0010\u0005\u0010\u0086\b\u0010\n\u0010"+
		"\f\u0010\u0089\t\u0010\u0003\u0010\u008b\b\u0010\u0003\u0010\u008d\b\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0005\u0012\u0095\b\u0012\n\u0012\f\u0012\u0098\t\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u009f\b\u0013\n"+
		"\u0013\f\u0013\u00a2\t\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0005\u0015\u00a9\b\u0015\n\u0015\f\u0015\u00ac\t\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0005\u0016\u00b2\b\u0016"+
		"\n\u0016\f\u0016\u00b5\t\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u00bd\b\u0017\n\u0017\f\u0017"+
		"\u00c0\t\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u00cb\b\u0018"+
		"\n\u0018\f\u0018\u00ce\t\u0018\u0001\u0018\u0003\u0018\u00d1\b\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0005"+
		"\u0019\u00d9\b\u0019\n\u0019\f\u0019\u00dc\t\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0004\u001a\u00e1\b\u001a\u000b\u001a\f\u001a\u00e2\u0001"+
		"\u001a\u0001\u001a\u0004\u0096\u00b3\u00be\u00cc\u0000\u001b\u0001\u0001"+
		"\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f"+
		"\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f"+
		"\u001f\u0010!\u0011#\u0000%\u0012\'\u0013)\u0000+\u0014-\u0000/\u0015"+
		"1\u00163\u00175\u0018\u0001\u0000\u0014\u0002\u0000SSss\u0002\u0000TT"+
		"tt\u0002\u0000RRrr\u0002\u0000IIii\u0002\u0000CCcc\u0002\u0000GGgg\u0002"+
		"\u0000AAaa\u0002\u0000PPpp\u0002\u0000HHhh\u0002\u0000DDdd\u0002\u0000"+
		"NNnn\u0002\u0000OOoo\u0002\u0000EEee\u0002\u0000UUuu\u0002\u0000BBbb\u0001"+
		"\u000009\u0004\u0000AZ__az\u0080\u00ff\u0002\u0000<<>>\u0002\u0000\n\n"+
		"\r\r\u0003\u0000\t\n\r\r  \u00f4\u0000\u0001\u0001\u0000\u0000\u0000\u0000"+
		"\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000"+
		"\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b"+
		"\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001"+
		"\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001"+
		"\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001"+
		"\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001"+
		"\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001"+
		"\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000"+
		"\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000"+
		"\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u00003"+
		"\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000\u0000\u00017\u0001\u0000"+
		"\u0000\u0000\u00039\u0001\u0000\u0000\u0000\u0005;\u0001\u0000\u0000\u0000"+
		"\u0007=\u0001\u0000\u0000\u0000\t?\u0001\u0000\u0000\u0000\u000bA\u0001"+
		"\u0000\u0000\u0000\rC\u0001\u0000\u0000\u0000\u000fE\u0001\u0000\u0000"+
		"\u0000\u0011H\u0001\u0000\u0000\u0000\u0013K\u0001\u0000\u0000\u0000\u0015"+
		"M\u0001\u0000\u0000\u0000\u0017T\u0001\u0000\u0000\u0000\u0019Z\u0001"+
		"\u0000\u0000\u0000\u001bb\u0001\u0000\u0000\u0000\u001dg\u0001\u0000\u0000"+
		"\u0000\u001fl\u0001\u0000\u0000\u0000!v\u0001\u0000\u0000\u0000#\u008e"+
		"\u0001\u0000\u0000\u0000%\u0090\u0001\u0000\u0000\u0000\'\u009b\u0001"+
		"\u0000\u0000\u0000)\u00a3\u0001\u0000\u0000\u0000+\u00a5\u0001\u0000\u0000"+
		"\u0000-\u00af\u0001\u0000\u0000\u0000/\u00b8\u0001\u0000\u0000\u00001"+
		"\u00c6\u0001\u0000\u0000\u00003\u00d6\u0001\u0000\u0000\u00005\u00e0\u0001"+
		"\u0000\u0000\u000078\u0005{\u0000\u00008\u0002\u0001\u0000\u0000\u0000"+
		"9:\u0005}\u0000\u0000:\u0004\u0001\u0000\u0000\u0000;<\u0005;\u0000\u0000"+
		"<\u0006\u0001\u0000\u0000\u0000=>\u0005[\u0000\u0000>\b\u0001\u0000\u0000"+
		"\u0000?@\u0005,\u0000\u0000@\n\u0001\u0000\u0000\u0000AB\u0005]\u0000"+
		"\u0000B\f\u0001\u0000\u0000\u0000CD\u0005=\u0000\u0000D\u000e\u0001\u0000"+
		"\u0000\u0000EF\u0005-\u0000\u0000FG\u0005>\u0000\u0000G\u0010\u0001\u0000"+
		"\u0000\u0000HI\u0005-\u0000\u0000IJ\u0005-\u0000\u0000J\u0012\u0001\u0000"+
		"\u0000\u0000KL\u0005:\u0000\u0000L\u0014\u0001\u0000\u0000\u0000MN\u0007"+
		"\u0000\u0000\u0000NO\u0007\u0001\u0000\u0000OP\u0007\u0002\u0000\u0000"+
		"PQ\u0007\u0003\u0000\u0000QR\u0007\u0004\u0000\u0000RS\u0007\u0001\u0000"+
		"\u0000S\u0016\u0001\u0000\u0000\u0000TU\u0007\u0005\u0000\u0000UV\u0007"+
		"\u0002\u0000\u0000VW\u0007\u0006\u0000\u0000WX\u0007\u0007\u0000\u0000"+
		"XY\u0007\b\u0000\u0000Y\u0018\u0001\u0000\u0000\u0000Z[\u0007\t\u0000"+
		"\u0000[\\\u0007\u0003\u0000\u0000\\]\u0007\u0005\u0000\u0000]^\u0007\u0002"+
		"\u0000\u0000^_\u0007\u0006\u0000\u0000_`\u0007\u0007\u0000\u0000`a\u0007"+
		"\b\u0000\u0000a\u001a\u0001\u0000\u0000\u0000bc\u0007\n\u0000\u0000cd"+
		"\u0007\u000b\u0000\u0000de\u0007\t\u0000\u0000ef\u0007\f\u0000\u0000f"+
		"\u001c\u0001\u0000\u0000\u0000gh\u0007\f\u0000\u0000hi\u0007\t\u0000\u0000"+
		"ij\u0007\u0005\u0000\u0000jk\u0007\f\u0000\u0000k\u001e\u0001\u0000\u0000"+
		"\u0000lm\u0007\u0000\u0000\u0000mn\u0007\r\u0000\u0000no\u0007\u000e\u0000"+
		"\u0000op\u0007\u0005\u0000\u0000pq\u0007\u0002\u0000\u0000qr\u0007\u0006"+
		"\u0000\u0000rs\u0007\u0007\u0000\u0000st\u0007\b\u0000\u0000t \u0001\u0000"+
		"\u0000\u0000uw\u0005-\u0000\u0000vu\u0001\u0000\u0000\u0000vw\u0001\u0000"+
		"\u0000\u0000w\u008c\u0001\u0000\u0000\u0000xz\u0005.\u0000\u0000y{\u0003"+
		"#\u0011\u0000zy\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|z\u0001"+
		"\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000}\u008d\u0001\u0000\u0000"+
		"\u0000~\u0080\u0003#\u0011\u0000\u007f~\u0001\u0000\u0000\u0000\u0080"+
		"\u0081\u0001\u0000\u0000\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081"+
		"\u0082\u0001\u0000\u0000\u0000\u0082\u008a\u0001\u0000\u0000\u0000\u0083"+
		"\u0087\u0005.\u0000\u0000\u0084\u0086\u0003#\u0011\u0000\u0085\u0084\u0001"+
		"\u0000\u0000\u0000\u0086\u0089\u0001\u0000\u0000\u0000\u0087\u0085\u0001"+
		"\u0000\u0000\u0000\u0087\u0088\u0001\u0000\u0000\u0000\u0088\u008b\u0001"+
		"\u0000\u0000\u0000\u0089\u0087\u0001\u0000\u0000\u0000\u008a\u0083\u0001"+
		"\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000\u0000\u008b\u008d\u0001"+
		"\u0000\u0000\u0000\u008cx\u0001\u0000\u0000\u0000\u008c\u007f\u0001\u0000"+
		"\u0000\u0000\u008d\"\u0001\u0000\u0000\u0000\u008e\u008f\u0007\u000f\u0000"+
		"\u0000\u008f$\u0001\u0000\u0000\u0000\u0090\u0096\u0005\"\u0000\u0000"+
		"\u0091\u0092\u0005\\\u0000\u0000\u0092\u0095\u0005\"\u0000\u0000\u0093"+
		"\u0095\t\u0000\u0000\u0000\u0094\u0091\u0001\u0000\u0000\u0000\u0094\u0093"+
		"\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000\u0000\u0000\u0096\u0097"+
		"\u0001\u0000\u0000\u0000\u0096\u0094\u0001\u0000\u0000\u0000\u0097\u0099"+
		"\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0099\u009a"+
		"\u0005\"\u0000\u0000\u009a&\u0001\u0000\u0000\u0000\u009b\u00a0\u0003"+
		")\u0014\u0000\u009c\u009f\u0003)\u0014\u0000\u009d\u009f\u0003#\u0011"+
		"\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009d\u0001\u0000\u0000"+
		"\u0000\u009f\u00a2\u0001\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000"+
		"\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1(\u0001\u0000\u0000\u0000"+
		"\u00a2\u00a0\u0001\u0000\u0000\u0000\u00a3\u00a4\u0007\u0010\u0000\u0000"+
		"\u00a4*\u0001\u0000\u0000\u0000\u00a5\u00aa\u0005<\u0000\u0000\u00a6\u00a9"+
		"\u0003-\u0016\u0000\u00a7\u00a9\b\u0011\u0000\u0000\u00a8\u00a6\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a9\u00ac\u0001"+
		"\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001"+
		"\u0000\u0000\u0000\u00ab\u00ad\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ae\u0005>\u0000\u0000\u00ae,\u0001\u0000\u0000"+
		"\u0000\u00af\u00b3\u0005<\u0000\u0000\u00b0\u00b2\t\u0000\u0000\u0000"+
		"\u00b1\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b5\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000"+
		"\u00b4\u00b6\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b6\u00b7\u0005>\u0000\u0000\u00b7.\u0001\u0000\u0000\u0000\u00b8\u00b9"+
		"\u0005/\u0000\u0000\u00b9\u00ba\u0005*\u0000\u0000\u00ba\u00be\u0001\u0000"+
		"\u0000\u0000\u00bb\u00bd\t\u0000\u0000\u0000\u00bc\u00bb\u0001\u0000\u0000"+
		"\u0000\u00bd\u00c0\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000"+
		"\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00bf\u00c1\u0001\u0000\u0000"+
		"\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005*\u0000\u0000"+
		"\u00c2\u00c3\u0005/\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4"+
		"\u00c5\u0006\u0017\u0000\u0000\u00c50\u0001\u0000\u0000\u0000\u00c6\u00c7"+
		"\u0005/\u0000\u0000\u00c7\u00c8\u0005/\u0000\u0000\u00c8\u00cc\u0001\u0000"+
		"\u0000\u0000\u00c9\u00cb\t\u0000\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000"+
		"\u0000\u00cb\u00ce\u0001\u0000\u0000\u0000\u00cc\u00cd\u0001\u0000\u0000"+
		"\u0000\u00cc\u00ca\u0001\u0000\u0000\u0000\u00cd\u00d0\u0001\u0000\u0000"+
		"\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d1\u0005\r\u0000\u0000"+
		"\u00d0\u00cf\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000\u0000\u0000"+
		"\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d3\u0005\n\u0000\u0000\u00d3"+
		"\u00d4\u0001\u0000\u0000\u0000\u00d4\u00d5\u0006\u0018\u0000\u0000\u00d5"+
		"2\u0001\u0000\u0000\u0000\u00d6\u00da\u0005#\u0000\u0000\u00d7\u00d9\b"+
		"\u0012\u0000\u0000\u00d8\u00d7\u0001\u0000\u0000\u0000\u00d9\u00dc\u0001"+
		"\u0000\u0000\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00da\u00db\u0001"+
		"\u0000\u0000\u0000\u00db\u00dd\u0001\u0000\u0000\u0000\u00dc\u00da\u0001"+
		"\u0000\u0000\u0000\u00dd\u00de\u0006\u0019\u0000\u0000\u00de4\u0001\u0000"+
		"\u0000\u0000\u00df\u00e1\u0007\u0013\u0000\u0000\u00e0\u00df\u0001\u0000"+
		"\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000"+
		"\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000"+
		"\u0000\u0000\u00e4\u00e5\u0006\u001a\u0001\u0000\u00e56\u0001\u0000\u0000"+
		"\u0000\u0013\u0000v|\u0081\u0087\u008a\u008c\u0094\u0096\u009e\u00a0\u00a8"+
		"\u00aa\u00b3\u00be\u00cc\u00d0\u00da\u00e2\u0002\u0000\u0001\u0000\u0006"+
		"\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}