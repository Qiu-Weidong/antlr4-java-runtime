

package runtime.dfa;

import runtime.Vocabulary;

import java.util.Arrays;
import java.util.List;


public class DFASerializer {

	private final DFA dfa;

	private final Vocabulary vocabulary;


	public DFASerializer(DFA dfa, Vocabulary vocabulary) {
		this.dfa = dfa;
		this.vocabulary = vocabulary;
	}

	@Override
	public String toString() {
		if ( dfa.s0==null ) return null;
		StringBuilder buf = new StringBuilder();
		List<DFAState> states = dfa.getStates();
		for (DFAState s : states) {
			int n = 0;
			if ( s.edges!=null ) n = s.edges.length;
			for (int i=0; i<n; i++) {
				DFAState t = s.edges[i];
				if ( t!=null && t.stateNumber != Integer.MAX_VALUE ) {
					buf.append(getStateString(s));
					String label = getEdgeLabel(i);
					buf.append("-").append(label).append("->").append(getStateString(t)).append('\n');
				}
			}
		}

		String output = buf.toString();
		if ( output.length()==0 ) return null;

		return output;
	}

	protected String getEdgeLabel(int i) {
		return vocabulary.getDisplayName(i - 1);
	}


	protected String getStateString(DFAState s) {
		int n = s.stateNumber;
		final String baseStateStr = (s.isAcceptState ? ":" : "") + "s" + n + (s.requiresFullContext ? "^" : "");
		if ( s.isAcceptState ) {
            if ( s.predicates!=null ) {
                return baseStateStr + "=>" + Arrays.toString(s.predicates);
            }
            else {
                return baseStateStr + "=>" + s.prediction;
            }
		}
		else {
			return baseStateStr;
		}
	}
}
