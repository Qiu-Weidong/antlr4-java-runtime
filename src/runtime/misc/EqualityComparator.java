
package runtime.misc;


public interface EqualityComparator<T> {

	
	int hashCode(T obj);

	
	boolean equals(T a, T b);

}
