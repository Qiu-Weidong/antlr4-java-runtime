

package runtime.atn;

import runtime.dfa.DFA;

import java.util.ArrayList;
import java.util.List;


public class ParseInfo {
	protected final ProfilingATNSimulator atnSimulator;

	public ParseInfo(ProfilingATNSimulator atnSimulator) {
		this.atnSimulator = atnSimulator;
	}

	
	public DecisionInfo[] getDecisionInfo() {
		return atnSimulator.getDecisionInfo();
	}

	
	public List<Integer> getLLDecisions() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		List<Integer> LL = new ArrayList<Integer>();
		for (int i=0; i<decisions.length; i++) {
			long fallBack = decisions[i].LL_Fallback;
			if ( fallBack>0 ) LL.add(i);
		}
		return LL;
	}

	
	public long getTotalTimeInPrediction() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long t = 0;
		for (int i=0; i<decisions.length; i++) {
			t += decisions[i].timeInPrediction;
		}
		return t;
	}

	
	public long getTotalSLLLookaheadOps() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long k = 0;
		for (int i = 0; i < decisions.length; i++) {
			k += decisions[i].SLL_TotalLook;
		}
		return k;
	}

	
	public long getTotalLLLookaheadOps() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long k = 0;
		for (int i = 0; i < decisions.length; i++) {
			k += decisions[i].LL_TotalLook;
		}
		return k;
	}

	
	public long getTotalSLLATNLookaheadOps() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long k = 0;
		for (int i = 0; i < decisions.length; i++) {
			k += decisions[i].SLL_ATNTransitions;
		}
		return k;
	}

	
	public long getTotalLLATNLookaheadOps() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long k = 0;
		for (int i = 0; i < decisions.length; i++) {
			k += decisions[i].LL_ATNTransitions;
		}
		return k;
	}

	
	public long getTotalATNLookaheadOps() {
		DecisionInfo[] decisions = atnSimulator.getDecisionInfo();
		long k = 0;
		for (int i = 0; i < decisions.length; i++) {
			k += decisions[i].SLL_ATNTransitions;
			k += decisions[i].LL_ATNTransitions;
		}
		return k;
	}

	
	public int getDFASize() {
		int n = 0;
		DFA[] decisionToDFA = atnSimulator.decisionToDFA;
		for (int i = 0; i < decisionToDFA.length; i++) {
			n += getDFASize(i);
		}
		return n;
	}

	
	public int getDFASize(int decision) {
		DFA decisionToDFA = atnSimulator.decisionToDFA[decision];
		return decisionToDFA.states.size();
	}
}
