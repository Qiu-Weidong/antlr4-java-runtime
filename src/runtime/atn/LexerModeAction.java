

package runtime.atn;

import runtime.Lexer;
import runtime.misc.MurmurHash;


public final class LexerModeAction implements LexerAction {
	private final int mode;

	
	public LexerModeAction(int mode) {
		this.mode = mode;
	}

	
	public int getMode() {
		return mode;
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.MODE;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.mode(mode);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		hash = MurmurHash.update(hash, mode);
		return MurmurHash.finish(hash, 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof LexerModeAction)) {
			return false;
		}

		return mode == ((LexerModeAction)obj).mode;
	}

	@Override
	public String toString() {
		return String.format("mode(%d)", mode);
	}
}
