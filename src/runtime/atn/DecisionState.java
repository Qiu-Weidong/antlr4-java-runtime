

package runtime.atn;

public abstract class DecisionState extends ATNState {
	public int decision = -1;
	public boolean nonGreedy;
}
