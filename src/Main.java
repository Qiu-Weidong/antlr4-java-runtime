import examples.DotLexer;
import examples.DotParser;
import runtime.CharStream;
import runtime.CharStreams;
import runtime.CommonTokenStream;
import runtime.misc.Interval;
import runtime.misc.IntervalSet;
import runtime.tree.ParseTree;

// 用作 Map 的主鍵的類 ATNConfig ATNState DFAState PredictionContext
public class Main {
    public static void main(String[] args) {
        CharStream stream = CharStreams.fromString("digraph { a -> b; c -- d; " +
                "subgraph cluster_0 { c -> x; }" +
                "}");
        DotLexer lexer = new DotLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        DotParser parser = new DotParser(tokens);
        ParseTree ast = parser.graph_list();

        String result = ast.toStringTree(parser);
        System.out.println(result);

    }
}