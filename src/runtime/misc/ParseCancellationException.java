

package runtime.misc;

import runtime.BailErrorStrategy;
import runtime.RecognitionException;

import java.util.concurrent.CancellationException;


public class ParseCancellationException extends CancellationException {

	public ParseCancellationException() {
	}

	public ParseCancellationException(String message) {
		super(message);
	}

	public ParseCancellationException(Throwable cause) {
		initCause(cause);
	}

	public ParseCancellationException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

}
