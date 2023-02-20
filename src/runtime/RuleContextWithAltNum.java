

package runtime;

import runtime.atn.ATN;


public class RuleContextWithAltNum extends ParserRuleContext {
	public int altNum;
	public RuleContextWithAltNum() { altNum = ATN.INVALID_ALT_NUMBER; }

	public RuleContextWithAltNum(ParserRuleContext parent, int invokingStateNumber) {
		super(parent, invokingStateNumber);
	}
	@Override public int getAltNumber() { return altNum; }
	@Override public void setAltNumber(int altNum) { this.altNum = altNum; }
}
