

package runtime.atn;

import runtime.misc.AbstractEqualityComparator;
import runtime.misc.FlexibleHashMap;
import runtime.misc.MurmurHash;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public enum PredictionMode {
	
	SLL,
	
	LL,
	
	LL_EXACT_AMBIG_DETECTION;

	
	static class AltAndContextMap extends FlexibleHashMap<ATNConfig,BitSet> {
		public AltAndContextMap() {
			super(AltAndContextConfigEqualityComparator.INSTANCE);
		}
	}

	private static final class AltAndContextConfigEqualityComparator extends AbstractEqualityComparator<ATNConfig> {
		public static final AltAndContextConfigEqualityComparator INSTANCE = new AltAndContextConfigEqualityComparator();

		private AltAndContextConfigEqualityComparator() {
		}

		
		@Override
		public int hashCode(ATNConfig o) {
			int hashCode = MurmurHash.initialize(7);
			hashCode = MurmurHash.update(hashCode, o.state.stateNumber);
			hashCode = MurmurHash.update(hashCode, o.context);
			hashCode = MurmurHash.finish(hashCode, 2);
	        return hashCode;
		}

		@Override
		public boolean equals(ATNConfig a, ATNConfig b) {
			if ( a==b ) return true;
			if ( a==null || b==null ) return false;
			return a.state.stateNumber==b.state.stateNumber
				&& a.context.equals(b.context);
		}
	}

	public static boolean hasSLLConflictTerminatingPrediction(PredictionMode mode, ATNConfigSet configs) {
		/* Configs in rule stop states indicate reaching the end of the decision
		 * rule (local context) or end of start rule (full context). If all
		 * configs meet this condition, then none of the configurations is able
		 * to match additional input so we terminate prediction.
		 */
		if (allConfigsInRuleStopStates(configs)) {
			return true;
		}

		// pure SLL mode parsing
		if ( mode == PredictionMode.SLL ) {
			// Don't bother with combining configs from different semantic
			// contexts if we can fail over to full LL; costs more time
			// since we'll often fail over anyway.
			if ( configs.hasSemanticContext ) {
				// dup configs, tossing out semantic predicates
				ATNConfigSet dup = new ATNConfigSet();
				for (ATNConfig c : configs) {
					c = new ATNConfig(c,SemanticContext.Empty.Instance);
					dup.add(c);
				}
				configs = dup;
			}
			// now we have combined contexts for configs with dissimilar preds
		}

		// pure SLL or combined SLL+LL mode parsing

		Collection<BitSet> altsets = getConflictingAltSubsets(configs);
		boolean heuristic =
			hasConflictingAltSet(altsets) && !hasStateAssociatedWithOneAlt(configs);
		return heuristic;
	}

	/**
	 * Checks if any configuration in {@code configs} is in a
	 * {@link RuleStopState}. Configurations meeting this condition have reached
	 * the end of the decision rule (local context) or end of start rule (full
	 * context).
	 *
	 * @param configs the configuration set to test
	 * @return {@code true} if any configuration in {@code configs} is in a
	 * {@link RuleStopState}, otherwise {@code false}
	 */
	public static boolean hasConfigInRuleStopState(ATNConfigSet configs) {
		for (ATNConfig c : configs) {
			if (c.state instanceof RuleStopState) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if all configurations in {@code configs} are in a
	 * {@link RuleStopState}. Configurations meeting this condition have reached
	 * the end of the decision rule (local context) or end of start rule (full
	 * context).
	 *
	 * @param configs the configuration set to test
	 * @return {@code true} if all configurations in {@code configs} are in a
	 * {@link RuleStopState}, otherwise {@code false}
	 */
	public static boolean allConfigsInRuleStopStates(ATNConfigSet configs) {
		for (ATNConfig config : configs) {
			if (!(config.state instanceof RuleStopState)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Full LL prediction termination.
	 *
	 * <p>Can we stop looking ahead during ATN simulation or is there some
	 * uncertainty as to which alternative we will ultimately pick, after
	 * consuming more input? Even if there are partial conflicts, we might know
	 * that everything is going to resolve to the same minimum alternative. That
	 * means we can stop since no more lookahead will change that fact. On the
	 * other hand, there might be multiple conflicts that resolve to different
	 * minimums. That means we need more look ahead to decide which of those
	 * alternatives we should predict.</p>
	 *
	 * <p>The basic idea is to split the set of configurations {@code C}, into
	 * conflicting subsets {@code (s, _, ctx, _)} and singleton subsets with
	 * non-conflicting configurations. Two configurations conflict if they have
	 * identical {@link ATNConfig#state} and {@link ATNConfig#context} values
	 * but different {@link ATNConfig#alt} value, e.g. {@code (s, i, ctx, _)}
	 * and {@code (s, j, ctx, _)} for {@code i!=j}.</p>
	 *
	 * <p>Reduce these configuration subsets to the set of possible alternatives.
	 * You can compute the alternative subsets in one pass as follows:</p>
	 *
	 * <p>{@code A_s,ctx = {i | (s, i, ctx, _)}} for each configuration in
	 * {@code C} holding {@code s} and {@code ctx} fixed.</p>
	 *
	 * <p>Or in pseudo-code, for each configuration {@code c} in {@code C}:</p>
	 *
	 * <pre>
	 * map[c] U= c.{@link ATNConfig#alt alt} # map hash/equals uses s and x, not
	 * alt and not pred
	 * </pre>
	 *
	 * <p>The values in {@code map} are the set of {@code A_s,ctx} sets.</p>
	 *
	 * <p>If {@code |A_s,ctx|=1} then there is no conflict associated with
	 * {@code s} and {@code ctx}.</p>
	 *
	 * <p>Reduce the subsets to singletons by choosing a minimum of each subset. If
	 * the union of these alternative subsets is a singleton, then no amount of
	 * more lookahead will help us. We will always pick that alternative. If,
	 * however, there is more than one alternative, then we are uncertain which
	 * alternative to predict and must continue looking for resolution. We may
	 * or may not discover an ambiguity in the future, even if there are no
	 * conflicting subsets this round.</p>
	 *
	 * <p>The biggest sin is to terminate early because it means we've made a
	 * decision but were uncertain as to the eventual outcome. We haven't used
	 * enough lookahead. On the other hand, announcing a conflict too late is no
	 * big deal; you will still have the conflict. It's just inefficient. It
	 * might even look until the end of file.</p>
	 *
	 * <p>No special consideration for semantic predicates is required because
	 * predicates are evaluated on-the-fly for full LL prediction, ensuring that
	 * no configuration contains a semantic context during the termination
	 * check.</p>
	 *
	 * <p><strong>CONFLICTING CONFIGS</strong></p>
	 *
	 * <p>Two configurations {@code (s, i, x)} and {@code (s, j, x')}, conflict
	 * when {@code i!=j} but {@code x=x'}. Because we merge all
	 * {@code (s, i, _)} configurations together, that means that there are at
	 * most {@code n} configurations associated with state {@code s} for
	 * {@code n} possible alternatives in the decision. The merged stacks
	 * complicate the comparison of configuration contexts {@code x} and
	 * {@code x'}. Sam checks to see if one is a subset of the other by calling
	 * merge and checking to see if the merged result is either {@code x} or
	 * {@code x'}. If the {@code x} associated with lowest alternative {@code i}
	 * is the superset, then {@code i} is the only possible prediction since the
	 * others resolve to {@code min(i)} as well. However, if {@code x} is
	 * associated with {@code j>i} then at least one stack configuration for
	 * {@code j} is not in conflict with alternative {@code i}. The algorithm
	 * should keep going, looking for more lookahead due to the uncertainty.</p>
	 *
	 * <p>For simplicity, I'm doing a equality check between {@code x} and
	 * {@code x'} that lets the algorithm continue to consume lookahead longer
	 * than necessary. The reason I like the equality is of course the
	 * simplicity but also because that is the test you need to detect the
	 * alternatives that are actually in conflict.</p>
	 *
	 * <p><strong>CONTINUE/STOP RULE</strong></p>
	 *
	 * <p>Continue if union of resolved alternative sets from non-conflicting and
	 * conflicting alternative subsets has more than one alternative. We are
	 * uncertain about which alternative to predict.</p>
	 *
	 * <p>The complete set of alternatives, {@code [i for (_,i,_)]}, tells us which
	 * alternatives are still in the running for the amount of input we've
	 * consumed at this point. The conflicting sets let us to strip away
	 * configurations that won't lead to more states because we resolve
	 * conflicts to the configuration with a minimum alternate for the
	 * conflicting set.</p>
	 *
	 * <p><strong>CASES</strong></p>
	 *
	 * <ul>
	 *
	 * <li>no conflicts and more than 1 alternative in set =&gt; continue</li>
	 *
	 * <li> {@code (s, 1, x)}, {@code (s, 2, x)}, {@code (s, 3, z)},
	 * {@code (s', 1, y)}, {@code (s', 2, y)} yields non-conflicting set
	 * {@code {3}} U conflicting sets {@code min({1,2})} U {@code min({1,2})} =
	 * {@code {1,3}} =&gt; continue
	 * </li>
	 *
	 * <li>{@code (s, 1, x)}, {@code (s, 2, x)}, {@code (s', 1, y)},
	 * {@code (s', 2, y)}, {@code (s'', 1, z)} yields non-conflicting set
	 * {@code {1}} U conflicting sets {@code min({1,2})} U {@code min({1,2})} =
	 * {@code {1}} =&gt; stop and predict 1</li>
	 *
	 * <li>{@code (s, 1, x)}, {@code (s, 2, x)}, {@code (s', 1, y)},
	 * {@code (s', 2, y)} yields conflicting, reduced sets {@code {1}} U
	 * {@code {1}} = {@code {1}} =&gt; stop and predict 1, can announce
	 * ambiguity {@code {1,2}}</li>
	 *
	 * <li>{@code (s, 1, x)}, {@code (s, 2, x)}, {@code (s', 2, y)},
	 * {@code (s', 3, y)} yields conflicting, reduced sets {@code {1}} U
	 * {@code {2}} = {@code {1,2}} =&gt; continue</li>
	 *
	 * <li>{@code (s, 1, x)}, {@code (s, 2, x)}, {@code (s', 3, y)},
	 * {@code (s', 4, y)} yields conflicting, reduced sets {@code {1}} U
	 * {@code {3}} = {@code {1,3}} =&gt; continue</li>
	 *
	 * </ul>
	 *
	 * <p><strong>EXACT AMBIGUITY DETECTION</strong></p>
	 *
	 * <p>If all states report the same conflicting set of alternatives, then we
	 * know we have the exact ambiguity set.</p>
	 *
	 * <p><code>|A_<em>i</em>|&gt;1</code> and
	 * <code>A_<em>i</em> = A_<em>j</em></code> for all <em>i</em>, <em>j</em>.</p>
	 *
	 * <p>In other words, we continue examining lookahead until all {@code A_i}
	 * have more than one alternative and all {@code A_i} are the same. If
	 * {@code A={{1,2}, {1,3}}}, then regular LL prediction would terminate
	 * because the resolved set is {@code {1}}. To determine what the real
	 * ambiguity is, we have to know whether the ambiguity is between one and
	 * two or one and three so we keep going. We can only stop prediction when
	 * we need exact ambiguity detection when the sets look like
	 * {@code A={{1,2}}} or {@code {{1,2},{1,2}}}, etc...</p>
	 */
	public static int resolvesToJustOneViableAlt(Collection<BitSet> altsets) {
		return getSingleViableAlt(altsets);
	}

	/**
	 * Determines if every alternative subset in {@code altsets} contains more
	 * than one alternative.
	 *
	 * @param altsets a collection of alternative subsets
	 * @return {@code true} if every {@link BitSet} in {@code altsets} has
	 * {@link BitSet#cardinality cardinality} &gt; 1, otherwise {@code false}
	 */
	public static boolean allSubsetsConflict(Collection<BitSet> altsets) {
		return !hasNonConflictingAltSet(altsets);
	}

	/**
	 * Determines if any single alternative subset in {@code altsets} contains
	 * exactly one alternative.
	 *
	 * @param altsets a collection of alternative subsets
	 * @return {@code true} if {@code altsets} contains a {@link BitSet} with
	 * {@link BitSet#cardinality cardinality} 1, otherwise {@code false}
	 */
	public static boolean hasNonConflictingAltSet(Collection<BitSet> altsets) {
		for (BitSet alts : altsets) {
			if ( alts.cardinality()==1 ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if any single alternative subset in {@code altsets} contains
	 * more than one alternative.
	 *
	 * @param altsets a collection of alternative subsets
	 * @return {@code true} if {@code altsets} contains a {@link BitSet} with
	 * {@link BitSet#cardinality cardinality} &gt; 1, otherwise {@code false}
	 */
	public static boolean hasConflictingAltSet(Collection<BitSet> altsets) {
		for (BitSet alts : altsets) {
			if ( alts.cardinality()>1 ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if every alternative subset in {@code altsets} is equivalent.
	 *
	 * @param altsets a collection of alternative subsets
	 * @return {@code true} if every member of {@code altsets} is equal to the
	 * others, otherwise {@code false}
	 */
	public static boolean allSubsetsEqual(Collection<BitSet> altsets) {
		Iterator<BitSet> it = altsets.iterator();
		BitSet first = it.next();
		while ( it.hasNext() ) {
			BitSet next = it.next();
			if ( !next.equals(first) ) return false;
		}
		return true;
	}

	/**
	 * Returns the unique alternative predicted by all alternative subsets in
	 * {@code altsets}. If no such alternative exists, this method returns
	 * {@link ATN#INVALID_ALT_NUMBER}.
	 *
	 * @param altsets a collection of alternative subsets
	 */
	public static int getUniqueAlt(Collection<BitSet> altsets) {
		BitSet all = getAlts(altsets);
		if ( all.cardinality()==1 ) return all.nextSetBit(0);
		return ATN.INVALID_ALT_NUMBER;
	}

	/**
	 * Gets the complete set of represented alternatives for a collection of
	 * alternative subsets. This method returns the union of each {@link BitSet}
	 * in {@code altsets}.
	 *
	 * @param altsets a collection of alternative subsets
	 * @return the set of represented alternatives in {@code altsets}
	 */
	public static BitSet getAlts(Collection<BitSet> altsets) {
		BitSet all = new BitSet();
		for (BitSet alts : altsets) {
			all.or(alts);
		}
		return all;
	}

	/**
	 * Get union of all alts from configs.
	 *
	 * @since 4.5.1
	 */
	public static BitSet getAlts(ATNConfigSet configs) {
		BitSet alts = new BitSet();
		for (ATNConfig config : configs) {
			alts.set(config.alt);
		}
		return alts;
	}

	/**
	 * This function gets the conflicting alt subsets from a configuration set.
	 * For each configuration {@code c} in {@code configs}:
	 *
	 * <pre>
	 * map[c] U= c.{@link ATNConfig#alt alt} # map hash/equals uses s and x, not
	 * alt and not pred
	 * </pre>
	 */
	public static Collection<BitSet> getConflictingAltSubsets(ATNConfigSet configs) {
		AltAndContextMap configToAlts = new AltAndContextMap();
		for (ATNConfig c : configs) {
			BitSet alts = configToAlts.get(c);
			if ( alts==null ) {
				alts = new BitSet();
				configToAlts.put(c, alts);
			}
			alts.set(c.alt);
		}
		return configToAlts.values();
	}

	/**
	 * Get a map from state to alt subset from a configuration set. For each
	 * configuration {@code c} in {@code configs}:
	 *
	 * <pre>
	 * map[c.{@link ATNConfig#state state}] U= c.{@link ATNConfig#alt alt}
	 * </pre>
	 */
	public static Map<ATNState, BitSet> getStateToAltMap(ATNConfigSet configs) {
		Map<ATNState, BitSet> m = new HashMap<ATNState, BitSet>();
		for (ATNConfig c : configs) {
			BitSet alts = m.get(c.state);
			if ( alts==null ) {
				alts = new BitSet();
				m.put(c.state, alts);
			}
			alts.set(c.alt);
		}
		return m;
	}

	public static boolean hasStateAssociatedWithOneAlt(ATNConfigSet configs) {
		Map<ATNState, BitSet> x = getStateToAltMap(configs);
		for (BitSet alts : x.values()) {
			if ( alts.cardinality()==1 ) return true;
		}
		return false;
	}

	public static int getSingleViableAlt(Collection<BitSet> altsets) {
		BitSet viableAlts = new BitSet();
		for (BitSet alts : altsets) {
			int minAlt = alts.nextSetBit(0);
			viableAlts.set(minAlt);
			if ( viableAlts.cardinality()>1 ) { // more than 1 viable alt
				return ATN.INVALID_ALT_NUMBER;
			}
		}
		return viableAlts.nextSetBit(0);
	}

}
