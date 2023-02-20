

package runtime.atn;

import java.util.HashMap;
import java.util.Map;


public class PredictionContextCache {
	protected final Map<PredictionContext, PredictionContext> cache =
		new HashMap<PredictionContext, PredictionContext>();

	
	public PredictionContext add(PredictionContext ctx) {
		if ( ctx==EmptyPredictionContext.Instance ) return EmptyPredictionContext.Instance;
		PredictionContext existing = cache.get(ctx);
		if ( existing!=null ) {

			return existing;
		}
		cache.put(ctx, ctx);
		return ctx;
	}

	public PredictionContext get(PredictionContext ctx) {
		return cache.get(ctx);
	}

	public int size() {
		return cache.size();
	}
}
