

package runtime.atn;

import runtime.CharStream;
import runtime.IntStream;
import runtime.Lexer;
import runtime.LexerNoViableAltException;
import runtime.Token;
import runtime.dfa.DFA;
import runtime.dfa.DFAState;
import runtime.misc.Interval;

import java.util.Locale;


public class LexerATNSimulator extends ATNSimulator {
	public static final boolean debug = false;
	public static final boolean dfa_debug = false;

	public static final int MIN_DFA_EDGE = 0;
	public static final int MAX_DFA_EDGE = 127;

	
	protected static class SimState {
		protected int index = -1;
		protected int line = 0;
		protected int charPos = -1;
		protected DFAState dfaState;

		protected void reset() {
			index = -1;
			line = 0;
			charPos = -1;
			dfaState = null;
		}
	}


	protected final Lexer recog;

	
	protected int startIndex = -1;

	
	protected int line = 1;

	
	protected int charPositionInLine = 0;


	public final DFA[] decisionToDFA;
	protected int mode = Lexer.DEFAULT_MODE;

	

	protected final SimState prevAccept = new SimState();

	public LexerATNSimulator(ATN atn, DFA[] decisionToDFA,
							 PredictionContextCache sharedContextCache)
	{
		this(null, atn, decisionToDFA,sharedContextCache);
	}

	public LexerATNSimulator(Lexer recog, ATN atn,
							 DFA[] decisionToDFA,
							 PredictionContextCache sharedContextCache)
	{
		super(atn,sharedContextCache);
		this.decisionToDFA = decisionToDFA;
		this.recog = recog;
	}

	public void copyState(LexerATNSimulator simulator) {
		this.charPositionInLine = simulator.charPositionInLine;
		this.line = simulator.line;
		this.mode = simulator.mode;
		this.startIndex = simulator.startIndex;
	}

	public int match(CharStream input, int mode) {
		this.mode = mode;
		int mark = input.mark();
		try {
			this.startIndex = input.index();
			this.prevAccept.reset();
			DFA dfa = decisionToDFA[mode];
			if ( dfa.s0==null ) {
				return matchATN(input);
			}
			else {
				return execATN(input, dfa.s0);
			}
		}
		finally {
			input.release(mark);
		}
	}

	@Override
	public void reset() {
		prevAccept.reset();
		startIndex = -1;
		line = 1;
		charPositionInLine = 0;
		mode = Lexer.DEFAULT_MODE;
	}

	@Override
	public void clearDFA() {
		for (int d = 0; d < decisionToDFA.length; d++) {
			decisionToDFA[d] = new DFA(atn.getDecisionState(d), d);
		}
	}

	protected int matchATN(CharStream input) {
		ATNState startState = atn.modeToStartState.get(mode);

		if ( debug ) {
			System.out.format(Locale.getDefault(), "matchATN mode %d start: %s\n", mode, startState);
		}

		int old_mode = mode;

		ATNConfigSet s0_closure = computeStartState(input, startState);
		boolean suppressEdge = s0_closure.hasSemanticContext;
		s0_closure.hasSemanticContext = false;

		DFAState next = addDFAState(s0_closure);
		if (!suppressEdge) {
			decisionToDFA[mode].s0 = next;
		}

		int predict = execATN(input, next);

		if ( debug ) {
			System.out.format(Locale.getDefault(), "DFA after matchATN: %s\n", decisionToDFA[old_mode].toLexerString());
		}

		return predict;
	}

	protected int execATN(CharStream input, DFAState ds0) {

		if ( debug ) {
			System.out.format(Locale.getDefault(), "start state closure=%s\n", ds0.configs);
		}

		if (ds0.isAcceptState) {

			captureSimState(prevAccept, input, ds0);
		}

		int t = input.LA(1);

		DFAState s = ds0;

		while ( true ) {
			if ( debug ) {
				System.out.format(Locale.getDefault(), "execATN loop starting closure: %s\n", s.configs);
			}


















			DFAState target = getExistingTargetState(s, t);
			if (target == null) {
				target = computeTargetState(input, s, t);
			}

			if (target == ERROR) {
				break;
			}





			if (t != IntStream.EOF) {
				consume(input);
			}

			if (target.isAcceptState) {
				captureSimState(prevAccept, input, target);
				if (t == IntStream.EOF) {
					break;
				}
			}

			t = input.LA(1);
			s = target;
		}

		return failOrAccept(prevAccept, input, s.configs, t);
	}

	

