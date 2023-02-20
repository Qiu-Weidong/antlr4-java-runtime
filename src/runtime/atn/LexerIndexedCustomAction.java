

package runtime.atn;

import runtime.CharStream;
import runtime.Lexer;
import runtime.misc.MurmurHash;


public final class LexerIndexedCustomAction implements LexerAction {
	private final int offset;
	private final LexerAction action;

	
	public LexerIndexedCustomAction(int offset, LexerAction action) {
		this.offset = offset;
		this.action = action;
	}

	
	public int getOffset() {
		return offset;
	}

	
	public LexerAction getAction() {
		return action;
	}

	
	@Override
	public LexerActionType getActionType() {
		return action.getActionType();
	}

	
	@Override
	public boolean isPositionDependent() {
		return true;
	}

	
	@Override
	public void execute(Lexer lexer) {

		action.execute(lexer);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, offset);
		hash = MurmurHash.update(hash, action);
		return MurmurHash.finish(hash, 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof LexerIndexedCustomAction)) {
			return false;
		}

		LexerIndexedCustomAction other = (LexerIndexedCustomAction)obj;
		return offset == other.offset
			&& action.equals(other.action);
	}

}
