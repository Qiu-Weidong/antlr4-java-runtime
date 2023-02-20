

package runtime;

import runtime.atn.ATNConfig;
import runtime.atn.ATNConfigSet;
import runtime.dfa.DFA;
import runtime.misc.Interval;

import java.util.BitSet;


public class DiagnosticErrorListener extends BaseErrorListener {
	
	protected final boolean exactOnly;

	
	public DiagnosticErrorListener() {
		this(true);
	}

	
	public DiagnosticErrorListener(boolean exactOnly) {
		this.exactOnly = exactOnly;
	}

	@Override
	public void reportAmbiguity(Parser recognizer,
								DFA dfa,
								int startIndex,
								int stopIndex,
								boolean exact,
								BitSet ambigAlts,
								ATNConfigSet configs)
	{
		if (exactOnly && !exact) {
			return;
		}

		String format = "reportAmbiguity d=%s: ambigAlts=%s, input='%s'";
		String decision = getDecisionDescription(recognizer, dfa);
		BitSet conflictingAlts = getConflictingAlts(ambigAlts, configs);
		String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
		String message = String.format(format, decision, conflictingAlts, text);
		recognizer.notifyErrorListeners(message);
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer,
											DFA dfa,
											int startIndex,
											int stopIndex,
											BitSet conflictingAlts,
											ATNConfigSet configs)
	{
		String format = "reportAttemptingFullContext d=%s, input='%s'";
		String decision = getDecisionDescription(recognizer, dfa);
		String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
		String message = String.format(format, decision, text);
		recognizer.notifyErrorListeners(message);
	}

	@Override
	public void reportContextSensitivity(Parser recognizer,
										 DFA dfa,
										 int startIndex,
										 int stopIndex,
										 int prediction,
										 ATNConfigSet configs)
	{
		String format = "reportContextSensitivity d=%s, input='%s'";
		String decision = getDecisionDescription(recognizer, dfa);
		String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
		String message = String.format(format, decision, text);
		recognizer.notifyErrorListeners(message);
	}

	protected String getDecisionDescription(Parser recognizer, DFA dfa) {
		int decision = dfa.decision;
		int ruleIndex = dfa.atnStartState.ruleIndex;

		String[] ruleNames = recognizer.getRuleNames();
		if (ruleIndex < 0 || ruleIndex >= ruleNames.length) {
			return String.valueOf(decision);
		}

		String ruleName = ruleNames[ruleIndex];
		if (ruleName == null || ruleName.isEmpty()) {
			return String.valueOf(decision);
		}

		return String.format("%d (%s)", decision, ruleName);
	}

	
	protected BitSet getConflictingAlts(BitSet reportedAlts, ATNConfigSet configs) {
		if (reportedAlts != null) {
			return reportedAlts;
		}

		BitSet result = new BitSet();
		for (ATNConfig config : configs) {
			result.set(config.alt);
		}

		return result;
	}
}
