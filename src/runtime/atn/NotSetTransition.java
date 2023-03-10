

package runtime.atn;

import runtime.misc.IntervalSet;

public final class NotSetTransition extends SetTransition {
	public NotSetTransition(ATNState target, IntervalSet set) {
		super(target, set);
	}

	@Override
	public int getSerializationType() {
		return NOT_SET;
	}

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return symbol >= minVocabSymbol
			&& symbol <= maxVocabSymbol
			&& !super.matches(symbol, minVocabSymbol, maxVocabSymbol);
	}

	@Override
	public String toString() {
		return '~'+super.toString();
	}
}
