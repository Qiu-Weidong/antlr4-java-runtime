

package runtime.atn;

import runtime.CharStream;
import runtime.Lexer;


public interface LexerAction {
	
	LexerActionType getActionType();

	
	boolean isPositionDependent();

	
	void execute(Lexer lexer);
}
