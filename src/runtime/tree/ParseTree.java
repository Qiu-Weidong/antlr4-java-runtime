

package runtime.tree;

import runtime.Parser;
import runtime.RuleContext;
import runtime.Token;


public interface ParseTree extends SyntaxTree {
	// the following methods narrow the return type; they are not additional methods
	@Override
	ParseTree getParent();
	@Override
	ParseTree getChild(int i);


	
	void setParent(RuleContext parent);

	
	<T> T accept(ParseTreeVisitor<? extends T> visitor);

	
	String getText();

	
	String toStringTree(Parser parser);
}
