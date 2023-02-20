

package runtime.atn;

import runtime.RuleContext;
import runtime.Token;
import runtime.misc.IntervalSet;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class LL1Analyzer {
	
	public static final int HIT_PRED = Token.INVALID_TYPE;

	public final ATN atn;

	public LL1Analyzer(ATN atn) { this.atn = atn; }

	
	public IntervalSet[] getDecisionLookahead(ATNState s) {

		if ( s==null ) {
			return null;
		}

		IntervalSet[] look = new IntervalSet[s.getNumberOfTransitions()];
		for (int alt = 0; alt < s.getNumberOfTransitions(); alt++) {
			look[alt] = new IntervalSet();
			Set<ATNConfig> lookBusy = new HashSet<ATNConfig>();
			boolean seeThruPreds = false;
			_LOOK(s.transition(alt).target, null, EmptyPredictionContext.Instance,
				  look[alt], lookBusy, new BitSet(), seeThruPreds, false);


			if ( look[alt].size()==0 || look[alt].contains(HIT_PRED) ) {
				look[alt] = null;
			}
		}
		return look;
	}

	
   	public IntervalSet LOOK(ATNState s, RuleContext ctx) {
		return LOOK(s, null, ctx);
   	}

	

   	public IntervalSet LOOK(ATNState s, ATNState stopState, RuleContext ctx) {
   		IntervalSet r = new IntervalSet();
		boolean seeThruPreds = true;
		PredictionContext lookContext = ctx != null ? PredictionContext.fromRuleContext(s.atn, ctx) : null;
   		_LOOK(s, stopState, lookContext,
			  r, new HashSet<ATNConfig>(), new BitSet(), seeThruPreds, true);
   		return r;
   	}

	
    protected void _LOOK(ATNState s,
						 ATNState stopState,
						 PredictionContext ctx,
						 IntervalSet look,
                         Set<ATNConfig> lookBusy,
						 BitSet calledRuleStack,
						 boolean seeThruPreds, boolean addEOF)
	{

        ATNConfig c = new ATNConfig(s, 0, ctx);
        if ( !lookBusy.add(c) ) return;

		if (s == stopState) {
			if (ctx == null) {
				look.add(Token.EPSILON);
				return;
			}
			else if (ctx.isEmpty() && addEOF) {
				look.add(Token.EOF);
				return;
			}
		}

        if ( s instanceof RuleStopState ) {
            if ( ctx==null ) {
                look.add(Token.EPSILON);
                return;
            }
            else if (ctx.isEmpty() && addEOF) {
				look.add(Token.EOF);
				return;
			}

			if ( ctx != EmptyPredictionContext.Instance ) {

				boolean removed = calledRuleStack.get(s.ruleIndex);
				try {
					calledRuleStack.clear(s.ruleIndex);
					for (int i = 0; i < ctx.size(); i++) {
						ATNState returnState = atn.states.get(ctx.getReturnState(i));

						_LOOK(returnState, stopState, ctx.getParent(i), look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
					}
				}
				finally {
					if (removed) {
						calledRuleStack.set(s.ruleIndex);
					}
				}
				return;
			}
        }

        int n = s.getNumberOfTransitions();
        for (int i=0; i<n; i++) {
			Transition t = s.transition(i);
			if ( t.getClass() == RuleTransition.class ) {
				if (calledRuleStack.get(((RuleTransition)t).target.ruleIndex)) {
					continue;
				}

				PredictionContext newContext =
					SingletonPredictionContext.create(ctx, ((RuleTransition)t).followState.stateNumber);

				try {
					calledRuleStack.set(((RuleTransition)t).target.ruleIndex);
					_LOOK(t.target, stopState, newContext, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
				}
				finally {
					calledRuleStack.clear(((RuleTransition)t).target.ruleIndex);
				}
			}
			else if ( t instanceof AbstractPredicateTransition ) {
				if ( seeThruPreds ) {
					_LOOK(t.target, stopState, ctx, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
				}
				else {
					look.add(HIT_PRED);
				}
			}
			else if ( t.isEpsilon() ) {
				_LOOK(t.target, stopState, ctx, look, lookBusy, calledRuleStack, seeThruPreds, addEOF);
			}
			else if ( t.getClass() == WildcardTransition.class ) {
				look.addAll( IntervalSet.of(Token.MIN_USER_TOKEN_TYPE, atn.maxTokenType) );
			}
			else {

				IntervalSet set = t.label();
				if (set != null) {
					if (t instanceof NotSetTransition) {
						set = set.complement(IntervalSet.of(Token.MIN_USER_TOKEN_TYPE, atn.maxTokenType));
					}
					look.addAll(set);
				}
			}
		}
	}
}
