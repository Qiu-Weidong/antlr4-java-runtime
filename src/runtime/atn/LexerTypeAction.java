

package runtime.atn;

import runtime.Lexer;
import runtime.misc.MurmurHash;


public class LexerTypeAction implements LexerAction {
	private final int type;

	
	public LexerTypeAction(int type) {
		this.type = type;
	}

	
	public int getType() {
		return type;
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.TYPE;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.setType(type);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		hash = MurmurHash.update(hash, type);
		return MurmurHash.finish(hash, 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof LexerTypeAction)) {
			return false;
		}

		return type == ((LexerTypeAction)obj).type;
	}

	@Override
	public String toString() {
		return String.format("type(%d)", type);
	}
}
