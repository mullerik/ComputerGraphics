package edu.cg.curves;

import edu.cg.CyclicList;
import edu.cg.algebra.Point;

public class Spline {
    private static final double DELTA = 1.0d / 1024d;
    private CyclicList<Polynom3d> curves;
    private CyclicList<Double> curvesEuclideanLength;
    private double length;

    public Spline(CyclicList<Polynom3d> curves) {
        this.curves = curves;
        this.curvesEuclideanLength = new CyclicList<>();
        this.length = 0;

        for (Polynom3d polynom3d: curves) {
            double size = 0d;
            double offset = 0d;
            for(int i = 0; i < 1024; i++) {
                double nextOffset = offset + DELTA;

                size+= polynom3d.get((float) offset).dist(polynom3d.get((float) nextOffset));
                offset = nextOffset;
            }
            curvesEuclideanLength.add(size);
            this.length+= size;
        }
    }

}
