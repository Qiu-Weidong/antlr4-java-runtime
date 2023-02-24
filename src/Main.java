import runtime.CharStream;
import runtime.CharStreams;
import runtime.IntStream;
import runtime.misc.Interval;

public class Main {
    public static void main(String[] args) {
        // String s = "\u0004\u0000\u0018\u00e6\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002";
        // System.out.println(s.length());


        CharStream stream = CharStreams.fromString("你好的，十六位的了我是打开另外的了哦对");

        String s = stream.getText(Interval.of(3, 7));
        System.out.println(s);
    }
}