	protected DFAState getExistingTargetState(DFAState s, int t) {
		if (s.edges == null || t < MIN_DFA_EDGE || t > MAX_DFA_EDGE) {
			return null;
		}

		DFAState target = s.edges[t - MIN_DFA_EDGE];
		if (debug && target != null) {
			System.out.println("reuse state "+s.stateNumber+
							   " edge to "+target.stateNumber);
		}

		return target;
	}

	

	protected DFAState computeTargetState(CharStream input, DFAState s, int t) {
		ATNConfigSet reach = new OrderedATNConfigSet();



		getReachableConfigSet(input, s.configs, reach, t);

		if ( reach.isEmpty() ) {
			if (!reach.hasSemanticContext) {


				addDFAEdge(s, t, ERROR);
			}


			return ERROR;
		}


		return addDFAEdge(s, t, reach);
	}

	protected int failOrAccept(SimState prevAccept, CharStream input,
							   ATNConfigSet reach, int t)
	{
		if (prevAccept.dfaState != null) {
			LexerActionExecutor lexerActionExecutor = prevAccept.dfaState.lexerActionExecutor;
			accept(input, lexerActionExecutor, startIndex,
				prevAccept.index, prevAccept.line, prevAccept.charPos);
			return prevAccept.dfaState.prediction;
		}
		else {

			if ( t==IntStream.EOF && input.index()==startIndex ) {
				return Token.EOF;
			}

			throw new LexerNoViableAltException(recog, input, startIndex, reach);
		}
	}

	
	protected void getReachableConfigSet(CharStream input, ATNConfigSet closure, ATNConfigSet reach, int t) {


		int skipAlt = ATN.INVALID_ALT_NUMBER;
		for (ATNConfig c : closure) {
			boolean currentAltReachedAcceptState = c.alt == skipAlt;
			if (currentAltReachedAcceptState && ((LexerATNConfig)c).hasPassedThroughNonGreedyDecision()) {
				continue;
			}

			if ( debug ) {
				System.out.format(Locale.getDefault(), "testing %s at %s\n", getTokenName(t), c.toString(recog, true));
			}

			int n = c.state.getNumberOfTransitions();
			for (int ti=0; ti<n; ti++) {
				Transition trans = c.state.transition(ti);
				ATNState target = getReachableTarget(trans, t);
				if ( target!=null ) {
					LexerActionExecutor lexerActionExecutor = ((LexerATNConfig)c).getLexerActionExecutor();
					if (lexerActionExecutor != null) {
						lexerActionExecutor = lexerActionExecutor.fixOffsetBeforeMatch(input.index() - startIndex);
					}

					boolean treatEofAsEpsilon = t == CharStream.EOF;
					if (closure(input, new LexerATNConfig((LexerATNConfig)c, target, lexerActionExecutor), reach, currentAltReachedAcceptState, true, treatEofAsEpsilon)) {


						skipAlt = c.alt;
						break;
					}
				}
			}
		}
	}

	protected void accept(CharStream input, LexerActionExecutor lexerActionExecutor,
						  int startIndex, int index, int line, int charPos)
	{
		if ( debug ) {
			System.out.format(Locale.getDefault(), "ACTION %s\n", lexerActionExecutor);
		}


		input.seek(index);
		this.line = line;
		this.charPositionInLine = charPos;

		if (lexerActionExecutor != null && recog != null) {
			lexerActionExecutor.execute(recog, input, startIndex);
		}
	}


	protected ATNState getReachableTarget(Transition trans, int t) {
		if (trans.matches(t, Lexer.MIN_CHAR_VALUE, Lexer.MAX_CHAR_VALUE)) {
			return trans.target;
		}

		return null;
	}


