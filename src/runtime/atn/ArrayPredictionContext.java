

package runtime.atn;

import java.util.Arrays;

public class ArrayPredictionContext extends PredictionContext {
	
	public final PredictionContext[] parents;

	
	public final int[] returnStates;

	public ArrayPredictionContext(SingletonPredictionContext a) {
		this(new PredictionContext[] {a.parent}, new int[] {a.returnState});
	}

	public ArrayPredictionContext(PredictionContext[] parents, int[] returnStates) {
		super(calculateHashCode(parents, returnStates));
		assert parents!=null && parents.length>0;
		assert returnStates!=null && returnStates.length>0;

		this.parents = parents;
		this.returnStates = returnStates;
	}

	@Override
	public boolean isEmpty() {


		return returnStates[0]==EMPTY_RETURN_STATE;
	}

	@Override
	public int size() {
		return returnStates.length;
	}

	@Override
	public PredictionContext getParent(int index) {
		return parents[index];
	}

	@Override
	public int getReturnState(int index) {
		return returnStates[index];
	}






	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		else if ( !(o instanceof ArrayPredictionContext) ) {
			return false;
		}

		if ( this.hashCode() != o.hashCode() ) {
			return false;
		}

		ArrayPredictionContext a = (ArrayPredictionContext)o;
		return Arrays.equals(returnStates, a.returnStates) &&
		       Arrays.equals(parents, a.parents);
	}

	@Override
	public String toString() {
		if ( isEmpty() ) return "[]";
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<returnStates.length; i++) {
			if ( i>0 ) buf.append(", ");
			if ( returnStates[i]==EMPTY_RETURN_STATE ) {
				buf.append("$");
				continue;
			}
			buf.append(returnStates[i]);
			if ( parents[i]!=null ) {
				buf.append(' ');
				buf.append(parents[i].toString());
			}
			else {
				buf.append("null");
			}
		}
		buf.append("]");
		return buf.toString();
	}
}
