

package runtime.atn;

import runtime.Token;
import runtime.misc.IntervalSet;


public class SetTransition extends Transition {
	public final IntervalSet set;


	public SetTransition(ATNState target, IntervalSet set) {
		super(target);
		if ( set == null ) set = IntervalSet.of(Token.INVALID_TYPE);
		this.set = set;
	}

	@Override
	public int getSerializationType() {
		return SET;
	}

	@Override

	public IntervalSet label() { return set; }

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return set.contains(symbol);
	}

	@Override

	public String toString() {
		return set.toString();
	}
}