	protected ATNConfigSet computeStartState(CharStream input,
											 ATNState p)
	{
		PredictionContext initialContext = EmptyPredictionContext.Instance;
		ATNConfigSet configs = new OrderedATNConfigSet();
		for (int i=0; i<p.getNumberOfTransitions(); i++) {
			ATNState target = p.transition(i).target;
			LexerATNConfig c = new LexerATNConfig(target, i+1, initialContext);
			closure(input, c, configs, false, false, false);
		}
		return configs;
	}

	
	protected boolean closure(CharStream input, LexerATNConfig config, ATNConfigSet configs, boolean currentAltReachedAcceptState, boolean speculative, boolean treatEofAsEpsilon) {
		if ( debug ) {
			System.out.println("closure("+config.toString(recog, true)+")");
		}

		if ( config.state instanceof RuleStopState ) {
			if ( debug ) {
				if ( recog!=null ) {
					System.out.format(Locale.getDefault(), "closure at %s rule stop %s\n", recog.getRuleNames()[config.state.ruleIndex], config);
				}
				else {
					System.out.format(Locale.getDefault(), "closure at rule stop %s\n", config);
				}
			}

			if ( config.context == null || config.context.hasEmptyPath() ) {
				if (config.context == null || config.context.isEmpty()) {
					configs.add(config);
					return true;
				}
				else {
					configs.add(new LexerATNConfig(config, config.state, EmptyPredictionContext.Instance));
					currentAltReachedAcceptState = true;
				}
			}

			if ( config.context!=null && !config.context.isEmpty() ) {
				for (int i = 0; i < config.context.size(); i++) {
					if (config.context.getReturnState(i) != PredictionContext.EMPTY_RETURN_STATE) {
						PredictionContext newContext = config.context.getParent(i);
						ATNState returnState = atn.states.get(config.context.getReturnState(i));
						LexerATNConfig c = new LexerATNConfig(config, returnState, newContext);
						currentAltReachedAcceptState = closure(input, c, configs, currentAltReachedAcceptState, speculative, treatEofAsEpsilon);
					}
				}
			}

			return currentAltReachedAcceptState;
		}


		if ( !config.state.onlyHasEpsilonTransitions() ) {
			if (!currentAltReachedAcceptState || !config.hasPassedThroughNonGreedyDecision()) {
				configs.add(config);
			}
		}

		ATNState p = config.state;
		for (int i=0; i<p.getNumberOfTransitions(); i++) {
			Transition t = p.transition(i);
			LexerATNConfig c = getEpsilonTarget(input, config, t, configs, speculative, treatEofAsEpsilon);
			if ( c!=null ) {
				currentAltReachedAcceptState = closure(input, c, configs, currentAltReachedAcceptState, speculative, treatEofAsEpsilon);
			}
		}

		return currentAltReachedAcceptState;
	}



	protected LexerATNConfig getEpsilonTarget(CharStream input,
										   LexerATNConfig config,
										   Transition t,
										   ATNConfigSet configs,
										   boolean speculative,
										   boolean treatEofAsEpsilon)
	{
		LexerATNConfig c = null;
		switch (t.getSerializationType()) {
			case Transition.RULE:
				RuleTransition ruleTransition = (RuleTransition)t;
				PredictionContext newContext =
					SingletonPredictionContext.create(config.context, ruleTransition.followState.stateNumber);
				c = new LexerATNConfig(config, t.target, newContext);
				break;

			case Transition.PRECEDENCE:
				throw new UnsupportedOperationException("Precedence predicates are not supported in lexers.");

			case Transition.PREDICATE:
				
				PredicateTransition pt = (PredicateTransition)t;
				if ( debug ) {
					System.out.println("EVAL rule "+pt.ruleIndex+":"+pt.predIndex);
				}
				configs.hasSemanticContext = true;
				if (evaluatePredicate(input, pt.ruleIndex, pt.predIndex, speculative)) {
					c = new LexerATNConfig(config, t.target);
				}
				break;

			case Transition.ACTION:
				if (config.context == null || config.context.hasEmptyPath()) {












					LexerActionExecutor lexerActionExecutor = LexerActionExecutor.append(config.getLexerActionExecutor(), atn.lexerActions[((ActionTransition)t).actionIndex]);
					c = new LexerATNConfig(config, t.target, lexerActionExecutor);
					break;
				}
				else {

					c = new LexerATNConfig(config, t.target);
					break;
				}

			case Transition.EPSILON:
				c = new LexerATNConfig(config, t.target);
				break;

			case Transition.ATOM:
			case Transition.RANGE:
			case Transition.SET:
				if (treatEofAsEpsilon) {
					if (t.matches(CharStream.EOF, Lexer.MIN_CHAR_VALUE, Lexer.MAX_CHAR_VALUE)) {
						c = new LexerATNConfig(config, t.target);
						break;
					}
				}

				break;
		}

		return c;
	}

	
	protected boolean evaluatePredicate(CharStream input, int ruleIndex, int predIndex, boolean speculative) {

		if (recog == null) {
			return true;
		}

		if (!speculative) {
			return recog.sempred(null, ruleIndex, predIndex);
		}

		int savedCharPositionInLine = charPositionInLine;
		int savedLine = line;
		int index = input.index();
		int marker = input.mark();
		try {
			consume(input);
			return recog.sempred(null, ruleIndex, predIndex);
		}
		finally {
			charPositionInLine = savedCharPositionInLine;
			line = savedLine;
			input.seek(index);
			input.release(marker);
		}
	}

