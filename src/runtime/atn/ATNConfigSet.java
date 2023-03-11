

package runtime.atn;

import runtime.misc.DoubleKeyMap;

import java.util.*;


public class ATNConfigSet implements Set<ATNConfig> {


	public final ArrayList<ATNConfig> configs = new ArrayList<ATNConfig>(7);



	public int uniqueAlt;
	
	protected BitSet conflictingAlts;



	public boolean hasSemanticContext;
	public boolean dipsIntoOuterContext;

	
	public final boolean fullCtx;

	public ATNConfigSet(boolean fullCtx) {
//		configLookup = new ConfigHashSet();
		this.fullCtx = fullCtx;
	}
	public ATNConfigSet() { this(true); }

	@Override
	public boolean add(ATNConfig config) {
		return add(config, null);
	}

	
	public boolean add(
		ATNConfig config,
		DoubleKeyMap<PredictionContext,PredictionContext,PredictionContext> mergeCache)
	{
//		if ( readonly ) throw new IllegalStateException("This set is readonly");
		if ( config.semanticContext != SemanticContext.Empty.Instance ) {
			hasSemanticContext = true;
		}
		if (config.getOuterContextDepth() > 0) {
			dipsIntoOuterContext = true;
		}
		ATNConfig existing = null;
		for(ATNConfig c: configs) {
			if(c.equals(config)) existing = c;
		}

		if ( existing==null) {
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

	public ATNConfig get(int i) { return configs.get(i); }

	public void optimizeConfigs(ATNSimulator interpreter) {
//		if ( readonly ) throw new IllegalStateException("This set is readonly");
//		if ( configLookup.isEmpty() ) return;

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
//		if (isReadonly()) {
//			if (cachedHashCode == -1) {
//				cachedHashCode = configs.hashCode();
//			}
//
//			return cachedHashCode;
//		}

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
		throw new UnsupportedOperationException("fuck");
	}

	@Override
	public Iterator<ATNConfig> iterator() {
		return configs.iterator();
	}

	@Override
	public void clear() {
//		if ( readonly ) throw new IllegalStateException("This set is readonly");
		configs.clear();
		//		configLookup.clear();
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
		return new ATNConfig[0];
//		return configLookup.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return a;
//		return configLookup.toArray(new ATNConfig[0]);
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

}
