

package runtime;

import runtime.atn.ATN;
import runtime.atn.ATNType;
import runtime.atn.LexerATNSimulator;
import runtime.atn.PredictionContextCache;
import runtime.dfa.DFA;

import java.util.Collection;

public class LexerInterpreter extends Lexer {
	protected final String grammarFileName;
	protected final ATN atn;

	protected final String[] ruleNames;
	protected final String[] channelNames;
	protected final String[] modeNames;


	private final Vocabulary vocabulary;

	protected final DFA[] _decisionToDFA;
	protected final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();

	public LexerInterpreter(String grammarFileName, Vocabulary vocabulary, Collection<String> ruleNames, Collection<String> channelNames, Collection<String> modeNames, ATN atn, CharStream input) {
		super(input);

		if (atn.grammarType != ATNType.LEXER) {
			throw new IllegalArgumentException("The ATN must be a lexer ATN.");
		}

		this.grammarFileName = grammarFileName;
		this.atn = atn;


		this.ruleNames = ruleNames.toArray(new String[0]);
		this.channelNames = channelNames.toArray(new String[0]);
		this.modeNames = modeNames.toArray(new String[0]);
		this.vocabulary = vocabulary;

		this._decisionToDFA = new DFA[atn.getNumberOfDecisions()];
		for (int i = 0; i < _decisionToDFA.length; i++) {
			_decisionToDFA[i] = new DFA(atn.getDecisionState(i), i);
		}
		this._interp = new LexerATNSimulator(this,atn,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public ATN getATN() {
		return atn;
	}

	@Override
	public String getGrammarFileName() {
		return grammarFileName;
	}

    @Override
	public String[] getRuleNames() {
		return ruleNames;
	}

	@Override
	public String[] getChannelNames() {
		return channelNames;
	}

	@Override
	public String[] getModeNames() {
		return modeNames;
	}

	@Override
	public Vocabulary getVocabulary() {
		if (vocabulary != null) {
			return vocabulary;
		}

		return super.getVocabulary();
	}
}
