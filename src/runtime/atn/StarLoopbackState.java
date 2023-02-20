

package runtime.atn;

public final class StarLoopbackState extends ATNState {
	public final StarLoopEntryState getLoopEntryState() {
		return (StarLoopEntryState)transition(0).target;
	}

	@Override
	public int getStateType() {
		return STAR_LOOP_BACK;
	}
}
