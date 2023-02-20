

package runtime.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


public class Array2DHashSet<T> implements Set<T> {
	public static final int INITAL_CAPACITY = 16;
	public static final int INITAL_BUCKET_CAPACITY = 8;
	public static final double LOAD_FACTOR = 0.75;


	protected final AbstractEqualityComparator<? super T> comparator;

	protected T[][] buckets;

	
	protected int n = 0;

	protected int currentPrime = 1;

	
	protected int threshold;
	protected final int initialCapacity;
	protected final int initialBucketCapacity;

	public Array2DHashSet() {
		this(null, INITAL_CAPACITY, INITAL_BUCKET_CAPACITY);
	}

	public Array2DHashSet(AbstractEqualityComparator<? super T> comparator) {
		this(comparator, INITAL_CAPACITY, INITAL_BUCKET_CAPACITY);
	}

	public Array2DHashSet(AbstractEqualityComparator<? super T> comparator, int initialCapacity, int initialBucketCapacity) {
		if (comparator == null) {
			comparator = ObjectEqualityComparator.INSTANCE;
		}

		this.comparator = comparator;
		this.initialCapacity = initialCapacity;
		this.initialBucketCapacity = initialBucketCapacity;
		this.buckets = createBuckets(initialCapacity);
		this.threshold = (int)Math.floor(initialCapacity * LOAD_FACTOR);
	}

	
	public final T getOrAdd(T o) {
		if ( n > threshold ) expand();
		return getOrAddImpl(o);
	}

	protected T getOrAddImpl(T o) {
		int b = getBucket(o);
		T[] bucket = buckets[b];


		if ( bucket==null ) {
			bucket = createBucket(initialBucketCapacity);
			bucket[0] = o;
			buckets[b] = bucket;
			n++;
			return o;
		}


		for (int i=0; i<bucket.length; i++) {
			T existing = bucket[i];
			if ( existing==null ) {
				bucket[i] = o;
				n++;
				return o;
			}
			if ( comparator.equals(existing, o) ) return existing;
		}


		int oldLength = bucket.length;
		bucket = Arrays.copyOf(bucket, bucket.length * 2);
		buckets[b] = bucket;
		bucket[oldLength] = o;
		n++;
		return o;
	}

	public T get(T o) {
		if ( o==null ) return o;
		int b = getBucket(o);
		T[] bucket = buckets[b];
		if ( bucket==null ) return null;
		for (T e : bucket) {
			if ( e==null ) return null;
			if ( comparator.equals(e, o) ) return e;
		}
		return null;
	}

	protected final int getBucket(T o) {
		int hash = comparator.hashCode(o);
		int b = hash & (buckets.length-1);
		return b;
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		for (T[] bucket : buckets) {
			if ( bucket==null ) continue;
			for (T o : bucket) {
				if ( o==null ) break;
				hash = MurmurHash.update(hash, comparator.hashCode(o));
			}
		}

		hash = MurmurHash.finish(hash, size());
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if ( !(o instanceof Array2DHashSet) ) return false;
		Array2DHashSet<?> other = (Array2DHashSet<?>)o;
		if ( other.size() != size() ) return false;
		boolean same = this.containsAll(other);
		return same;
	}

	protected void expand() {
		T[][] old = buckets;
		currentPrime += 4;
		int newCapacity = buckets.length * 2;
		T[][] newTable = createBuckets(newCapacity);
		int[] newBucketLengths = new int[newTable.length];
		buckets = newTable;
		threshold = (int)(newCapacity * LOAD_FACTOR);


		int oldSize = size();
		for (T[] bucket : old) {
			if ( bucket==null ) {
				continue;
			}

			for (T o : bucket) {
				if ( o==null ) {
					break;
				}

				int b = getBucket(o);
				int bucketLength = newBucketLengths[b];
				T[] newBucket;
				if (bucketLength == 0) {

					newBucket = createBucket(initialBucketCapacity);
					newTable[b] = newBucket;
				}
				else {
					newBucket = newTable[b];
					if (bucketLength == newBucket.length) {

						newBucket = Arrays.copyOf(newBucket, newBucket.length * 2);
						newTable[b] = newBucket;
					}
				}

				newBucket[bucketLength] = o;
				newBucketLengths[b]++;
			}
		}

		assert n == oldSize;
	}

	@Override
	public final boolean add(T t) {
		T existing = getOrAdd(t);
		return existing==t;
	}

	@Override
	public final int size() {
		return n;
	}

	@Override
	public final boolean isEmpty() {
		return n==0;
	}

	@Override
	public final boolean contains(Object o) {
		return containsFast(asElementType(o));
	}

	public boolean containsFast(T obj) {
		if (obj == null) {
			return false;
		}

		return get(obj) != null;
	}

