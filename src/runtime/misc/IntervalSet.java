
package runtime.misc;

import runtime.Lexer;
import runtime.Token;
import runtime.Vocabulary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


public class IntervalSet implements IntSet {
	public static final IntervalSet COMPLETE_CHAR_SET = IntervalSet.of(Lexer.MIN_CHAR_VALUE, Lexer.MAX_CHAR_VALUE);
	static {
		COMPLETE_CHAR_SET.setReadonly(true);
	}

	public static final IntervalSet EMPTY_SET = new IntervalSet();
	static {
		EMPTY_SET.setReadonly(true);
	}

	
    protected List<Interval> intervals;

    protected boolean readonly;

	public IntervalSet(List<Interval> intervals) {
		this.intervals = intervals;
	}

	public IntervalSet(IntervalSet set) {
		this();
		addAll(set);
	}

	// 将每个整数变为 区间 并添加
	public IntervalSet(int... els) {
		if ( els==null ) {
			intervals = new ArrayList<Interval>(2);
		}
		else {
			intervals = new ArrayList<Interval>(els.length);
			for (int e : els) add(e);
		}
	}

	
	// [ a..a ]
    public static IntervalSet of(int a) {
		IntervalSet s = new IntervalSet();
        s.add(a);
        return s;
    }

    // [ a..b ]
	public static IntervalSet of(int a, int b) {
		IntervalSet s = new IntervalSet();
		s.add(a,b);
		return s;
	}

	public void clear() {
        if ( readonly ) throw new IllegalStateException("can't alter readonly IntervalSet");
		intervals.clear();
	}

    // add 是添加 el..el
    @Override
    public void add(int el) {
        if ( readonly ) throw new IllegalStateException("can't alter readonly IntervalSet");
        add(el,el);
    }

    
    public IntervalSet add(int a, int b) {
        add(Interval.of(a, b));
		return null;
	}

	// 向区间集合中添加一个区间
	protected void add(Interval addition) {
        if ( readonly ) throw new IllegalStateException("can't alter readonly IntervalSet");

		if ( addition.b<addition.a ) {
			return;
		}


		for (ListIterator<Interval> iter = intervals.listIterator(); iter.hasNext();) {
			Interval r = iter.next();
			if ( addition.equals(r) ) {
				return;
			}
			if ( addition.adjacent(r) || !addition.disjoint(r) ) {

				Interval bigger = addition.union(r);
				iter.set(bigger);


				while ( iter.hasNext() ) {
					Interval next = iter.next();
					if ( !bigger.adjacent(next) && bigger.disjoint(next) ) {
						break;
					}


					iter.remove();
					iter.previous();
					iter.set(bigger.union(next));
					iter.next();
				}
				return;
			}
			if ( addition.startsBeforeDisjoint(r) ) {

				iter.previous();
				iter.add(addition);
				return;
			}

		}


		intervals.add(addition);
	}

	// 求并集
	public static IntervalSet or(IntervalSet[] sets) {
		IntervalSet r = new IntervalSet();
		for (IntervalSet s : sets) r.addAll(s);
		return r;
	}

	// 如果参数是区间集合，则将区间全部添加，如果是整数集合，则将整数作为 单区间添加
	@Override
	public IntervalSet addAll(IntSet set) {
		if ( set==null ) {
			return this;
		}

		if (set instanceof IntervalSet other) {

			int n = other.intervals.size();
			for (int i = 0; i < n; i++) {
				Interval I = other.intervals.get(i);
				this.add(I.a,I.b);
			}
		}
		else {
			for (int value : set.toList()) {
				add(value);
			}
		}

		return this;
    }

	// 求补集
	@Override
    public IntervalSet complement(IntSet vocabulary) {
		if ( vocabulary==null || vocabulary.isNil() ) {
			return null;
		}

		IntervalSet vocabularyIS;
		if (vocabulary instanceof IntervalSet) {
			vocabularyIS = (IntervalSet)vocabulary;
		}
		else {
			vocabularyIS = new IntervalSet();
			vocabularyIS.addAll(vocabulary);
		}

		return vocabularyIS.subtract(this);
    }

	@Override
	public IntervalSet subtract(IntSet a) {
		if (a == null || a.isNil()) {
			return new IntervalSet(this);
		}

		if (a instanceof IntervalSet) {
			return subtract(this, (IntervalSet)a);
		}

		IntervalSet other = new IntervalSet();
		other.addAll(a);
		return subtract(this, other);
	}

	

