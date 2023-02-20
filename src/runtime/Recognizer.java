

package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNSimulator;
import runtime.atn.ParseInfo;
import runtime.misc.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Recognizer<Symbol, ATNInterpreter extends ATNSimulator> {
	public static final int EOF=-1;

	private static final Map<Vocabulary, Map<String, Integer>> tokenTypeMapCache =
		new WeakHashMap<Vocabulary, Map<String, Integer>>();
	private static final Map<String[], Map<String, Integer>> ruleIndexMapCache =
		new WeakHashMap<String[], Map<String, Integer>>();


	private List<ANTLRErrorListener> _listeners =
		new CopyOnWriteArrayList<ANTLRErrorListener>() {{
			add(ConsoleErrorListener.INSTANCE);
		}};

	protected ATNInterpreter _interp;

	private int _stateNumber = -1;


	@Deprecated
	public abstract String[] getTokenNames();

	public abstract String[] getRuleNames();


	@SuppressWarnings("deprecation")
	public Vocabulary getVocabulary() {
		return VocabularyImpl.fromTokenNames(getTokenNames());
	}


	public Map<String, Integer> getTokenTypeMap() {
		Vocabulary vocabulary = getVocabulary();
		synchronized (tokenTypeMapCache) {
			Map<String, Integer> result = tokenTypeMapCache.get(vocabulary);
			if (result == null) {
				result = new HashMap<String, Integer>();
				for (int i = 0; i <= getATN().maxTokenType; i++) {
					String literalName = vocabulary.getLiteralName(i);
					if (literalName != null) {
						result.put(literalName, i);
					}

					String symbolicName = vocabulary.getSymbolicName(i);
					if (symbolicName != null) {
						result.put(symbolicName, i);
					}
				}

				result.put("EOF", Token.EOF);
				result = Collections.unmodifiableMap(result);
				tokenTypeMapCache.put(vocabulary, result);
			}

			return result;
		}
	}


	public Map<String, Integer> getRuleIndexMap() {
		String[] ruleNames = getRuleNames();
		if (ruleNames == null) {
			throw new UnsupportedOperationException("The current recognizer does not provide a list of rule names.");
		}

		synchronized (ruleIndexMapCache) {
			Map<String, Integer> result = ruleIndexMapCache.get(ruleNames);
			if (result == null) {
				result = Collections.unmodifiableMap(Utils.toMap(ruleNames));
				ruleIndexMapCache.put(ruleNames, result);
			}

			return result;
		}
	}

	public int getTokenType(String tokenName) {
		Integer ttype = getTokenTypeMap().get(tokenName);
		if ( ttype!=null ) return ttype;
		return Token.INVALID_TYPE;
	}


	public String getSerializedATN() {
		throw new UnsupportedOperationException("there is no serialized ATN");
	}


	public abstract String getGrammarFileName();


	public abstract ATN getATN();


	public ATNInterpreter getInterpreter() {
		return _interp;
	}


	public ParseInfo getParseInfo() {
		return null;
	}


	public void setInterpreter(ATNInterpreter interpreter) {
		_interp = interpreter;
	}


	public String getErrorHeader(RecognitionException e) {
		int line = e.getOffendingToken().getLine();
		int charPositionInLine = e.getOffendingToken().getCharPositionInLine();
		return "line "+line+":"+charPositionInLine;
	}


	@Deprecated
	public String getTokenErrorDisplay(Token t) {
		if ( t==null ) return "<no token>";
		String s = t.getText();
		if ( s==null ) {
			if ( t.getType()==Token.EOF ) {
				s = "<EOF>";
			}
			else {
				s = "<"+t.getType()+">";
			}
		}
		s = s.replace("\n","\\n");
		s = s.replace("\r","\\r");
		s = s.replace("\t","\\t");
		return "'"+s+"'";
	}


	public void addErrorListener(ANTLRErrorListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null.");
		}

		_listeners.add(listener);
	}

	public void removeErrorListener(ANTLRErrorListener listener) {
		_listeners.remove(listener);
	}

	public void removeErrorListeners() {
		_listeners.clear();
	}


	public List<? extends ANTLRErrorListener> getErrorListeners() {
		return _listeners;
	}

	public ANTLRErrorListener getErrorListenerDispatch() {
		return new ProxyErrorListener(getErrorListeners());
	}



	public boolean sempred(RuleContext _localctx, int ruleIndex, int actionIndex) {
		return true;
	}

	public boolean precpred(RuleContext localctx, int precedence) {
		return true;
	}

	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
	}

	public final int getState() {
		return _stateNumber;
	}

	/** Indicate that the recognizer has changed internal state that is
	 *  consistent with the ATN state passed in.  This way we always know
	 *  where we are in the ATN as the parser goes along. The rule
	 *  context objects form a stack that lets us see the stack of
	 *  invoking rules. Combine this and we have complete ATN
	 *  configuration information.
	 */
	public final void setState(int atnState) {
//		System.err.println("setState "+atnState);
		_stateNumber = atnState;
//		if ( traceATNStates ) _ctx.trace(atnState);
	}

	public abstract IntStream getInputStream();

	public abstract void setInputStream(IntStream input);


	public abstract TokenFactory<?> getTokenFactory();

	public abstract void setTokenFactory(TokenFactory<?> input);
}