	@Override
	public Iterator<T> iterator() {
		return new SetIterator(toArray());
	}

	@Override
	public T[] toArray() {
		T[] a = createBucket(size());
		int i = 0;
		for (T[] bucket : buckets) {
			if ( bucket==null ) {
				continue;
			}

			for (T o : bucket) {
				if ( o==null ) {
					break;
				}

				a[i++] = o;
			}
		}

		return a;
	}

	@Override
	public <U> U[] toArray(U[] a) {
		if (a.length < size()) {
			a = Arrays.copyOf(a, size());
		}

		int i = 0;
		for (T[] bucket : buckets) {
			if ( bucket==null ) {
				continue;
			}

			for (T o : bucket) {
				if ( o==null ) {
					break;
				}

				@SuppressWarnings("unchecked")
				U targetElement = (U)o;
				a[i++] = targetElement;
			}
		}
		return a;
	}

	@Override
	public final boolean remove(Object o) {
		return removeFast(asElementType(o));
	}

	public boolean removeFast(T obj) {
		if (obj == null) {
			return false;
		}

		int b = getBucket(obj);
		T[] bucket = buckets[b];
		if ( bucket==null ) {

			return false;
		}

		for (int i=0; i<bucket.length; i++) {
			T e = bucket[i];
			if ( e==null ) {

				return false;
			}

			if ( comparator.equals(e, obj) ) {

				System.arraycopy(bucket, i+1, bucket, i, bucket.length-i-1);
				bucket[bucket.length - 1] = null;
				n--;
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		if ( collection instanceof Array2DHashSet ) {
			Array2DHashSet<?> s = (Array2DHashSet<?>)collection;
			for (Object[] bucket : s.buckets) {
				if ( bucket==null ) continue;
				for (Object o : bucket) {
					if ( o==null ) break;
					if ( !this.containsFast(asElementType(o)) ) return false;
				}
			}
		}
		else {
			for (Object o : collection) {
				if ( !this.containsFast(asElementType(o)) ) return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		for (T o : c) {
			T existing = getOrAdd(o);
			if ( existing!=o ) changed=true;
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		int newsize = 0;
		for (T[] bucket : buckets) {
			if (bucket == null) {
				continue;
			}

			int i;
			int j;
			for (i = 0, j = 0; i < bucket.length; i++) {
				if (bucket[i] == null) {
					break;
				}

				if (!c.contains(bucket[i])) {

					continue;
				}


				if (i != j) {
					bucket[j] = bucket[i];
				}

				j++;
				newsize++;
			}

			newsize += j;

			while (j < i) {
				bucket[j] = null;
				j++;
			}
		}

		boolean changed = newsize != n;
		n = newsize;
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object o : c) {
			changed |= removeFast(asElementType(o));
		}

		return changed;
	}

	@Override
	public void clear() {
		n = 0;
		buckets = createBuckets(this.initialCapacity);
		threshold = (int)Math.floor(this.initialCapacity * LOAD_FACTOR);
	}

	@Override
	public String toString() {
		if ( size()==0 ) return "{}";

		StringBuilder buf = new StringBuilder();
		buf.append('{');
		boolean first = true;
		for (T[] bucket : buckets) {
			if ( bucket==null ) continue;
			for (T o : bucket) {
				if ( o==null ) break;
				if ( first ) first=false;
				else buf.append(", ");
				buf.append(o.toString());
			}
		}
		buf.append('}');
		return buf.toString();
	}

	public String toTableString() {
		StringBuilder buf = new StringBuilder();
		for (T[] bucket : buckets) {
			if ( bucket==null ) {
				buf.append("null\n");
				continue;
			}
			buf.append('[');
			boolean first = true;
			for (T o : bucket) {
				if ( first ) first=false;
				else buf.append(" ");
				if ( o==null ) buf.append("_");
				else buf.append(o.toString());
			}
			buf.append("]\n");
		}
		return buf.toString();
	}

	
	@SuppressWarnings("unchecked")
	protected T asElementType(Object o) {
		return (T)o;
	}

	
	@SuppressWarnings("unchecked")
	protected T[][] createBuckets(int capacity) {
		return (T[][])new Object[capacity][];
	}

	
	@SuppressWarnings("unchecked")
	protected T[] createBucket(int capacity) {
		return (T[])new Object[capacity];
	}

	protected class SetIterator implements Iterator<T> {
		final T[] data;
		int nextIndex = 0;
		boolean removed = true;

		public SetIterator(T[] data) {
			this.data = data;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < data.length;
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			removed = false;
			return data[nextIndex++];
		}

		@Override
		public void remove() {
			if (removed) {
				throw new IllegalStateException();
			}

			Array2DHashSet.this.remove(data[nextIndex - 1]);
			removed = true;
		}
	}
}
