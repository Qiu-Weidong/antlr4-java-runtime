

package runtime;

import runtime.misc.Pair;


public interface TokenFactory<Symbol extends Token> {

	Symbol create(Pair<TokenSource, CharStream> source, int type, String text,
				  int channel, int start, int stop,
				  int line, int charPositionInLine);


	Symbol create(int type, String text);
}
