

package runtime;

import runtime.misc.Interval;


public interface TokenStream extends IntStream {

	public Token LT(int k);


	public Token get(int index);


	public TokenSource getTokenSource();


	public String getText(Interval interval);


	public String getText();


	public String getText(RuleContext ctx);


	public String getText(Token start, Token stop);
}
