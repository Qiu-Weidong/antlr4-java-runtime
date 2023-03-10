

package runtime.tree;

import runtime.Parser;
import runtime.RuleContext;
import runtime.Token;
import runtime.misc.Interval;

public class TerminalNodeImpl implements TerminalNode {
	public Token symbol;
	public ParseTree parent;

	public TerminalNodeImpl(Token symbol) {	this.symbol = symbol;	}

	@Override
	public ParseTree getChild(int i) {return null;}

	@Override
	public Token getSymbol() {return symbol;}

	@Override
	public ParseTree getParent() { return parent; }

	@Override
	public void setParent(RuleContext parent) {
		this.parent = parent;
	}

	@Override
	public Token getPayload() { return symbol; }

	@Override
	public Interval getSourceInterval() {
		if ( symbol ==null ) return Interval.INVALID;

		int tokenIndex = symbol.getTokenIndex();
		return new Interval(tokenIndex, tokenIndex);
	}

	@Override
	public int getChildCount() { return 0; }

	@Override
	public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
		return visitor.visitTerminal(this);
	}

	@Override
	public String getText() { return symbol.getText(); }

	@Override
	public String toStringTree(Parser parser) {
		return toString();
	}

	@Override
	public String toString() {
			if ( symbol.getType() == Token.EOF ) return "<EOF>";
			return symbol.getText();
	}

	@Override
	public String toStringTree() {
		return toString();
	}
}
