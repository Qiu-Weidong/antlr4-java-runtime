

package runtime.atn;

import runtime.misc.IntervalSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class Transition {

	public static final int EPSILON			= 1;
	public static final int RANGE			= 2;
	public static final int RULE			= 3;
	public static final int PREDICATE		= 4;
	public static final int ATOM			= 5;
	public static final int ACTION			= 6;
	public static final int SET				= 7;
	public static final int NOT_SET			= 8;
	public static final int WILDCARD		= 9;
	public static final int PRECEDENCE		= 10;


	public static final List<String> serializationNames =
		Collections.unmodifiableList(Arrays.asList(
			"INVALID",
			"EPSILON",
			"RANGE",
			"RULE",
			"PREDICATE",
			"ATOM",
			"ACTION",
			"SET",
			"NOT_SET",
			"WILDCARD",
			"PRECEDENCE"
		));

	public static final Map<Class<? extends Transition>, Integer> serializationTypes =
		Collections.unmodifiableMap(new HashMap<Class<? extends Transition>, Integer>() {{
			put(EpsilonTransition.class, EPSILON);
			put(RangeTransition.class, RANGE);
			put(RuleTransition.class, RULE);
			put(PredicateTransition.class, PREDICATE);
			put(AtomTransition.class, ATOM);
			put(ActionTransition.class, ACTION);
			put(SetTransition.class, SET);
			put(NotSetTransition.class, NOT_SET);
			put(WildcardTransition.class, WILDCARD);
			put(PrecedencePredicateTransition.class, PRECEDENCE);
		}});

	

	public ATNState target;

	protected Transition(ATNState target) {
		if (target == null) {
			throw new NullPointerException("target cannot be null.");
		}

		this.target = target;
	}

	public abstract int getSerializationType();

	
	public boolean isEpsilon() {
		return false;
	}


	public IntervalSet label() { return null; }

	public abstract boolean matches(int symbol, int minVocabSymbol, int maxVocabSymbol);
}
