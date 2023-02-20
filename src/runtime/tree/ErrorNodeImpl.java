

package runtime.tree;

import runtime.Token;


public class ErrorNodeImpl extends TerminalNodeImpl implements ErrorNode {
	public ErrorNodeImpl(Token token) {
		super(token);
	}

	@Override
	public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
		return visitor.visitErrorNode(this);
	}
}
