

package runtime.atn;

import runtime.Parser;
import runtime.ParserRuleContext;
import runtime.TokenStream;
import runtime.dfa.DFA;
import runtime.dfa.DFAState;

import java.util.BitSet;


public class ProfilingATNSimulator extends ParserATNSimulator {
	protected final DecisionInfo[] decisions;
	protected int numDecisions;

	protected int _sllStopIndex;
	protected int _llStopIndex;

	protected int currentDecision;
	protected DFAState currentState;

 	
	protected int conflictingAltResolvedBySLL;

	public ProfilingATNSimulator(Parser parser) {
		super(parser,
				parser.getInterpreter().atn,
				parser.getInterpreter().decisionToDFA,
				parser.getInterpreter().sharedContextCache);
		numDecisions = atn.decisionToState.size();
		decisions = new DecisionInfo[numDecisions];
		for (int i=0; i<numDecisions; i++) {
			decisions[i] = new DecisionInfo(i);
		}
	}

	@Override
	public int adaptivePredict(TokenStream input, int decision, ParserRuleContext outerContext) {
		try {
			this._sllStopIndex = -1;
			this._llStopIndex = -1;
			this.currentDecision = decision;
			long start = System.nanoTime();
			int alt = super.adaptivePredict(input, decision, outerContext);
			long stop = System.nanoTime();
			decisions[decision].timeInPrediction += (stop-start);
			decisions[decision].invocations++;

			int SLL_k = _sllStopIndex - _startIndex + 1;
			decisions[decision].SLL_TotalLook += SLL_k;
			decisions[decision].SLL_MinLook = decisions[decision].SLL_MinLook==0 ? SLL_k : Math.min(decisions[decision].SLL_MinLook, SLL_k);
			if ( SLL_k > decisions[decision].SLL_MaxLook ) {
				decisions[decision].SLL_MaxLook = SLL_k;
				decisions[decision].SLL_MaxLookEvent =
						new LookaheadEventInfo(decision, null, alt, input, _startIndex, _sllStopIndex, false);
			}

			if (_llStopIndex >= 0) {
				int LL_k = _llStopIndex - _startIndex + 1;
				decisions[decision].LL_TotalLook += LL_k;
				decisions[decision].LL_MinLook = decisions[decision].LL_MinLook==0 ? LL_k : Math.min(decisions[decision].LL_MinLook, LL_k);
				if ( LL_k > decisions[decision].LL_MaxLook ) {
					decisions[decision].LL_MaxLook = LL_k;
					decisions[decision].LL_MaxLookEvent =
							new LookaheadEventInfo(decision, null, alt, input, _startIndex, _llStopIndex, true);
				}
			}

			return alt;
		}
		finally {
			this.currentDecision = -1;
		}
	}

	@Override
	protected DFAState getExistingTargetState(DFAState previousD, int t) {


		_sllStopIndex = _input.index();

		DFAState existingTargetState = super.getExistingTargetState(previousD, t);
		if ( existingTargetState!=null ) {
			decisions[currentDecision].SLL_DFATransitions++;
			if ( existingTargetState==ERROR ) {
				decisions[currentDecision].errors.add(
						new ErrorInfo(currentDecision, previousD.configs, _input, _startIndex, _sllStopIndex, false)
				);
			}
		}

		currentState = existingTargetState;
		return existingTargetState;
	}

	@Override
	protected DFAState computeTargetState(DFA dfa, DFAState previousD, int t) {
		DFAState state = super.computeTargetState(dfa, previousD, t);
		currentState = state;
		return state;
	}

	@Override
	protected ATNConfigSet computeReachSet(ATNConfigSet closure, int t, boolean fullCtx) {
		if (fullCtx) {


			_llStopIndex = _input.index();
		}

		ATNConfigSet reachConfigs = super.computeReachSet(closure, t, fullCtx);
		if (fullCtx) {
			decisions[currentDecision].LL_ATNTransitions++;
			if ( reachConfigs!=null ) {
			}
			else {

				decisions[currentDecision].errors.add(
					new ErrorInfo(currentDecision, closure, _input, _startIndex, _llStopIndex, true)
				);
			}
		}
		else {
			decisions[currentDecision].SLL_ATNTransitions++;
			if ( reachConfigs!=null ) {
			}
			else {
				decisions[currentDecision].errors.add(
					new ErrorInfo(currentDecision, closure, _input, _startIndex, _sllStopIndex, false)
				);
			}
		}
		return reachConfigs;
	}

	@Override
	protected boolean evalSemanticContext(SemanticContext pred, ParserRuleContext parserCallStack, int alt, boolean fullCtx) {
		boolean result = super.evalSemanticContext(pred, parserCallStack, alt, fullCtx);
		if (!(pred instanceof SemanticContext.PrecedencePredicate)) {
			boolean fullContext = _llStopIndex >= 0;
			int stopIndex = fullContext ? _llStopIndex : _sllStopIndex;
			decisions[currentDecision].predicateEvals.add(
				new PredicateEvalInfo(currentDecision, _input, _startIndex, stopIndex, pred, result, alt, fullCtx)
			);
		}

		return result;
	}

	@Override
	protected void reportAttemptingFullContext(DFA dfa, BitSet conflictingAlts, ATNConfigSet configs, int startIndex, int stopIndex) {
		if ( conflictingAlts!=null ) {
			conflictingAltResolvedBySLL = conflictingAlts.nextSetBit(0);
		}
		else {
			conflictingAltResolvedBySLL = configs.getAlts().nextSetBit(0);
		}
		decisions[currentDecision].LL_Fallback++;
		super.reportAttemptingFullContext(dfa, conflictingAlts, configs, startIndex, stopIndex);
	}

	@Override
	protected void reportContextSensitivity(DFA dfa, int prediction, ATNConfigSet configs, int startIndex, int stopIndex) {
		if ( prediction != conflictingAltResolvedBySLL ) {
			decisions[currentDecision].contextSensitivities.add(
					new ContextSensitivityInfo(currentDecision, configs, _input, startIndex, stopIndex)
			);
		}
		super.reportContextSensitivity(dfa, prediction, configs, startIndex, stopIndex);
	}

	@Override
	protected void reportAmbiguity(DFA dfa, DFAState D, int startIndex, int stopIndex, boolean exact,
								   BitSet ambigAlts, ATNConfigSet configs)
	{
		int prediction;
		if ( ambigAlts!=null ) {
			prediction = ambigAlts.nextSetBit(0);
		}
		else {
			prediction = configs.getAlts().nextSetBit(0);
		}
		if ( configs.fullCtx && prediction != conflictingAltResolvedBySLL ) {





			decisions[currentDecision].contextSensitivities.add(
					new ContextSensitivityInfo(currentDecision, configs, _input, startIndex, stopIndex)
			);
		}
		decisions[currentDecision].ambiguities.add(
			new AmbiguityInfo(currentDecision, configs, ambigAlts,
							  _input, startIndex, stopIndex, configs.fullCtx)
		);
		super.reportAmbiguity(dfa, D, startIndex, stopIndex, exact, ambigAlts, configs);
	}



	public DecisionInfo[] getDecisionInfo() {
		return decisions;
	}

	public DFAState getCurrentState() {
		return currentState;
	}
}
