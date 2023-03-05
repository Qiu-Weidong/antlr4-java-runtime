
package runtime.misc;

import java.util.*;


public class IntegerList {

	private final static int[] EMPTY_DATA = new int[0];

	private static final int INITIAL_SIZE = 4;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;


	private int[] _data;

	private int _size;

	public IntegerList() {
		_data = EMPTY_DATA;
	}

	public IntegerList(int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}

		if (capacity == 0) {
			_data = EMPTY_DATA;
		}
		else {
			_data = new int[capacity];
		}
	}

	public IntegerList(IntegerList list) {
		_data = list._data.clone();
		_size = list._size;
	}

	public final void add(int value) {
		if (_data.length == _size) {
			ensureCapacity(_size + 1);
		}

		_data[_size] = value;
		_size++;
	}

	public final int get(int index) {
		if (index < 0 || index >= _size) {
			throw new IndexOutOfBoundsException();
		}

		return _data[index];
	}

	public final boolean contains(int value) {
		for (int i = 0; i < _size; i++) {
			if (_data[i] == value) {
				return true;
			}
		}

		return false;
	}

	public final int set(int index, int value) {
		if (index < 0 || index >= _size) {
			throw new IndexOutOfBoundsException();
		}

		int previous = _data[index];
		_data[index] = value;
		return previous;
	}

	public final int removeAt(int index) {
		int value = get(index);
		System.arraycopy(_data, index + 1, _data, index, _size - index - 1);
		_data[_size - 1] = 0;
		_size--;
		return value;
	}

	public final boolean isEmpty() {
		return _size == 0;
	}

	public final int size() {
		return _size;
	}

	public final void clear() {
		Arrays.fill(_data, 0, _size, 0);
		_size = 0;
	}

	public final int[] toArray() {
		if (_size == 0) {
			return EMPTY_DATA;
		}

		return Arrays.copyOf(_data, _size);
	}


	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof IntegerList)) {
			return false;
		}

		IntegerList other = (IntegerList)o;
		if (_size != other._size) {
			return false;
		}

		for (int i = 0; i < _size; i++) {
			if (_data[i] != other._data[i]) {
				return false;
			}
		}

		return true;
	}

	
	@Override
	public int hashCode() {
		int hashCode = 1;
		for (int i = 0; i < _size; i++) {
			hashCode = 31*hashCode + _data[i];
		}

		return hashCode;
	}

	
	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}

	private void ensureCapacity(int capacity) {
		if (capacity < 0 || capacity > MAX_ARRAY_SIZE) {
			throw new OutOfMemoryError();
		}

		int newLength;
		if (_data.length == 0) {
			newLength = INITIAL_SIZE;
		}
		else {
			newLength = _data.length;
		}

		while (newLength < capacity) {
			newLength = newLength * 2;
			if (newLength < 0 || newLength > MAX_ARRAY_SIZE) {
				newLength = MAX_ARRAY_SIZE;
			}
		}

		_data = Arrays.copyOf(_data, newLength);
	}


}
