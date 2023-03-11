

package runtime.misc;

import java.util.concurrent.CancellationException;


public class ParseCancellationException extends CancellationException {

	public ParseCancellationException(Throwable cause) {
		initCause(cause);
	}

}
