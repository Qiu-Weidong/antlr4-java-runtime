

package runtime.tree;

import runtime.RuleContext;
import runtime.Token;


public interface Tree {
	
	Tree getParent();

	
	Object getPayload();

	
	Tree getChild(int i);

	
	int getChildCount();

	
	String toStringTree();
}
