

package runtime.atn;


public final class PredicateTransition extends AbstractPredicateTransition {
	public final int ruleIndex;
	public final int predIndex;
	public final boolean isCtxDependent;

	public PredicateTransition(ATNState target, int ruleIndex, int predIndex, boolean isCtxDependent) {
		super(target);
		this.ruleIndex = ruleIndex;
		this.predIndex = predIndex;
		this.isCtxDependent = isCtxDependent;
	}

	@Override
	public int getSerializationType() {
		return PREDICATE;
	}

	@Override
	public boolean isEpsilon() { return true; }

	@Override
	public boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol) {
		return false;
	}

    public SemanticContext.Predicate getPredicate() {
   		return new SemanticContext.Predicate(ruleIndex, predIndex, isCtxDependent);
   	}

	@Override
	public String toString() {
		return "pred_"+ruleIndex+":"+predIndex;
	}

}
