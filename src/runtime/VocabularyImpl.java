
package runtime;

import java.util.Arrays;


public class VocabularyImpl implements Vocabulary {
	private static final String[] EMPTY_NAMES = new String[0];


	public static final VocabularyImpl EMPTY_VOCABULARY = new VocabularyImpl(EMPTY_NAMES, EMPTY_NAMES, EMPTY_NAMES);


	private final String[] literalNames;

	private final String[] symbolicNames;

	private final String[] displayNames;

	private final int maxTokenType;


	public VocabularyImpl(String[] literalNames, String[] symbolicNames) {
		this(literalNames, symbolicNames, null);
	}


	public VocabularyImpl(String[] literalNames, String[] symbolicNames, String[] displayNames) {
		this.literalNames = literalNames != null ? literalNames : EMPTY_NAMES;
		this.symbolicNames = symbolicNames != null ? symbolicNames : EMPTY_NAMES;
		this.displayNames = displayNames != null ? displayNames : EMPTY_NAMES;

		this.maxTokenType =
			Math.max(this.displayNames.length,
					 Math.max(this.literalNames.length, this.symbolicNames.length)) - 1;
	}


	public static Vocabulary fromTokenNames(String[] tokenNames) {
		if (tokenNames == null || tokenNames.length == 0) {
			return EMPTY_VOCABULARY;
		}

		String[] literalNames = Arrays.copyOf(tokenNames, tokenNames.length);
		String[] symbolicNames = Arrays.copyOf(tokenNames, tokenNames.length);
		for (int i = 0; i < tokenNames.length; i++) {
			String tokenName = tokenNames[i];
			if (tokenName == null) {
				continue;
			}

			if (!tokenName.isEmpty()) {
				char firstChar = tokenName.charAt(0);
				if (firstChar == '\'') {
					symbolicNames[i] = null;
					continue;
				}
				else if (Character.isUpperCase(firstChar)) {
					literalNames[i] = null;
					continue;
				}
			}


			literalNames[i] = null;
			symbolicNames[i] = null;
		}

		return new VocabularyImpl(literalNames, symbolicNames, tokenNames);
	}

	@Override
	public int getMaxTokenType() {
		return maxTokenType;
	}

	@Override
	public String getLiteralName(int tokenType) {
		if (tokenType >= 0 && tokenType < literalNames.length) {
			return literalNames[tokenType];
		}

		return null;
	}

	@Override
	public String getSymbolicName(int tokenType) {
		if (tokenType >= 0 && tokenType < symbolicNames.length) {
			return symbolicNames[tokenType];
		}

		if (tokenType == Token.EOF) {
			return "EOF";
		}

		return null;
	}

	@Override
	public String getDisplayName(int tokenType) {
		if (tokenType >= 0 && tokenType < displayNames.length) {
			String displayName = displayNames[tokenType];
			if (displayName != null) {
				return displayName;
			}
		}

		String literalName = getLiteralName(tokenType);
		if (literalName != null) {
			return literalName;
		}

		String symbolicName = getSymbolicName(tokenType);
		if (symbolicName != null) {
			return symbolicName;
		}

		return Integer.toString(tokenType);
	}



	public String[] getLiteralNames() {
		return literalNames;
	}

	public String[] getSymbolicNames() {
		return symbolicNames;
	}

	public String[] getDisplayNames() {
		return displayNames;
	}
}
