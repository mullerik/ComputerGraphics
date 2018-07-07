package edu.cg;

import edu.cg.algebra.Point;

public class TrackPoints {

	public static CyclicList<Point> track1() {
		CyclicList<Point> ans = new CyclicList<>();
		ans.add(new Point(0.7,-0.2,0.1));
		ans.add(new Point(-0.2,-0.6,0.5));
		ans.add(new Point(-0.7,-0.4,0.6));
		ans.add(new Point(-0.78,0,0.6));
		ans.add(new Point(-0.7,0.4,0.6));
		ans.add(new Point(-0.2,0.6,0.5));
		ans.add(new Point(0.7,0.2,0.1));
		return ans;
	}

	public static CyclicList<Point> track2() {
		CyclicList<Point> ans = new CyclicList<>();
		ans.add(new Point(0, 0, 0));
		ans.add(new Point(-0.5, 0, 0.5));
		ans.add(new Point(0, 0, 1));
		ans.add(new Point(0.5, 0, 0.5));
		return ans;
	}

	public static CyclicList<Point> track3() {
		CyclicList<Point> ans = new CyclicList<>();
		ans.add(new Point(-.5,-.5,0));
		ans.add(new Point(-.5,.5,.5));
		ans.add(new Point(.5,.5,0));
		ans.add(new Point(.5,-.5,.5));
		return ans;
	}

    public static CyclicList<Point> track4() {
        CyclicList<Point> ans = new CyclicList<>();
        ans.add(new Point(-0.6, 0, 0.3));
        ans.add(new Point(-0.2, 0, 0.1));
        ans.add(new Point(0.2, 0, 0.1));
        ans.add(new Point(0.6, 0, 0.3));
        ans.add(new Point(0.2, 0, 0.7));
        ans.add(new Point(-0.2 , 0, 0.7));
        return ans;
    }

	public static CyclicList<Point> track5() {
		CyclicList<Point> ans = new CyclicList<>();
		ans.add(new Point(0.4, 0, 0.1));
		ans.add(new Point(0.4, 0.4, 0.2));
		ans.add(new Point(0 , -0.4, 0.3));
		ans.add(new Point(-0.4, 0, 0.2));
		return ans;
	}
}
