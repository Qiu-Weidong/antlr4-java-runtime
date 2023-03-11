

package runtime.atn;

import runtime.*;
import runtime.dfa.DFA;
import runtime.dfa.DFAState;
import runtime.misc.DoubleKeyMap;
import runtime.misc.Interval;
import runtime.misc.IntervalSet;
import runtime.misc.Pair;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import static runtime.atn.ATNState.BLOCK_END;

public class ParserATNSimulator extends ATNSimulator {
	public static final boolean debug = false;
	public static final boolean debug_list_atn_decisions = false;
	public static final boolean dfa_debug = false;
	public static final boolean retry_debug = false;

	/** Just in case this optimization is bad, add an ENV variable to turn it off */
	public static final boolean TURN_OFF_LR_LOOP_ENTRY_BRANCH_OPT = Boolean.parseBoolean(getSafeEnv("TURN_OFF_LR_LOOP_ENTRY_BRANCH_OPT"));

	protected final Parser parser;

	public final DFA[] decisionToDFA;

	/** SLL, LL, or LL + exact ambig detection? */

	private PredictionMode mode = PredictionMode.LL;

	/** Each prediction operation uses a cache for merge of prediction contexts.
	 *  Don't keep around as it wastes huge amounts of memory. DoubleKeyMap
	 *  isn't synchronized but we're ok since two threads shouldn't reuse same
	 *  parser/atnsim object because it can only handle one input at a time.
	 *  This maps graphs a and b to merged result c. (a,b)&rarr;c. We can avoid
	 *  the merge if we ever see a and b again.  Note that (b,a)&rarr;c should
	 *  also be examined during cache lookup.
	 */
	protected DoubleKeyMap<PredictionContext,PredictionContext,PredictionContext> mergeCache;

	// LAME globals to avoid parameters!!!!! I need these down deep in predTransition
	protected TokenStream _input;
	protected int _startIndex;
	protected ParserRuleContext _outerContext;
	protected DFA _dfa;

	/** Testing only! */
	public ParserATNSimulator(ATN atn, DFA[] decisionToDFA,
							  PredictionContextCache sharedContextCache)
	{
		this(null, atn, decisionToDFA, sharedContextCache);
	}

	public ParserATNSimulator(Parser parser, ATN atn,
							  DFA[] decisionToDFA,
							  PredictionContextCache sharedContextCache)
	{
		super(atn, sharedContextCache);
		this.parser = parser;
		this.decisionToDFA = decisionToDFA;
		//		DOTGenerator dot = new DOTGenerator(null);
		//		System.out.println(dot.getDOT(atn.rules.get(0), parser.getRuleNames()));
		//		System.out.println(dot.getDOT(atn.rules.get(1), parser.getRuleNames()));
	}

	@Override
	public void reset() {
	}

	@Override
	public void clearDFA() {
		for (int d = 0; d < decisionToDFA.length; d++) {
			decisionToDFA[d] = new DFA(atn.getDecisionState(d), d);
		}
	}

	public int adaptivePredict(TokenStream input, int decision,
							   ParserRuleContext outerContext)
	{
		if ( debug || debug_list_atn_decisions )  {
			System.out.println("adaptivePredict decision "+decision+
								   " exec LA(1)=="+ getLookaheadName(input)+
								   " line "+input.LT(1).getLine()+":"+input.LT(1).getCharPositionInLine());
		}

		_input = input;
		_startIndex = input.index();
		_outerContext = outerContext;
		DFA dfa = decisionToDFA[decision];
		_dfa = dfa;

		int m = input.mark();
		int index = _startIndex;

		// Now we are certain to have a specific decision's DFA
		// But, do we still need an initial state?
		try {
			DFAState s0;
			if (dfa.isPrecedenceDfa()) {
				// the start state for a precedence DFA depends on the current
				// parser precedence, and is provided by a DFA method.
				s0 = dfa.getPrecedenceStartState(parser.getPrecedence());
			}
			else {
				// the start state for a "regular" DFA is just s0
				s0 = dfa.s0;
			}

			if (s0 == null) {
				if ( outerContext ==null ) outerContext = ParserRuleContext.EMPTY;
				if ( debug || debug_list_atn_decisions )  {
					System.out.println("predictATN decision "+ dfa.decision+
									   " exec LA(1)=="+ getLookaheadName(input) +
									   ", outerContext="+ outerContext.toString(parser));
				}

				boolean fullCtx = false;
				ATNConfigSet s0_closure =
					computeStartState(dfa.atnStartState,
									  ParserRuleContext.EMPTY,
									  fullCtx);

				if (dfa.isPrecedenceDfa()) {
					/* If this is a precedence DFA, we use applyPrecedenceFilter
					 * to convert the computed start state to a precedence start
					 * state. We then use DFA.setPrecedenceStartState to set the
					 * appropriate start state for the precedence level rather
					 * than simply setting DFA.s0.
					 */
					dfa.s0.configs = s0_closure; // not used for prediction but useful to know start configs anyway
					s0_closure = applyPrecedenceFilter(s0_closure);
					s0 = addDFAState(dfa, new DFAState(s0_closure));
					dfa.setPrecedenceStartState(parser.getPrecedence(), s0);
				}
				else {
					s0 = addDFAState(dfa, new DFAState(s0_closure));
					dfa.s0 = s0;
				}
			}

			int alt = execATN(dfa, s0, input, index, outerContext);
			if ( debug ) System.out.println("DFA after predictATN: "+ dfa.toString(parser.getVocabulary()));
			return alt;
		}
		finally {
			mergeCache = null; // wack cache after each prediction
			_dfa = null;
			input.seek(index);
			input.release(m);
		}
	}

	/** Performs ATN simulation to compute a predicted alternative based
	 *  upon the remaining input, but also updates the DFA cache to avoid
	 *  having to traverse the ATN again for the same input sequence.

	 There are some key conditions we're looking for after computing a new
	 set of ATN configs (proposed DFA state):
	       * if the set is empty, there is no viable alternative for current symbol
	       * does the state uniquely predict an alternative?
	       * does the state have a conflict that would prevent us from
	         putting it on the work list?

	 We also have some key operations to do:
	       * add an edge from previous DFA state to potentially new DFA state, D,
	         upon current symbol but only if adding to work list, which means in all
	         cases except no viable alternative (and possibly non-greedy decisions?)
	       * collecting predicates and adding semantic context to DFA accept states
	       * adding rule context to context-sensitive DFA accept states
	       * consuming an input symbol
	       * reporting a conflict
	       * reporting an ambiguity
	       * reporting a context sensitivity
	       * reporting insufficient predicates

	 cover these cases:
	    dead end
	    single alt
	    single alt + preds
	    conflict
	    conflict + preds
	 */
	protected int execATN(DFA dfa, DFAState s0,
					   TokenStream input, int startIndex,
					   ParserRuleContext outerContext)
	{
		if ( debug || debug_list_atn_decisions) {
			System.out.println("execATN decision "+dfa.decision+
							   " exec LA(1)=="+ getLookaheadName(input)+
							   " line "+input.LT(1).getLine()+":"+input.LT(1).getCharPositionInLine());
		}

		DFAState previousD = s0;

		if ( debug ) System.out.println("s0 = "+s0);

		int t = input.LA(1);

		while (true) { // while more work
			DFAState D = getExistingTargetState(previousD, t);
			if (D == null) {
				D = computeTargetState(dfa, previousD, t);
			}

			if (D == ERROR) {
				// if any configs in previous dipped into outer context, that
				// means that input up to t actually finished entry rule
				// at least for SLL decision. Full LL doesn't dip into outer
				// so don't need special case.
				// We will get an error no matter what so delay until after
				// decision; better error message. Also, no reachable target
				// ATN states in SLL implies LL will also get nowhere.
				// If conflict in states that dip out, choose min since we
				// will get error no matter what.
				NoViableAltException e = noViableAlt(input, outerContext, previousD.configs, startIndex);
				input.seek(startIndex);
				int alt = getSynValidOrSemInvalidAltThatFinishedDecisionEntryRule(previousD.configs, outerContext);
				if ( alt!=ATN.INVALID_ALT_NUMBER ) {
					return alt;
				}
				throw e;
			}

			if ( D.requiresFullContext && mode != PredictionMode.SLL ) {
				// IF PREDS, MIGHT RESOLVE TO SINGLE ALT => SLL (or syntax error)
				BitSet conflictingAlts = D.configs.conflictingAlts;
				if ( D.predicates!=null ) {
					if ( debug ) System.out.println("DFA state has preds in DFA sim LL failover");
					int conflictIndex = input.index();
					if (conflictIndex != startIndex) {
						input.seek(startIndex);
					}

					conflictingAlts = evalSemanticContext(D.predicates, outerContext, true);
					if ( conflictingAlts.cardinality()==1 ) {
						if ( debug ) System.out.println("Full LL avoided");
						return conflictingAlts.nextSetBit(0);
					}

					if (conflictIndex != startIndex) {
						// restore the index so reporting the fallback to full
						// context occurs with the index at the correct spot
						input.seek(conflictIndex);
					}
				}

				if ( dfa_debug ) System.out.println("ctx sensitive state "+outerContext+" in "+D);
				boolean fullCtx = true;
				ATNConfigSet s0_closure =
					computeStartState(dfa.atnStartState, outerContext,
									  fullCtx);
				reportAttemptingFullContext(dfa, conflictingAlts, D.configs, startIndex, input.index());
				int alt = execATNWithFullContext(dfa, D, s0_closure,
												 input, startIndex,
												 outerContext);
				return alt;
			}

			if ( D.isAcceptState ) {
				if (D.predicates == null) {
					return D.prediction;
				}

				int stopIndex = input.index();
				input.seek(startIndex);
				BitSet alts = evalSemanticContext(D.predicates, outerContext, true);
				switch (alts.cardinality()) {
				case 0:
					throw noViableAlt(input, outerContext, D.configs, startIndex);

				case 1:
					return alts.nextSetBit(0);

				default:
					// report ambiguity after predicate evaluation to make sure the correct
					// set of ambig alts is reported.
					reportAmbiguity(dfa, D, startIndex, stopIndex, false, alts, D.configs);
					return alts.nextSetBit(0);
				}
			}

			previousD = D;

			if (t != IntStream.EOF) {
				input.consume();
				t = input.LA(1);
			}
		}
	}

