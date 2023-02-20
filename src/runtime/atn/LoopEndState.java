

package runtime.atn;


public final class LoopEndState extends ATNState {
	public ATNState loopBackState;

	@Override
	public int getStateType() {
		return LOOP_END;
	}
}
