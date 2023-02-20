
package runtime.dfa;

import runtime.Parser;
import runtime.Vocabulary;
import runtime.VocabularyImpl;
import runtime.atn.ATNConfigSet;
import runtime.atn.DecisionState;
import runtime.atn.StarLoopEntryState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DFA {
	

	public final Map<DFAState, DFAState> states = new HashMap<DFAState, DFAState>();

	public volatile DFAState s0;

	public final int decision;

	

	public final DecisionState atnStartState;

	
	private final boolean precedenceDfa;

	public DFA(DecisionState atnStartState) {
		this(atnStartState, 0);
	}

	public DFA(DecisionState atnStartState, int decision) {
		this.atnStartState = atnStartState;
		this.decision = decision;

		boolean precedenceDfa = false;
		if (atnStartState instanceof StarLoopEntryState) {
			if (((StarLoopEntryState)atnStartState).isPrecedenceDecision) {
				precedenceDfa = true;
				DFAState precedenceState = new DFAState(new ATNConfigSet());
				precedenceState.edges = new DFAState[0];
				precedenceState.isAcceptState = false;
				precedenceState.requiresFullContext = false;
				this.s0 = precedenceState;
			}
		}

		this.precedenceDfa = precedenceDfa;
	}

	
	public final boolean isPrecedenceDfa() {
		return precedenceDfa;
	}

	
	@SuppressWarnings("null")
	public final DFAState getPrecedenceStartState(int precedence) {
		if (!isPrecedenceDfa()) {
			throw new IllegalStateException("Only precedence DFAs may contain a precedence start state.");
		}


		if (precedence < 0 || precedence >= s0.edges.length) {
			return null;
		}

		return s0.edges[precedence];
	}

	
	@SuppressWarnings({"SynchronizeOnNonFinalField", "null"})
	public final void setPrecedenceStartState(int precedence, DFAState startState) {
		if (!isPrecedenceDfa()) {
			throw new IllegalStateException("Only precedence DFAs may contain a precedence start state.");
		}

		if (precedence < 0) {
			return;
		}



		synchronized (s0) {

			if (precedence >= s0.edges.length) {
				s0.edges = Arrays.copyOf(s0.edges, precedence + 1);
			}

			s0.edges[precedence] = startState;
		}
	}

	
	@Deprecated
	public final void setPrecedenceDfa(boolean precedenceDfa) {
		if (precedenceDfa != isPrecedenceDfa()) {
			throw new UnsupportedOperationException("The precedenceDfa field cannot change after a DFA is constructed.");
		}
	}

	

	public List<DFAState> getStates() {
		List<DFAState> result = new ArrayList<DFAState>(states.keySet());
		Collections.sort(result, new Comparator<DFAState>() {
			@Override
			public int compare(DFAState o1, DFAState o2) {
				return o1.stateNumber - o2.stateNumber;
			}
		});

		return result;
	}

	@Override
	public String toString() { return toString(VocabularyImpl.EMPTY_VOCABULARY); }

	
	@Deprecated
	public String toString(String[] tokenNames) {
		if ( s0==null ) return "";
		DFASerializer serializer = new DFASerializer(this,tokenNames);
		return serializer.toString();
	}

	public String toString(Vocabulary vocabulary) {
		if (s0 == null) {
			return "";
		}

		DFASerializer serializer = new DFASerializer(this, vocabulary);
		return serializer.toString();
	}

	public String toLexerString() {
		if ( s0==null ) return "";
		DFASerializer serializer = new LexerDFASerializer(this);
		return serializer.toString();
	}

}
