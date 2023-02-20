

package runtime.atn;

import runtime.ANTLRErrorListener;
import runtime.TokenStream;

import java.util.BitSet;


public class AmbiguityInfo extends DecisionEventInfo {
	
	public BitSet ambigAlts;

	
	public AmbiguityInfo(int decision,
						 ATNConfigSet configs,
						 BitSet ambigAlts,
						 TokenStream input, int startIndex, int stopIndex,
						 boolean fullCtx)
	{
		super(decision, configs, input, startIndex, stopIndex, fullCtx);
		this.ambigAlts = ambigAlts;
	}
}
