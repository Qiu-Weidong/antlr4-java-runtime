
package runtime;

import runtime.misc.Utils;

import java.io.IOException;


@Deprecated
public class ANTLRFileStream extends ANTLRInputStream {
	protected String fileName;

	public ANTLRFileStream(String fileName) throws IOException {
		this(fileName, null);
	}

	public ANTLRFileStream(String fileName, String encoding) throws IOException {
		this.fileName = fileName;
		load(fileName, encoding);
	}

	public void load(String fileName, String encoding)
		throws IOException
	{
		data = Utils.readFile(fileName, encoding);
		this.n = data.length;
	}

	@Override
	public String getSourceName() {
		return fileName;
	}
}
