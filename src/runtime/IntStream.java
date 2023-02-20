

package runtime;


public interface IntStream {

	public static final int EOF = -1;


	public static final String UNKNOWN_SOURCE_NAME = "<unknown>";


	void consume();


	int LA(int i);


	int mark();


	void release(int marker);


	int index();

	
	void seek(int index);

	
	int size();

	

	public String getSourceName();
}
