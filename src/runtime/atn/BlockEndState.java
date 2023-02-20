

package runtime.atn;


public final class BlockEndState extends ATNState {
	public BlockStartState startState;

	@Override
	public int getStateType() {
		return BLOCK_END;
	}
}
