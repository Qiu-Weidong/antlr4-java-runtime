

package runtime.atn;

import runtime.misc.MurmurHash;

import java.util.Objects;


public class ATNConfig {
	
	private static final int SUPPRESS_PRECEDENCE_FILTER = 0x40000000;

	
	public final ATNState state;

	
	public final int alt;

	
	public PredictionContext context;

	
	public int reachesIntoOuterContext;


    public final SemanticContext semanticContext;

	public ATNConfig(ATNState state,
					 int alt,
					 PredictionContext context)
	{
		this(state, alt, context, SemanticContext.Empty.Instance);
	}

	public ATNConfig(ATNState state,
					 int alt,
					 PredictionContext context,
					 SemanticContext semanticContext)
	{
		this.state = state;
		this.alt = alt;
		this.context = context;
		this.semanticContext = semanticContext;
	}

    public ATNConfig(ATNConfig c, ATNState state) {
   		this(c, state, c.context, c.semanticContext);
   	}

	public ATNConfig(ATNConfig c, ATNState state,
		 SemanticContext semanticContext)
{
		this(c, state, c.context, semanticContext);
	}

	public ATNConfig(ATNConfig c,
					 SemanticContext semanticContext)
	{
		this(c, c.state, c.context, semanticContext);
	}

    public ATNConfig(ATNConfig c, ATNState state,
					 PredictionContext context)
	{
        this(c, state, context, c.semanticContext);
    }

	public ATNConfig(ATNConfig c, ATNState state,
					 PredictionContext context,
                     SemanticContext semanticContext)
    {
		this.state = state;
		this.alt = c.alt;
		this.context = context;
		this.semanticContext = semanticContext;
		this.reachesIntoOuterContext = c.reachesIntoOuterContext;
	}

	
	public final int getOuterContextDepth() {
		return reachesIntoOuterContext & ~SUPPRESS_PRECEDENCE_FILTER;
	}

	public final boolean isPrecedenceFilterSuppressed() {
		return (reachesIntoOuterContext & SUPPRESS_PRECEDENCE_FILTER) != 0;
	}

	public final void setPrecedenceFilterSuppressed(boolean value) {
		if (value) {
			this.reachesIntoOuterContext |= 0x40000000;
		}
		else {
			this.reachesIntoOuterContext &= ~SUPPRESS_PRECEDENCE_FILTER;
		}
	}

	
    @Override
    public boolean equals(Object o) {
		if (!(o instanceof ATNConfig)){
			return false;
		}

		return this.equals((ATNConfig)o);
	}

	public boolean equals(ATNConfig other) {
		if (this == other) {
			return true;
		}
		else if (other == null) {
			return false;
		}

		return this.state.stateNumber==other.state.stateNumber
			&& this.alt==other.alt
			&& Objects.equals(this.context, other.context)
			&& this.semanticContext.equals(other.semanticContext)
			&& this.isPrecedenceFilterSuppressed() == other.isPrecedenceFilterSuppressed();
	}

	@Override
	public int hashCode() {
		int hashCode = MurmurHash.initialize(7);
		hashCode = MurmurHash.update(hashCode, state.stateNumber);
		hashCode = MurmurHash.update(hashCode, alt);
		hashCode = MurmurHash.update(hashCode, context);
		hashCode = MurmurHash.update(hashCode, semanticContext);
		hashCode = MurmurHash.finish(hashCode, 4);
		return hashCode;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean showAlt) {
		StringBuilder buf = new StringBuilder();




		buf.append('(');
		buf.append(state);
		if ( showAlt ) {
            buf.append(",");
            buf.append(alt);
        }
        if ( context!=null ) {
            buf.append(",[");
            buf.append(context.toString());
			buf.append("]");
        }
        if ( semanticContext!=null && semanticContext != SemanticContext.Empty.Instance ) {
            buf.append(",");
            buf.append(semanticContext);
        }
        if ( getOuterContextDepth()>0 ) {
            buf.append(",up=").append(getOuterContextDepth());
        }
		buf.append(')');
		return buf.toString();
    }
}