	public static IntervalSet subtract(IntervalSet left, IntervalSet right) {
		if (left == null || left.isNil()) {
			return new IntervalSet();
		}

		IntervalSet result = new IntervalSet(left);
		if (right == null || right.isNil()) {

			return result;
		}

		int resultI = 0;
		int rightI = 0;
		while (resultI < result.intervals.size() && rightI < right.intervals.size()) {
			Interval resultInterval = result.intervals.get(resultI);
			Interval rightInterval = right.intervals.get(rightI);



			if (rightInterval.b < resultInterval.a) {
				rightI++;
				continue;
			}

			if (rightInterval.a > resultInterval.b) {
				resultI++;
				continue;
			}

			Interval beforeCurrent = null;
			Interval afterCurrent = null;
			if (rightInterval.a > resultInterval.a) {
				beforeCurrent = new Interval(resultInterval.a, rightInterval.a - 1);
			}

			if (rightInterval.b < resultInterval.b) {
				afterCurrent = new Interval(rightInterval.b + 1, resultInterval.b);
			}

			if (beforeCurrent != null) {
				if (afterCurrent != null) {

					result.intervals.set(resultI, beforeCurrent);
					result.intervals.add(resultI + 1, afterCurrent);
					resultI++;
					rightI++;
					continue;
				}
				else {

					result.intervals.set(resultI, beforeCurrent);
					resultI++;
					continue;
				}
			}
			else {
				if (afterCurrent != null) {

					result.intervals.set(resultI, afterCurrent);
					rightI++;
					continue;
				}
				else {

					result.intervals.remove(resultI);
					continue;
				}
			}
		}




		return result;
	}

	@Override
	public IntervalSet or(IntSet a) {
		IntervalSet o = new IntervalSet();
		o.addAll(this);
		o.addAll(a);
		return o;
	}

    
	@Override
	public IntervalSet and(IntSet other) {
		if ( other==null ) {
			return null;
		}

		List<Interval> myIntervals = this.intervals;
		List<Interval> theirIntervals = ((IntervalSet)other).intervals;
		IntervalSet intersection = null;
		int mySize = myIntervals.size();
		int theirSize = theirIntervals.size();
		int i = 0;
		int j = 0;

		while ( i<mySize && j<theirSize ) {
			Interval mine = myIntervals.get(i);
			Interval theirs = theirIntervals.get(j);

			if ( mine.startsBeforeDisjoint(theirs) ) {

				i++;
			}
			else if ( theirs.startsBeforeDisjoint(mine) ) {

				j++;
			}
			else if ( mine.properlyContains(theirs) ) {

				if ( intersection==null ) {
					intersection = new IntervalSet();
				}
				intersection.add(mine.intersection(theirs));
				j++;
			}
			else if ( theirs.properlyContains(mine) ) {

				if ( intersection==null ) {
					intersection = new IntervalSet();
				}
				intersection.add(mine.intersection(theirs));
				i++;
			}
			else if ( !mine.disjoint(theirs) ) {

				if ( intersection==null ) {
					intersection = new IntervalSet();
				}
				intersection.add(mine.intersection(theirs));







				if ( mine.startsAfterNonDisjoint(theirs) ) {
					j++;
				}
				else if ( theirs.startsAfterNonDisjoint(mine) ) {
					i++;
				}
			}
		}
		if ( intersection==null ) {
			return new IntervalSet();
		}
		return intersection;
	}

    // 判断区间是否包含某个点
    @Override
    public boolean contains(int el) {
		int n = intervals.size();
		int l = 0;
		int r = n - 1;


		while (l <= r) {
			int m = (l + r) / 2;
			Interval I = intervals.get(m);
			int a = I.a;
			int b = I.b;
			if ( b<el ) {
				l = m + 1;
			} else if ( a>el ) {
				r = m - 1;
			} else {
				return true;
			}
		}
		return false;
    }

    
    @Override
    public boolean isNil() {
        return intervals==null || intervals.isEmpty();
    }


