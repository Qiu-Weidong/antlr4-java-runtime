

package runtime;

import runtime.misc.Interval;


public interface CharStream extends IntStream {

	public String getText(Interval interval);
}
