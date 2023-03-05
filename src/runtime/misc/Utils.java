

package runtime.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {

    public static <T> String join(Iterator<T> iter, String separator) {
        StringBuilder buf = new StringBuilder();
        while ( iter.hasNext() ) {
            buf.append(iter.next());
            if ( iter.hasNext() ) {
                buf.append(separator);
            }
        }
        return buf.toString();
    }

	public static String escapeWhitespace(String s, boolean escapeSpaces) {
		StringBuilder buf = new StringBuilder();
		for (char c : s.toCharArray()) {
			if ( c==' ' && escapeSpaces ) buf.append('\u00B7');
			else if ( c=='\t' ) buf.append("\\t");
			else if ( c=='\n' ) buf.append("\\n");
			else if ( c=='\r' ) buf.append("\\r");
			else buf.append(c);
		}
		return buf.toString();
	}


	public static Map<String, Integer> toMap(String[] keys) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (int i=0; i<keys.length; i++) {
			m.put(keys[i], i);
		}
		return m;
	}


}
