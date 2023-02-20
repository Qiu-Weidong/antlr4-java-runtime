

package runtime;

import runtime.atn.ATNConfigSet;
import runtime.atn.DecisionInfo;
import runtime.atn.ParserATNSimulator;
import runtime.atn.PredictionMode;
import runtime.dfa.DFA;

import java.util.BitSet;


public interface ANTLRErrorListener {
	
	public void syntaxError(Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e);

	
	void reportAmbiguity(Parser recognizer,
						 DFA dfa,
						 int startIndex,
						 int stopIndex,
						 boolean exact,
						 BitSet ambigAlts,
						 ATNConfigSet configs);

	
	void reportAttemptingFullContext(Parser recognizer,
									 DFA dfa,
									 int startIndex,
									 int stopIndex,
									 BitSet conflictingAlts,
									 ATNConfigSet configs);

	
	void reportContextSensitivity(Parser recognizer,
								  DFA dfa,
								  int startIndex,
								  int stopIndex,
								  int prediction,
								  ATNConfigSet configs);
}
