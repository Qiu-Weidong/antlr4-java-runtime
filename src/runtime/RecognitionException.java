
package runtime;

import runtime.atn.DecisionState;
import runtime.misc.IntervalSet;


public class RecognitionException extends RuntimeException {
	
	private final Recognizer<?, ?> recognizer;

	private final RuleContext ctx;

	private final IntStream input;

	
	private Token offendingToken;

	private int offendingState = -1;

	public RecognitionException(Recognizer<?, ?> recognizer,
								IntStream input,
								ParserRuleContext ctx)
	{
		this.recognizer = recognizer;
		this.input = input;
		this.ctx = ctx;
		if ( recognizer!=null ) this.offendingState = recognizer.getState();
	}

	public RecognitionException(String message,
								Recognizer<?, ?> recognizer,
								IntStream input,
								ParserRuleContext ctx)
	{
		super(message);
		this.recognizer = recognizer;
		this.input = input;
		this.ctx = ctx;
		if ( recognizer!=null ) this.offendingState = recognizer.getState();
	}

	
	public int getOffendingState() {
		return offendingState;
	}

	protected final void setOffendingState(int offendingState) {
		this.offendingState = offendingState;
	}

	
	public IntervalSet getExpectedTokens() {
		if (recognizer != null) {
			return recognizer.getATN().getExpectedTokens(offendingState, ctx);
		}

		return null;
	}

	
	public RuleContext getCtx() {
		return ctx;
	}

	
	public IntStream getInputStream() {
		return input;
	}


	public Token getOffendingToken() {
		return offendingToken;
	}

	protected final void setOffendingToken(Token offendingToken) {
		this.offendingToken = offendingToken;
	}

	
	public Recognizer<?, ?> getRecognizer() {
		return recognizer;
	}
}
