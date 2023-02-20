
package runtime.misc;


public class Interval {
	public static final int INTERVAL_POOL_MAX_VALUE = 1000;

	public static final Interval INVALID = new Interval(-1,-2);

	static final Interval[] cache = new Interval[INTERVAL_POOL_MAX_VALUE+1];

	public int a;
	public int b;

	public Interval(int a, int b) { this.a=a; this.b=b; }

	
	public static Interval of(int a, int b) {

		if ( a!=b || a<0 || a>INTERVAL_POOL_MAX_VALUE ) {
			return new Interval(a,b);
		}
		if ( cache[a]==null ) {
			cache[a] = new Interval(a,a);
		}
		return cache[a];
	}

	
	public int length() {
		if ( b<a ) return 0;
		return b-a+1;
	}

	@Override
	public boolean equals(Object o) {
		if ( o==null || !(o instanceof Interval) ) {
			return false;
		}
		Interval other = (Interval)o;
		return this.a==other.a && this.b==other.b;
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash = hash * 31 + a;
		hash = hash * 31 + b;
		return hash;
	}

	
	public boolean startsBeforeDisjoint(Interval other) {
		return this.a<other.a && this.b<other.a;
	}

	
	public boolean startsBeforeNonDisjoint(Interval other) {
		return this.a<=other.a && this.b>=other.a;
	}

	
	public boolean startsAfter(Interval other) { return this.a>other.a; }

	
	public boolean startsAfterDisjoint(Interval other) {
		return this.a>other.b;
	}

	
	public boolean startsAfterNonDisjoint(Interval other) {
		return this.a>other.a && this.a<=other.b;
	}

	
	public boolean disjoint(Interval other) {
		return startsBeforeDisjoint(other) || startsAfterDisjoint(other);
	}

	
	public boolean adjacent(Interval other) {
		return this.a == other.b+1 || this.b == other.a-1;
	}

	public boolean properlyContains(Interval other) {
		return other.a >= this.a && other.b <= this.b;
	}

	
	public Interval union(Interval other) {
		return Interval.of(Math.min(a, other.a), Math.max(b, other.b));
	}

	
	public Interval intersection(Interval other) {
		return Interval.of(Math.max(a, other.a), Math.min(b, other.b));
	}

	
	public Interval differenceNotProperlyContained(Interval other) {
		Interval diff = null;

		if ( other.startsBeforeNonDisjoint(this) ) {
			diff = Interval.of(Math.max(this.a, other.b + 1),
							   this.b);
		}


		else if ( other.startsAfterNonDisjoint(this) ) {
			diff = Interval.of(this.a, other.a - 1);
		}
		return diff;
	}

	@Override
	public String toString() {
		return a+".."+b;
	}
}
