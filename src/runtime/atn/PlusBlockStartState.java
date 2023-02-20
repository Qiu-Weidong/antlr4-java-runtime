

package runtime.atn;


public final class PlusBlockStartState extends BlockStartState {
	public PlusLoopbackState loopBackState;

	@Override
	public int getStateType() {
		return PLUS_BLOCK_START;
	}
}
