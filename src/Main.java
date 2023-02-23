import runtime.CharStream;
import runtime.CharStreams;
import runtime.misc.Interval;
import runtime.misc.IntervalSet;

public class Main {
    public static void main(String[] args) {
        // 闭区间
        IntervalSet total = IntervalSet.of(0, 255);

        IntervalSet set1 = IntervalSet.of(128);
        set1.setReadonly(false);
        set1.add(0, 8);
        set1.add(8, 10);
        set1.add(23, 45);
        set1.add(90, 127);
        set1.add(78, 119);
        set1.add(145, 197);

        IntervalSet set2 = IntervalSet.of(120);
        set2.add(8, 100);
        set2.add(7,12);
        set2.add(9, 34);
        set2.add(129, 213);

        System.out.println(set1);
        System.out.println(set2);
        System.out.println(set1.and(set2));
        System.out.println(set1.or(set2));
        System.out.println(set1.subtract(set2));
        System.out.println(set1.complement(set2));


    }
}