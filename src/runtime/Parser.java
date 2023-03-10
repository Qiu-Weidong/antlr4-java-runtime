
package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNDeserializationOptions;
import runtime.atn.ATNDeserializer;
import runtime.atn.ATNSimulator;
import runtime.atn.ATNState;
import runtime.atn.ParseInfo;
import runtime.atn.ParserATNSimulator;
import runtime.atn.PredictionMode;
import runtime.atn.ProfilingATNSimulator;
import runtime.atn.RuleTransition;
import runtime.dfa.DFA;
import runtime.misc.IntegerStack;
import runtime.misc.IntervalSet;
import runtime.tree.ErrorNode;
import runtime.tree.ErrorNodeImpl;
import runtime.tree.ParseTreeListener;
import runtime.tree.TerminalNode;
import runtime.tree.TerminalNodeImpl;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Parser extends Recognizer<Token, ParserATNSimulator> {


	private ATN bypassAltsAtnCache;



	protected ANTLRErrorStrategy _errHandler = new DefaultErrorStrategy();


	protected TokenStream _input;

	protected final IntegerStack _precedenceStack;
	{
		_precedenceStack = new IntegerStack();
		_precedenceStack.push(0);
	}


	protected ParserRuleContext _ctx;


	protected boolean _buildParseTrees = true;


	protected List<ParseTreeListener> _parseListeners;


	protected int _syntaxErrors;


	protected boolean matchedEOF;

	public Parser(TokenStream input) {
		setInputStream(input);
	}


	public void reset() {
		if ( getInputStream()!=null ) getInputStream().seek(0);
		_errHandler.reset(this);
		_ctx = null;
		_syntaxErrors = 0;
		matchedEOF = false;
//		setTrace(false);
		_precedenceStack.clear();
		_precedenceStack.push(0);
		ATNSimulator interpreter = getInterpreter();
		if (interpreter != null) {
			interpreter.reset();
		}
	}


	public Token match(int ttype) throws RecognitionException {
		Token t = getCurrentToken();
		if ( t.getType()==ttype ) {
			if ( ttype==Token.EOF ) {
				matchedEOF = true;
			}
			_errHandler.reportMatch(this);
			consume();
		}
		else {
			t = _errHandler.recoverInline(this);
			if ( _buildParseTrees && t.getTokenIndex()==-1 ) {
				// we must have conjured up a new token during single token insertion
				// if it's not the current symbol
				_ctx.addErrorNode(createErrorNode(_ctx,t));
			}
		}
		return t;
	}

	
	public Token matchWildcard() throws RecognitionException {
		Token t = getCurrentToken();
		if (t.getType() > 0) {
			_errHandler.reportMatch(this);
			consume();
		}
		else {
			t = _errHandler.recoverInline(this);
			if (_buildParseTrees && t.getTokenIndex() == -1) {
				// we must have conjured up a new token during single token insertion
				// if it's not the current symbol
				_ctx.addErrorNode(createErrorNode(_ctx,t));
			}
		}

		return t;
	}

	
	public void setBuildParseTree(boolean buildParseTrees) {
		this._buildParseTrees = buildParseTrees;
	}

	
	public boolean getBuildParseTree() {
		return _buildParseTrees;
	}


	public List<ParseTreeListener> getParseListeners() {
		List<ParseTreeListener> listeners = _parseListeners;
		if (listeners == null) {
			return Collections.emptyList();
		}

		return listeners;
	}

	
	public void addParseListener(ParseTreeListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		if (_parseListeners == null) {
			_parseListeners = new ArrayList<ParseTreeListener>();
		}

		this._parseListeners.add(listener);
	}

	
	public void removeParseListener(ParseTreeListener listener) {
		if (_parseListeners != null) {
			if (_parseListeners.remove(listener)) {
				if (_parseListeners.isEmpty()) {
					_parseListeners = null;
				}
			}
		}
	}

	
	public void removeParseListeners() {
		_parseListeners = null;
	}

	
	protected void triggerEnterRuleEvent() {
		for (ParseTreeListener listener : _parseListeners) {
			listener.enterEveryRule(_ctx);
			_ctx.enterRule(listener);
		}
	}

	
	protected void triggerExitRuleEvent() {
		// reverse order walk of listeners
		for (int i = _parseListeners.size()-1; i >= 0; i--) {
			ParseTreeListener listener = _parseListeners.get(i);
			_ctx.exitRule(listener);
			listener.exitEveryRule(_ctx);
		}
	}

	
	public int getNumberOfSyntaxErrors() {
		return _syntaxErrors;
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		return _input.getTokenSource().getTokenFactory();
	}

	
	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		_input.getTokenSource().setTokenFactory(factory);
	}

	

	public ATN getATNWithBypassAlts() {
		String serializedAtn = getSerializedATN();
		if (serializedAtn == null) {
			throw new UnsupportedOperationException("The current parser does not support an ATN with bypass alternatives.");
		}

		synchronized (this) {
			if ( bypassAltsAtnCache!=null ) {
				return bypassAltsAtnCache;
			}
			ATNDeserializationOptions deserializationOptions = new ATNDeserializationOptions();
			deserializationOptions.setGenerateRuleBypassTransitions(true);
			bypassAltsAtnCache = new ATNDeserializer(deserializationOptions).deserialize(serializedAtn.toCharArray());
			return bypassAltsAtnCache;
		}
	}


	public ANTLRErrorStrategy getErrorHandler() {
		return _errHandler;
	}

	public void setErrorHandler(ANTLRErrorStrategy handler) {
		this._errHandler = handler;
	}

	@Override
	public TokenStream getInputStream() { return getTokenStream(); }

	@Override
	public final void setInputStream(IntStream input) {
		setTokenStream((TokenStream)input);
	}

	public TokenStream getTokenStream() {
		return _input;
	}

	
	public void setTokenStream(TokenStream input) {
		this._input = null;
		reset();
		this._input = input;
	}

    

    public Token getCurrentToken() {
		return _input.LT(1);
	}

	public final void notifyErrorListeners(String msg)	{
		notifyErrorListeners(getCurrentToken(), msg, null);
	}

	public void notifyErrorListeners(Token offendingToken, String msg,
									 RecognitionException e)
	{
		_syntaxErrors++;
		int line = -1;
		int charPositionInLine = -1;
		line = offendingToken.getLine();
		charPositionInLine = offendingToken.getCharPositionInLine();

		ANTLRErrorListener listener = getErrorListenerDispatch();
		listener.syntaxError(this, offendingToken, line, charPositionInLine, msg, e);
	}

	
	public Token consume() {
		Token o = getCurrentToken();
		if (o.getType() != EOF) {
			getInputStream().consume();
		}
		boolean hasListener = _parseListeners != null && !_parseListeners.isEmpty();
		if (_buildParseTrees || hasListener) {
			if ( _errHandler.inErrorRecoveryMode(this) ) {
				ErrorNode node = _ctx.addErrorNode(createErrorNode(_ctx,o));
				if (_parseListeners != null) {
					for (ParseTreeListener listener : _parseListeners) {
						listener.visitErrorNode(node);
					}
				}
			}
			else {
				TerminalNode node = _ctx.addChild(createTerminalNode(_ctx,o));
				if (_parseListeners != null) {
					for (ParseTreeListener listener : _parseListeners) {
						listener.visitTerminal(node);
					}
				}
			}
		}
		return o;
	}

	
	public TerminalNode createTerminalNode(ParserRuleContext parent, Token t) {
		return new TerminalNodeImpl(t);
	}

	
	public ErrorNode createErrorNode(ParserRuleContext parent, Token t) {
		return new ErrorNodeImpl(t);
	}

	protected void addContextToParseTree() {
		ParserRuleContext parent = (ParserRuleContext)_ctx.parent;
		// add current context to parent if we have a parent
		if ( parent!=null )	{
			parent.addChild(_ctx);
		}
	}

	
	public void enterRule(ParserRuleContext localctx, int state, int ruleIndex) {
		setState(state);
		_ctx = localctx;
		_ctx.start = _input.LT(1);
		if (_buildParseTrees) addContextToParseTree();
        if ( _parseListeners != null) triggerEnterRuleEvent();
	}

    public void exitRule() {
		if ( matchedEOF ) {
			// if we have matched EOF, it cannot consume past EOF so we use LT(1) here
			_ctx.stop = _input.LT(1); // LT(1) will be end of file
		}
		else {
			_ctx.stop = _input.LT(-1); // stop node is what we just matched
		}
        // trigger event on _ctx, before it reverts to parent
        if ( _parseListeners != null) triggerExitRuleEvent();
		setState(_ctx.invokingState);
		_ctx = (ParserRuleContext)_ctx.parent;
    }

	public void enterOuterAlt(ParserRuleContext localctx, int altNum) {
		localctx.setAltNumber(altNum);
		// if we have new localctx, make sure we replace existing ctx
		// that is previous child of parse tree
		if ( _buildParseTrees && _ctx != localctx ) {
			ParserRuleContext parent = (ParserRuleContext)_ctx.parent;
			if ( parent!=null )	{
				parent.removeLastChild();
				parent.addChild(localctx);
			}
		}
		_ctx = localctx;
	}

	
	public final int getPrecedence() {
		if (_precedenceStack.isEmpty()) {
			return -1;
		}

		return _precedenceStack.peek();
	}


	public void enterRecursionRule(ParserRuleContext localctx, int state, int ruleIndex, int precedence) {
		setState(state);
		_precedenceStack.push(precedence);
		_ctx = localctx;
		_ctx.start = _input.LT(1);
		if (_parseListeners != null) {
			triggerEnterRuleEvent(); // simulates rule entry for left-recursive rules
		}
	}

	
	public void pushNewRecursionContext(ParserRuleContext localctx, int state, int ruleIndex) {
		ParserRuleContext previous = _ctx;
		previous.parent = localctx;
		previous.invokingState = state;
		previous.stop = _input.LT(-1);

		_ctx = localctx;
		_ctx.start = previous.start;
		if (_buildParseTrees) {
			_ctx.addChild(previous);
		}

		if ( _parseListeners != null ) {
			triggerEnterRuleEvent(); // simulates rule entry for left-recursive rules
		}
	}

	public void unrollRecursionContexts(ParserRuleContext _parentctx) {
		_precedenceStack.pop();
		_ctx.stop = _input.LT(-1);
		ParserRuleContext retctx = _ctx; // save current ctx (return value)

		// unroll so _ctx is as it was before call to recursive method
		if ( _parseListeners != null ) {
			while ( _ctx != _parentctx ) {
				triggerExitRuleEvent();
				_ctx = (ParserRuleContext)_ctx.parent;
			}
		}
		else {
			_ctx = _parentctx;
		}

		// hook into tree
		retctx.parent = _parentctx;

		if (_buildParseTrees && _parentctx != null) {
			// add return ctx into invoking rule's tree
			_parentctx.addChild(retctx);
		}
	}

	public ParserRuleContext getInvokingContext(int ruleIndex) {
		ParserRuleContext p = _ctx;
		while ( p!=null ) {
			if ( p.getRuleIndex() == ruleIndex ) return p;
			p = (ParserRuleContext)p.parent;
		}
		return null;
	}

	public ParserRuleContext getContext() {
		return _ctx;
	}

	public void setContext(ParserRuleContext ctx) {
		_ctx = ctx;
	}

	@Override
	public boolean precpred(RuleContext localctx, int precedence) {
		return precedence >= _precedenceStack.peek();
	}


	public boolean isExpectedToken(int symbol) {
//   		return getInterpreter().atn.nextTokens(_ctx);
        ATN atn = getInterpreter().atn;
		ParserRuleContext ctx = _ctx;
        ATNState s = atn.states.get(getState());
        IntervalSet following = atn.nextTokens(s);
        if (following.contains(symbol)) {
            return true;
        }
//        System.out.println("following "+s+"="+following);
        if ( !following.contains(Token.EPSILON) ) return false;

        while ( ctx!=null && ctx.invokingState>=0 && following.contains(Token.EPSILON) ) {
            ATNState invokingState = atn.states.get(ctx.invokingState);
            RuleTransition rt = (RuleTransition)invokingState.get_transition(0);
            following = atn.nextTokens(rt.followState);
            if (following.contains(symbol)) {
                return true;
            }

            ctx = (ParserRuleContext)ctx.parent;
        }

        if ( following.contains(Token.EPSILON) && symbol == Token.EOF ) {
            return true;
        }

        return false;
    }

	public boolean isMatchedEOF() {
		return matchedEOF;
	}

	
	public IntervalSet getExpectedTokens() {
		return getATN().getExpectedTokens(getState(), getContext());
	}


    public IntervalSet getExpectedTokensWithinCurrentRule() {
        ATN atn = getInterpreter().atn;
        ATNState s = atn.states.get(getState());
   		return atn.nextTokens(s);
   	}

	
	public int getRuleIndex(String ruleName) {
		Integer ruleIndex = getRuleIndexMap().get(ruleName);
		if ( ruleIndex!=null ) return ruleIndex;
		return -1;
	}

	public ParserRuleContext getRuleContext() { return _ctx; }

	
	public List<String> getRuleInvocationStack() {
		return getRuleInvocationStack(_ctx);
	}

	public List<String> getRuleInvocationStack(RuleContext p) {
		String[] ruleNames = getRuleNames();
		List<String> stack = new ArrayList<String>();
		while ( p!=null ) {
			// compute what follows who invoked us
			int ruleIndex = p.getRuleIndex();
			if ( ruleIndex<0 ) stack.add("n/a");
			else stack.add(ruleNames[ruleIndex]);
			p = p.parent;
		}
		return stack;
	}

	
	public List<String> getDFAStrings() {
		synchronized (_interp.decisionToDFA) {
			List<String> s = new ArrayList<String>();
			for (int d = 0; d < _interp.decisionToDFA.length; d++) {
				DFA dfa = _interp.decisionToDFA[d];
				s.add( dfa.toString(getVocabulary()) );
			}
			return s;
		}
    }

	public void dumpDFA() {
		dumpDFA(System.out);
	}

	
	public void dumpDFA(PrintStream dumpStream) {
		synchronized (_interp.decisionToDFA) {
			boolean seenOne = false;
			for (int d = 0; d < _interp.decisionToDFA.length; d++) {
				DFA dfa = _interp.decisionToDFA[d];
				if ( !dfa.states.isEmpty() ) {
					if ( seenOne ) dumpStream.println();
					dumpStream.println("Decision " + dfa.decision + ":");
					dumpStream.print(dfa.toString(getVocabulary()));
					seenOne = true;
				}
			}
		}
    }

	public String getSourceName() {
		return _input.getSourceName();
	}

	@Override
	public ParseInfo getParseInfo() {
		ParserATNSimulator interp = getInterpreter();
		if (interp instanceof ProfilingATNSimulator) {
			return new ParseInfo((ProfilingATNSimulator)interp);
		}
		return null;
	}


}

