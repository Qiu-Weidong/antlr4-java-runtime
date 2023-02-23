

package runtime.atn;

import runtime.misc.IntervalSet;


public final class AtomTransition extends Transition {
	
	public final int atom_label;

	public AtomTransition(ATNState target, int label) {
		super(target);
		this.atom_label = label;
	}

	@Override
	public int getSerializationType() {
		return ATOM;
	}

	@Override

	public IntervalSet label() { return IntervalSet.of(atom_label); }

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return atom_label == symbol;
	}

	@Override
	public String toString() {
		return String.valueOf(atom_label);
	}
}
