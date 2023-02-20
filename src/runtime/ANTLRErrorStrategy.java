

package runtime;

import runtime.tree.ErrorNode;


public interface ANTLRErrorStrategy {
	
	void reset(Parser recognizer);

	
	Token recoverInline(Parser recognizer) throws RecognitionException;

	
	void recover(Parser recognizer, RecognitionException e) throws RecognitionException;

	
	void sync(Parser recognizer) throws RecognitionException;

	
	boolean inErrorRecoveryMode(Parser recognizer);

	
	void reportMatch(Parser recognizer);

	
	void reportError(Parser recognizer, RecognitionException e);
}
