

package runtime.atn;

import runtime.CharStream;
import runtime.Lexer;
import runtime.Recognizer;
import runtime.misc.MurmurHash;


public final class LexerCustomAction implements LexerAction {
	private final int ruleIndex;
	private final int actionIndex;

	
	public LexerCustomAction(int ruleIndex, int actionIndex) {
		this.ruleIndex = ruleIndex;
		this.actionIndex = actionIndex;
	}

	
	public int getRuleIndex() {
		return ruleIndex;
	}

	
	public int getActionIndex() {
		return actionIndex;
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.CUSTOM;
	}

	
	@Override
	public boolean isPositionDependent() {
		return true;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.action(null, ruleIndex, actionIndex);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		hash = MurmurHash.update(hash, ruleIndex);
		hash = MurmurHash.update(hash, actionIndex);
		return MurmurHash.finish(hash, 3);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof LexerCustomAction)) {
			return false;
		}

		LexerCustomAction other = (LexerCustomAction)obj;
		return ruleIndex == other.ruleIndex
			&& actionIndex == other.actionIndex;
	}
}
