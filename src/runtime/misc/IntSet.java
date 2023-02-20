
package runtime.misc;


import java.util.List;


public interface IntSet {
	
	void add(int el);

	

	IntSet addAll(IntSet set);

	

	IntSet and(IntSet a);

	

	IntSet complement(IntSet elements);

	

	IntSet or(IntSet a);

	

	IntSet subtract(IntSet a);

	
	int size();

	
	boolean isNil();

	
	@Override
	boolean equals(Object obj);

	
	boolean contains(int el);

	
	void remove(int el);

	

	List<Integer> toList();

	
	@Override
	String toString();
}
