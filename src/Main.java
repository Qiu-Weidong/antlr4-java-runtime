import runtime.CharStream;
import runtime.CharStreams;
import runtime.misc.Interval;
import runtime.misc.IntervalSet;

public class Main {
    public static void main(String[] args) {
        // 闭区间
        Interval range = Interval.of(0, 5);
        IntervalSet set1 = IntervalSet.of(0, 5);
        set1.add(8, 10);

        IntervalSet set2 = IntervalSet.of(8);
        set2.add(7, 9);

        System.out.println(set1);
        System.out.println(set2);

        IntervalSet set3 = set1.complement(set2);
        System.out.println(set3);

    }
}