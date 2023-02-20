

package runtime.atn;

import runtime.misc.IntervalSet;


public final class AtomTransition extends Transition {
	
	public final int label;

	public AtomTransition(ATNState target, int label) {
		super(target);
		this.label = label;
	}

	@Override
	public int getSerializationType() {
		return ATOM;
	}

	@Override

	public IntervalSet label() { return IntervalSet.of(label); }

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return label == symbol;
	}

	@Override
	public String toString() {
		return String.valueOf(label);
	}
}
