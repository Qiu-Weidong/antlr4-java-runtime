

package runtime.atn;

import runtime.misc.ObjectEqualityComparator;


public class OrderedATNConfigSet extends ATNConfigSet {

	public OrderedATNConfigSet() {
		this.configLookup = new LexerConfigHashSet();
	}

	public static class LexerConfigHashSet extends AbstractConfigHashSet {
		public LexerConfigHashSet() {
			super(ObjectEqualityComparator.INSTANCE);
		}
	}
}
