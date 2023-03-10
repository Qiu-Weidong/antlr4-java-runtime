

package runtime;


public interface IntStream {

	int EOF = -1;


	String UNKNOWN_SOURCE_NAME = "<unknown>";


	void consume();


	int LA(int i);


	int mark();


	void release(int marker);


	int index();

	
	void seek(int index);

	
	int size();

	

	String getSourceName();
}
