

package runtime.atn;

public final class RuleStartState extends ATNState {
	public RuleStopState stopState;
	public boolean isLeftRecursiveRule;

	@Override
	public int getStateType() {
		return RULE_START;
	}
}
