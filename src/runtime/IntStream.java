

package runtime;


public interface IntStream {

	public static final int EOF = -1;


	public static final String UNKNOWN_SOURCE_NAME = "<unknown>";


	void consume();


	int LA(int i);


	int mark();


	void release(int marker);


	int index();

	/**
	 * Set the input cursor to the position indicated by {@code index}. If the
	 * specified index lies past the end of the stream, the operation behaves as
	 * though {@code index} was the index of the EOF symbol. After this method
	 * returns without throwing an exception, then at least one of the following
	 * will be true.
	 *
	 * <ul>
	 *   <li>{@link #index index()} will return the index of the first symbol
	 *     appearing at or after the specified {@code index}. Specifically,
	 *     implementations which filter their sources should automatically
	 *     adjust {@code index} forward the minimum amount required for the
	 *     operation to target a non-ignored symbol.</li>
	 *   <li>{@code LA(1)} returns {@link #EOF}</li>
	 * </ul>
	 *
	 * This operation is guaranteed to not throw an exception if {@code index}
	 * lies within a marked region. For more information on marked regions, see
	 * {@link #mark}. The behavior of this method is unspecified if no call to
	 * an {@link IntStream initializing method} has occurred after this stream
	 * was constructed.
	 *
	 * @param index The absolute index to seek to.
	 *
	 * @throws IllegalArgumentException if {@code index} is less than 0
	 * @throws UnsupportedOperationException if the stream does not support
	 * seeking to the specified index
	 */
	void seek(int index);

	/**
	 * Returns the total number of symbols in the stream, including a single EOF
	 * symbol.
	 *
	 * @throws UnsupportedOperationException if the size of the stream is
	 * unknown.
	 */
	int size();

	/**
	 * Gets the name of the underlying symbol source. This method returns a
	 * non-null, non-empty string. If such a name is not known, this method
	 * returns {@link #UNKNOWN_SOURCE_NAME}.
	 */

	public String getSourceName();
}
