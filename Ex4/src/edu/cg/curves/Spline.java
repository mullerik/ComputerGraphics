package edu.cg.curves;

import edu.cg.CyclicList;

public class Spline {
    private static final double DELTA_RIEMANN_SUM = 1.0d / 1024d;
    private static final float RAIL_LENGTH = 0.05f;
    private static final float RAIL_POSITION_TARGET_DELTA = 0.002f;
    private static final float RAIL_POSITION_DELTA_THRESHOLD = 0.0005f;
    private static final float RAIL_POSITION_SEARCH_DELTA = 0.0002f;

    private CyclicList<Polynom3d> curves;
    private CyclicList<Double> curvesEuclideanLength;
    private CyclicList<Axis> chainOfRails;
    private CyclicList<Axis> preprocessedPositions;

    public Spline(CyclicList<Polynom3d> curves) {
        this.curves = curves;
        this.curvesEuclideanLength = new CyclicList<>();
        double length = 0;

        for (Polynom3d polynom3d: curves) {
            double size = 0d;
            double offset = 0d;
            for(int i = 0; i < 1024; i++) {
                double nextOffset = offset + DELTA_RIEMANN_SUM;

                size+= polynom3d.get((float) offset).dist(polynom3d.get((float) nextOffset));
                offset = nextOffset;
            }
            curvesEuclideanLength.add(size);
            length += size;
        }
        createChainOfRails();
        preparePreprocessedPositions();
    }

    private void createChainOfRails() {
        chainOfRails = new CyclicList<>();
        for(int i = 0; i < curves.size(); i++) {
            Polynom3d curve = curves.get(i);
            for(float f = 0f; f < 1f; f+= RAIL_LENGTH) {
                chainOfRails.add(curve.getAxis(f));
            }
        }
    }

    public CyclicList<Axis> getChainOfRails() {
        return chainOfRails;
    }

    private void preparePreprocessedPositions() {
        preprocessedPositions = new CyclicList<>();
        Axis lastAxis = curves.get(0).getAxis(0f);
        preprocessedPositions.add(lastAxis);

        for(int i = 0; i < curves.size(); i++) {
            Polynom3d curve = curves.get(i);
            for(float f = 0f; f < 1f; f+= RAIL_POSITION_SEARCH_DELTA) {
                Axis currentAxis = curve.getAxis(f);
                float deltaFromLastAxis = currentAxis.getPosition().sub(lastAxis.getPosition()).length();
                if(deltaFromLastAxis > RAIL_POSITION_DELTA_THRESHOLD + RAIL_POSITION_TARGET_DELTA || Math.abs(deltaFromLastAxis - RAIL_POSITION_TARGET_DELTA) < RAIL_POSITION_DELTA_THRESHOLD) {
                    preprocessedPositions.add(currentAxis);
                    lastAxis = currentAxis;
                }
            }
        }
    }

    public Axis getTrainPosition(int i) {
        return preprocessedPositions.get(i);
    }

    public int maxPossiblePostions() {
        return preprocessedPositions.size();
    }
}
