

package runtime.atn;

import runtime.RuleContext;
import runtime.Token;
import runtime.misc.IntervalSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ATN {
	public static final int INVALID_ALT_NUMBER = 0;


	public final List<ATNState> states = new ArrayList<ATNState>();

	
	public final List<DecisionState> decisionToState = new ArrayList<DecisionState>();

	
	public RuleStartState[] ruleToStartState;

	
	public RuleStopState[] ruleToStopState;


	public final Map<String, TokensStartState> modeNameToStartState =
		new LinkedHashMap<String, TokensStartState>();

	
	public final ATNType grammarType;

	
	public final int maxTokenType;

	
	public int[] ruleToTokenType;

	
	public LexerAction[] lexerActions;

	public final List<TokensStartState> modeToStartState = new ArrayList<TokensStartState>();

	
	public ATN(ATNType grammarType, int maxTokenType) {
		this.grammarType = grammarType;
		this.maxTokenType = maxTokenType;
	}

	
	public IntervalSet nextTokens(ATNState s, RuleContext ctx) {
		LL1Analyzer anal = new LL1Analyzer(this);
		IntervalSet next = anal.LOOK(s, ctx);
		return next;
	}

    
    public IntervalSet nextTokens(ATNState s) {
        if ( s.nextTokenWithinRule != null ) return s.nextTokenWithinRule;
        s.nextTokenWithinRule = nextTokens(s, null);
        s.nextTokenWithinRule.setReadonly(true);
        return s.nextTokenWithinRule;
    }

	public void addState(ATNState state) {
		if (state != null) {
			state.atn = this;
			state.stateNumber = states.size();
		}

		states.add(state);
	}

	public void removeState(ATNState state) {
		states.set(state.stateNumber, null);
	}

	public int defineDecisionState(DecisionState s) {
		decisionToState.add(s);
		s.decision = decisionToState.size()-1;
		return s.decision;
	}

    public DecisionState getDecisionState(int decision) {
        if ( !decisionToState.isEmpty() ) {
            return decisionToState.get(decision);
        }
        return null;
    }

	public int getNumberOfDecisions() {
		return decisionToState.size();
	}

	
	public IntervalSet getExpectedTokens(int stateNumber, RuleContext context) {
		if (stateNumber < 0 || stateNumber >= states.size()) {
			throw new IllegalArgumentException("Invalid state number.");
		}

		RuleContext ctx = context;
		ATNState s = states.get(stateNumber);
		IntervalSet following = nextTokens(s);
		if (!following.contains(Token.EPSILON)) {
			return following;
		}

		IntervalSet expected = new IntervalSet();
		expected.addAll(following);
		expected.remove(Token.EPSILON);
		while (ctx != null && ctx.invokingState >= 0 && following.contains(Token.EPSILON)) {
			ATNState invokingState = states.get(ctx.invokingState);
			RuleTransition rt = (RuleTransition)invokingState.transition(0);
			following = nextTokens(rt.followState);
			expected.addAll(following);
			expected.remove(Token.EPSILON);
			ctx = ctx.parent;
		}

		if (following.contains(Token.EPSILON)) {
			expected.add(Token.EOF);
		}

		return expected;
	}
}
