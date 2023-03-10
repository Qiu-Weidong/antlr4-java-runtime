

package runtime.atn;

import runtime.Token;
import runtime.misc.IntervalSet;
import runtime.misc.Pair;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class ATNDeserializer {
	public static final int SERIALIZED_VERSION;
	static {
		SERIALIZED_VERSION = 4;
	}

	private final ATNDeserializationOptions deserializationOptions;

	public ATNDeserializer() {
		this(ATNDeserializationOptions.getDefaultOptions());
	}

	public ATNDeserializer(ATNDeserializationOptions deserializationOptions) {
		if (deserializationOptions == null) {
			deserializationOptions = ATNDeserializationOptions.getDefaultOptions();
		}

		this.deserializationOptions = deserializationOptions;
	}

	public ATN deserialize(char[] data) {
		return deserialize(decodeIntsEncodedAs16BitWords(data));
	}

	public ATN deserialize(int[] data) {
//		System.out.println(Arrays.toString(data));
		int p = 0;
		int version = data[p++];
		if (version != SERIALIZED_VERSION) {
			String reason = String.format(Locale.getDefault(), "Could not deserialize ATN with version %d (expected %d).", version, SERIALIZED_VERSION);
			throw new UnsupportedOperationException(new InvalidClassException(ATN.class.getName(), reason));
		}

		ATNType grammarType = ATNType.values()[data[p++]];
		int maxTokenType = data[p++];
		ATN atn = new ATN(grammarType, maxTokenType);




		List<Pair<LoopEndState, Integer>> loopBackStateNumbers = new ArrayList<Pair<LoopEndState, Integer>>();
		List<Pair<BlockStartState, Integer>> endStateNumbers = new ArrayList<Pair<BlockStartState, Integer>>();
		int nstates = data[p++];
		for (int i=0; i<nstates; i++) {
			int stype = data[p++];

			if ( stype==ATNState.INVALID_TYPE ) {
				atn.addState(null);
				continue;
			}

			int ruleIndex = data[p++];
			ATNState s = stateFactory(stype, ruleIndex);
			if ( stype == ATNState.LOOP_END ) {
				int loopBackStateNumber = data[p++];
				loopBackStateNumbers.add(new Pair<LoopEndState, Integer>((LoopEndState)s, loopBackStateNumber));
			}
			else if (s instanceof BlockStartState) {
				int endStateNumber = data[p++];
				endStateNumbers.add(new Pair<BlockStartState, Integer>((BlockStartState)s, endStateNumber));
			}
			atn.addState(s);
		}

		for (Pair<LoopEndState, Integer> pair : loopBackStateNumbers) {
			pair.a.loopBackState = atn.states.get(pair.b);
		}

		for (Pair<BlockStartState, Integer> pair : endStateNumbers) {
			pair.a.endState = (BlockEndState)atn.states.get(pair.b);
		}

		int numNonGreedyStates = data[p++];
		for (int i = 0; i < numNonGreedyStates; i++) {
			int stateNumber = data[p++];
			((DecisionState)atn.states.get(stateNumber)).nonGreedy = true;
		}

		int numPrecedenceStates = data[p++];
		for (int i = 0; i < numPrecedenceStates; i++) {
			int stateNumber = data[p++];
			((RuleStartState)atn.states.get(stateNumber)).isLeftRecursiveRule = true;
		}




		int nrules = data[p++];
		if ( atn.grammarType == ATNType.LEXER ) {
			atn.ruleToTokenType = new int[nrules];
		}

		atn.ruleToStartState = new RuleStartState[nrules];
		for (int i=0; i<nrules; i++) {
			int s = data[p++];
			RuleStartState startState = (RuleStartState)atn.states.get(s);
			atn.ruleToStartState[i] = startState;
			if ( atn.grammarType == ATNType.LEXER ) {
				int tokenType = data[p++];
				atn.ruleToTokenType[i] = tokenType;
			}
		}

		atn.ruleToStopState = new RuleStopState[nrules];
		for (ATNState state : atn.states) {
			if (!(state instanceof RuleStopState)) {
				continue;
			}

			RuleStopState stopState = (RuleStopState)state;
			atn.ruleToStopState[state.ruleIndex] = stopState;
			atn.ruleToStartState[state.ruleIndex].stopState = stopState;
		}




		int nmodes = data[p++];
		for (int i=0; i<nmodes; i++) {
			int s = data[p++];
			atn.modeToStartState.add((TokensStartState)atn.states.get(s));
		}




		List<IntervalSet> sets = new ArrayList<IntervalSet>();
		p = deserializeSets(data, p, sets);




		int nedges = data[p++];
		for (int i=0; i<nedges; i++) {
			int src = data[p];
			int trg = data[p+1];
			int ttype = data[p+2];
			int arg1 = data[p+3];
			int arg2 = data[p+4];
			int arg3 = data[p+5];
			Transition trans = edgeFactory(atn, ttype, src, trg, arg1, arg2, arg3, sets);




			ATNState srcState = atn.states.get(src);
			srcState.addTransition(trans);
			p += 6;
		}


		for (ATNState state : atn.states) {
			for (int i = 0; i < state.getNumberOfTransitions(); i++) {
				Transition t = state.get_transition(i);
				if (!(t instanceof RuleTransition)) {
					continue;
				}

				RuleTransition ruleTransition = (RuleTransition)t;
				int outermostPrecedenceReturn = -1;
				if (atn.ruleToStartState[ruleTransition.target.ruleIndex].isLeftRecursiveRule) {
					if (ruleTransition.precedence == 0) {
						outermostPrecedenceReturn = ruleTransition.target.ruleIndex;
					}
				}

				EpsilonTransition returnTransition = new EpsilonTransition(ruleTransition.followState, outermostPrecedenceReturn);
				atn.ruleToStopState[ruleTransition.target.ruleIndex].addTransition(returnTransition);
			}
		}

		for (ATNState state : atn.states) {
			if (state instanceof BlockStartState) {

				if (((BlockStartState)state).endState == null) {
					throw new IllegalStateException();
				}


				if (((BlockStartState)state).endState.startState != null) {
					throw new IllegalStateException();
				}

				((BlockStartState)state).endState.startState = (BlockStartState)state;
			}

			if (state instanceof PlusLoopbackState) {
				PlusLoopbackState loopbackState = (PlusLoopbackState)state;
				for (int i = 0; i < loopbackState.getNumberOfTransitions(); i++) {
					ATNState target = loopbackState.get_transition(i).target;
					if (target instanceof PlusBlockStartState) {
						((PlusBlockStartState)target).loopBackState = loopbackState;
					}
				}
			}
			else if (state instanceof StarLoopbackState) {
				StarLoopbackState loopbackState = (StarLoopbackState)state;
				for (int i = 0; i < loopbackState.getNumberOfTransitions(); i++) {
					ATNState target = loopbackState.get_transition(i).target;
					if (target instanceof StarLoopEntryState) {
						((StarLoopEntryState)target).loopBackState = loopbackState;
					}
				}
			}
		}




		int ndecisions = data[p++];
		for (int i=0; i<ndecisions; i++) {
			int s = data[p++];
			DecisionState decState = (DecisionState)atn.states.get(s);
			atn.decisionToState.add(decState);
			decState.decision = i;
		}




		if (atn.grammarType == ATNType.LEXER) {
			atn.lexerActions = new LexerAction[data[p++]];
			for (int i = 0; i < atn.lexerActions.length; i++) {
				LexerActionType actionType = LexerActionType.values()[data[p++]];
				int data1 = data[p++];
				int data2 = data[p++];

				LexerAction lexerAction = lexerActionFactory(actionType, data1, data2);

				atn.lexerActions[i] = lexerAction;
			}
		}

		markPrecedenceDecisions(atn);

		if (deserializationOptions.isVerifyATN()) {
			verifyATN(atn);
		}

		if (deserializationOptions.isGenerateRuleBypassTransitions() && atn.grammarType == ATNType.PARSER) {
			atn.ruleToTokenType = new int[atn.ruleToStartState.length];
			for (int i = 0; i < atn.ruleToStartState.length; i++) {
				atn.ruleToTokenType[i] = atn.maxTokenType + i + 1;
			}









			for (int i = 0; i < atn.ruleToStartState.length; i++) {
				BasicBlockStartState bypassStart = new BasicBlockStartState();
				bypassStart.ruleIndex = i;
				atn.addState(bypassStart);

				BlockEndState bypassStop = new BlockEndState();
				bypassStop.ruleIndex = i;
				atn.addState(bypassStop);

				bypassStart.endState = bypassStop;
				atn.defineDecisionState(bypassStart);

				bypassStop.startState = bypassStart;

				ATNState endState = null;
				Transition excludeTransition = null;
				if (atn.ruleToStartState[i].isLeftRecursiveRule) {

//					endState = null;
					for (ATNState state : atn.states) {
						if (state.ruleIndex != i) {
							continue;
						}

						if (!(state instanceof StarLoopEntryState)) {
							continue;
						}

						ATNState maybeLoopEndState = state.get_transition(state.getNumberOfTransitions() - 1).target;
						if (!(maybeLoopEndState instanceof LoopEndState)) {
							continue;
						}

						if (maybeLoopEndState.epsilonOnlyTransitions && maybeLoopEndState.get_transition(0).target instanceof RuleStopState) {
							endState = state;
							break;
						}
					}

					if (endState == null) {
						throw new UnsupportedOperationException("Couldn't identify final state of the precedence rule prefix section.");
					}

					excludeTransition = ((StarLoopEntryState)endState).loopBackState.get_transition(0);
				}
				else {
					endState = atn.ruleToStopState[i];
				}


				for (ATNState state : atn.states) {
					for (Transition transition : state.transitions) {
						if (transition == excludeTransition) {
							continue;
						}

						if (transition.target == endState) {
							transition.target = bypassStop;
						}
					}
				}


				while (atn.ruleToStartState[i].getNumberOfTransitions() > 0) {
					Transition transition = atn.ruleToStartState[i].removeTransition(atn.ruleToStartState[i].getNumberOfTransitions() - 1);
					bypassStart.addTransition(transition);
				}


				atn.ruleToStartState[i].addTransition(new EpsilonTransition(bypassStart));
				bypassStop.addTransition(new EpsilonTransition(endState));

				ATNState matchState = new BasicState();
				atn.addState(matchState);
				matchState.addTransition(new AtomTransition(bypassStop, atn.ruleToTokenType[i]));
				bypassStart.addTransition(new EpsilonTransition(matchState));
			}
















			if (deserializationOptions.isVerifyATN()) {

				verifyATN(atn);
			}
		}

		return atn;
	}

	private int deserializeSets(int[] data, int p, List<IntervalSet> sets) {
		int nsets = data[p++];
		for (int i=0; i<nsets; i++) {
			int nintervals = data[p];
			p++;
			IntervalSet set = new IntervalSet();
			sets.add(set);

			boolean containsEof = data[p++] != 0;
			if (containsEof) {
				set.add(-1);
			}

			for (int j=0; j<nintervals; j++) {
				int a = data[p++];
				int b = data[p++];
				set.add(a, b);
			}
		}
		return p;
	}

	
	protected void markPrecedenceDecisions(ATN atn) {
		for (ATNState state : atn.states) {
			if (!(state instanceof StarLoopEntryState)) {
				continue;
			}

			
			if (atn.ruleToStartState[state.ruleIndex].isLeftRecursiveRule) {
				ATNState maybeLoopEndState = state.get_transition(state.getNumberOfTransitions() - 1).target;
				if (maybeLoopEndState instanceof LoopEndState) {
					if (maybeLoopEndState.epsilonOnlyTransitions && maybeLoopEndState.get_transition(0).target instanceof RuleStopState) {
						((StarLoopEntryState)state).isPrecedenceDecision = true;
					}
				}
			}
		}
	}

	protected void verifyATN(ATN atn) {

		for (ATNState state : atn.states) {
			if (state == null) {
				continue;
			}

			checkCondition(state.onlyHasEpsilonTransitions() || state.getNumberOfTransitions() <= 1);

			if (state instanceof PlusBlockStartState) {
				checkCondition(((PlusBlockStartState)state).loopBackState != null);
			}

			if (state instanceof StarLoopEntryState) {
				StarLoopEntryState starLoopEntryState = (StarLoopEntryState)state;
				checkCondition(starLoopEntryState.loopBackState != null);
				checkCondition(starLoopEntryState.getNumberOfTransitions() == 2);

				if (starLoopEntryState.get_transition(0).target instanceof StarBlockStartState) {
					checkCondition(starLoopEntryState.get_transition(1).target instanceof LoopEndState);
					checkCondition(!starLoopEntryState.nonGreedy);
				}
				else if (starLoopEntryState.get_transition(0).target instanceof LoopEndState) {
					checkCondition(starLoopEntryState.get_transition(1).target instanceof StarBlockStartState);
					checkCondition(starLoopEntryState.nonGreedy);
				}
				else {
					throw new IllegalStateException();
				}
			}

			if (state instanceof StarLoopbackState) {
				checkCondition(state.getNumberOfTransitions() == 1);
				checkCondition(state.get_transition(0).target instanceof StarLoopEntryState);
			}

			if (state instanceof LoopEndState) {
				checkCondition(((LoopEndState)state).loopBackState != null);
			}

			if (state instanceof RuleStartState) {
				checkCondition(((RuleStartState)state).stopState != null);
			}

			if (state instanceof BlockStartState) {
				checkCondition(((BlockStartState)state).endState != null);
			}

			if (state instanceof BlockEndState) {
				checkCondition(((BlockEndState)state).startState != null);
			}

			if (state instanceof DecisionState) {
				DecisionState decisionState = (DecisionState)state;
				checkCondition(decisionState.getNumberOfTransitions() <= 1 || decisionState.decision >= 0);
			}
			else {
				checkCondition(state.getNumberOfTransitions() <= 1 || state instanceof RuleStopState);
			}
		}
	}

	protected void checkCondition(boolean condition) {
		checkCondition(condition, null);
	}

	protected void checkCondition(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	protected Transition edgeFactory(ATN atn,
										 int type, int src, int trg,
										 int arg1, int arg2, int arg3,
										 List<IntervalSet> sets)
	{
		ATNState target = atn.states.get(trg);
		switch (type) {
			case Transition.EPSILON : return new EpsilonTransition(target);
			case Transition.RANGE :
				if (arg3 != 0) {
					return new RangeTransition(target, Token.EOF, arg2);
				}
				else {
					return new RangeTransition(target, arg1, arg2);
				}
			case Transition.RULE :
				RuleTransition rt = new RuleTransition((RuleStartState)atn.states.get(arg1), arg2, arg3, target);
				return rt;
			case Transition.PREDICATE :
				PredicateTransition pt = new PredicateTransition(target, arg1, arg2, arg3 != 0);
				return pt;
			case Transition.PRECEDENCE:
				return new PrecedencePredicateTransition(target, arg1);
			case Transition.ATOM :
				if (arg3 != 0) {
					return new AtomTransition(target, Token.EOF);
				}
				else {
					return new AtomTransition(target, arg1);
				}
			case Transition.ACTION :
				ActionTransition a = new ActionTransition(target, arg1, arg2, arg3 != 0);
				return a;
			case Transition.SET : return new SetTransition(target, sets.get(arg1));
			case Transition.NOT_SET : return new NotSetTransition(target, sets.get(arg1));
			case Transition.WILDCARD : return new WildcardTransition(target);
		}

		throw new IllegalArgumentException("The specified transition type is not valid.");
	}

	protected ATNState stateFactory(int type, int ruleIndex) {
//		System.out.println(type);
		ATNState s;
		switch (type) {
			case ATNState.INVALID_TYPE: return null;
			case ATNState.BASIC : s = new BasicState(); break;
			case ATNState.RULE_START : s = new RuleStartState(); break;
			case ATNState.BLOCK_START : s = new BasicBlockStartState(); break;
			case ATNState.PLUS_BLOCK_START : s = new PlusBlockStartState(); break;
			case ATNState.STAR_BLOCK_START : s = new StarBlockStartState(); break;
			case ATNState.TOKEN_START : s = new TokensStartState(); break;
			case ATNState.RULE_STOP : s = new RuleStopState(); break;
			case ATNState.BLOCK_END : s = new BlockEndState(); break;
			case ATNState.STAR_LOOP_BACK : s = new StarLoopbackState(); break;
			case ATNState.STAR_LOOP_ENTRY : s = new StarLoopEntryState(); break;
			case ATNState.PLUS_LOOP_BACK : s = new PlusLoopbackState(); break;
			case ATNState.LOOP_END : s = new LoopEndState(); break;
			default :
				String message = String.format(Locale.getDefault(), "The specified state type %d is not valid.", type);
				throw new IllegalArgumentException(message);
		}

		s.ruleIndex = ruleIndex;
		return s;
	}

	protected LexerAction lexerActionFactory(LexerActionType type, int data1, int data2) {
		switch (type) {
		case CHANNEL:
			return new LexerChannelAction(data1);

		case CUSTOM:
			return new LexerCustomAction(data1, data2);

		case MODE:
			return new LexerModeAction(data1);

		case MORE:
			return LexerMoreAction.INSTANCE;

		case POP_MODE:
			return LexerPopModeAction.INSTANCE;

		case PUSH_MODE:
			return new LexerPushModeAction(data1);

		case SKIP:
			return LexerSkipAction.INSTANCE;

		case TYPE:
			return new LexerTypeAction(data1);

		default:
			throw new IllegalArgumentException(String.format(Locale.getDefault(), "The specified lexer action type %s is not valid.", type));
		}
	}


	public static int[] decodeIntsEncodedAs16BitWords(char[] data16) {
		return decodeIntsEncodedAs16BitWords(data16, false);
	}

	
	public static int[] decodeIntsEncodedAs16BitWords(char[] data16, boolean trimToSize) {

		int[] data = new int[data16.length];
		int i = 0;
		int i2 = 0;
		while ( i < data16.length ) {
			char v = data16[i++];
			if ( (v & 0x8000) == 0 ) {
				data[i2++] = v;
			}
			else {
				char vnext = data16[i++];
				if ( v==0xFFFF && vnext == 0xFFFF ) {
					data[i2++] = -1;
				}
				else {
					data[i2++] = (v & 0x7FFF) << 16 | (vnext & 0xFFFF);
				}
			}
		}
		if ( trimToSize ) {
			return Arrays.copyOf(data, i2);
		}
		return data;
	}
}
