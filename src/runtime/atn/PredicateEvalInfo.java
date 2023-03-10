

package runtime.atn;

import runtime.ParserRuleContext;
import runtime.Recognizer;
import runtime.RuleContext;
import runtime.TokenStream;


public class PredicateEvalInfo extends DecisionEventInfo {
	
	public final SemanticContext semctx;
	
	public final int predictedAlt;
	
	public final boolean evalResult;

	
	public PredicateEvalInfo(int decision,
							 TokenStream input, int startIndex, int stopIndex,
							 SemanticContext semctx,
							 boolean evalResult,
							 int predictedAlt,
							 boolean fullCtx)
	{
		super(decision, new ATNConfigSet(), input, startIndex, stopIndex, fullCtx);
		this.semctx = semctx;
		this.evalResult = evalResult;
		this.predictedAlt = predictedAlt;
	}
}
