

package runtime.tree;


public interface ParseTreeVisitor<T> {

	
	T visit(ParseTree tree);

	
	T visitChildren(RuleNode node);

	
	T visitTerminal(TerminalNode node);

	
	T visitErrorNode(ErrorNode node);

}
