

package runtime;

import runtime.misc.Interval;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class UnbufferedCharStream implements CharStream {
	
	protected int[] data;

	
   	protected int n;

	
   	protected int p=0;

	
	protected int numMarkers = 0;

	
	protected int lastChar = -1;

	
	protected int lastCharBufferStart;

	
    protected int currentCharIndex = 0;

    protected Reader input;

	
	public String name;

	
	public UnbufferedCharStream() {
		this(256);
	}

	
	public UnbufferedCharStream(int bufferSize) {
		n = 0;
		data = new int[bufferSize];
	}

	public UnbufferedCharStream(InputStream input) {
		this(input, 256);
	}

	public UnbufferedCharStream(Reader input) {
		this(input, 256);
	}

	public UnbufferedCharStream(InputStream input, int bufferSize) {
		this(input, bufferSize, StandardCharsets.UTF_8);
	}

	public UnbufferedCharStream(InputStream input, int bufferSize, Charset charset) {
		this(bufferSize);
		this.input = new InputStreamReader(input, charset);
		fill(1); // prime
	}

	public UnbufferedCharStream(Reader input, int bufferSize) {
		this(bufferSize);
		this.input = input;
		fill(1); // prime
	}

	@Override
	public void consume() {
		if (LA(1) == IntStream.EOF) {
			throw new IllegalStateException("cannot consume EOF");
		}

		// buf always has at least data[p==0] in this method due to ctor
		lastChar = data[p];   // track last char for LA(-1)

		if (p == n-1 && numMarkers==0) {
			n = 0;
			p = -1; // p++ will leave this at 0
			lastCharBufferStart = lastChar;
		}

		p++;
		currentCharIndex++;
		sync(1);
	}

	
	protected void sync(int want) {
		int need = (p+want-1) - n + 1; // how many more elements we need?
		if ( need > 0 ) {
			fill(need);
		}
	}

	
	protected int fill(int n) {
		for (int i=0; i<n; i++) {
			if (this.n > 0 && data[this.n - 1] == IntStream.EOF) {
				return i;
			}

			try {
				int c = nextChar();
				if (c > Character.MAX_VALUE || c == IntStream.EOF) {
					add(c);
				}
				else {
					char ch = (char) c;
					if (Character.isLowSurrogate(ch)) {
						throw new RuntimeException("Invalid UTF-16 (low surrogate with no preceding high surrogate)");
					}
					else if (Character.isHighSurrogate(ch)) {
						int lowSurrogate = nextChar();
						if (lowSurrogate > Character.MAX_VALUE) {
							throw new RuntimeException("Invalid UTF-16 (high surrogate followed by code point > U+FFFF");
						}
						else if (lowSurrogate == IntStream.EOF) {
							throw new RuntimeException("Invalid UTF-16 (dangling high surrogate at end of file)");
						}
						else {
							char lowSurrogateChar = (char) lowSurrogate;
							if (Character.isLowSurrogate(lowSurrogateChar)) {
								add(Character.toCodePoint(ch, lowSurrogateChar));
							}
							else {
								throw new RuntimeException("Invalid UTF-16 (dangling high surrogate");
							}
						}
					}
					else {
						add(c);
					}
				}
			}
			catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}

		return n;
	}

	
	protected int nextChar() throws IOException {
		return input.read();
	}

	protected void add(int c) {
		if ( n>=data.length ) {
			data = Arrays.copyOf(data, data.length * 2);
        }
        data[n++] = c;
    }

    @Override
    public int LA(int i) {
		if ( i==-1 ) return lastChar; // special case
        sync(i);
        int index = p + i - 1;
        if ( index < 0 ) throw new IndexOutOfBoundsException();
		if ( index >= n ) return IntStream.EOF;
        return data[index];
    }

	
    @Override
    public int mark() {
		if (numMarkers == 0) {
			lastCharBufferStart = lastChar;
		}

		int mark = -numMarkers - 1;
		numMarkers++;
		return mark;
    }

	
    @Override
    public void release(int marker) {
		int expectedMark = -numMarkers;
		if ( marker!=expectedMark ) {
			throw new IllegalStateException("release() called with an invalid marker.");
		}

		numMarkers--;
		if ( numMarkers==0 && p > 0 ) { // release buffer when we can, but don't do unnecessary work
			// Copy data[p]..data[n-1] to data[0]..data[(n-1)-p], reset ptrs
			// p is last valid char; move nothing if p==n as we have no valid char
			System.arraycopy(data, p, data, 0, n - p); // shift n-p char from p to 0
			n = n - p;
			p = 0;
			lastCharBufferStart = lastChar;
		}
    }

    @Override
    public int index() {
		return currentCharIndex;
    }

	
    @Override
    public void seek(int index) {
		if (index == currentCharIndex) {
			return;
		}

		if (index > currentCharIndex) {
			sync(index - currentCharIndex);
			index = Math.min(index, getBufferStartIndex() + n - 1);
		}

        // index == to bufferStartIndex should set p to 0
        int i = index - getBufferStartIndex();
        if ( i < 0 ) {
			throw new IllegalArgumentException("cannot seek to negative index " + index);
		}
		else if (i >= n) {
            throw new UnsupportedOperationException("seek to index outside buffer: "+
                    index+" not in "+getBufferStartIndex()+".."+(getBufferStartIndex()+n));
        }

		p = i;
		currentCharIndex = index;
		if (p == 0) {
			lastChar = lastCharBufferStart;
		}
		else {
			lastChar = data[p-1];
		}
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Unbuffered stream cannot know its size");
    }

    @Override
    public String getSourceName() {
		if (name == null || name.isEmpty()) {
			return UNKNOWN_SOURCE_NAME;
		}

		return name;
	}

	@Override
	public String getText(Interval interval) {
		if (interval.a < 0 || interval.b < interval.a - 1) {
			throw new IllegalArgumentException("invalid interval");
		}

		int bufferStartIndex = getBufferStartIndex();
		if (n > 0 && data[n - 1] == Character.MAX_VALUE) {
			if (interval.a + interval.length() > bufferStartIndex + n) {
				throw new IllegalArgumentException("the interval extends past the end of the stream");
			}
		}

		if (interval.a < bufferStartIndex || interval.b >= bufferStartIndex + n) {
			throw new UnsupportedOperationException("interval "+interval+" outside buffer: "+
			                    bufferStartIndex+".."+(bufferStartIndex+n-1));
		}
		// convert from absolute to local index
		int i = interval.a - bufferStartIndex;
		return new String(data, i, interval.length());
	}

	protected final int getBufferStartIndex() {
		return currentCharIndex - p;
	}
}
