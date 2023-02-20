

package runtime.tree;

import runtime.Token;

public interface TerminalNode extends ParseTree {
	Token getSymbol();
}
