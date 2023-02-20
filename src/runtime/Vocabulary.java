
package runtime;


public interface Vocabulary {

	int getMaxTokenType();


	String getLiteralName(int tokenType);


	String getSymbolicName(int tokenType);


	String getDisplayName(int tokenType);
}
