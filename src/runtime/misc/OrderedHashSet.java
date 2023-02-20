

package runtime.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;


public class OrderedHashSet<T> extends LinkedHashSet<T> {
    
    protected ArrayList<T> elements = new ArrayList<T>();

    public T get(int i) {
        return elements.get(i);
    }

    
    public T set(int i, T value) {
        T oldElement = elements.get(i);
        elements.set(i,value);
        super.remove(oldElement);
        super.add(value);
        return oldElement;
    }

	public boolean remove(int i) {
		T o = elements.remove(i);
        return super.remove(o);
	}

    
    @Override
    public boolean add(T value) {
        boolean result = super.add(value);
		if ( result ) {
			elements.add(value);
		}
		return result;
    }

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
    }

	@Override
	public void clear() {
        elements.clear();
        super.clear();
    }

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OrderedHashSet<?>)) {
			return false;
		}


		boolean same = elements!=null && elements.equals(((OrderedHashSet<?>)o).elements);

		return same;
	}

	@Override
	public Iterator<T> iterator() {
		return elements.iterator();
	}

	
    public List<T> elements() {
        return elements;
    }

    @Override
    public Object clone() {
        @SuppressWarnings("unchecked")
        OrderedHashSet<T> dup = (OrderedHashSet<T>)super.clone();
        dup.elements = new ArrayList<T>(this.elements);
        return dup;
    }

    @Override
	public Object[] toArray() {
		return elements.toArray();
	}

	@Override
	public String toString() {
        return elements.toString();
    }
}