	/**
	 * Get an existing target state for an edge in the DFA. If the target state
	 * for the edge has not yet been computed or is otherwise not available,
	 * this method returns {@code null}.
	 *
	 * @param previousD The current DFA state
	 * @param t The next input symbol
	 * @return The existing target DFA state for the given input symbol
	 * {@code t}, or {@code null} if the target state for this edge is not
	 * already cached
	 */
	protected DFAState getExistingTargetState(DFAState previousD, int t) {
		DFAState[] edges = previousD.edges;
		if (edges == null || t + 1 < 0 || t + 1 >= edges.length) {
			return null;
		}

		return edges[t + 1];
	}

	/**
	 * Compute a target state for an edge in the DFA, and attempt to add the
	 * computed state and corresponding edge to the DFA.
	 *
	 * @param dfa The DFA
	 * @param previousD The current DFA state
	 * @param t The next input symbol
	 *
	 * @return The computed target DFA state for the given input symbol
	 * {@code t}. If {@code t} does not lead to a valid DFA state, this method
	 * returns {@link #ERROR}.
	 */
	protected DFAState computeTargetState(DFA dfa, DFAState previousD, int t) {
		ATNConfigSet reach = computeReachSet(previousD.configs, t, false);
		if ( reach==null ) {
			addDFAEdge(dfa, previousD, t, ERROR);
			return ERROR;
		}

		// create new target state; we'll add to DFA after it's complete
		DFAState D = new DFAState(reach);

		int predictedAlt = getUniqueAlt(reach);

		if ( debug ) {
			Collection<BitSet> altSubSets = PredictionMode.getConflictingAltSubsets(reach);
			System.out.println("SLL altSubSets="+altSubSets+
							   ", configs="+reach+
							   ", predict="+predictedAlt+", allSubsetsConflict="+
								   PredictionMode.allSubsetsConflict(altSubSets)+", conflictingAlts="+
							   getConflictingAlts(reach));
		}

		if ( predictedAlt!=ATN.INVALID_ALT_NUMBER ) {
			// NO CONFLICT, UNIQUELY PREDICTED ALT
			D.isAcceptState = true;
			D.configs.uniqueAlt = predictedAlt;
			D.prediction = predictedAlt;
		}
		else if ( PredictionMode.hasSLLConflictTerminatingPrediction(mode, reach) ) {
			// MORE THAN ONE VIABLE ALTERNATIVE
			D.configs.conflictingAlts = getConflictingAlts(reach);
			D.requiresFullContext = true;
			// in SLL-only mode, we will stop at this state and return the minimum alt
			D.isAcceptState = true;
			D.prediction = D.configs.conflictingAlts.nextSetBit(0);
		}

		if ( D.isAcceptState && D.configs.hasSemanticContext ) {
			predicateDFAState(D, atn.getDecisionState(dfa.decision));
			if (D.predicates != null) {
				D.prediction = ATN.INVALID_ALT_NUMBER;
			}
		}

		// all adds to dfa are done after we've created full D state
		D = addDFAEdge(dfa, previousD, t, D);
		return D;
	}

	protected void predicateDFAState(DFAState dfaState, DecisionState decisionState) {
		// We need to test all predicates, even in DFA states that
		// uniquely predict alternative.
		int nalts = decisionState.getNumberOfTransitions();
		// Update DFA so reach becomes accept state with (predicate,alt)
		// pairs if preds found for conflicting alts
		BitSet altsToCollectPredsFrom = getConflictingAltsOrUniqueAlt(dfaState.configs);
		SemanticContext[] altToPred = getPredsForAmbigAlts(altsToCollectPredsFrom, dfaState.configs, nalts);
		if ( altToPred!=null ) {
			dfaState.predicates = getPredicatePredictions(altsToCollectPredsFrom, altToPred);
			dfaState.prediction = ATN.INVALID_ALT_NUMBER; // make sure we use preds
		}
		else {
			// There are preds in configs but they might go away
			// when OR'd together like {p}? || NONE == NONE. If neither
			// alt has preds, resolve to min alt
			dfaState.prediction = altsToCollectPredsFrom.nextSetBit(0);
		}
	}

	// comes back with reach.uniqueAlt set to a valid alt
	protected int execATNWithFullContext(DFA dfa,
										 DFAState D, // how far we got in SLL DFA before failing over
										 ATNConfigSet s0,
										 TokenStream input, int startIndex,
										 ParserRuleContext outerContext)
	{
		if ( debug || debug_list_atn_decisions ) {
			System.out.println("execATNWithFullContext "+s0);
		}
		boolean fullCtx = true;
		boolean foundExactAmbig = false;
		ATNConfigSet reach = null;
		ATNConfigSet previous = s0;
		input.seek(startIndex);
		int t = input.LA(1);
		int predictedAlt;
		while (true) { // while more work
//			System.out.println("LL REACH "+getLookaheadName(input)+
//							   " from configs.size="+previous.size()+
//							   " line "+input.LT(1).getLine()+":"+input.LT(1).getCharPositionInLine());
			reach = computeReachSet(previous, t, fullCtx);
			if ( reach==null ) {
				// if any configs in previous dipped into outer context, that
				// means that input up to t actually finished entry rule
				// at least for LL decision. Full LL doesn't dip into outer
				// so don't need special case.
				// We will get an error no matter what so delay until after
				// decision; better error message. Also, no reachable target
				// ATN states in SLL implies LL will also get nowhere.
				// If conflict in states that dip out, choose min since we
				// will get error no matter what.
				NoViableAltException e = noViableAlt(input, outerContext, previous, startIndex);
				input.seek(startIndex);
				int alt = getSynValidOrSemInvalidAltThatFinishedDecisionEntryRule(previous, outerContext);
				if ( alt!=ATN.INVALID_ALT_NUMBER ) {
					return alt;
				}
				throw e;
			}

			Collection<BitSet> altSubSets = PredictionMode.getConflictingAltSubsets(reach);
			if ( debug ) {
				System.out.println("LL altSubSets="+altSubSets+
								   ", predict="+PredictionMode.getUniqueAlt(altSubSets)+
								   ", resolvesToJustOneViableAlt="+
									   PredictionMode.resolvesToJustOneViableAlt(altSubSets));
			}

//			System.out.println("altSubSets: "+altSubSets);
//			System.err.println("reach="+reach+", "+reach.conflictingAlts);
			reach.uniqueAlt = getUniqueAlt(reach);
			// unique prediction?
			if ( reach.uniqueAlt!=ATN.INVALID_ALT_NUMBER ) {
				predictedAlt = reach.uniqueAlt;
				break;
			}
			if ( mode != PredictionMode.LL_EXACT_AMBIG_DETECTION ) {
				predictedAlt = PredictionMode.resolvesToJustOneViableAlt(altSubSets);
				if ( predictedAlt != ATN.INVALID_ALT_NUMBER ) {
					break;
				}
			}
			else {
				// In exact ambiguity mode, we never try to terminate early.
				// Just keeps scarfing until we know what the conflict is
				if ( PredictionMode.allSubsetsConflict(altSubSets) &&
					 PredictionMode.allSubsetsEqual(altSubSets) )
				{
					foundExactAmbig = true;
					predictedAlt = PredictionMode.getSingleViableAlt(altSubSets);
					break;
				}
				// else there are multiple non-conflicting subsets or
				// we're not sure what the ambiguity is yet.
				// So, keep going.
			}

			previous = reach;
			if (t != IntStream.EOF) {
				input.consume();
				t = input.LA(1);
			}
		}

		// If the configuration set uniquely predicts an alternative,
		// without conflict, then we know that it's a full LL decision
		// not SLL.
		if ( reach.uniqueAlt != ATN.INVALID_ALT_NUMBER ) {
			reportContextSensitivity(dfa, predictedAlt, reach, startIndex, input.index());
			return predictedAlt;
		}

		// We do not check predicates here because we have checked them
		// on-the-fly when doing full context prediction.

		/*
		In non-exact ambiguity detection mode, we might	actually be able to
		detect an exact ambiguity, but I'm not going to spend the cycles
		needed to check. We only emit ambiguity warnings in exact ambiguity
		mode.

		For example, we might know that we have conflicting configurations.
		But, that does not mean that there is no way forward without a
		conflict. It's possible to have nonconflicting alt subsets as in:

		   LL altSubSets=[{1, 2}, {1, 2}, {1}, {1, 2}]

		from

		   [(17,1,[5 $]), (13,1,[5 10 $]), (21,1,[5 10 $]), (11,1,[$]),
			(13,2,[5 10 $]), (21,2,[5 10 $]), (11,2,[$])]

		In this case, (17,1,[5 $]) indicates there is some next sequence that
		would resolve this without conflict to alternative 1. Any other viable
		next sequence, however, is associated with a conflict.  We stop
		looking for input because no amount of further lookahead will alter
		the fact that we should predict alternative 1.  We just can't say for
		sure that there is an ambiguity without looking further.
		*/
		reportAmbiguity(dfa, D, startIndex, input.index(), foundExactAmbig,
						reach.getAlts(), reach);

		return predictedAlt;
	}

