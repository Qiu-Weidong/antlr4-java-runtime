

package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNSimulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Recognizer<Symbol, ATNInterpreter extends ATNSimulator> {
	public static final int EOF=-1;


	private final List<ANTLRErrorListener> _listeners =
		new CopyOnWriteArrayList<ANTLRErrorListener>() {{
			add(ConsoleErrorListener.INSTANCE);
		}};

	protected ATNInterpreter _interp;

	private int _stateNumber = -1;


	public abstract String[] getRuleNames();


	public abstract Vocabulary getVocabulary();


	public String getSerializedATN() {
		throw new UnsupportedOperationException("there is no serialized ATN");
	}


	public abstract ATN getATN();


	public ATNInterpreter getInterpreter() {
		return _interp;
	}


	public void setInterpreter(ATNInterpreter interpreter) {
		_interp = interpreter;
	}


	public String getErrorHeader(RecognitionException e) {
		int line = e.getOffendingToken().getLine();
		int charPositionInLine = e.getOffendingToken().getCharPositionInLine();
		return "line "+line+":"+charPositionInLine;
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

	public final void setState(int atnState) {
		_stateNumber = atnState;
	}

	public abstract IntStream getInputStream();

	public abstract void setInputStream(IntStream input);


	public abstract TokenFactory<?> getTokenFactory();

	public abstract void setTokenFactory(TokenFactory<?> input);
}
