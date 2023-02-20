

package runtime;

import runtime.atn.ATNConfigSet;
import runtime.misc.Interval;
import runtime.misc.Utils;

import java.util.Locale;

public class LexerNoViableAltException extends RecognitionException {
	
	private final int startIndex;

	
	private final ATNConfigSet deadEndConfigs;

	public LexerNoViableAltException(Lexer lexer,
									 CharStream input,
									 int startIndex,
									 ATNConfigSet deadEndConfigs) {
		super(lexer, input, null);
		this.startIndex = startIndex;
		this.deadEndConfigs = deadEndConfigs;
	}

	public int getStartIndex() {
		return startIndex;
	}


	public ATNConfigSet getDeadEndConfigs() {
		return deadEndConfigs;
	}

	@Override
	public CharStream getInputStream() {
		return (CharStream)super.getInputStream();
	}

	@Override
	public String toString() {
		String symbol = "";
		if (startIndex >= 0 && startIndex < getInputStream().size()) {
			symbol = getInputStream().getText(Interval.of(startIndex,startIndex));
			symbol = Utils.escapeWhitespace(symbol, false);
		}

		return String.format(Locale.getDefault(), "%s('%s')", LexerNoViableAltException.class.getSimpleName(), symbol);
	}
}