	protected ATNConfigSet computeReachSet(ATNConfigSet closure, int t,
										   boolean fullCtx)
	{
		if ( debug )
			System.out.println("in computeReachSet, starting closure: " + closure);

		if (mergeCache == null) {
			mergeCache = new DoubleKeyMap<PredictionContext, PredictionContext, PredictionContext>();
		}

		ATNConfigSet intermediate = new ATNConfigSet(fullCtx);

		/* Configurations already in a rule stop state indicate reaching the end
		 * of the decision rule (local context) or end of the start rule (full
		 * context). Once reached, these configurations are never updated by a
		 * closure operation, so they are handled separately for the performance
		 * advantage of having a smaller intermediate set when calling closure.
		 *
		 * For full-context reach operations, separate handling is required to
		 * ensure that the alternative matching the longest overall sequence is
		 * chosen when multiple such configurations can match the input.
		 */
		List<ATNConfig> skippedStopStates = null;

		// First figure out where we can reach on input t
		for (ATNConfig c : closure) {
			if ( debug ) System.out.println("testing "+getTokenName(t)+" at "+c.toString());

			if (c.state instanceof RuleStopState) {
				assert c.context.isEmpty();
				if (fullCtx || t == IntStream.EOF) {
					if (skippedStopStates == null) {
						skippedStopStates = new ArrayList<ATNConfig>();
					}

					skippedStopStates.add(c);
				}

				continue;
			}

			int n = c.state.getNumberOfTransitions();
			for (int ti=0; ti<n; ti++) {               // for each transition
				Transition trans = c.state.get_transition(ti);
				ATNState target = getReachableTarget(trans, t);
				if ( target!=null ) {
					intermediate.add(new ATNConfig(c, target), mergeCache);
				}
			}
		}

		// Now figure out where the reach operation can take us...

		ATNConfigSet reach = null;

		/* This block optimizes the reach operation for intermediate sets which
		 * trivially indicate a termination state for the overall
		 * adaptivePredict operation.
		 *
		 * The conditions assume that intermediate
		 * contains all configurations relevant to the reach set, but this
		 * condition is not true when one or more configurations have been
		 * withheld in skippedStopStates, or when the current symbol is EOF.
		 */
		if (skippedStopStates == null && t != Token.EOF) {
			if ( intermediate.size()==1 ) {
				// Don't pursue the closure if there is just one state.
				// It can only have one alternative; just add to result
				// Also don't pursue the closure if there is unique alternative
				// among the configurations.
				reach = intermediate;
			}
			else if ( getUniqueAlt(intermediate)!=ATN.INVALID_ALT_NUMBER ) {
				// Also don't pursue the closure if there is unique alternative
				// among the configurations.
				reach = intermediate;
			}
		}

		/* If the reach set could not be trivially determined, perform a closure
		 * operation on the intermediate set to compute its initial value.
		 */
		if (reach == null) {
			reach = new ATNConfigSet(fullCtx);
			Set<ATNConfig> closureBusy = new HashSet<ATNConfig>();
			boolean treatEofAsEpsilon = t == Token.EOF;
			for (ATNConfig c : intermediate) {
				closure(c, reach, closureBusy, false, fullCtx, treatEofAsEpsilon);
			}
		}

		if (t == IntStream.EOF) {
			/* After consuming EOF no additional input is possible, so we are
			 * only interested in configurations which reached the end of the
			 * decision rule (local context) or end of the start rule (full
			 * context). Update reach to contain only these configurations. This
			 * handles both explicit EOF transitions in the grammar and implicit
			 * EOF transitions following the end of the decision or start rule.
			 *
			 * When reach==intermediate, no closure operation was performed. In
			 * this case, removeAllConfigsNotInRuleStopState needs to check for
			 * reachable rule stop states as well as configurations already in
			 * a rule stop state.
			 *
			 * This is handled before the configurations in skippedStopStates,
			 * because any configurations potentially added from that list are
			 * already guaranteed to meet this condition whether or not it's
			 * required.
			 */
			reach = removeAllConfigsNotInRuleStopState(reach, reach == intermediate);
		}

		/* If skippedStopStates is not null, then it contains at least one
		 * configuration. For full-context reach operations, these
		 * configurations reached the end of the start rule, in which case we
		 * only add them back to reach if no configuration during the current
		 * closure operation reached such a state. This ensures adaptivePredict
		 * chooses an alternative matching the longest overall sequence when
		 * multiple alternatives are viable.
		 */
		if (skippedStopStates != null && (!fullCtx || !PredictionMode.hasConfigInRuleStopState(reach))) {
			assert !skippedStopStates.isEmpty();
			for (ATNConfig c : skippedStopStates) {
				reach.add(c, mergeCache);
			}
		}

		if ( reach.isEmpty() ) return null;
		return reach;
	}


	protected ATNConfigSet removeAllConfigsNotInRuleStopState(ATNConfigSet configs, boolean lookToEndOfRule) {
		if (PredictionMode.allConfigsInRuleStopStates(configs)) {
			return configs;
		}

		ATNConfigSet result = new ATNConfigSet(configs.fullCtx);
		for (ATNConfig config : configs) {
			if (config.state instanceof RuleStopState) {
				result.add(config, mergeCache);
				continue;
			}

			if (lookToEndOfRule && config.state.onlyHasEpsilonTransitions()) {
				IntervalSet nextTokens = atn.nextTokens(config.state);
				if (nextTokens.contains(Token.EPSILON)) {
					ATNState endOfRuleState = atn.ruleToStopState[config.state.ruleIndex];
					result.add(new ATNConfig(config, endOfRuleState), mergeCache);
				}
			}
		}

		return result;
	}


	protected ATNConfigSet computeStartState(ATNState p,
										  RuleContext ctx,
										  boolean fullCtx)
	{
		// always at least the implicit call to start rule
		PredictionContext initialContext = PredictionContext.fromRuleContext(atn, ctx);
		ATNConfigSet configs = new ATNConfigSet(fullCtx);

		for (int i=0; i<p.getNumberOfTransitions(); i++) {
			ATNState target = p.get_transition(i).target;
			ATNConfig c = new ATNConfig(target, i+1, initialContext);
			Set<ATNConfig> closureBusy = new HashSet<ATNConfig>();
			closure(c, configs, closureBusy, true, fullCtx, false);
		}

		return configs;
	}

