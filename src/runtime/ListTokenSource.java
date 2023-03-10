

package runtime;

import runtime.misc.Pair;

import java.util.List;


public class ListTokenSource implements TokenSource {

	protected final List<? extends Token> tokens;


	private final String sourceName;


	protected int i;


	protected Token eofToken;


	private TokenFactory<?> _factory = CommonTokenFactory.DEFAULT;


	public ListTokenSource(List<? extends Token> tokens) {
		this(tokens, null);
	}


	public ListTokenSource(List<? extends Token> tokens, String sourceName) {
		if (tokens == null) {
			throw new NullPointerException("tokens cannot be null");
		}

		this.tokens = tokens;
		this.sourceName = sourceName;
	}


	@Override
	public int getCharPositionInLine() {
		if (i < tokens.size()) {
			return tokens.get(i).getCharPositionInLine();
		}
		else if (eofToken != null) {
			return eofToken.getCharPositionInLine();
		}
		else if (tokens.size() > 0) {


			Token lastToken = tokens.get(tokens.size() - 1);
			String tokenText = lastToken.getText();
			if (tokenText != null) {
				int lastNewLine = tokenText.lastIndexOf('\n');
				if (lastNewLine >= 0) {
					return tokenText.length() - lastNewLine - 1;
				}
			}

			return lastToken.getCharPositionInLine() + lastToken.getStopIndex() - lastToken.getStartIndex() + 1;
		}



		return 0;
	}


	@Override
	public Token nextToken() {
		if (i >= tokens.size()) {
			if (eofToken == null) {
				int start = -1;
				if (tokens.size() > 0) {
					int previousStop = tokens.get(tokens.size() - 1).getStopIndex();
					if (previousStop != -1) {
						start = previousStop + 1;
					}
				}

				int stop = Math.max(-1, start - 1);
				eofToken = _factory.create(new Pair<TokenSource, CharStream>(this, getInputStream()), Token.EOF, "EOF", Token.DEFAULT_CHANNEL, start, stop, getLine(), getCharPositionInLine());
			}

			return eofToken;
		}

		Token t = tokens.get(i);
		if (i == tokens.size() - 1 && t.getType() == Token.EOF) {
			eofToken = t;
		}

		i++;
		return t;
	}


	@Override
	public int getLine() {
		if (i < tokens.size()) {
			return tokens.get(i).getLine();
		}
		else if (eofToken != null) {
			return eofToken.getLine();
		}
		else if (tokens.size() > 0) {
			// have to calculate the result from the line/column of the previous
			// token, along with the text of the token.
			Token lastToken = tokens.get(tokens.size() - 1);
			int line = lastToken.getLine();

			String tokenText = lastToken.getText();
			if (tokenText != null) {
				for (int i = 0; i < tokenText.length(); i++) {
					if (tokenText.charAt(i) == '\n') {
						line++;
					}
				}
			}

			// if no text is available, assume the token did not contain any newline characters.
			return line;
		}

		// only reach this if tokens is empty, meaning EOF occurs at the first
		// position in the input
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharStream getInputStream() {
		if (i < tokens.size()) {
			return tokens.get(i).getInputStream();
		}
		else if (eofToken != null) {
			return eofToken.getInputStream();
		}
		else if (tokens.size() > 0) {
			return tokens.get(tokens.size() - 1).getInputStream();
		}

		// no input stream information is available
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSourceName() {
		if (sourceName != null) {
			return sourceName;
		}

		CharStream inputStream = getInputStream();
		if (inputStream != null) {
			return inputStream.getSourceName();
		}

		return "List";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		this._factory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TokenFactory<?> getTokenFactory() {
		return _factory;
	}
}
