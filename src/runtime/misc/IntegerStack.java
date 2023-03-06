
package runtime.misc;


public class IntegerStack extends IntegerList {

	public IntegerStack() {
	}

	public final void push(int value) {
		add(value);
	}

	public final int pop() {
		return removeAt(size() - 1);
	}

	public final int peek() {
		return get(size() - 1);
	}

}
