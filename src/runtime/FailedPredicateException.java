
package runtime;

import runtime.atn.ATNState;
import runtime.atn.AbstractPredicateTransition;
import runtime.atn.PredicateTransition;

import java.util.Locale;


public class FailedPredicateException extends RecognitionException {
	private final int ruleIndex;
	private final int predicateIndex;
	private final String predicate;

	public FailedPredicateException(Parser recognizer) {
		this(recognizer, null);
	}

	public FailedPredicateException(Parser recognizer, String predicate) {
		this(recognizer, predicate, null);
	}

	public FailedPredicateException(Parser recognizer,
									String predicate,
									String message)
	{
		super(formatMessage(predicate, message), recognizer, recognizer.getInputStream(), recognizer._ctx);
		ATNState s = recognizer.getInterpreter().atn.states.get(recognizer.getState());

		AbstractPredicateTransition trans = (AbstractPredicateTransition)s.get_transition(0);
		if (trans instanceof PredicateTransition) {
			this.ruleIndex = ((PredicateTransition)trans).ruleIndex;
			this.predicateIndex = ((PredicateTransition)trans).predIndex;
		}
		else {
			this.ruleIndex = 0;
			this.predicateIndex = 0;
		}

		this.predicate = predicate;
		this.setOffendingToken(recognizer.getCurrentToken());
	}

	public int getRuleIndex() {
		return ruleIndex;
	}

	public int getPredIndex() {
		return predicateIndex;
	}


	public String getPredicate() {
		return predicate;
	}


	private static String formatMessage(String predicate, String message) {
		if (message != null) {
			return message;
		}

		return String.format(Locale.getDefault(), "failed predicate: {%s}?", predicate);
	}
}
