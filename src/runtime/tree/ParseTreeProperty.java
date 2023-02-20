

package runtime.tree;

import java.util.IdentityHashMap;
import java.util.Map;


public class ParseTreeProperty<V> {
	protected Map<ParseTree, V> annotations = new IdentityHashMap<ParseTree, V>();

	public V get(ParseTree node) { return annotations.get(node); }
	public void put(ParseTree node, V value) { annotations.put(node, value); }
	public V removeFrom(ParseTree node) { return annotations.remove(node); }
}
