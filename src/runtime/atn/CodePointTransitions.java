

package runtime.atn;


public abstract class CodePointTransitions {
	
	public static Transition createWithCodePoint(ATNState target, int codePoint) {
		return createWithCodePointRange(target, codePoint, codePoint);
	}

	
	public static Transition createWithCodePointRange(ATNState target, int codePointFrom, int codePointTo) {
		return codePointFrom == codePointTo
				? new AtomTransition(target, codePointFrom)
				: new RangeTransition(target, codePointFrom, codePointTo);
	}
}