	/* parrt internal source braindump that doesn't mess up
	 * external API spec.
		context-sensitive in that they can only be properly evaluated
		in the context of the proper prec argument. Without pruning,
		these predicates are normal predicates evaluated when we reach
		conflict state (or unique prediction). As we cannot evaluate
		these predicates out of context, the resulting conflict leads
		to full LL evaluation and nonlinear prediction which shows up
		very clearly with fairly large expressions.

		Example grammar:

		e : e '*' e
		  | e '+' e
		  | INT
		  ;

		We convert that to the following:

		e[int prec]
			:   INT
				( {3>=prec}? '*' e[4]
				| {2>=prec}? '+' e[3]
				)*
			;

		The (..)* loop has a decision for the inner block as well as
		an enter or exit decision, which is what concerns us here. At
		the 1st + of input 1+2+3, the loop entry sees both predicates
		and the loop exit also sees both predicates by falling off the
		edge of e.  This is because we have no stack information with
		SLL and find the follow of e, which will hit the return states
		inside the loop after e[4] and e[3], which brings it back to
		the enter or exit decision. In this case, we know that we
		cannot evaluate those predicates because we have fallen off
		the edge of the stack and will in general not know which prec
		parameter is the right one to use in the predicate.

		Because we have special information, that these are precedence
		predicates, we can resolve them without failing over to full
		LL despite their context sensitive nature. We make an
		assumption that prec[-1] <= prec[0], meaning that the current
		precedence level is greater than or equal to the precedence
		level of recursive invocations above us in the stack. For
		example, if predicate {3>=prec}? is true of the current prec,
		then one option is to enter the loop to match it now. The
		other option is to exit the loop and the left recursive rule
		to match the current operator in rule invocation further up
		the stack. But, we know that all of those prec are lower or
		the same value and so we can decide to enter the loop instead
		of matching it later. That means we can strip out the other
		configuration for the exit branch.

		So imagine we have (14,1,$,{2>=prec}?) and then
		(14,2,$-dipsIntoOuterContext,{2>=prec}?). The optimization
		allows us to collapse these two configurations. We know that
		if {2>=prec}? is true for the current prec parameter, it will
		also be true for any prec from an invoking e call, indicated
		by dipsIntoOuterContext. As the predicates are both true, we
		have the option to evaluate them early in the decision start
		state. We do this by stripping both predicates and choosing to
		enter the loop as it is consistent with the notion of operator
		precedence. It's also how the full LL conflict resolution
		would work.

		The solution requires a different DFA start state for each
		precedence level.

		The basic filter mechanism is to remove configurations of the
		form (p, 2, pi) if (p, 1, pi) exists for the same p and pi. In
		other words, for the same ATN state and predicate context,
		remove any configuration associated with an exit branch if
		there is a configuration associated with the enter branch.

		It's also the case that the filter evaluates precedence
		predicates and resolves conflicts according to precedence
		levels. For example, for input 1+2+3 at the first +, we see
		prediction filtering

		[(11,1,[$],{3>=prec}?), (14,1,[$],{2>=prec}?), (5,2,[$],up=1),
		 (11,2,[$],up=1), (14,2,[$],up=1)],hasSemanticContext=true,dipsIntoOuterContext

		to

		[(11,1,[$]), (14,1,[$]), (5,2,[$],up=1)],dipsIntoOuterContext

		This filters because {3>=prec}? evals to true and collapses
		(11,1,[$],{3>=prec}?) and (11,2,[$],up=1) since early conflict
		resolution based upon rules of operator precedence fits with
		our usual match first alt upon conflict.

		We noticed a problem where a recursive call resets precedence
		to 0. Sam's fix: each config has flag indicating if it has
		returned from an expr[0] call. then just don't filter any
		config with that flag set. flag is carried along in
		closure(). so to avoid adding field, set bit just under sign
		bit of dipsIntoOuterContext (SUPPRESS_PRECEDENCE_FILTER).
		With the change you filter "unless (p, 2, pi) was reached
		after leaving the rule stop state of the LR rule containing
		state p, corresponding to a rule invocation with precedence
		level 0"
	 */

	/**
	 * This method transforms the start state computed by
	 * {@link #computeStartState} to the special start state used by a
	 * precedence DFA for a particular precedence value. The transformation
	 * process applies the following changes to the start state's configuration
	 * set.
	 *
	 * <ol>
	 * <li>Evaluate the precedence predicates for each configuration using
	 * {@link SemanticContext#evalPrecedence}.</li>
	 * <li>When {@link ATNConfig#isPrecedenceFilterSuppressed} is {@code false},
	 * remove all configurations which predict an alternative greater than 1,
	 * for which another configuration that predicts alternative 1 is in the
	 * same ATN state with the same prediction context. This transformation is
	 * valid for the following reasons:
	 * <ul>
	 * <li>The closure block cannot contain any epsilon transitions which bypass
	 * the body of the closure, so all states reachable via alternative 1 are
	 * part of the precedence alternatives of the transformed left-recursive
	 * rule.</li>
	 * <li>The "primary" portion of a left recursive rule cannot contain an
	 * epsilon transition, so the only way an alternative other than 1 can exist
	 * in a state that is also reachable via alternative 1 is by nesting calls
	 * to the left-recursive rule, with the outer calls not being at the
	 * preferred precedence level. The
	 * {@link ATNConfig#isPrecedenceFilterSuppressed} property marks ATN
	 * configurations which do not meet this condition, and therefore are not
	 * eligible for elimination during the filtering process.</li>
	 * </ul>
	 * </li>
	 * </ol>
	 *
	 * <p>
	 * The prediction context must be considered by this filter to address
	 * situations like the following.
	 * </p>
	 * <code>
	 * <pre>
	 * grammar TA;
	 * prog: statement* EOF;
	 * statement: letterA | statement letterA 'b' ;
	 * letterA: 'a';
	 * </pre>
	 * </code>
	 * <p>
	 * If the above grammar, the ATN state immediately before the token
	 * reference {@code 'a'} in {@code letterA} is reachable from the left edge
	 * of both the primary and closure blocks of the left-recursive rule
	 * {@code statement}. The prediction context associated with each of these
	 * configurations distinguishes between them, and prevents the alternative
	 * which stepped out to {@code prog} (and then back in to {@code statement}
	 * from being eliminated by the filter.
	 * </p>
	 *
	 * @param configs The configuration set computed by
	 * {@link #computeStartState} as the start state for the DFA.
	 * @return The transformed configuration set representing the start state
	 * for a precedence DFA at a particular precedence level (determined by
	 * calling {@link Parser#getPrecedence}).
	 */
	protected ATNConfigSet applyPrecedenceFilter(ATNConfigSet configs) {
		Map<Integer, PredictionContext> statesFromAlt1 = new HashMap<Integer, PredictionContext>();
		ATNConfigSet configSet = new ATNConfigSet(configs.fullCtx);
		for (ATNConfig config : configs) {
			// handle alt 1 first
			if (config.alt != 1) {
				continue;
			}

			SemanticContext updatedContext = config.semanticContext.evalPrecedence(parser, _outerContext);
			if (updatedContext == null) {
				// the configuration was eliminated
				continue;
			}

			statesFromAlt1.put(config.state.stateNumber, config.context);
			if (updatedContext != config.semanticContext) {
				configSet.add(new ATNConfig(config, updatedContext), mergeCache);
			}
			else {
				configSet.add(config, mergeCache);
			}
		}

		for (ATNConfig config : configs) {
			if (config.alt == 1) {
				// already handled
				continue;
			}

			if (!config.isPrecedenceFilterSuppressed()) {
				/* In the future, this elimination step could be updated to also
				 * filter the prediction context for alternatives predicting alt>1
				 * (basically a graph subtraction algorithm).
				 */
				PredictionContext context = statesFromAlt1.get(config.state.stateNumber);
				if (context != null && context.equals(config.context)) {
					// eliminated
					continue;
				}
			}

			configSet.add(config, mergeCache);
		}

		return configSet;
	}

	protected ATNState getReachableTarget(Transition trans, int ttype) {
		if (trans.matches(ttype, 0, atn.maxTokenType)) {
			return trans.target;
		}

		return null;
	}

