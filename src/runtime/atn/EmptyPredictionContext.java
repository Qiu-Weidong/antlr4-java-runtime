

package runtime.atn;

public class EmptyPredictionContext extends SingletonPredictionContext {
	
	public static final EmptyPredictionContext Instance = new EmptyPredictionContext();

	private EmptyPredictionContext() {
		super(null, EMPTY_RETURN_STATE);
	}

	@Override
	public boolean isEmpty() { return true; }

	@Override
	public int size() {
		return 1;
	}

	@Override
	public PredictionContext getParent(int index) {
		return null;
	}

	@Override
	public int getReturnState(int index) {
		return returnState;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public String toString() {
		return "$";
	}
}
