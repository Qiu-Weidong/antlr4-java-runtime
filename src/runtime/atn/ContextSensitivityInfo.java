

package runtime.atn;

import runtime.ANTLRErrorListener;
import runtime.TokenStream;


public class ContextSensitivityInfo extends DecisionEventInfo {
	
	public ContextSensitivityInfo(int decision,
								  ATNConfigSet configs,
								  TokenStream input, int startIndex, int stopIndex)
	{
		super( decision, configs, input, startIndex, stopIndex, true);
	}
}
