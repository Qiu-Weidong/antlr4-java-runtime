

package runtime.atn;

import runtime.misc.AbstractEqualityComparator;
import runtime.misc.Array2DHashSet;
import runtime.misc.DoubleKeyMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ATNConfigSet implements Set<ATNConfig> {
	
	public static class ConfigHashSet extends AbstractConfigHashSet {
		public ConfigHashSet() {
			super(ConfigEqualityComparator.INSTANCE);
		}
	}

	public static final class ConfigEqualityComparator extends AbstractEqualityComparator<ATNConfig> {
		public static final ConfigEqualityComparator INSTANCE = new ConfigEqualityComparator();

		private ConfigEqualityComparator() {
		}

		@Override
		public int hashCode(ATNConfig o) {
			int hashCode = 7;
			hashCode = 31 * hashCode + o.state.stateNumber;
			hashCode = 31 * hashCode + o.alt;
			hashCode = 31 * hashCode + o.semanticContext.hashCode();
	        return hashCode;
		}

		@Override
		public boolean equals(ATNConfig a, ATNConfig b) {
			if ( a==b ) return true;
			if ( a==null || b==null ) return false;
			return a.state.stateNumber==b.state.stateNumber
				&& a.alt==b.alt
				&& a.semanticContext.equals(b.semanticContext);
		}
	}

	
	protected boolean readonly = false;

	
	public AbstractConfigHashSet configLookup;

	
	public final ArrayList<ATNConfig> configs = new ArrayList<ATNConfig>(7);



	public int uniqueAlt;
	
	protected BitSet conflictingAlts;



	public boolean hasSemanticContext;
	public boolean dipsIntoOuterContext;

	
	public final boolean fullCtx;

	private int cachedHashCode = -1;

	public ATNConfigSet(boolean fullCtx) {
		configLookup = new ConfigHashSet();
		this.fullCtx = fullCtx;
	}
	public ATNConfigSet() { this(true); }

	public ATNConfigSet(ATNConfigSet old) {
		this(old.fullCtx);
		addAll(old);
		this.uniqueAlt = old.uniqueAlt;
		this.conflictingAlts = old.conflictingAlts;
		this.hasSemanticContext = old.hasSemanticContext;
		this.dipsIntoOuterContext = old.dipsIntoOuterContext;
	}

	@Override
	public boolean add(ATNConfig config) {
		return add(config, null);
	}

	
	public boolean add(
		ATNConfig config,
		DoubleKeyMap<PredictionContext,PredictionContext,PredictionContext> mergeCache)
	{
		if ( readonly ) throw new IllegalStateException("This set is readonly");
		if ( config.semanticContext != SemanticContext.Empty.Instance ) {
			hasSemanticContext = true;
		}
		if (config.getOuterContextDepth() > 0) {
			dipsIntoOuterContext = true;
		}
		ATNConfig existing = configLookup.getOrAdd(config);
		if ( existing==config ) {
			cachedHashCode = -1;
			configs.add(config);
			return true;
		}

		boolean rootIsWildcard = !fullCtx;
		PredictionContext merged =
			PredictionContext.merge(existing.context, config.context, rootIsWildcard, mergeCache);



		existing.reachesIntoOuterContext =
			Math.max(existing.reachesIntoOuterContext, config.reachesIntoOuterContext);


		if (config.isPrecedenceFilterSuppressed()) {
			existing.setPrecedenceFilterSuppressed(true);
		}

		existing.context = merged;
		return true;
	}

	
    public List<ATNConfig> elements() { return configs; }

	public Set<ATNState> getStates() {
		Set<ATNState> states = new HashSet<ATNState>();
		for (ATNConfig c : configs) {
			states.add(c.state);
		}
		return states;
	}

	

	public BitSet getAlts() {
		BitSet alts = new BitSet();
		for (ATNConfig config : configs) {
			alts.set(config.alt);
		}
		return alts;
	}

	public List<SemanticContext> getPredicates() {
		List<SemanticContext> preds = new ArrayList<SemanticContext>();
		for (ATNConfig c : configs) {
			if ( c.semanticContext!=SemanticContext.Empty.Instance ) {
				preds.add(c.semanticContext);
			}
		}
		return preds;
	}

	public ATNConfig get(int i) { return configs.get(i); }

	public void optimizeConfigs(ATNSimulator interpreter) {
		if ( readonly ) throw new IllegalStateException("This set is readonly");
		if ( configLookup.isEmpty() ) return;

		for (ATNConfig config : configs) {

			config.context = interpreter.getCachedContext(config.context);


		}
	}

	@Override
	public boolean addAll(Collection<? extends ATNConfig> coll) {
		for (ATNConfig c : coll) add(c);
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		else if (!(o instanceof ATNConfigSet)) {
			return false;
		}


		ATNConfigSet other = (ATNConfigSet)o;
		boolean same = configs!=null &&
			configs.equals(other.configs) &&
			this.fullCtx == other.fullCtx &&
			this.uniqueAlt == other.uniqueAlt &&
			this.conflictingAlts == other.conflictingAlts &&
			this.hasSemanticContext == other.hasSemanticContext &&
			this.dipsIntoOuterContext == other.dipsIntoOuterContext;


		return same;
	}

	@Override
	public int hashCode() {
		if (isReadonly()) {
			if (cachedHashCode == -1) {
				cachedHashCode = configs.hashCode();
			}

			return cachedHashCode;
		}

		return configs.hashCode();
	}

	@Override
	public int size() {
		return configs.size();
	}

	@Override
	public boolean isEmpty() {
		return configs.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		if (configLookup == null) {
			throw new UnsupportedOperationException("This method is not implemented for readonly sets.");
		}

		return configLookup.contains(o);
	}

	@Override
	public Iterator<ATNConfig> iterator() {
		return configs.iterator();
	}

	@Override
	public void clear() {
		if ( readonly ) throw new IllegalStateException("This set is readonly");
		configs.clear();
		cachedHashCode = -1;
		configLookup.clear();
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
		configLookup = null;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(elements().toString());
		if ( hasSemanticContext ) buf.append(",hasSemanticContext=").append(hasSemanticContext);
		if ( uniqueAlt!=ATN.INVALID_ALT_NUMBER ) buf.append(",uniqueAlt=").append(uniqueAlt);
		if ( conflictingAlts!=null ) buf.append(",conflictingAlts=").append(conflictingAlts);
		if ( dipsIntoOuterContext ) buf.append(",dipsIntoOuterContext");
		return buf.toString();
	}



	@Override
	public ATNConfig[] toArray() {
		return configLookup.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return configLookup.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public static abstract class AbstractConfigHashSet extends Array2DHashSet<ATNConfig> {

		public AbstractConfigHashSet(AbstractEqualityComparator<? super ATNConfig> comparator) {
			this(comparator, 16, 2);
		}

		public AbstractConfigHashSet(AbstractEqualityComparator<? super ATNConfig> comparator, int initialCapacity, int initialBucketCapacity) {
			super(comparator, initialCapacity, initialBucketCapacity);
		}

		@Override
		protected final ATNConfig asElementType(Object o) {
			if (!(o instanceof ATNConfig)) {
				return null;
			}

			return (ATNConfig)o;
		}

		@Override
		protected final ATNConfig[][] createBuckets(int capacity) {
			return new ATNConfig[capacity][];
		}

		@Override
		protected final ATNConfig[] createBucket(int capacity) {
			return new ATNConfig[capacity];
		}

	}
}
