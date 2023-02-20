

package runtime.atn;


public final class BasicBlockStartState extends BlockStartState {
	@Override
	public int getStateType() {
		return BLOCK_START;
	}
}
