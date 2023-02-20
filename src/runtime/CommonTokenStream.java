

package runtime;


public class CommonTokenStream extends BufferedTokenStream {
	/**
	 * Specifies the channel to use for filtering tokens.
	 *
	 * <p>
	 * The default value is {@link Token#DEFAULT_CHANNEL}, which matches the
	 * default channel assigned to tokens created by the lexer.</p>
	 */
    protected int channel = Token.DEFAULT_CHANNEL;

	/**
	 * Constructs a new {@link CommonTokenStream} using the specified token
	 * source and the default token channel ({@link Token#DEFAULT_CHANNEL}).
	 *
	 * @param tokenSource The token source.
	 */
    public CommonTokenStream(TokenSource tokenSource) {
        super(tokenSource);
    }

	/**
	 * Constructs a new {@link CommonTokenStream} using the specified token
	 * source and filtering tokens to the specified channel. Only tokens whose
	 * {@link Token#getChannel} matches {@code channel} or have the
	 * {@link Token#getType} equal to {@link Token#EOF} will be returned by the
	 * token stream lookahead methods.
	 *
	 * @param tokenSource The token source.
	 * @param channel The channel to use for filtering tokens.
	 */
    public CommonTokenStream(TokenSource tokenSource, int channel) {
        this(tokenSource);
        this.channel = channel;
    }

	@Override
	protected int adjustSeekIndex(int i) {
		return nextTokenOnChannel(i, channel);
	}

    @Override
    protected Token LB(int k) {
        if ( k==0 || (p-k)<0 ) return null;

        int i = p;
        int n = 1;
        // find k good tokens looking backwards
        while ( n<=k && i>0 ) {
            // skip off-channel tokens
            i = previousTokenOnChannel(i - 1, channel);
            n++;
        }
        if ( i<0 ) return null;
        return tokens.get(i);
    }

    @Override
    public Token LT(int k) {
        //System.out.println("enter LT("+k+")");
        lazyInit();
        if ( k == 0 ) return null;
        if ( k < 0 ) return LB(-k);
        int i = p;
        int n = 1; // we know tokens[p] is a good one
        // find k good tokens
        while ( n<k ) {
            // skip off-channel tokens, but make sure to not look past EOF
			if (sync(i + 1)) {
				i = nextTokenOnChannel(i + 1, channel);
			}
            n++;
        }
//		if ( i>range ) range = i;
        return tokens.get(i);
    }

	/** Count EOF just once. */
	public int getNumberOfOnChannelTokens() {
		int n = 0;
		fill();
		for (int i = 0; i < tokens.size(); i++) {
			Token t = tokens.get(i);
			if ( t.getChannel()==channel ) n++;
			if ( t.getType()==Token.EOF ) break;
		}
		return n;
	}
}
