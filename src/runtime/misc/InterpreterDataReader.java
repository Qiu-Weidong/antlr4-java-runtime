

package runtime.misc;

import runtime.Vocabulary;
import runtime.VocabularyImpl;
import runtime.atn.ATN;
import runtime.atn.ATNDeserializer;
import runtime.dfa.DFA;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;


public class InterpreterDataReader {

	public static class InterpreterData {
	  ATN atn;
	  Vocabulary vocabulary;
	  List<String> ruleNames;
	  List<String> channels;
	  List<String> modes;
	};

	
	public static InterpreterData parseFile(String fileName) {
		InterpreterData result = new InterpreterData();
		result.ruleNames = new ArrayList<String>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		  	List<String> literalNames = new ArrayList<String>();
		  	List<String> symbolicNames = new ArrayList<String>();

			line = br.readLine();
			if ( !line.equals("token literal names:") )
				throw new RuntimeException("Unexpected data entry");
		    while ((line = br.readLine()) != null) {
		       if ( line.isEmpty() )
					break;
				literalNames.add(line.equals("null") ? "" : line);
		    }

			line = br.readLine();
			if ( !line.equals("token symbolic names:") )
				throw new RuntimeException("Unexpected data entry");
		    while ((line = br.readLine()) != null) {
		       if ( line.isEmpty() )
					break;
				symbolicNames.add(line.equals("null") ? "" : line);
		    }

		  	result.vocabulary = new VocabularyImpl(literalNames.toArray(new String[0]), symbolicNames.toArray(new String[0]));

			line = br.readLine();
			if ( !line.equals("rule names:") )
				throw new RuntimeException("Unexpected data entry");
		    while ((line = br.readLine()) != null) {
		       if ( line.isEmpty() )
					break;
				result.ruleNames.add(line);
		    }

			line = br.readLine();
			if ( line.equals("channel names:") ) {
				result.channels = new ArrayList<String>();
			    while ((line = br.readLine()) != null) {
			       if ( line.isEmpty() )
						break;
					result.channels.add(line);
			    }

				line = br.readLine();
				if ( !line.equals("mode names:") )
					throw new RuntimeException("Unexpected data entry");
				result.modes = new ArrayList<String>();
			    while ((line = br.readLine()) != null) {
			       if ( line.isEmpty() )
						break;
					result.modes.add(line);
			    }
			}

		  	line = br.readLine();
		  	if ( !line.equals("atn:") )
		  		throw new RuntimeException("Unexpected data entry");
			line = br.readLine();
			String[] elements = line.substring(1,line.length()-1).split(",");
	  		int[] serializedATN = new int[elements.length];

			for (int i = 0; i < elements.length; ++i) {
				serializedATN[i] = Integer.parseInt(elements[i].trim());
			}

		  	ATNDeserializer deserializer = new ATNDeserializer();
		  	result.atn = deserializer.deserialize(serializedATN);
		}
		catch (java.io.IOException e) {

		}

		return result;
	}

}
