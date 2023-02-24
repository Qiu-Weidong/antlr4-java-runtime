
package runtime;

import runtime.misc.Interval;
import runtime.misc.Pair;

import java.io.Serializable;

public class CommonToken implements WritableToken, Serializable {

	protected static final Pair<TokenSource, CharStream> EMPTY_SOURCE =
		new Pair<TokenSource, CharStream>(null, null);


	protected int type;


	protected int line;


	protected int charPositionInLine = -1;


	protected int channel=DEFAULT_CHANNEL;



	protected Pair<TokenSource, CharStream> source;


	protected String text;


	protected int index = -1;


	protected int start;


	protected int stop;


	public CommonToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
		this.source = source;
		this.type = type;
		this.channel = channel;
		this.start = start;
		this.stop = stop;
		if (source.a != null) {
			this.line = source.a.getLine();
			this.charPositionInLine = source.a.getCharPositionInLine();
		}
	}


	public CommonToken(int type, String text) {
		this.type = type;
		this.channel = DEFAULT_CHANNEL;
		this.text = text;
		this.source = EMPTY_SOURCE;
	}


	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public String getText() {
		if ( text!=null ) {
			return text;
		}

		CharStream input = getInputStream();
		if ( input==null ) return null;
		int n = input.size();
		if ( start<n && stop<n) {
			return input.getText(Interval.of(start,stop));
		}
		else {
			return "<EOF>";
		}
	}


	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getCharPositionInLine() {
		return charPositionInLine;
	}

	@Override
	public void setCharPositionInLine(int charPositionInLine) {
		this.charPositionInLine = charPositionInLine;
	}

	@Override
	public int getChannel() {
		return channel;
	}

	@Override
	public void setChannel(int channel) {
		this.channel = channel;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getStartIndex() {
		return start;
	}

	public void setStartIndex(int start) {
		this.start = start;
	}

	@Override
	public int getStopIndex() {
		return stop;
	}

	public void setStopIndex(int stop) {
		this.stop = stop;
	}

	@Override
	public int getTokenIndex() {
		return index;
	}

	@Override
	public void setTokenIndex(int index) {
		this.index = index;
	}

	@Override
	public TokenSource getTokenSource() {
		return source.a;
	}

	@Override
	public CharStream getInputStream() {
		return source.b;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	public String toString(Recognizer<?, ?> r) {
		String channelStr = "";
		if ( channel>0 ) {
			channelStr=",channel="+channel;
		}
		String txt = getText();
		if ( txt!=null ) {
			txt = txt.replace("\n","\\n");
			txt = txt.replace("\r","\\r");
			txt = txt.replace("\t","\\t");
		}
		else {
			txt = "<no text>";
		}
		String typeString = String.valueOf(type);
		if ( r!=null ) {
			typeString = r.getVocabulary().getDisplayName(type);
		}
		return "[@"+getTokenIndex()+","+start+":"+stop+"='"+txt+"',<"+typeString+">"+channelStr+","+line+":"+getCharPositionInLine()+"]";
	}
}
