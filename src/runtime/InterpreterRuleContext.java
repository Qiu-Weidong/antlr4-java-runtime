
package runtime;


public class InterpreterRuleContext extends ParserRuleContext {
	
	protected int ruleIndex = -1;

	public InterpreterRuleContext() { }

	
	public InterpreterRuleContext(ParserRuleContext parent,
								  int invokingStateNumber,
								  int ruleIndex)
	{
		super(parent, invokingStateNumber);
		this.ruleIndex = ruleIndex;
	}

	@Override
	public int getRuleIndex() {
		return ruleIndex;
	}
}