	protected SemanticContext[] getPredsForAmbigAlts(BitSet ambigAlts,
												  ATNConfigSet configs,
												  int nalts)
	{
		// REACH=[1|1|[]|0:0, 1|2|[]|0:1]
		/* altToPred starts as an array of all null contexts. The entry at index i
		 * corresponds to alternative i. altToPred[i] may have one of three values:
		 *   1. null: no ATNConfig c is found such that c.alt==i
		 *   2. SemanticContext.NONE: At least one ATNConfig c exists such that
		 *      c.alt==i and c.semanticContext==SemanticContext.NONE. In other words,
		 *      alt i has at least one unpredicated config.
		 *   3. Non-NONE Semantic Context: There exists at least one, and for all
		 *      ATNConfig c such that c.alt==i, c.semanticContext!=SemanticContext.NONE.
		 *
		 * From this, it is clear that NONE||anything==NONE.
		 */
		SemanticContext[] altToPred = new SemanticContext[nalts + 1];
		for (ATNConfig c : configs) {
			if ( ambigAlts.get(c.alt) ) {
				altToPred[c.alt] = SemanticContext.or(altToPred[c.alt], c.semanticContext);
			}
		}

		int nPredAlts = 0;
		for (int i = 1; i <= nalts; i++) {
			if (altToPred[i] == null) {
				altToPred[i] = SemanticContext.Empty.Instance;
			}
			else if (altToPred[i] != SemanticContext.Empty.Instance) {
				nPredAlts++;
			}
		}

//		// Optimize away p||p and p&&p TODO: optimize() was a no-op
//		for (int i = 0; i < altToPred.length; i++) {
//			altToPred[i] = altToPred[i].optimize();
//		}

		// nonambig alts are null in altToPred
		if ( nPredAlts==0 ) altToPred = null;
		if ( debug ) System.out.println("getPredsForAmbigAlts result "+Arrays.toString(altToPred));
		return altToPred;
	}

	protected DFAState.PredPrediction[] getPredicatePredictions(BitSet ambigAlts,
															 SemanticContext[] altToPred)
	{
		List<DFAState.PredPrediction> pairs = new ArrayList<DFAState.PredPrediction>();
		boolean containsPredicate = false;
		for (int i = 1; i < altToPred.length; i++) {
			SemanticContext pred = altToPred[i];

			// unpredicated is indicated by SemanticContext.NONE
			assert pred != null;

			if (ambigAlts!=null && ambigAlts.get(i)) {
				pairs.add(new DFAState.PredPrediction(pred, i));
			}
			if ( pred!=SemanticContext.Empty.Instance ) containsPredicate = true;
		}

		if ( !containsPredicate ) {
			return null;
		}

//		System.out.println(Arrays.toString(altToPred)+"->"+pairs);
		return pairs.toArray(new DFAState.PredPrediction[0]);
	}

	/**
	 * This method is used to improve the localization of error messages by
	 * choosing an alternative rather than throwing a
	 * {@link NoViableAltException} in particular prediction scenarios where the
	 * {@link #ERROR} state was reached during ATN simulation.
	 *
	 * <p>
	 * The default implementation of this method uses the following
	 * algorithm to identify an ATN configuration which successfully parsed the
	 * decision entry rule. Choosing such an alternative ensures that the
	 * {@link ParserRuleContext} returned by the calling rule will be complete
	 * and valid, and the syntax error will be reported later at a more
	 * localized location.</p>
	 *
	 * <ul>
	 * <li>If a syntactically valid path or paths reach the end of the decision rule and
	 * they are semantically valid if predicated, return the min associated alt.</li>
	 * <li>Else, if a semantically invalid but syntactically valid path exist
	 * or paths exist, return the minimum associated alt.
	 * </li>
	 * <li>Otherwise, return {@link ATN#INVALID_ALT_NUMBER}.</li>
	 * </ul>
	 *
	 * <p>
	 * In some scenarios, the algorithm described above could predict an
	 * alternative which will result in a {@link FailedPredicateException} in
	 * the parser. Specifically, this could occur if the <em>only</em> configuration
	 * capable of successfully parsing to the end of the decision rule is
	 * blocked by a semantic predicate. By choosing this alternative within
	 * {@link #adaptivePredict} instead of throwing a
	 * {@link NoViableAltException}, the resulting
	 * {@link FailedPredicateException} in the parser will identify the specific
	 * predicate which is preventing the parser from successfully parsing the
	 * decision rule, which helps developers identify and correct logic errors
	 * in semantic predicates.
	 * </p>
	 *
	 * @param configs The ATN configurations which were valid immediately before
	 * the {@link #ERROR} state was reached
	 * @param outerContext The is the \gamma_0 initial parser context from the paper
	 * or the parser stack at the instant before prediction commences.
	 *
	 * @return The value to return from {@link #adaptivePredict}, or
	 * {@link ATN#INVALID_ALT_NUMBER} if a suitable alternative was not
	 * identified and {@link #adaptivePredict} should report an error instead.
	 */
	protected int getSynValidOrSemInvalidAltThatFinishedDecisionEntryRule(ATNConfigSet configs,
																		  ParserRuleContext outerContext)
	{
		Pair<ATNConfigSet,ATNConfigSet> sets =
			splitAccordingToSemanticValidity(configs, outerContext);
		ATNConfigSet semValidConfigs = sets.a;
		ATNConfigSet semInvalidConfigs = sets.b;
		int alt = getAltThatFinishedDecisionEntryRule(semValidConfigs);
		if ( alt!=ATN.INVALID_ALT_NUMBER ) { // semantically/syntactically viable path exists
			return alt;
		}
		// Is there a syntactically valid path with a failed pred?
		if ( semInvalidConfigs.size()>0 ) {
			alt = getAltThatFinishedDecisionEntryRule(semInvalidConfigs);
			if ( alt!=ATN.INVALID_ALT_NUMBER ) { // syntactically viable path exists
				return alt;
			}
		}
		return ATN.INVALID_ALT_NUMBER;
	}

	protected int getAltThatFinishedDecisionEntryRule(ATNConfigSet configs) {
		IntervalSet alts = new IntervalSet();
		for (ATNConfig c : configs) {
			if ( c.getOuterContextDepth()>0 || (c.state instanceof RuleStopState && c.context.hasEmptyPath()) ) {
				alts.add(c.alt);
			}
		}
		if ( alts.size()==0 ) return ATN.INVALID_ALT_NUMBER;
		return alts.getMinElement();
	}

	/** Walk the list of configurations and split them according to
	 *  those that have preds evaluating to true/false.  If no pred, assume
	 *  true pred and include in succeeded set.  Returns Pair of sets.
	 *
	 *  Create a new set so as not to alter the incoming parameter.
	 *
	 *  Assumption: the input stream has been restored to the starting point
	 *  prediction, which is where predicates need to evaluate.
 	 */
	protected Pair<ATNConfigSet,ATNConfigSet> splitAccordingToSemanticValidity(
		ATNConfigSet configs,
		ParserRuleContext outerContext)
	{
		ATNConfigSet succeeded = new ATNConfigSet(configs.fullCtx);
		ATNConfigSet failed = new ATNConfigSet(configs.fullCtx);
		for (ATNConfig c : configs) {
			if ( c.semanticContext!=SemanticContext.Empty.Instance ) {
				boolean predicateEvaluationResult = evalSemanticContext(c.semanticContext, outerContext, c.alt, configs.fullCtx);
				if ( predicateEvaluationResult ) {
					succeeded.add(c);
				}
				else {
					failed.add(c);
				}
			}
			else {
				succeeded.add(c);
			}
		}
		return new Pair<ATNConfigSet, ATNConfigSet>(succeeded,failed);
	}

	/** Look through a list of predicate/alt pairs, returning alts for the
	 *  pairs that win. A {@code NONE} predicate indicates an alt containing an
	 *  unpredicated config which behaves as "always true." If !complete
	 *  then we stop at the first predicate that evaluates to true. This
	 *  includes pairs with null predicates.
	 */
	protected BitSet evalSemanticContext(DFAState.PredPrediction[] predPredictions,
									  ParserRuleContext outerContext,
									  boolean complete)
	{
		BitSet predictions = new BitSet();
		for (DFAState.PredPrediction pair : predPredictions) {
			if ( pair.pred==SemanticContext.Empty.Instance ) {
				predictions.set(pair.alt);
				if (!complete) {
					break;
				}
				continue;
			}

			boolean fullCtx = false; // in dfa
			boolean predicateEvaluationResult = evalSemanticContext(pair.pred, outerContext, pair.alt, fullCtx);
			if ( debug || dfa_debug ) {
				System.out.println("eval pred "+pair+"="+predicateEvaluationResult);
			}

			if ( predicateEvaluationResult ) {
				if ( debug || dfa_debug ) System.out.println("PREDICT "+pair.alt);
				predictions.set(pair.alt);
				if (!complete) {
					break;
				}
			}
		}

		return predictions;
	}