	protected void captureSimState(SimState settings,
								   CharStream input,
								   DFAState dfaState)
	{
		settings.index = input.index();
		settings.line = line;
		settings.charPos = charPositionInLine;
		settings.dfaState = dfaState;
	}


	protected DFAState addDFAEdge(DFAState from,
								  int t,
								  ATNConfigSet q)
	{
		
		boolean suppressEdge = q.hasSemanticContext;
		q.hasSemanticContext = false;


		DFAState to = addDFAState(q);

		if (suppressEdge) {
			return to;
		}

		addDFAEdge(from, t, to);
		return to;
	}

	protected void addDFAEdge(DFAState p, int t, DFAState q) {
		if (t < MIN_DFA_EDGE || t > MAX_DFA_EDGE) {

			return;
		}

		if ( debug ) {
			System.out.println("EDGE "+p+" -> "+q+" upon "+((char)t));
		}

		synchronized (p) {
			if ( p.edges==null ) {

				p.edges = new DFAState[MAX_DFA_EDGE-MIN_DFA_EDGE+1];
			}
			p.edges[t - MIN_DFA_EDGE] = q;
		}
	}

	

	protected DFAState addDFAState(ATNConfigSet configs) {
		
		assert !configs.hasSemanticContext;

		DFAState proposed = new DFAState(configs);
		ATNConfig firstConfigWithRuleStopState = null;
		for (ATNConfig c : configs) {
			if ( c.state instanceof RuleStopState )	{
				firstConfigWithRuleStopState = c;
				break;
			}
		}

		if ( firstConfigWithRuleStopState!=null ) {
			proposed.isAcceptState = true;
			proposed.lexerActionExecutor = ((LexerATNConfig)firstConfigWithRuleStopState).getLexerActionExecutor();
			proposed.prediction = atn.ruleToTokenType[firstConfigWithRuleStopState.state.ruleIndex];
		}

		DFA dfa = decisionToDFA[mode];
		synchronized (dfa.states) {
			DFAState existing = dfa.states.get(proposed);
			if ( existing!=null ) return existing;

			DFAState newState = proposed;

			newState.stateNumber = dfa.states.size();
			configs.setReadonly(true);
			newState.configs = configs;
			dfa.states.put(newState, newState);
			return newState;
		}
	}


	public final DFA getDFA(int mode) {
		return decisionToDFA[mode];
	}

	

	public String getText(CharStream input) {

		return input.getText(Interval.of(startIndex, input.index()-1));
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getCharPositionInLine() {
		return charPositionInLine;
	}

	public void setCharPositionInLine(int charPositionInLine) {
		this.charPositionInLine = charPositionInLine;
	}

	public void consume(CharStream input) {
		int curChar = input.LA(1);
		if ( curChar=='\n' ) {
			line++;
			charPositionInLine=0;
		}
		else {
			charPositionInLine++;
		}
		input.consume();
	}


	public String getTokenName(int t) {
		if ( t==-1 ) return "EOF";

		return "'"+(char)t+"'";
	}
}
