

package runtime.tree;

import runtime.TokenStream;
import runtime.misc.Interval;


public interface SyntaxTree extends Tree {
	
	Interval getSourceInterval();
}