	/**
	 * Evaluate a semantic context within a specific parser context.
	 *
	 * <p>
	 * This method might not be called for every semantic context evaluated
	 * during the prediction process. In particular, we currently do not
	 * evaluate the following but it may change in the future:</p>
	 *
	 * <ul>
	 * <li>Precedence predicates (represented by
	 * {@link SemanticContext.PrecedencePredicate}) are not currently evaluated
	 * through this method.</li>
	 * <li>Operator predicates (represented by {@link SemanticContext.AND} and
	 * {@link SemanticContext.OR}) are evaluated as a single semantic
	 * context, rather than evaluating the operands individually.
	 * Implementations which require evaluation results from individual
	 * predicates should override this method to explicitly handle evaluation of
	 * the operands within operator predicates.</li>
	 * </ul>
	 *
	 * @param pred The semantic context to evaluate
	 * @param parserCallStack The parser context in which to evaluate the
	 * semantic context
	 * @param alt The alternative which is guarded by {@code pred}
	 * @param fullCtx {@code true} if the evaluation is occurring during LL
	 * prediction; otherwise, {@code false} if the evaluation is occurring
	 * during SLL prediction
	 *
	 * @since 4.3
	 */
	protected boolean evalSemanticContext(SemanticContext pred, ParserRuleContext parserCallStack, int alt, boolean fullCtx) {
		return pred.eval(parser, parserCallStack);
	}

	/* TODO: If we are doing predicates, there is no point in pursuing
		 closure operations if we reach a DFA state that uniquely predicts
		 alternative. We will not be caching that DFA state and it is a
		 waste to pursue the closure. Might have to advance when we do
		 ambig detection thought :(
		  */

	protected void closure(ATNConfig config,
						   ATNConfigSet configs,
						   Set<ATNConfig> closureBusy,
						   boolean collectPredicates,
						   boolean fullCtx,
						   boolean treatEofAsEpsilon)
	{
		final int initialDepth = 0;
		closureCheckingStopState(config, configs, closureBusy, collectPredicates,
								 fullCtx,
								 initialDepth, treatEofAsEpsilon);
		assert !fullCtx || !configs.dipsIntoOuterContext;
	}

	protected void closureCheckingStopState(ATNConfig config,
											ATNConfigSet configs,
											Set<ATNConfig> closureBusy,
											boolean collectPredicates,
											boolean fullCtx,
											int depth,
											boolean treatEofAsEpsilon)
	{
		if ( debug ) System.out.println("closure("+config.toString(true)+")");

		if ( config.state instanceof RuleStopState ) {
			// We hit rule end. If we have context info, use it
			// run thru all possible stack tops in ctx
			if ( !config.context.isEmpty() ) {
				for (int i = 0; i < config.context.size(); i++) {
					if ( config.context.getReturnState(i)==PredictionContext.EMPTY_RETURN_STATE ) {
						if (fullCtx) {
							configs.add(new ATNConfig(config, config.state, EmptyPredictionContext.Instance), mergeCache);
							continue;
						}
						else {
							// we have no context info, just chase follow links (if greedy)
							if ( debug ) System.out.println("FALLING off rule "+
															getRuleName(config.state.ruleIndex));
							closure_(config, configs, closureBusy, collectPredicates,
									 fullCtx, depth, treatEofAsEpsilon);
						}
						continue;
					}
					ATNState returnState = atn.states.get(config.context.getReturnState(i));
					PredictionContext newContext = config.context.getParent(i); // "pop" return state
					ATNConfig c = new ATNConfig(returnState, config.alt, newContext,
												config.semanticContext);
					// While we have context to pop back from, we may have
					// gotten that context AFTER having falling off a rule.
					// Make sure we track that we are now out of context.
					//
					// This assignment also propagates the
					// isPrecedenceFilterSuppressed() value to the new
					// configuration.
					c.reachesIntoOuterContext = config.reachesIntoOuterContext;
					assert depth > Integer.MIN_VALUE;
					closureCheckingStopState(c, configs, closureBusy, collectPredicates,
											 fullCtx, depth - 1, treatEofAsEpsilon);
				}
				return;
			}
			else if (fullCtx) {
				// reached end of start rule
				configs.add(config, mergeCache);
				return;
			}
			else {
				// else if we have no context info, just chase follow links (if greedy)
				if ( debug ) System.out.println("FALLING off rule "+
												getRuleName(config.state.ruleIndex));
			}
		}

		closure_(config, configs, closureBusy, collectPredicates,
				 fullCtx, depth, treatEofAsEpsilon);
	}

	/** Do the actual work of walking epsilon edges */
	protected void closure_(ATNConfig config,
							ATNConfigSet configs,
							Set<ATNConfig> closureBusy,
							boolean collectPredicates,
							boolean fullCtx,
							int depth,
							boolean treatEofAsEpsilon)
	{
		ATNState p = config.state;
		// optimization
		if ( !p.onlyHasEpsilonTransitions() ) {
            configs.add(config, mergeCache);
			// make sure to not return here, because EOF transitions can act as
			// both epsilon transitions and non-epsilon transitions.
//            if ( debug ) System.out.println("added config "+configs);
        }

		for (int i=0; i<p.getNumberOfTransitions(); i++) {
			if ( i==0 && canDropLoopEntryEdgeInLeftRecursiveRule(config) ) continue;

			Transition t = p.get_transition(i);
			boolean continueCollecting =
				!(t instanceof ActionTransition) && collectPredicates;
			ATNConfig c = getEpsilonTarget(config, t, continueCollecting,
										   depth == 0, fullCtx, treatEofAsEpsilon);
			if ( c!=null ) {
				int newDepth = depth;
				if ( config.state instanceof RuleStopState) {
					assert !fullCtx;
					// target fell off end of rule; mark resulting c as having dipped into outer context
					// We can't get here if incoming config was rule stop and we had context
					// track how far we dip into outer context.  Might
					// come in handy and we avoid evaluating context dependent
					// preds if this is > 0.

					if (_dfa != null && _dfa.isPrecedenceDfa()) {
						int outermostPrecedenceReturn = ((EpsilonTransition)t).outermostPrecedenceReturn();
						if (outermostPrecedenceReturn == _dfa.atnStartState.ruleIndex) {
							c.setPrecedenceFilterSuppressed(true);
						}
					}

					c.reachesIntoOuterContext++;

					if (!closureBusy.add(c)) {
						// avoid infinite recursion for right-recursive rules
						continue;
					}

					configs.dipsIntoOuterContext = true; // TODO: can remove? only care when we add to set per middle of this method
					assert newDepth > Integer.MIN_VALUE;
					newDepth--;
					if ( debug ) System.out.println("dips into outer ctx: "+c);
				}
				else {
					if (!t.isEpsilon() && !closureBusy.add(c)) {
						// avoid infinite recursion for EOF* and EOF+
						continue;
					}

					if (t instanceof RuleTransition) {
						// latch when newDepth goes negative - once we step out of the entry context we can't return
						if (newDepth >= 0) {
							newDepth++;
						}
					}
				}

				closureCheckingStopState(c, configs, closureBusy, continueCollecting,
										 fullCtx, newDepth, treatEofAsEpsilon);
			}
		}
	}

