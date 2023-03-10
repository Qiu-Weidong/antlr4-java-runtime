

package runtime.atn;

import runtime.misc.IntervalSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public abstract class ATNState {
	public static final int INITIAL_NUM_TRANSITIONS = 4;


	public static final int INVALID_TYPE = 0;
	public static final int BASIC = 1;
	public static final int RULE_START = 2;
	public static final int BLOCK_START = 3;
	public static final int PLUS_BLOCK_START = 4;
	public static final int STAR_BLOCK_START = 5;
	public static final int TOKEN_START = 6;
	public static final int RULE_STOP = 7;
	public static final int BLOCK_END = 8;
	public static final int STAR_LOOP_BACK = 9;
	public static final int STAR_LOOP_ENTRY = 10;
	public static final int PLUS_LOOP_BACK = 11;
	public static final int LOOP_END = 12;

	public static final int INVALID_STATE_NUMBER = -1;

    
   	public ATN atn = null;

	public int stateNumber = INVALID_STATE_NUMBER;

	public int ruleIndex;

	public boolean epsilonOnlyTransitions = false;

	
	protected final List<Transition> transitions =
		new ArrayList<Transition>(INITIAL_NUM_TRANSITIONS);

	
    public IntervalSet nextTokenWithinRule;

	@Override
	public int hashCode() { return stateNumber; }

	@Override
	public boolean equals(Object o) {

		if ( o instanceof ATNState ) return stateNumber==((ATNState)o).stateNumber;
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(stateNumber);
	}

	public int getNumberOfTransitions() {
		return transitions.size();
	}

	public void addTransition(Transition e) {
		addTransition(transitions.size(), e);
	}

	public void addTransition(int index, Transition e) {
		if (transitions.isEmpty()) {
			epsilonOnlyTransitions = e.isEpsilon();
		}
		else if (epsilonOnlyTransitions != e.isEpsilon()) {
			System.err.format(Locale.getDefault(), "ATN state %d has both epsilon and non-epsilon transitions.\n", stateNumber);
			epsilonOnlyTransitions = false;
		}

		boolean alreadyPresent = false;
		for (Transition t : transitions) {
			if ( t.target.stateNumber == e.target.stateNumber ) {
				if ( t.label()!=null && e.label()!=null && t.label().equals(e.label()) ) {

					alreadyPresent = true;
					break;
				}
				else if ( t.isEpsilon() && e.isEpsilon() ) {

					alreadyPresent = true;
					break;
				}
			}
		}
		if ( !alreadyPresent ) {
			transitions.add(index, e);
		}
	}

	public Transition get_transition(int i) { return transitions.get(i); }

	public void setTransition(int i, Transition e) {
		transitions.set(i, e);
	}

	public Transition removeTransition(int index) {
		return transitions.remove(index);
	}

	public abstract int getStateType();

	public final boolean onlyHasEpsilonTransitions() {
		return epsilonOnlyTransitions;
	}

	public void setRuleIndex(int ruleIndex) { this.ruleIndex = ruleIndex; }
}
