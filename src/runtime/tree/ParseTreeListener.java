

package runtime.tree;

import runtime.ParserRuleContext;


public interface ParseTreeListener {
	void visitTerminal(TerminalNode node);
	void visitErrorNode(ErrorNode node);
    void enterEveryRule(ParserRuleContext ctx);
    void exitEveryRule(ParserRuleContext ctx);
}
