

package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNState;
import runtime.atn.ActionTransition;
import runtime.atn.AtomTransition;
import runtime.atn.DecisionState;
import runtime.atn.LoopEndState;
import runtime.atn.ParserATNSimulator;
import runtime.atn.PrecedencePredicateTransition;
import runtime.atn.PredicateTransition;
import runtime.atn.PredictionContextCache;
import runtime.atn.RuleStartState;
import runtime.atn.RuleTransition;
import runtime.atn.StarLoopEntryState;
import runtime.atn.Transition;
import runtime.dfa.DFA;
import runtime.misc.Pair;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;


public class ParserInterpreter extends Parser {
	protected final String grammarFileName;
	protected final ATN atn;

	protected final DFA[] decisionToDFA; // not shared like it is for generated parsers
	protected final PredictionContextCache sharedContextCache = new PredictionContextCache();

	protected final String[] ruleNames;

	private final Vocabulary vocabulary;

	
	protected final Deque<Pair<ParserRuleContext, Integer>> _parentContextStack =
		new ArrayDeque<Pair<ParserRuleContext, Integer>>();

	
	protected int overrideDecision = -1;
	protected int overrideDecisionInputIndex = -1;
	protected int overrideDecisionAlt = -1;
	protected boolean overrideDecisionReached = false; // latch and only override once; error might trigger infinite loop

	
	protected InterpreterRuleContext overrideDecisionRoot = null;


	protected InterpreterRuleContext rootContext;

	public ParserInterpreter(String grammarFileName, Vocabulary vocabulary,
							 Collection<String> ruleNames, ATN atn, TokenStream input)
	{
		super(input);
		this.grammarFileName = grammarFileName;
		this.atn = atn;

		this.ruleNames = ruleNames.toArray(new String[0]);
		this.vocabulary = vocabulary;

		int numberOfDecisions = atn.getNumberOfDecisions();
		this.decisionToDFA = new DFA[numberOfDecisions];
		for (int i = 0; i < numberOfDecisions; i++) {
			DecisionState decisionState = atn.getDecisionState(i);
			decisionToDFA[i] = new DFA(decisionState, i);
		}

		setInterpreter(new ParserATNSimulator(this, atn,
											  decisionToDFA,
											  sharedContextCache));
	}

	@Override
	public void reset() {
		super.reset();
		overrideDecisionReached = false;
		overrideDecisionRoot = null;
	}

	@Override
	public ATN getATN() {
		return atn;
	}

	@Override
	public Vocabulary getVocabulary() {
		return vocabulary;
	}

	@Override
	public String[] getRuleNames() {
		return ruleNames;
	}


	public ParserRuleContext parse(int startRuleIndex) {
		RuleStartState startRuleStartState = atn.ruleToStartState[startRuleIndex];

		rootContext = createInterpreterRuleContext(null, ATNState.INVALID_STATE_NUMBER, startRuleIndex);
		if (startRuleStartState.isLeftRecursiveRule) {
			enterRecursionRule(rootContext, startRuleStartState.stateNumber, startRuleIndex, 0);
		}
		else {
			enterRule(rootContext, startRuleStartState.stateNumber, startRuleIndex);
		}

		while ( true ) {
			ATNState p = getATNState();
			switch ( p.getStateType() ) {
			case ATNState.RULE_STOP :
				// pop; return from rule
				if ( _ctx.isEmpty() ) {
					if (startRuleStartState.isLeftRecursiveRule) {
						ParserRuleContext result = _ctx;
						Pair<ParserRuleContext, Integer> parentContext = _parentContextStack.pop();
						unrollRecursionContexts(parentContext.a);
						return result;
					}
					else {
						exitRule();
						return rootContext;
					}
				}

				visitRuleStopState(p);
				break;

			default :
				try {
					visitState(p);
				}
				catch (RecognitionException e) {
					setState(atn.ruleToStopState[p.ruleIndex].stateNumber);
					getContext().exception = e;
					getErrorHandler().reportError(this, e);
					recover(e);
				}

				break;
			}
		}
	}

	@Override
	public void enterRecursionRule(ParserRuleContext localctx, int state, int ruleIndex, int precedence) {
		Pair<ParserRuleContext, Integer> pair = new Pair<ParserRuleContext, Integer>(_ctx, localctx.invokingState);
		_parentContextStack.push(pair);
		super.enterRecursionRule(localctx, state, ruleIndex, precedence);
	}

	protected ATNState getATNState() {
		return atn.states.get(getState());
	}

