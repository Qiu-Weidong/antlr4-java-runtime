

package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNState;
import runtime.atn.RuleTransition;
import runtime.misc.IntervalSet;
import runtime.misc.Pair;


public class DefaultErrorStrategy implements ANTLRErrorStrategy {
	
	protected boolean errorRecoveryMode = false;

	
	protected int lastErrorIndex = -1;

	protected IntervalSet lastErrorStates;

	
	protected ParserRuleContext nextTokensContext;

	
	protected int nextTokensState;

	
	@Override
	public void reset(Parser recognizer) {
		endErrorCondition(recognizer);
	}

	
	protected void beginErrorCondition(Parser recognizer) {
		errorRecoveryMode = true;
	}

	
	@Override
	public boolean inErrorRecoveryMode(Parser recognizer) {
		return errorRecoveryMode;
	}

	
	protected void endErrorCondition(Parser recognizer) {
		errorRecoveryMode = false;
		lastErrorStates = null;
		lastErrorIndex = -1;
	}

	
	@Override
	public void reportMatch(Parser recognizer) {
		endErrorCondition(recognizer);
	}

	
	@Override
	public void reportError(Parser recognizer,
							RecognitionException e)
	{

		if (inErrorRecoveryMode(recognizer)) {

			return;
		}
		beginErrorCondition(recognizer);
		if ( e instanceof NoViableAltException ) {
			reportNoViableAlternative(recognizer, (NoViableAltException) e);
		}
		else if ( e instanceof InputMismatchException ) {
			reportInputMismatch(recognizer, (InputMismatchException)e);
		}
		else if ( e instanceof FailedPredicateException ) {
			reportFailedPredicate(recognizer, (FailedPredicateException)e);
		}
		else {
			System.err.println("unknown recognition error type: "+e.getClass().getName());
			recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
		}
	}

	
	@Override
	public void recover(Parser recognizer, RecognitionException e) {

		if ( lastErrorIndex==recognizer.getInputStream().index() &&
			lastErrorStates != null &&
			lastErrorStates.contains(recognizer.getState()) ) {

			recognizer.consume();
		}
		lastErrorIndex = recognizer.getInputStream().index();
		if ( lastErrorStates==null ) lastErrorStates = new IntervalSet();
		lastErrorStates.add(recognizer.getState());
		IntervalSet followSet = getErrorRecoverySet(recognizer);
		consumeUntil(recognizer, followSet);
	}

	
	@Override
	public void sync(Parser recognizer) throws RecognitionException {
		ATNState s = recognizer.getInterpreter().atn.states.get(recognizer.getState());

		if (inErrorRecoveryMode(recognizer)) {
			return;
		}

        TokenStream tokens = recognizer.getInputStream();
        int la = tokens.LA(1);


		IntervalSet nextTokens = recognizer.getATN().nextTokens(s);
		if (nextTokens.contains(la)) {
			nextTokensContext = null;
			nextTokensState = ATNState.INVALID_STATE_NUMBER;
			return;
		}

		if (nextTokens.contains(Token.EPSILON)) {
			if (nextTokensContext == null) {

				nextTokensContext = recognizer.getContext();
				nextTokensState = recognizer.getState();
			}
			return;
		}

		switch (s.getStateType()) {
		case ATNState.BLOCK_START:
		case ATNState.STAR_BLOCK_START:
		case ATNState.PLUS_BLOCK_START:
		case ATNState.STAR_LOOP_ENTRY:

			if ( singleTokenDeletion(recognizer)!=null ) {
				return;
			}

			throw new InputMismatchException(recognizer);

		case ATNState.PLUS_LOOP_BACK:
		case ATNState.STAR_LOOP_BACK:

			reportUnwantedToken(recognizer);
			IntervalSet expecting = recognizer.getExpectedTokens();
			IntervalSet whatFollowsLoopIterationOrRule =
				expecting.or(getErrorRecoverySet(recognizer));
			consumeUntil(recognizer, whatFollowsLoopIterationOrRule);
			break;

		default:

			break;
		}
	}

	
	protected void reportNoViableAlternative(Parser recognizer,
											 NoViableAltException e)
	{
		TokenStream tokens = recognizer.getInputStream();
		String input;
		if ( tokens!=null ) {
			if ( e.getStartToken().getType()==Token.EOF ) input = "<EOF>";
			else input = tokens.getText(e.getStartToken(), e.getOffendingToken());
		}
		else {
			input = "<unknown input>";
		}
		String msg = "no viable alternative at input "+escapeWSAndQuote(input);
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
	}

	
	protected void reportInputMismatch(Parser recognizer,
									   InputMismatchException e)
	{
		String msg = "mismatched input "+getTokenErrorDisplay(e.getOffendingToken())+
		" expecting "+e.getExpectedTokens().toString(recognizer.getVocabulary());
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
	}

	
	protected void reportFailedPredicate(Parser recognizer,
										 FailedPredicateException e)
	{
		String ruleName = recognizer.getRuleNames()[recognizer._ctx.getRuleIndex()];
		String msg = "rule "+ruleName+" "+e.getMessage();
		recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
	}

	
	protected void reportUnwantedToken(Parser recognizer) {
		if (inErrorRecoveryMode(recognizer)) {
			return;
		}

		beginErrorCondition(recognizer);

		Token t = recognizer.getCurrentToken();
		String tokenName = getTokenErrorDisplay(t);
		IntervalSet expecting = getExpectedTokens(recognizer);
		String msg = "extraneous input "+tokenName+" expecting "+
			expecting.toString(recognizer.getVocabulary());
		recognizer.notifyErrorListeners(t, msg, null);
	}

	
	protected void reportMissingToken(Parser recognizer) {
		if (inErrorRecoveryMode(recognizer)) {
			return;
		}

		beginErrorCondition(recognizer);

		Token t = recognizer.getCurrentToken();
		IntervalSet expecting = getExpectedTokens(recognizer);
		String msg = "missing "+expecting.toString(recognizer.getVocabulary())+
			" at "+getTokenErrorDisplay(t);

		recognizer.notifyErrorListeners(t, msg, null);
	}

	
	@Override
	public Token recoverInline(Parser recognizer)
		throws RecognitionException
	{

		Token matchedSymbol = singleTokenDeletion(recognizer);
		if ( matchedSymbol!=null ) {

			recognizer.consume();
			return matchedSymbol;
		}


		if ( singleTokenInsertion(recognizer) ) {
			return getMissingSymbol(recognizer);
		}

		// even that didn't work; must throw the exception
		InputMismatchException e;
		if (nextTokensContext == null) {
			e = new InputMismatchException(recognizer);
		} else {
			e = new InputMismatchException(recognizer, nextTokensState, nextTokensContext);
		}

		throw e;
	}

	
	protected boolean singleTokenInsertion(Parser recognizer) {
		int currentSymbolType = recognizer.getInputStream().LA(1);

		ATNState currentState = recognizer.getInterpreter().atn.states.get(recognizer.getState());
		ATNState next = currentState.transition(0).target;
		ATN atn = recognizer.getInterpreter().atn;
		IntervalSet expectingAtLL2 = atn.nextTokens(next, recognizer._ctx);

		if ( expectingAtLL2.contains(currentSymbolType) ) {
			reportMissingToken(recognizer);
			return true;
		}
		return false;
	}

	
	protected Token singleTokenDeletion(Parser recognizer) {
		int nextTokenType = recognizer.getInputStream().LA(2);
		IntervalSet expecting = getExpectedTokens(recognizer);
		if ( expecting.contains(nextTokenType) ) {
			reportUnwantedToken(recognizer);
			
			recognizer.consume();
			Token matchedSymbol = recognizer.getCurrentToken();
			reportMatch(recognizer);
			return matchedSymbol;
		}
		return null;
	}

	
	protected Token getMissingSymbol(Parser recognizer) {
		Token currentSymbol = recognizer.getCurrentToken();
		IntervalSet expecting = getExpectedTokens(recognizer);
		int expectedTokenType = Token.INVALID_TYPE;
		if ( !expecting.isNil() ) {
			expectedTokenType = expecting.getMinElement(); // get any element
		}
		String tokenText;
		if ( expectedTokenType== Token.EOF ) tokenText = "<missing EOF>";
		else tokenText = "<missing "+recognizer.getVocabulary().getDisplayName(expectedTokenType)+">";
		Token current = currentSymbol;
		Token lookback = recognizer.getInputStream().LT(-1);
		if ( current.getType() == Token.EOF && lookback!=null ) {
			current = lookback;
		}
		return
			recognizer.getTokenFactory().create(new Pair<TokenSource, CharStream>(current.getTokenSource(), current.getTokenSource().getInputStream()), expectedTokenType, tokenText,
							Token.DEFAULT_CHANNEL,
							-1, -1,
							current.getLine(), current.getCharPositionInLine());
	}


