

package runtime.atn;

import runtime.Lexer;
import runtime.misc.MurmurHash;


public final class LexerSkipAction implements LexerAction {
	
	public static final LexerSkipAction INSTANCE = new LexerSkipAction();

	
	private LexerSkipAction() {
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.SKIP;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.skip();
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		return MurmurHash.finish(hash, 1);
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return "skip";
	}
}