	protected void visitState(ATNState p) {
//		System.out.println("visitState "+p.stateNumber);
		int predictedAlt = 1;
		if ( p instanceof DecisionState ) {
			predictedAlt = visitDecisionState((DecisionState) p);
		}

		Transition transition = p.get_transition(predictedAlt - 1);
		switch (transition.getSerializationType()) {
			case Transition.EPSILON:
				if ( p.getStateType()==ATNState.STAR_LOOP_ENTRY &&
					 ((StarLoopEntryState)p).isPrecedenceDecision &&
					 !(transition.target instanceof LoopEndState))
				{
					// We are at the start of a left recursive rule's (...)* loop
					// and we're not taking the exit branch of loop.
					InterpreterRuleContext localctx =
						createInterpreterRuleContext(_parentContextStack.peek().a,
													 _parentContextStack.peek().b,
													 _ctx.getRuleIndex());
					pushNewRecursionContext(localctx,
											atn.ruleToStartState[p.ruleIndex].stateNumber,
											_ctx.getRuleIndex());
				}
				break;

			case Transition.ATOM:
				match(((AtomTransition)transition).atom_label);
				break;

			case Transition.RANGE:
			case Transition.SET:
			case Transition.NOT_SET:
				if (!transition.matches(_input.LA(1), Token.MIN_USER_TOKEN_TYPE, 65535)) {
					recoverInline();
				}
				matchWildcard();
				break;

			case Transition.WILDCARD:
				matchWildcard();
				break;

			case Transition.RULE:
				RuleStartState ruleStartState = (RuleStartState)transition.target;
				int ruleIndex = ruleStartState.ruleIndex;
				InterpreterRuleContext newctx = createInterpreterRuleContext(_ctx, p.stateNumber, ruleIndex);
				if (ruleStartState.isLeftRecursiveRule) {
					enterRecursionRule(newctx, ruleStartState.stateNumber, ruleIndex, ((RuleTransition)transition).precedence);
				}
				else {
					enterRule(newctx, transition.target.stateNumber, ruleIndex);
				}
				break;

			case Transition.PREDICATE:
				PredicateTransition predicateTransition = (PredicateTransition)transition;
				if (!sempred(_ctx, predicateTransition.ruleIndex, predicateTransition.predIndex)) {
					throw new FailedPredicateException(this);
				}

				break;

			case Transition.ACTION:
				ActionTransition actionTransition = (ActionTransition)transition;
				action(_ctx, actionTransition.ruleIndex, actionTransition.actionIndex);
				break;

			case Transition.PRECEDENCE:
				if (!precpred(_ctx, ((PrecedencePredicateTransition)transition).precedence)) {
					throw new FailedPredicateException(this, String.format("precpred(_ctx, %d)", ((PrecedencePredicateTransition)transition).precedence));
				}
				break;

			default:
				throw new UnsupportedOperationException("Unrecognized ATN transition type.");
		}

		setState(transition.target.stateNumber);
	}

	
	protected int visitDecisionState(DecisionState p) {
		int predictedAlt = 1;
		if ( p.getNumberOfTransitions()>1 ) {
			getErrorHandler().sync(this);
			int decision = p.decision;
			if ( decision == overrideDecision && _input.index() == overrideDecisionInputIndex &&
			     !overrideDecisionReached )
			{
				predictedAlt = overrideDecisionAlt;
				overrideDecisionReached = true;
			}
			else {
				predictedAlt = getInterpreter().adaptivePredict(_input, decision, _ctx);
			}
		}
		return predictedAlt;
	}

	
	protected InterpreterRuleContext createInterpreterRuleContext(
		ParserRuleContext parent,
		int invokingStateNumber,
		int ruleIndex)
	{
		return new InterpreterRuleContext(parent, invokingStateNumber, ruleIndex);
	}

	protected void visitRuleStopState(ATNState p) {
		RuleStartState ruleStartState = atn.ruleToStartState[p.ruleIndex];
		if (ruleStartState.isLeftRecursiveRule) {
			Pair<ParserRuleContext, Integer> parentContext = _parentContextStack.pop();
			unrollRecursionContexts(parentContext.a);
			setState(parentContext.b);
		}
		else {
			exitRule();
		}

		RuleTransition ruleTransition = (RuleTransition)atn.states.get(getState()).get_transition(0);
		setState(ruleTransition.followState.stateNumber);
	}

	
	public void addDecisionOverride(int decision, int tokenIndex, int forcedAlt) {
		overrideDecision = decision;
		overrideDecisionInputIndex = tokenIndex;
		overrideDecisionAlt = forcedAlt;
	}

	public InterpreterRuleContext getOverrideDecisionRoot() {
		return overrideDecisionRoot;
	}

	
	protected void recover(RecognitionException e) {
		int i = _input.index();
		getErrorHandler().recover(this, e);
		if ( _input.index()==i ) {
			// no input consumed, better add an error node
			if ( e instanceof InputMismatchException ) {
				InputMismatchException ime = (InputMismatchException)e;
				Token tok = e.getOffendingToken();
				int expectedTokenType = Token.INVALID_TYPE;
				if ( !ime.getExpectedTokens().isNil() ) {
					expectedTokenType = ime.getExpectedTokens().getMinElement(); // get any element
				}
				Token errToken =
					getTokenFactory().create(new Pair<TokenSource, CharStream>(tok.getTokenSource(), tok.getTokenSource().getInputStream()),
				                             expectedTokenType, tok.getText(),
				                             Token.DEFAULT_CHANNEL,
				                            -1, -1, // invalid start/stop
				                             tok.getLine(), tok.getCharPositionInLine());
				_ctx.addErrorNode(createErrorNode(_ctx,errToken));
			}
			else { // NoViableAlt
				Token tok = e.getOffendingToken();
				Token errToken =
					getTokenFactory().create(new Pair<TokenSource, CharStream>(tok.getTokenSource(), tok.getTokenSource().getInputStream()),
				                             Token.INVALID_TYPE, tok.getText(),
				                             Token.DEFAULT_CHANNEL,
				                            -1, -1, // invalid start/stop
				                             tok.getLine(), tok.getCharPositionInLine());
				_ctx.addErrorNode(createErrorNode(_ctx,errToken));
			}
		}
	}

	protected Token recoverInline() {
		return _errHandler.recoverInline(this);
	}

	
	public InterpreterRuleContext getRootContext() {
		return rootContext;
	}
}

