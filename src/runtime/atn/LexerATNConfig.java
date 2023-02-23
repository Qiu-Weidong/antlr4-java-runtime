

package runtime.atn;

import runtime.misc.MurmurHash;
import runtime.misc.ObjectEqualityComparator;

public class LexerATNConfig extends ATNConfig {
	
	private final LexerActionExecutor lexerActionExecutor;

	private final boolean passedThroughNonGreedyDecision;

	public LexerATNConfig(ATNState state,
						  int alt,
						  PredictionContext context)
	{
		super(state, alt, context, SemanticContext.Empty.Instance);
		this.passedThroughNonGreedyDecision = false;
		this.lexerActionExecutor = null;
	}

	public LexerATNConfig(LexerATNConfig c, ATNState state) {
		super(c, state, c.context, c.semanticContext);
		this.lexerActionExecutor = c.lexerActionExecutor;
		this.passedThroughNonGreedyDecision = checkNonGreedyDecision(c, state);
	}

	public LexerATNConfig(LexerATNConfig c, ATNState state,
						  LexerActionExecutor lexerActionExecutor)
	{
		super(c, state, c.context, c.semanticContext);
		this.lexerActionExecutor = lexerActionExecutor;
		this.passedThroughNonGreedyDecision = checkNonGreedyDecision(c, state);
	}

	public LexerATNConfig(LexerATNConfig c, ATNState state,
						  PredictionContext context) {
		super(c, state, context, c.semanticContext);
		this.lexerActionExecutor = c.lexerActionExecutor;
		this.passedThroughNonGreedyDecision = checkNonGreedyDecision(c, state);
	}

	
	public final LexerActionExecutor getLexerActionExecutor() {
		return lexerActionExecutor;
	}

	public final boolean hasPassedThroughNonGreedyDecision() {
		return passedThroughNonGreedyDecision;
	}

	@Override
	public int hashCode() {
		int hashCode = MurmurHash.initialize(7);
		hashCode = MurmurHash.update(hashCode, state.stateNumber);
		hashCode = MurmurHash.update(hashCode, alt);
		hashCode = MurmurHash.update(hashCode, context);
		hashCode = MurmurHash.update(hashCode, semanticContext);
		hashCode = MurmurHash.update(hashCode, passedThroughNonGreedyDecision ? 1 : 0);
		hashCode = MurmurHash.update(hashCode, lexerActionExecutor);
		hashCode = MurmurHash.finish(hashCode, 6);
		return hashCode;
	}

	@Override
	public boolean equals(ATNConfig other) {
		if (this == other) {
			return true;
		}
		else if (!(other instanceof LexerATNConfig)) {
			return false;
		}

		LexerATNConfig lexerOther = (LexerATNConfig)other;
		if (passedThroughNonGreedyDecision != lexerOther.passedThroughNonGreedyDecision) {
			return false;
		}

		if (!ObjectEqualityComparator.INSTANCE.equals(lexerActionExecutor, lexerOther.lexerActionExecutor)) {
			return false;
		}

		return super.equals(other);
	}

	private static boolean checkNonGreedyDecision(LexerATNConfig source, ATNState target) {
		return source.passedThroughNonGreedyDecision
			|| target instanceof DecisionState && ((DecisionState)target).nonGreedy;
	}
}
