
package runtime;


public class NoViableAltException extends RecognitionException {


	private final Token startToken;

	public NoViableAltException(Parser recognizer) { // LL(1) error
		this(recognizer,
			 recognizer.getInputStream(),
			 recognizer.getCurrentToken(),
			 recognizer.getCurrentToken(),
				recognizer._ctx);
	}

	public NoViableAltException(Parser recognizer,
								TokenStream input,
								Token startToken,
								Token offendingToken,
								ParserRuleContext ctx)
	{
		super(recognizer, input, ctx);
		this.startToken = startToken;
		this.setOffendingToken(offendingToken);
	}


	public Token getStartToken() {
		return startToken;
	}


}
