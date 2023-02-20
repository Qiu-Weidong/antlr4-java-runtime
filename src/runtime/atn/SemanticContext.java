

package runtime.atn;

import runtime.Recognizer;
import runtime.RuleContext;
import runtime.misc.MurmurHash;
import runtime.misc.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public abstract class SemanticContext {
	
    public abstract boolean eval(Recognizer<?,?> parser, RuleContext parserCallStack);

	
	public SemanticContext evalPrecedence(Recognizer<?,?> parser, RuleContext parserCallStack) {
		return this;
	}

	public static class Empty extends SemanticContext {
		
		public static final Empty Instance = new Empty();

		@Override
		public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
			return false;
		}
	}

    public static class Predicate extends SemanticContext {
        public final int ruleIndex;
       	public final int predIndex;
       	public final boolean isCtxDependent;

        protected Predicate() {
            this.ruleIndex = -1;
            this.predIndex = -1;
            this.isCtxDependent = false;
        }

        public Predicate(int ruleIndex, int predIndex, boolean isCtxDependent) {
            this.ruleIndex = ruleIndex;
            this.predIndex = predIndex;
            this.isCtxDependent = isCtxDependent;
        }

        @Override
        public boolean eval(Recognizer<?,?> parser, RuleContext parserCallStack) {
            RuleContext localctx = isCtxDependent ? parserCallStack : null;
            return parser.sempred(localctx, ruleIndex, predIndex);
        }

		@Override
		public int hashCode() {
			int hashCode = MurmurHash.initialize();
			hashCode = MurmurHash.update(hashCode, ruleIndex);
			hashCode = MurmurHash.update(hashCode, predIndex);
			hashCode = MurmurHash.update(hashCode, isCtxDependent ? 1 : 0);
			hashCode = MurmurHash.finish(hashCode, 3);
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if ( !(obj instanceof Predicate) ) return false;
			if ( this == obj ) return true;
			Predicate p = (Predicate)obj;
			return this.ruleIndex == p.ruleIndex &&
				   this.predIndex == p.predIndex &&
				   this.isCtxDependent == p.isCtxDependent;
		}

		@Override
		public String toString() {
            return "{"+ruleIndex+":"+predIndex+"}?";
        }
    }

	public static class PrecedencePredicate extends SemanticContext implements Comparable<PrecedencePredicate> {
		public final int precedence;

		protected PrecedencePredicate() {
			this.precedence = 0;
		}

		public PrecedencePredicate(int precedence) {
			this.precedence = precedence;
		}

		@Override
		public boolean eval(Recognizer<?, ?> parser, RuleContext parserCallStack) {
			return parser.precpred(parserCallStack, precedence);
		}

		@Override
		public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
			if (parser.precpred(parserCallStack, precedence)) {
				return Empty.Instance;
			}
			else {
				return null;
			}
		}

		@Override
		public int compareTo(PrecedencePredicate o) {
			return precedence - o.precedence;
		}

		@Override
		public int hashCode() {
			int hashCode = 1;
			hashCode = 31 * hashCode + precedence;
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PrecedencePredicate)) {
				return false;
			}

			if (this == obj) {
				return true;
			}

			PrecedencePredicate other = (PrecedencePredicate)obj;
			return this.precedence == other.precedence;
		}

		@Override

		public String toString() {
			return "{"+precedence+">=prec}?";
		}
	}

	
	public static abstract class Operator extends SemanticContext {
		

		public abstract Collection<SemanticContext> getOperands();
	}

	
    public static class AND extends Operator {
		public final SemanticContext[] opnds;

		public AND(SemanticContext a, SemanticContext b) {
			Set<SemanticContext> operands = new HashSet<SemanticContext>();
			if ( a instanceof AND ) operands.addAll(Arrays.asList(((AND)a).opnds));
			else operands.add(a);
			if ( b instanceof AND ) operands.addAll(Arrays.asList(((AND)b).opnds));
			else operands.add(b);

			List<PrecedencePredicate> precedencePredicates = filterPrecedencePredicates(operands);
			if (!precedencePredicates.isEmpty()) {

				PrecedencePredicate reduced = Collections.min(precedencePredicates);
				operands.add(reduced);
			}

			opnds = operands.toArray(new SemanticContext[0]);
        }

		@Override
		public Collection<SemanticContext> getOperands() {
			return Arrays.asList(opnds);
		}

		@Override
		public boolean equals(Object obj) {
			if ( this==obj ) return true;
			if ( !(obj instanceof AND) ) return false;
			AND other = (AND)obj;
			return Arrays.equals(this.opnds, other.opnds);
		}

		@Override
		public int hashCode() {
			return MurmurHash.hashCode(opnds, AND.class.hashCode());
		}

		
		@Override
		public boolean eval(Recognizer<?,?> parser, RuleContext parserCallStack) {
			for (SemanticContext opnd : opnds) {
				if ( !opnd.eval(parser, parserCallStack) ) return false;
			}
			return true;
        }

		@Override
		public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
			boolean differs = false;
			List<SemanticContext> operands = new ArrayList<SemanticContext>();
			for (SemanticContext context : opnds) {
				SemanticContext evaluated = context.evalPrecedence(parser, parserCallStack);
				differs |= (evaluated != context);
				if (evaluated == null) {

					return null;
				}
				else if (evaluated != Empty.Instance) {

					operands.add(evaluated);
				}
			}

			if (!differs) {
				return this;
			}

			if (operands.isEmpty()) {

				return Empty.Instance;
			}

			SemanticContext result = operands.get(0);
			for (int i = 1; i < operands.size(); i++) {
				result = SemanticContext.and(result, operands.get(i));
			}

			return result;
		}

		@Override
		public String toString() {
			return Utils.join(Arrays.asList(opnds).iterator(), "&&");
        }
    }

	
    public static class OR extends Operator {
		public final SemanticContext[] opnds;

		public OR(SemanticContext a, SemanticContext b) {
			Set<SemanticContext> operands = new HashSet<SemanticContext>();
			if ( a instanceof OR ) operands.addAll(Arrays.asList(((OR)a).opnds));
			else operands.add(a);
			if ( b instanceof OR ) operands.addAll(Arrays.asList(((OR)b).opnds));
			else operands.add(b);

			List<PrecedencePredicate> precedencePredicates = filterPrecedencePredicates(operands);
			if (!precedencePredicates.isEmpty()) {

				PrecedencePredicate reduced = Collections.max(precedencePredicates);
				operands.add(reduced);
			}

			this.opnds = operands.toArray(new SemanticContext[0]);
        }

		@Override
		public Collection<SemanticContext> getOperands() {
			return Arrays.asList(opnds);
		}

		@Override
		public boolean equals(Object obj) {
			if ( this==obj ) return true;
			if ( !(obj instanceof OR) ) return false;
			OR other = (OR)obj;
			return Arrays.equals(this.opnds, other.opnds);
		}

		@Override
		public int hashCode() {
			return MurmurHash.hashCode(opnds, OR.class.hashCode());
		}

		
		@Override
        public boolean eval(Recognizer<?,?> parser, RuleContext parserCallStack) {
			for (SemanticContext opnd : opnds) {
				if ( opnd.eval(parser, parserCallStack) ) return true;
			}
			return false;
        }

		@Override
		public SemanticContext evalPrecedence(Recognizer<?, ?> parser, RuleContext parserCallStack) {
			boolean differs = false;
			List<SemanticContext> operands = new ArrayList<SemanticContext>();
			for (SemanticContext context : opnds) {
				SemanticContext evaluated = context.evalPrecedence(parser, parserCallStack);
				differs |= (evaluated != context);
				if (evaluated == Empty.Instance) {

					return Empty.Instance;
				}
				else if (evaluated != null) {

					operands.add(evaluated);
				}
			}

			if (!differs) {
				return this;
			}

			if (operands.isEmpty()) {

				return null;
			}

			SemanticContext result = operands.get(0);
			for (int i = 1; i < operands.size(); i++) {
				result = SemanticContext.or(result, operands.get(i));
			}

			return result;
		}

        @Override
        public String toString() {
			return Utils.join(Arrays.asList(opnds).iterator(), "||");
        }
    }

	public static SemanticContext and(SemanticContext a, SemanticContext b) {
		if ( a == null || a == Empty.Instance ) return b;
		if ( b == null || b == Empty.Instance ) return a;
		AND result = new AND(a, b);
		if (result.opnds.length == 1) {
			return result.opnds[0];
		}

		return result;
	}

	
	public static SemanticContext or(SemanticContext a, SemanticContext b) {
		if ( a == null ) return b;
		if ( b == null ) return a;
		if ( a == Empty.Instance || b == Empty.Instance ) return Empty.Instance;
		OR result = new OR(a, b);
		if (result.opnds.length == 1) {
			return result.opnds[0];
		}

		return result;
	}

	private static List<PrecedencePredicate> filterPrecedencePredicates(Collection<? extends SemanticContext> collection) {
		ArrayList<PrecedencePredicate> result = null;
		for (Iterator<? extends SemanticContext> iterator = collection.iterator(); iterator.hasNext(); ) {
			SemanticContext context = iterator.next();
			if (context instanceof PrecedencePredicate) {
				if (result == null) {
					result = new ArrayList<PrecedencePredicate>();
				}

				result.add((PrecedencePredicate)context);
				iterator.remove();
			}
		}

		if (result == null) {
			return Collections.emptyList();
		}

		return result;
	}
}
