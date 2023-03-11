

package runtime.dfa;

import runtime.atn.ATNConfig;
import runtime.atn.ATNConfigSet;
import runtime.atn.LexerActionExecutor;
import runtime.atn.SemanticContext;
import runtime.misc.MurmurHash;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class DFAState {
	public int stateNumber = -1;


	public ATNConfigSet configs;

	

	public DFAState[] edges;

	public boolean isAcceptState = false;

	
	public int prediction;

	public LexerActionExecutor lexerActionExecutor;

	
	public boolean requiresFullContext;

	

	public PredPrediction[] predicates;

	
	public static class PredPrediction {

		public SemanticContext pred;
		public int alt;
		public PredPrediction(SemanticContext pred, int alt) {
			this.alt = alt;
			this.pred = pred;
		}
		@Override
		public String toString() {
			return "("+pred+", "+alt+ ")";
		}
	}

	public DFAState(ATNConfigSet configs) { this.configs = configs; }

	
	public Set<Integer> getAltSet() {
		Set<Integer> alts = new HashSet<Integer>();
		if ( configs!=null ) {
			for (ATNConfig c : configs) {
				alts.add(c.alt);
			}
		}
		if ( alts.isEmpty() ) return null;
		return alts;
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize(7);
		hash = MurmurHash.update(hash, configs.hashCode());
		hash = MurmurHash.finish(hash, 1);
		return hash;
	}

	
	@Override
	public boolean equals(Object o) {

		if ( this==o ) return true;

		if (!(o instanceof DFAState)) {
			return false;
		}

		DFAState other = (DFAState)o;

		boolean sameSet = this.configs.equals(other.configs);

		return sameSet;
	}

	@Override
	public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(stateNumber).append(":").append(configs);
        if ( isAcceptState ) {
            buf.append("=>");
            if ( predicates!=null ) {
                buf.append(Arrays.toString(predicates));
            }
            else {
                buf.append(prediction);
            }
        }
		return buf.toString();
	}
}
