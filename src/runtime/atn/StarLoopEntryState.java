

package runtime.atn;

import runtime.ParserInterpreter;
import runtime.dfa.DFA;

public final class StarLoopEntryState extends DecisionState {
	public StarLoopbackState loopBackState;

	
	public boolean isPrecedenceDecision;

	@Override
	public int getStateType() {
		return STAR_LOOP_ENTRY;
	}
}