	public int getMinElement() {
		if ( isNil() ) {
			throw new RuntimeException("set is empty");
		}

		return intervals.get(0).a;
	}


	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		for (Interval I : intervals) {
			hash = MurmurHash.update(hash, I.a);
			hash = MurmurHash.update(hash, I.b);
		}

		hash = MurmurHash.finish(hash, intervals.size() * 2);
		return hash;
	}

	
    @Override
    public boolean equals(Object obj) {
        if ( obj==null || !(obj instanceof IntervalSet) ) {
            return false;
        }
        IntervalSet other = (IntervalSet)obj;
		return this.intervals.equals(other.intervals);
	}

	@Override
	public String toString() { return toString(false); }

	public String toString(boolean elemAreChar) {
		StringBuilder buf = new StringBuilder();
		if ( this.intervals==null || this.intervals.isEmpty() ) {
			return "{}";
		}
		if ( this.size()>1 ) {
			buf.append("{");
		}
		Iterator<Interval> iter = this.intervals.iterator();
		while (iter.hasNext()) {
			Interval I = iter.next();
			int a = I.a;
			int b = I.b;
			if ( a==b ) {
				if ( a==Token.EOF ) buf.append("<EOF>");
				else if ( elemAreChar ) buf.append("'").appendCodePoint(a).append("'");
				else buf.append(a);
			}
			else {
				if ( elemAreChar ) buf.append("'").appendCodePoint(a).append("'..'").appendCodePoint(b).append("'");
				else buf.append(a).append("..").append(b);
			}
			if ( iter.hasNext() ) {
				buf.append(", ");
			}
		}
		if ( this.size()>1 ) {
			buf.append("}");
		}
		return buf.toString();
	}


	public String toString(Vocabulary vocabulary) {
		StringBuilder buf = new StringBuilder();
		if ( this.intervals==null || this.intervals.isEmpty() ) {
			return "{}";
		}
		if ( this.size()>1 ) {
			buf.append("{");
		}
		Iterator<Interval> iter = this.intervals.iterator();
		while (iter.hasNext()) {
			Interval I = iter.next();
			int a = I.a;
			int b = I.b;
			if ( a==b ) {
				buf.append(elementName(vocabulary, a));
			}
			else {
				for (int i=a; i<=b; i++) {
					if ( i>a ) buf.append(", ");
                    buf.append(elementName(vocabulary, i));
				}
			}
			if ( iter.hasNext() ) {
				buf.append(", ");
			}
		}
		if ( this.size()>1 ) {
			buf.append("}");
		}
        return buf.toString();
    }


	protected String elementName(Vocabulary vocabulary, int a) {
		if (a == Token.EOF) {
			return "<EOF>";
		}
		else if (a == Token.EPSILON) {
			return "<EPSILON>";
		}
		else {
			return vocabulary.getDisplayName(a);
		}
	}

    @Override
    public int size() {
		int n = 0;
		int numIntervals = intervals.size();
		if ( numIntervals==1 ) {
			Interval firstInterval = this.intervals.get(0);
			return firstInterval.b-firstInterval.a+1;
		}
		for (int i = 0; i < numIntervals; i++) {
			Interval I = intervals.get(i);
			n += (I.b-I.a+1);
		}
		return n;
    }

	@Override
    public List<Integer> toList() {
		List<Integer> values = new ArrayList<Integer>();
		int n = intervals.size();
		for (int i = 0; i < n; i++) {
			Interval I = intervals.get(i);
			int a = I.a;
			int b = I.b;
			for (int v=a; v<=b; v++) {
				values.add(v);
			}
		}
		return values;
	}


	@Override
	public void remove(int el) {
        if ( readonly ) throw new IllegalStateException("can't alter readonly IntervalSet");
        int n = intervals.size();
        for (int i = 0; i < n; i++) {
            Interval I = intervals.get(i);
            int a = I.a;
            int b = I.b;
            if ( el<a ) {
                break;
            }

            if ( el==a && el==b ) {
                intervals.remove(i);
                break;
            }

            if ( el==a ) {
                I.a++;
                break;
            }

            if ( el==b ) {
                I.b--;
                break;
            }

            if ( el>a && el<b ) {
                int oldb = I.b;
                I.b = el-1;
                add(el+1, oldb);
            }
        }
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        if ( this.readonly && !readonly ) throw new IllegalStateException("can't alter readonly IntervalSet");
        this.readonly = readonly;
    }
}
