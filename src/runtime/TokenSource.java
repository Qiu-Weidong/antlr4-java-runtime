
package runtime;


public interface TokenSource {

	public Token nextToken();


	public int getLine();


	public int getCharPositionInLine();


	public CharStream getInputStream();


	public String getSourceName();


	public void setTokenFactory(TokenFactory<?> factory);


	public TokenFactory<?> getTokenFactory();
}
