

package runtime.atn;

import runtime.Lexer;
import runtime.misc.MurmurHash;


public final class LexerPushModeAction implements LexerAction {
	private final int mode;

	
	public LexerPushModeAction(int mode) {
		this.mode = mode;
	}

	
	public int getMode() {
		return mode;
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.PUSH_MODE;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.pushMode(mode);
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
		else if (!(obj instanceof LexerPushModeAction)) {
			return false;
		}

		return mode == ((LexerPushModeAction)obj).mode;
	}

	@Override
	public String toString() {
		return String.format("pushMode(%d)", mode);
	}
}