	/** Implements first-edge (loop entry) elimination as an optimization
	 *  during closure operations.  See antlr/antlr4#1398.
	 *
	 * The optimization is to avoid adding the loop entry config when
	 * the exit path can only lead back to the same
	 * StarLoopEntryState after popping context at the rule end state
	 * (traversing only epsilon edges, so we're still in closure, in
	 * this same rule).
	 *
	 * We need to detect any state that can reach loop entry on
	 * epsilon w/o exiting rule. We don't have to look at FOLLOW
	 * links, just ensure that all stack tops for config refer to key
	 * states in LR rule.
	 *
	 * To verify we are in the right situation we must first check
	 * closure is at a StarLoopEntryState generated during LR removal.
	 * Then we check that each stack top of context is a return state
	 * from one of these cases:
	 *
	 *   1. 'not' expr, '(' type ')' expr. The return state points at loop entry state
	 *   2. expr op expr. The return state is the block end of internal block of (...)*
	 *   3. 'between' expr 'and' expr. The return state of 2nd expr reference.
	 *      That state points at block end of internal block of (...)*.
	 *   4. expr '?' expr ':' expr. The return state points at block end,
	 *      which points at loop entry state.
	 *
	 * If any is true for each stack top, then closure does not add a
	 * config to the current config set for edge[0], the loop entry branch.
	 *
	 *  Conditions fail if any context for the current config is:
	 *
	 *   a. empty (we'd fall out of expr to do a global FOLLOW which could
	 *      even be to some weird spot in expr) or,
	 *   b. lies outside of expr or,
	 *   c. lies within expr but at a state not the BlockEndState
	 *   generated during LR removal
	 *
	 * Do we need to evaluate predicates ever in closure for this case?
	 *
	 * No. Predicates, including precedence predicates, are only
	 * evaluated when computing a DFA start state. I.e., only before
	 * the lookahead (but not parser) consumes a token.
	 *
	 * There are no epsilon edges allowed in LR rule alt blocks or in
	 * the "primary" part (ID here). If closure is in
	 * StarLoopEntryState any lookahead operation will have consumed a
	 * token as there are no epsilon-paths that lead to
	 * StarLoopEntryState. We do not have to evaluate predicates
	 * therefore if we are in the generated StarLoopEntryState of a LR
	 * rule. Note that when making a prediction starting at that
	 * decision point, decision d=2, compute-start-state performs
	 * closure starting at edges[0], edges[1] emanating from
	 * StarLoopEntryState. That means it is not performing closure on
	 * StarLoopEntryState during compute-start-state.
	 *
	 * How do we know this always gives same prediction answer?
	 *
	 * Without predicates, loop entry and exit paths are ambiguous
	 * upon remaining input +b (in, say, a+b). Either paths lead to
	 * valid parses. Closure can lead to consuming + immediately or by
	 * falling out of this call to expr back into expr and loop back
	 * again to StarLoopEntryState to match +b. In this special case,
	 * we choose the more efficient path, which is to take the bypass
	 * path.
	 *
	 * The lookahead language has not changed because closure chooses
	 * one path over the other. Both paths lead to consuming the same
	 * remaining input during a lookahead operation. If the next token
	 * is an operator, lookahead will enter the choice block with
	 * operators. If it is not, lookahead will exit expr. Same as if
	 * closure had chosen to enter the choice block immediately.
	 *
	 * Closure is examining one config (some loopentrystate, some alt,
	 * context) which means it is considering exactly one alt. Closure
	 * always copies the same alt to any derived configs.
	 *
	 * How do we know this optimization doesn't mess up precedence in
	 * our parse trees?
	 *
	 * Looking through expr from left edge of stat only has to confirm
	 * that an input, say, a+b+c; begins with any valid interpretation
	 * of an expression. The precedence actually doesn't matter when
	 * making a decision in stat seeing through expr. It is only when
	 * parsing rule expr that we must use the precedence to get the
	 * right interpretation and, hence, parse tree.
	 *
	 * @since 4.6
	 */
	protected boolean canDropLoopEntryEdgeInLeftRecursiveRule(ATNConfig config) {
		if ( TURN_OFF_LR_LOOP_ENTRY_BRANCH_OPT ) return false;
		ATNState p = config.state;
		// First check to see if we are in StarLoopEntryState generated during
		// left-recursion elimination. For efficiency, also check if
		// the context has an empty stack case. If so, it would mean
		// global FOLLOW so we can't perform optimization
		if ( p.getStateType() != ATNState.STAR_LOOP_ENTRY ||
			 !((StarLoopEntryState)p).isPrecedenceDecision || // Are we the special loop entry/exit state?
			 config.context.isEmpty() ||                      // If SLL wildcard
			 config.context.hasEmptyPath())
		{
			return false;
		}

		// Require all return states to return back to the same rule
		// that p is in.
		int numCtxs = config.context.size();
		for (int i = 0; i < numCtxs; i++) { // for each stack context
			ATNState returnState = atn.states.get(config.context.getReturnState(i));
			if ( returnState.ruleIndex != p.ruleIndex ) return false;
		}

		BlockStartState decisionStartState = (BlockStartState)p.get_transition(0).target;
		int blockEndStateNum = decisionStartState.endState.stateNumber;
		BlockEndState blockEndState = (BlockEndState)atn.states.get(blockEndStateNum);

		// Verify that the top of each stack context leads to loop entry/exit
		// state through epsilon edges and w/o leaving rule.
		for (int i = 0; i < numCtxs; i++) {                           // for each stack context
			int returnStateNumber = config.context.getReturnState(i);
			ATNState returnState = atn.states.get(returnStateNumber);
			// all states must have single outgoing epsilon edge
			if ( returnState.getNumberOfTransitions()!=1 ||
				 !returnState.get_transition(0).isEpsilon() )
			{
				return false;
			}
			// Look for prefix op case like 'not expr', (' type ')' expr
			ATNState returnStateTarget = returnState.get_transition(0).target;
			if ( returnState.getStateType()==BLOCK_END && returnStateTarget==p ) {
				continue;
			}
			// Look for 'expr op expr' or case where expr's return state is block end
			// of (...)* internal block; the block end points to loop back
			// which points to p but we don't need to check that
			if ( returnState==blockEndState ) {
				continue;
			}
			// Look for ternary expr ? expr : expr. The return state points at block end,
			// which points at loop entry state
			if ( returnStateTarget==blockEndState ) {
				continue;
			}
			// Look for complex prefix 'between expr and expr' case where 2nd expr's
			// return state points at block end state of (...)* internal block
			if ( returnStateTarget.getStateType() == BLOCK_END &&
				 returnStateTarget.getNumberOfTransitions()==1 &&
				 returnStateTarget.get_transition(0).isEpsilon() &&
				 returnStateTarget.get_transition(0).target == p )
			{
				continue;
			}

			// anything else ain't conforming
			return false;
		}

		return true;
	}


	public String getRuleName(int index) {
		if ( parser!=null && index>=0 ) return parser.getRuleNames()[index];
		return "<rule "+index+">";
	}


	protected ATNConfig getEpsilonTarget(ATNConfig config,
									  Transition t,
									  boolean collectPredicates,
									  boolean inContext,
									  boolean fullCtx,
									  boolean treatEofAsEpsilon)
	{
		switch (t.getSerializationType()) {
		case Transition.RULE:
			return ruleTransition(config, (RuleTransition)t);

		case Transition.PRECEDENCE:
			return precedenceTransition(config, (PrecedencePredicateTransition)t, collectPredicates, inContext, fullCtx);

		case Transition.PREDICATE:
			return predTransition(config, (PredicateTransition)t,
								  collectPredicates,
								  inContext,
								  fullCtx);

		case Transition.ACTION:
			return actionTransition(config, (ActionTransition)t);

		case Transition.EPSILON:
			return new ATNConfig(config, t.target);

		case Transition.ATOM:
		case Transition.RANGE:
		case Transition.SET:
			// EOF transitions act like epsilon transitions after the first EOF
			// transition is traversed
			if (treatEofAsEpsilon) {
				if (t.matches(Token.EOF, 0, 1)) {
					return new ATNConfig(config, t.target);
				}
			}

			return null;

		default:
			return null;
		}
	}


	protected ATNConfig actionTransition(ATNConfig config, ActionTransition t) {
		if ( debug ) System.out.println("ACTION edge "+t.ruleIndex+":"+t.actionIndex);
		return new ATNConfig(config, t.target);
	}


	public ATNConfig precedenceTransition(ATNConfig config,
									PrecedencePredicateTransition pt,
									boolean collectPredicates,
									boolean inContext,
									boolean fullCtx)
	{
		if ( debug ) {
			System.out.println("PRED (collectPredicates="+collectPredicates+") "+
                    pt.precedence+">=_p"+
					", ctx dependent=true");
			if ( parser != null ) {
                System.out.println("context surrounding pred is "+
                                   parser.getRuleInvocationStack());
            }
		}

        ATNConfig c = null;
        if (collectPredicates && inContext) {
			if ( fullCtx ) {
				// In full context mode, we can evaluate predicates on-the-fly
				// during closure, which dramatically reduces the size of
				// the config sets. It also obviates the need to test predicates
				// later during conflict resolution.
				int currentPosition = _input.index();
				_input.seek(_startIndex);
				boolean predSucceeds = evalSemanticContext(pt.getPredicate(), _outerContext, config.alt, fullCtx);
				_input.seek(currentPosition);
				if ( predSucceeds ) {
					c = new ATNConfig(config, pt.target); // no pred context
				}
			}
			else {
				SemanticContext newSemCtx =
					SemanticContext.and(config.semanticContext, pt.getPredicate());
				c = new ATNConfig(config, pt.target, newSemCtx);
			}
        }
		else {
			c = new ATNConfig(config, pt.target);
		}

		if ( debug ) System.out.println("config from pred transition="+c);
        return c;
	}


