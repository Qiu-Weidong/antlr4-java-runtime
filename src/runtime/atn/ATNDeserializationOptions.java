

package runtime.atn;


public class ATNDeserializationOptions {
	private static final ATNDeserializationOptions defaultOptions;
	static {
		defaultOptions = new ATNDeserializationOptions();
		defaultOptions.makeReadOnly();
	}

	private boolean readOnly;
	private boolean verifyATN;
	private boolean generateRuleBypassTransitions;

	public ATNDeserializationOptions() {
		this.verifyATN = true;
		this.generateRuleBypassTransitions = false;
	}


	public static ATNDeserializationOptions getDefaultOptions() {
		return defaultOptions;
	}

	public final boolean isReadOnly() {
		return readOnly;
	}

	public final void makeReadOnly() {
		readOnly = true;
	}

	public final boolean isVerifyATN() {
		return verifyATN;
	}

	public final void setVerifyATN(boolean verifyATN) {
		throwIfReadOnly();
		this.verifyATN = verifyATN;
	}

	public final boolean isGenerateRuleBypassTransitions() {
		return generateRuleBypassTransitions;
	}

	public final void setGenerateRuleBypassTransitions(boolean generateRuleBypassTransitions) {
		throwIfReadOnly();
		this.generateRuleBypassTransitions = generateRuleBypassTransitions;
	}

	protected void throwIfReadOnly() {
		if (isReadOnly()) {
			throw new IllegalStateException("The object is read only.");
		}
	}
}
