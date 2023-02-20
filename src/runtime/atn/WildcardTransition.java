

package runtime.atn;

public final class WildcardTransition extends Transition {
	public WildcardTransition(ATNState target) { super(target); }

	@Override
	public int getSerializationType() {
		return WILDCARD;
	}

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return symbol >= minVocabSymbol && symbol <= maxVocabSymbol;
	}

	@Override
	public String toString() {
		return ".";
	}
}
