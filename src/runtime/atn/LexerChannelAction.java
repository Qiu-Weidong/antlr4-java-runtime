

package runtime.atn;

import runtime.Lexer;
import runtime.Token;
import runtime.misc.MurmurHash;


public final class LexerChannelAction implements LexerAction {
	private final int channel;

	
	public LexerChannelAction(int channel) {
		this.channel = channel;
	}

	
	public int getChannel() {
		return channel;
	}

	
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.CHANNEL;
	}

	
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	
	@Override
	public void execute(Lexer lexer) {
		lexer.setChannel(channel);
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		hash = MurmurHash.update(hash, channel);
		return MurmurHash.finish(hash, 2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		else if (!(obj instanceof LexerChannelAction)) {
			return false;
		}

		return channel == ((LexerChannelAction)obj).channel;
	}

	@Override
	public String toString() {
		return String.format("channel(%d)", channel);
	}
}