	protected ATNConfig predTransition(ATNConfig config,
									PredicateTransition pt,
									boolean collectPredicates,
									boolean inContext,
									boolean fullCtx)
	{
		if ( debug ) {
			System.out.println("PRED (collectPredicates="+collectPredicates+") "+
                    pt.ruleIndex+":"+pt.predIndex+
					", ctx dependent="+pt.isCtxDependent);
			if ( parser != null ) {
                System.out.println("context surrounding pred is "+
                                   parser.getRuleInvocationStack());
            }
		}

		ATNConfig c = null;
		if ( collectPredicates &&
			 (!pt.isCtxDependent || (pt.isCtxDependent&&inContext)) )
		{
			if ( fullCtx ) {
				// In full context mode, we can evaluate predicates on-the-fly
				// during closure, which dramatically reduces the size of
				// the config sets. It also obviates the need to test predicates
				// later during conflict resolution.
				int currentPosition = _input.index();
				_input.seek(_startIndex);
				boolean predSucceeds = evalSemanticContext(pt.getPredicate(), _outerContext, config.alt, fullCtx);
				_input.seek(currentPosition);
				if ( predSucceeds ) {
					c = new ATNConfig(config, pt.target); // no pred context
				}
			}
			else {
				SemanticContext newSemCtx =
					SemanticContext.and(config.semanticContext, pt.getPredicate());
				c = new ATNConfig(config, pt.target, newSemCtx);
			}
		}
		else {
			c = new ATNConfig(config, pt.target);
		}

		if ( debug ) System.out.println("config from pred transition="+c);
        return c;
	}


	protected ATNConfig ruleTransition(ATNConfig config, RuleTransition t) {
		if ( debug ) {
			System.out.println("CALL rule "+getRuleName(t.target.ruleIndex)+
							   ", ctx="+config.context);
		}

		ATNState returnState = t.followState;
		PredictionContext newContext =
			SingletonPredictionContext.create(config.context, returnState.stateNumber);
		return new ATNConfig(config, t.target, newContext);
	}

	protected BitSet getConflictingAlts(ATNConfigSet configs) {
		Collection<BitSet> altsets = PredictionMode.getConflictingAltSubsets(configs);
		return PredictionMode.getAlts(altsets);
	}

	protected BitSet getConflictingAltsOrUniqueAlt(ATNConfigSet configs) {
		BitSet conflictingAlts;
		if ( configs.uniqueAlt!= ATN.INVALID_ALT_NUMBER ) {
			conflictingAlts = new BitSet();
			conflictingAlts.set(configs.uniqueAlt);
		}
		else {
			conflictingAlts = configs.conflictingAlts;
		}
		return conflictingAlts;
	}


	public String getTokenName(int t) {
		if (t == Token.EOF) {
			return "EOF";
		}

		Vocabulary vocabulary = parser != null ? parser.getVocabulary() : VocabularyImpl.EMPTY_VOCABULARY;
		String displayName = vocabulary.getDisplayName(t);
		if (displayName.equals(Integer.toString(t))) {
			return displayName;
		}

		return displayName + "<" + t + ">";
	}

	public String getLookaheadName(TokenStream input) {
		return getTokenName(input.LA(1));
	}


	protected NoViableAltException noViableAlt(TokenStream input,
											ParserRuleContext outerContext,
											ATNConfigSet configs,
											int startIndex)
	{
		return new NoViableAltException(parser, input,
											input.get(startIndex),
											input.LT(1),
				outerContext);
	}

	protected static int getUniqueAlt(ATNConfigSet configs) {
		int alt = ATN.INVALID_ALT_NUMBER;
		for (ATNConfig c : configs) {
			if ( alt == ATN.INVALID_ALT_NUMBER ) {
				alt = c.alt; // found first alt
			}
			else if ( c.alt!=alt ) {
				return ATN.INVALID_ALT_NUMBER;
			}
		}
		return alt;
	}

	/**
	 * Add an edge to the DFA, if possible. This method calls
	 * {@link #addDFAState} to ensure the {@code to} state is present in the
	 * DFA. If {@code from} is {@code null}, or if {@code t} is outside the
	 * range of edges that can be represented in the DFA tables, this method
	 * returns without adding the edge to the DFA.
	 *
	 * <p>If {@code to} is {@code null}, this method returns {@code null}.
	 * Otherwise, this method returns the {@link DFAState} returned by calling
	 * {@link #addDFAState} for the {@code to} state.</p>
	 *
	 * @param dfa The DFA
	 * @param from The source state for the edge
	 * @param t The input symbol
	 * @param to The target state for the edge
	 *
	 * @return If {@code to} is {@code null}, this method returns {@code null};
	 * otherwise this method returns the result of calling {@link #addDFAState}
	 * on {@code to}
	 */
	protected DFAState addDFAEdge(DFA dfa,
								  DFAState from,
								  int t,
								  DFAState to)
	{
		if ( debug ) {
			System.out.println("EDGE "+from+" -> "+to+" upon "+getTokenName(t));
		}

		if (to == null) {
			return null;
		}

		to = addDFAState(dfa, to); // used existing if possible not incoming
		if (from == null || t < -1 || t > atn.maxTokenType) {
			return to;
		}

		synchronized (from) {
			if ( from.edges==null ) {
				from.edges = new DFAState[atn.maxTokenType+1+1];
			}

			from.edges[t+1] = to; // connect
		}

		if ( debug ) {
			System.out.println("DFA=\n"+dfa.toString(parser!=null?parser.getVocabulary():VocabularyImpl.EMPTY_VOCABULARY));
		}

		return to;
	}

	/**
	 * Add state {@code D} to the DFA if it is not already present, and return
	 * the actual instance stored in the DFA. If a state equivalent to {@code D}
	 * is already in the DFA, the existing state is returned. Otherwise this
	 * method returns {@code D} after adding it to the DFA.
	 *
	 * <p>If {@code D} is {@link #ERROR}, this method returns {@link #ERROR} and
	 * does not change the DFA.</p>
	 *
	 * @param dfa The dfa
	 * @param D The DFA state to add
	 * @return The state stored in the DFA. This will be either the existing
	 * state if {@code D} is already in the DFA, or {@code D} itself if the
	 * state was not already present.
	 */
	protected DFAState addDFAState(DFA dfa, DFAState D) {
		if (D == ERROR) {
			return D;
		}

		synchronized (dfa.states) {
			DFAState existing = dfa.states.get(D);
			if ( existing!=null ) return existing;

			D.stateNumber = dfa.states.size();

			D.configs.optimizeConfigs(this);
//			D.configs.setReadonly(true);

			dfa.states.put(D, D);
			if ( debug ) System.out.println("adding new DFA state: "+D);
			return D;
		}
	}

	protected void reportAttemptingFullContext(DFA dfa, BitSet conflictingAlts, ATNConfigSet configs, int startIndex, int stopIndex) {
        if ( debug || retry_debug ) {
			Interval interval = Interval.of(startIndex, stopIndex);
			System.out.println("reportAttemptingFullContext decision="+dfa.decision+":"+configs+
                               ", input="+parser.getTokenStream().getText(interval));
        }
        if ( parser!=null ) parser.getErrorListenerDispatch().reportAttemptingFullContext(parser, dfa, startIndex, stopIndex, conflictingAlts, configs);
    }

	protected void reportContextSensitivity(DFA dfa, int prediction, ATNConfigSet configs, int startIndex, int stopIndex) {
        if ( debug || retry_debug ) {
			Interval interval = Interval.of(startIndex, stopIndex);
            System.out.println("reportContextSensitivity decision="+dfa.decision+":"+configs+
                               ", input="+parser.getTokenStream().getText(interval));
        }
        if ( parser!=null ) parser.getErrorListenerDispatch().reportContextSensitivity(parser, dfa, startIndex, stopIndex, prediction, configs);
    }

    /** If context sensitive parsing, we know it's ambiguity not conflict */
    protected void reportAmbiguity(DFA dfa,
								   DFAState D, // the DFA state from execATN() that had SLL conflicts
								   int startIndex, int stopIndex,
								   boolean exact,
								   BitSet ambigAlts,
								   ATNConfigSet configs) // configs that LL not SLL considered conflicting
	{
		if ( debug || retry_debug ) {
			Interval interval = Interval.of(startIndex, stopIndex);
			System.out.println("reportAmbiguity "+
							   ambigAlts+":"+configs+
                               ", input="+parser.getTokenStream().getText(interval));
        }
        if ( parser!=null ) parser.getErrorListenerDispatch().reportAmbiguity(parser, dfa, startIndex, stopIndex,
																			  exact, ambigAlts, configs);
    }

	public final void setPredictionMode(PredictionMode mode) {
		this.mode = mode;
	}


	public final PredictionMode getPredictionMode() {
		return mode;
	}

	/**
	 * @since 4.3
	 */
	public Parser getParser() {
		return parser;
	}

	public static String getSafeEnv(String envName) {
		try {
			return AccessController.doPrivileged(new PrivilegedAction<String>() {
				@Override
				public String run() {
					return System.getenv(envName);
				}
			});
		} catch (SecurityException e) { }
		return null;
	}
}
