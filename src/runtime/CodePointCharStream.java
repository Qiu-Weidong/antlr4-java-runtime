

package runtime;

import runtime.misc.Interval;

import java.nio.charset.StandardCharsets;


public abstract class CodePointCharStream implements CharStream {
	protected final int size;
	protected final String name;

	protected int position;

	private CodePointCharStream(int position, int remaining, String name) {
		// TODO
		assert position == 0;
		this.size = remaining;
		this.name = name;
		this.position = 0;
	}

	abstract Object getInternalStorage();

	
	public static CodePointCharStream fromBuffer(CodePointBuffer codePointBuffer) {
		return fromBuffer(codePointBuffer, UNKNOWN_SOURCE_NAME);
	}

	
	public static CodePointCharStream fromBuffer(CodePointBuffer codePointBuffer, String name) {

		switch (codePointBuffer.getType()) {
			case BYTE:
				return new CodePoint8BitCharStream(
						codePointBuffer.position(),
						codePointBuffer.remaining(),
						name,
						codePointBuffer.byteArray(),
						codePointBuffer.arrayOffset());
			case CHAR:
				return new CodePoint16BitCharStream(
						codePointBuffer.position(),
						codePointBuffer.remaining(),
						name,
						codePointBuffer.charArray(),
						codePointBuffer.arrayOffset());
			case INT:
				return new CodePoint32BitCharStream(
						codePointBuffer.position(),
						codePointBuffer.remaining(),
						name,
						codePointBuffer.intArray(),
						codePointBuffer.arrayOffset());
		}
		throw new UnsupportedOperationException("Not reached");
	}

	@Override
	public final void consume() {
		if (size - position == 0) {
			assert LA(1) == IntStream.EOF;
			throw new IllegalStateException("cannot consume EOF");
		}
		position = position + 1;
	}

	@Override
	public final int index() {
		return position;
	}

	@Override
	public final int size() {
		return size;
	}

	
	@Override
	public final int mark() {
		return -1;
	}

	@Override
	public final void release(int marker) {
	}

	@Override
	public final void seek(int index) {
		position = index;
	}

	@Override
	public final String getSourceName() {
		if (name == null || name.isEmpty()) {
			return UNKNOWN_SOURCE_NAME;
		}

		return name;
	}

	@Override
	public final String toString() {
		return getText(Interval.of(0, size - 1));
	}

	// 8-bit storage for code points <= U+00FF.
	private static final class CodePoint8BitCharStream extends CodePointCharStream {
		private final byte[] byteArray;

		private CodePoint8BitCharStream(int position, int remaining, String name, byte[] byteArray, int arrayOffset) {
			super(position, remaining, name);
			// TODO
			assert arrayOffset == 0;
			this.byteArray = byteArray;
		}

		
		@Override
		public String getText(Interval interval) {
			int startIdx = Math.min(interval.a, size);
			int len = Math.min(interval.b - interval.a + 1, size - startIdx);

			return new String(byteArray, startIdx, len, StandardCharsets.ISO_8859_1);
		}

		@Override
		public int LA(int i) {
			int offset;
			switch (Integer.signum(i)) {
				case -1:
					offset = position + i;
					if (offset < 0) {
						return IntStream.EOF;
					}
					return byteArray[offset] & 0xFF;
				case 0:
					// Undefined
					return 0;
				case 1:
					offset = position + i - 1;
					if (offset >= size) {
						return IntStream.EOF;
					}
					return byteArray[offset] & 0xFF;
			}
			throw new UnsupportedOperationException("Not reached");
		}

		@Override
		Object getInternalStorage() {
			return byteArray;
		}
	}


	private static final class CodePoint16BitCharStream extends CodePointCharStream {
		private final char[] charArray;

		private CodePoint16BitCharStream(int position, int remaining, String name, char[] charArray, int arrayOffset) {
			super(position, remaining, name);
			this.charArray = charArray;
			// TODO
			assert arrayOffset == 0;
		}

		
		@Override
		public String getText(Interval interval) {
			int startIdx = Math.min(interval.a, size);
			int len = Math.min(interval.b - interval.a + 1, size - startIdx);

			return new String(charArray, startIdx, len);
		}

		@Override
		public int LA(int i) {
			int offset;
			switch (Integer.signum(i)) {
				case -1:
					offset = position + i;
					if (offset < 0) {
						return IntStream.EOF;
					}
					return charArray[offset] & 0xFFFF;
				case 0:
					// Undefined
					return 0;
				case 1:
					offset = position + i - 1;
					if (offset >= size) {
						return IntStream.EOF;
					}
					return charArray[offset] & 0xFFFF;
			}
			throw new UnsupportedOperationException("Not reached");
		}

		@Override
		Object getInternalStorage() {
			return charArray;
		}
	}


	private static final class CodePoint32BitCharStream extends CodePointCharStream {
		private final int[] intArray;

		private CodePoint32BitCharStream(int position, int remaining, String name, int[] intArray, int arrayOffset) {
			super(position, remaining, name);
			this.intArray = intArray;
			// TODO
			assert arrayOffset == 0;
		}

		
		@Override
		public String getText(Interval interval) {
			int startIdx = Math.min(interval.a, size);
			int len = Math.min(interval.b - interval.a + 1, size - startIdx);

			return new String(intArray, startIdx, len);
		}

		@Override
		public int LA(int i) {
			int offset;
			switch (Integer.signum(i)) {
				case -1:
					offset = position + i;
					if (offset < 0) {
						return IntStream.EOF;
					}
					return intArray[offset];
				case 0:
					// Undefined
					return 0;
				case 1:
					offset = position + i - 1;
					if (offset >= size) {
						return IntStream.EOF;
					}
					return intArray[offset];
			}
			throw new UnsupportedOperationException("Not reached");
		}

		@Override
		Object getInternalStorage() {
			return intArray;
		}
	}
}
