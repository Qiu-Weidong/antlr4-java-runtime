

package runtime.atn;


public final class TokensStartState extends DecisionState {

	@Override
	public int getStateType() {
		return TOKEN_START;
	}
}
