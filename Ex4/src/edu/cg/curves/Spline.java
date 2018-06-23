package edu.cg.curves;

import edu.cg.CyclicList;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class Spline {
    private static final double DELTA_RIEMANN_SUM = 1.0d / 1024d;
    private static final float RAIL_LENGTH = 0.05f;

    private CyclicList<Polynom3d> curves;
    private CyclicList<Double> curvesEuclideanLength;
    private double length;
    private CyclicList<Axis> chainOfRails;

    public Spline(CyclicList<Polynom3d> curves) {
        this.curves = curves;
        this.curvesEuclideanLength = new CyclicList<>();
        this.length = 0;

        for (Polynom3d polynom3d: curves) {
            double size = 0d;
            double offset = 0d;
            for(int i = 0; i < 1024; i++) {
                double nextOffset = offset + DELTA_RIEMANN_SUM;

                size+= polynom3d.get((float) offset).dist(polynom3d.get((float) nextOffset));
                offset = nextOffset;
            }
            curvesEuclideanLength.add(size);
            this.length+= size;
        }
        createChainOfRails();
    }

    private void createChainOfRails() {
        chainOfRails = new CyclicList<>();
        for(int i = 0; i < curves.size(); i++) {
            Polynom3d curve = curves.get(i);
            for(float f = 0f; f < 1f; f+= RAIL_LENGTH) {
                Point position = curve.get(f);
                Vec forward = curve.getdt(f).toVec().normalize();
                Vec right = curve.getdtdt(f).toVec().cross(forward).normalize();
                Vec up = forward.cross(right).normalize();
                chainOfRails.add(new Axis(position, forward, up, right));
            }
        }



    }

    public CyclicList<Axis> getChainOfRails() {
        return chainOfRails;
    }
}