	protected IntervalSet getExpectedTokens(Parser recognizer) {
		return recognizer.getExpectedTokens();
	}

	
	protected String getTokenErrorDisplay(Token t) {
		if ( t==null ) return "<no token>";
		String s = getSymbolText(t);
		if ( s==null ) {
			if ( getSymbolType(t)==Token.EOF ) {
				s = "<EOF>";
			}
			else {
				s = "<"+getSymbolType(t)+">";
			}
		}
		return escapeWSAndQuote(s);
	}

	protected String getSymbolText(Token symbol) {
		return symbol.getText();
	}

	protected int getSymbolType(Token symbol) {
		return symbol.getType();
	}


	protected String escapeWSAndQuote(String s) {

		s = s.replace("\n","\\n");
		s = s.replace("\r","\\r");
		s = s.replace("\t","\\t");
		return "'"+s+"'";
	}
	
	protected IntervalSet getErrorRecoverySet(Parser recognizer) {
		ATN atn = recognizer.getInterpreter().atn;
		RuleContext ctx = recognizer._ctx;
		IntervalSet recoverSet = new IntervalSet();
		while ( ctx!=null && ctx.invokingState>=0 ) {
			// compute what follows who invoked us
			ATNState invokingState = atn.states.get(ctx.invokingState);
			RuleTransition rt = (RuleTransition)invokingState.transition(0);
			IntervalSet follow = atn.nextTokens(rt.followState);
			recoverSet.addAll(follow);
			ctx = ctx.parent;
		}
        recoverSet.remove(Token.EPSILON);

		return recoverSet;
	}

	
	protected void consumeUntil(Parser recognizer, IntervalSet set) {

		int ttype = recognizer.getInputStream().LA(1);
		while (ttype != Token.EOF && !set.contains(ttype) ) {

            recognizer.consume();
            ttype = recognizer.getInputStream().LA(1);
        }
    }
}
