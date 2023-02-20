

package runtime.atn;

public class SingletonPredictionContext extends PredictionContext {
	public final PredictionContext parent;
	public final int returnState;

	SingletonPredictionContext(PredictionContext parent, int returnState) {
		super(parent != null ? calculateHashCode(parent, returnState) : calculateEmptyHashCode());
		assert returnState!=ATNState.INVALID_STATE_NUMBER;
		this.parent = parent;
		this.returnState = returnState;
	}

	public static SingletonPredictionContext create(PredictionContext parent, int returnState) {
		if ( returnState == EMPTY_RETURN_STATE && parent == null ) {

			return EmptyPredictionContext.Instance;
		}
		return new SingletonPredictionContext(parent, returnState);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public PredictionContext getParent(int index) {
		assert index == 0;
		return parent;
	}

	@Override
	public int getReturnState(int index) {
		assert index == 0;
		return returnState;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		else if ( !(o instanceof SingletonPredictionContext) ) {
			return false;
		}

		if ( this.hashCode() != o.hashCode() ) {
			return false;
		}

		SingletonPredictionContext s = (SingletonPredictionContext)o;
		return returnState == s.returnState &&
			(parent!=null && parent.equals(s.parent));
	}

	@Override
	public String toString() {
		String up = parent!=null ? parent.toString() : "";
		if ( up.length()==0 ) {
			if ( returnState == EMPTY_RETURN_STATE ) {
				return "$";
			}
			return String.valueOf(returnState);
		}
		return String.valueOf(returnState)+" "+up;
	}
}
