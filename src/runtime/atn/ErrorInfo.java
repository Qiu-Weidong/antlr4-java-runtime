

package runtime.atn;

import runtime.ANTLRErrorListener;
import runtime.Parser;
import runtime.RecognitionException;
import runtime.Token;
import runtime.TokenStream;


public class ErrorInfo extends DecisionEventInfo {
	
	public ErrorInfo(int decision,
					 ATNConfigSet configs,
					 TokenStream input, int startIndex, int stopIndex,
					 boolean fullCtx)
	{
		super(decision, configs, input, startIndex, stopIndex, fullCtx);
	}
}
