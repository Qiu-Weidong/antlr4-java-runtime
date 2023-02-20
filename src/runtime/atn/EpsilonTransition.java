

package runtime.atn;

public final class EpsilonTransition extends Transition {

	private final int outermostPrecedenceReturn;

	public EpsilonTransition(ATNState target) {
		this(target, -1);
	}

	public EpsilonTransition(ATNState target, int outermostPrecedenceReturn) {
		super(target);
		this.outermostPrecedenceReturn = outermostPrecedenceReturn;
	}

	
	public int outermostPrecedenceReturn() {
		return outermostPrecedenceReturn;
	}

	@Override
	public int getSerializationType() {
		return EPSILON;
	}

	@Override
	public boolean isEpsilon() { return true; }

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return false;
	}

	@Override

	public String toString() {
		return "epsilon";
	}
}
