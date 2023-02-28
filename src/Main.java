import examples.dot.DotLexer;
import examples.dot.DotParser;
import examples.lexer.HelloLexer;
import runtime.*;
import runtime.atn.ATNDeserializer;
import runtime.misc.Interval;
import runtime.misc.IntervalSet;
import runtime.misc.MurmurHash;
import runtime.tree.ParseTree;


// 用作 Map 的主鍵的類 ATNConfig ATNState DFAState PredictionContext
public class Main {
    public static void main(String[] args) {
//        CharStream stream = CharStreams.fromString("digraph { a -> b; c -- d; " +
//                "subgraph cluster_0 { c -> x; }" +
//                "}");
//        DotLexer lexer = new DotLexer(stream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        DotParser parser = new DotParser(tokens);
//        ParseTree ast = parser.graph_list();
//
//        String result = ast.toStringTree(parser);
//        System.out.println(result);

//        CharStream stream = CharStreams.fromString("hello  /** world */ 123456 + 1201 - _qwd91 * / ____123");
//        HelloLexer lexer = new HelloLexer(stream);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//        for(int i=0; i<tokens.getNumberOfOnChannelTokens(); i++)
//        {
//            System.out.println(tokens.get(i).toString(lexer.getVocabulary()));
//        }


        CharStream stream = CharStreams.fromString("hello world 123456 h1091 ppp UIa7 4892");
        HelloLexer lexer = new HelloLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        for(int i=0; i<tokens.getNumberOfOnChannelTokens(); i++)
        {
            System.out.println(tokens.get(i).toString(lexer.getVocabulary()));
        }


    }
}