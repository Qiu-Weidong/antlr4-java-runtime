
package runtime;

import runtime.atn.LexerATNSimulator;
import runtime.misc.IntegerStack;
import runtime.misc.Interval;
import runtime.misc.Pair;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;


public abstract class Lexer extends Recognizer<Integer, LexerATNSimulator>
	implements TokenSource
{
	public static final int DEFAULT_MODE = 0;
	public static final int MORE = -2;
	public static final int SKIP = -3;

	public static final int DEFAULT_TOKEN_CHANNEL = Token.DEFAULT_CHANNEL;
	public static final int HIDDEN = Token.HIDDEN_CHANNEL;
	public static final int MIN_CHAR_VALUE = 0x0000;
	public static final int MAX_CHAR_VALUE = 0x10FFFF;

	public CharStream _input;
	protected Pair<TokenSource, CharStream> _tokenFactorySourcePair;


	protected TokenFactory<?> _factory = CommonTokenFactory.DEFAULT;


	public Token _token;


	public int _tokenStartCharIndex = -1;


	public int _tokenStartLine;


	public int _tokenStartCharPositionInLine;


	public boolean _hitEOF;


	public int _channel;


	public int _type;

	public final IntegerStack _modeStack = new IntegerStack();
	public int _mode = Lexer.DEFAULT_MODE;


	public String _text;

	public Lexer() { }

	public Lexer(CharStream input) {
		this._input = input;
		this._tokenFactorySourcePair = new Pair<TokenSource, CharStream>(this, input);
	}

	public void reset() {
		// wack Lexer state variables
		if ( _input !=null ) {
			_input.seek(0); // rewind the input
		}
		_token = null;
		_type = Token.INVALID_TYPE;
		_channel = Token.DEFAULT_CHANNEL;
		_tokenStartCharIndex = -1;
		_tokenStartCharPositionInLine = -1;
		_tokenStartLine = -1;
		_text = null;

		_hitEOF = false;
		_mode = Lexer.DEFAULT_MODE;
		_modeStack.clear();

		getInterpreter().reset();
	}


	@Override
	public Token nextToken() {
		if (_input == null) {
			throw new IllegalStateException("nextToken requires a non-null input stream.");
		}

		int tokenStartMarker = _input.mark();
		try{
			while (true) {
				if (_hitEOF) {
					emitEOF();
					return _token;
				}

				_token = null;
				_channel = Token.DEFAULT_CHANNEL;
				_tokenStartCharIndex = _input.index();
				_tokenStartCharPositionInLine = getInterpreter().getCharPositionInLine();
				_tokenStartLine = getInterpreter().getLine();
				_text = null;
				do {
					_type = Token.INVALID_TYPE;
					int ttype;
					try {
						ttype = getInterpreter().match(_input, _mode);
					} catch (LexerNoViableAltException e) {
						notifyListeners(e);        // report error
						recover(e);
						ttype = SKIP;
					}
					if (_input.LA(1) == IntStream.EOF) {
						_hitEOF = true;
					}
					if (_type == Token.INVALID_TYPE) _type = ttype;

				} while (_type == MORE);
				if (_type == SKIP) {
					continue;
				}
				if (_token == null) emit();
				return _token;
			}
		}
		finally {
			// make sure we release marker after match or
			// unbuffered char stream will keep buffering
			_input.release(tokenStartMarker);
		}
	}

	public void skip() {
		_type = SKIP;
	}

	public void more() {
		_type = MORE;
	}

	public void mode(int m) {
		_mode = m;
	}

	public void pushMode(int m) {
		if ( LexerATNSimulator.debug ) System.out.println("pushMode "+m);
		_modeStack.push(_mode);
		mode(m);
	}

	public int popMode() {
		if ( _modeStack.isEmpty() ) throw new EmptyStackException();
		if ( LexerATNSimulator.debug ) System.out.println("popMode back to "+ _modeStack.peek());
		mode( _modeStack.pop() );
		return _mode;
	}

	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		this._factory = factory;
	}

	@Override
	public TokenFactory<? extends Token> getTokenFactory() {
		return _factory;
	}

	
	@Override
	public void setInputStream(IntStream input) {
		this._input = null;
		this._tokenFactorySourcePair = new Pair<TokenSource, CharStream>(this, _input);
		reset();
		this._input = (CharStream)input;
		this._tokenFactorySourcePair = new Pair<TokenSource, CharStream>(this, _input);
	}

	@Override
	public String getSourceName() {
		return _input.getSourceName();
	}

	@Override
	public CharStream getInputStream() {
		return _input;
	}

	public void emit(Token token) {
		//System.err.println("emit "+token);
		this._token = token;
	}

	public void emit() {
		Token t = _factory.create(_tokenFactorySourcePair, _type, _text, _channel, _tokenStartCharIndex, getCharIndex()-1,
								  _tokenStartLine, _tokenStartCharPositionInLine);
		emit(t);
	}

	public void emitEOF() {
		int cpos = getCharPositionInLine();
		int line = getLine();
		Token eof = _factory.create(_tokenFactorySourcePair, Token.EOF,
				null, Token.DEFAULT_CHANNEL,
				_input.index(),
				_input.index()-1,
									line, cpos);
		emit(eof);
	}

	@Override
	public int getLine() {
		return getInterpreter().getLine();
	}

	@Override
	public int getCharPositionInLine() {
		return getInterpreter().getCharPositionInLine();
	}

	public void setLine(int line) {
		getInterpreter().setLine(line);
	}

	public void setCharPositionInLine(int charPositionInLine) {
		getInterpreter().setCharPositionInLine(charPositionInLine);
	}

	public int getCharIndex() {
		return _input.index();
	}

	public String getText() {
		if ( _text !=null ) {
			return _text;
		}
		return getInterpreter().getText(_input);
	}

	public void setText(String text) {
		this._text = text;
	}

	public Token getToken() { return _token; }

	public void setToken(Token _token) {
		this._token = _token;
	}

	public void setType(int ttype) {
		_type = ttype;
	}

	public int getType() {
		return _type;
	}

	public void setChannel(int channel) {
		_channel = channel;
	}

	public int getChannel() {
		return _channel;
	}

	public String[] getChannelNames() { return null; }

	public String[] getModeNames() {
		return null;
	}

	public List<? extends Token> getAllTokens() {
		List<Token> tokens = new ArrayList<Token>();
		Token t = nextToken();
		while ( t.getType()!=Token.EOF ) {
			tokens.add(t);
			t = nextToken();
		}
		return tokens;
	}

	public void recover(LexerNoViableAltException e) {
		if (_input.LA(1) != IntStream.EOF) {
			// skip a char and try again
			getInterpreter().consume(_input);
		}
	}

	public void notifyListeners(LexerNoViableAltException e) {
		String text = _input.getText(Interval.of(_tokenStartCharIndex, _input.index()));
		String msg = "token recognition error at: '"+ getErrorDisplay(text) + "'";

		ANTLRErrorListener listener = getErrorListenerDispatch();
		listener.syntaxError(this, null, _tokenStartLine, _tokenStartCharPositionInLine, msg, e);
	}

	public String getErrorDisplay(String s) {
		StringBuilder buf = new StringBuilder();
		for (char c : s.toCharArray()) {
			buf.append(getErrorDisplay(c));
		}
		return buf.toString();
	}

	public String getErrorDisplay(int c) {
		String s = String.valueOf((char)c);
		switch ( c ) {
			case Token.EOF :
				s = "<EOF>";
				break;
			case '\n' :
				s = "\\n";
				break;
			case '\t' :
				s = "\\t";
				break;
			case '\r' :
				s = "\\r";
				break;
		}
		return s;
	}


}
