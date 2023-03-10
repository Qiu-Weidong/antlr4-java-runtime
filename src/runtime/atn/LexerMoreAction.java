

package runtime.atn;

import runtime.Lexer;
import runtime.misc.MurmurHash;

// 这是一个单例
public final class LexerMoreAction implements LexerAction {
	
	public static final LexerMoreAction INSTANCE = new LexerMoreAction();

	
	private LexerMoreAction() {
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.MORE;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.more();
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
		return "more";
	}
}
