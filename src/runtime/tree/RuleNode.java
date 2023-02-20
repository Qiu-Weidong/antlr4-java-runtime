

package runtime.tree;

import runtime.RuleContext;

public interface RuleNode extends ParseTree {
	RuleContext getRuleContext();
}
