

package runtime.atn;


public final class PrecedencePredicateTransition extends AbstractPredicateTransition {
	public final int precedence;

	public PrecedencePredicateTransition(ATNState target, int precedence) {
		super(target);
		this.precedence = precedence;
	}

	@Override
	public int getSerializationType() {
		return PRECEDENCE;
	}

	@Override
	public boolean isEpsilon() {
		return true;
	}

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return false;
	}

	public SemanticContext.PrecedencePredicate getPredicate() {
		return new SemanticContext.PrecedencePredicate(precedence);
	}

	@Override
	public String toString() {
		return precedence + " >= _p";
	}

}
