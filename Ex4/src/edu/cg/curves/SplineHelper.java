package edu.cg.curves;

import Jama.Matrix;
import edu.cg.CyclicList;
import edu.cg.algebra.Point;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SplineHelper {

    public static Spline createSpline(CyclicList<Point> trackPoints) {
        CyclicList<Polynom3d> curves = new CyclicList<>();

        // Find constraints for x, y, z
        List<Constraint> xConstraints  = IntStream.range(0, trackPoints.size()).boxed()
                .flatMap(index ->
                        Constraint.createConstraints(trackPoints.get(index).x, index, trackPoints.size()).stream())
                .collect(Collectors.toList());
        List<Constraint> yConstraints  = IntStream.range(0, trackPoints.size()).boxed()
                .flatMap(index ->
                        Constraint.createConstraints(trackPoints.get(index).y, index, trackPoints.size()).stream())
                .collect(Collectors.toList());
        List<Constraint> zConstraints  = IntStream.range(0, trackPoints.size()).boxed()
                .flatMap(index ->
                        Constraint.createConstraints(trackPoints.get(index).z, index, trackPoints.size()).stream())
                .collect(Collectors.toList());


        double[] xSolved = solveConstraints(xConstraints);
        double[] ySolved = solveConstraints(yConstraints);
        double[] zSolved = solveConstraints(zConstraints);

        for(int i = 0; i < xSolved.length; i+= 4) {
            Polynom3d polynom3d = new Polynom3d(
                    copyArray(xSolved, i, i + 4),
                    copyArray(ySolved, i, i + 4),
                    copyArray(zSolved, i, i + 4));
            curves.add(polynom3d);
        }
        return new Spline(curves);
    }

    private static float[] copyArray(double[] arr, int from, int toExclusive) {
        float[] result = new float[toExclusive - from];
        for(int i = from; i < toExclusive; i++) {
            result[i - from] = (float) arr[i];
        }
        return result;
    }

    private static double[] solveConstraints(List<Constraint> constraints) {
        double[] result = new double[constraints.size()];
        double[][] constraintTable = new double[constraints.size()][0];
        double[] bArr = new double[constraints.size()];

        for(int i = 0; i < constraints.size(); i++) {
            constraintTable[i] = constraints.get(i).values;
            bArr[i] = constraints.get(i).b;
        }

        Matrix matrixLines = new Matrix(constraintTable);
        Matrix b = new Matrix(bArr, bArr.length);

        for (int i = 0; i < constraintTable.length; i++) {
            for (int j = 0; j < constraintTable[i].length; j++) {
                System.out.print(constraintTable[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        for(int i = 0; i < bArr.length; i++) {
            System.out.println(bArr[i]);
        }

        Matrix solution = matrixLines.solve(b);

        for(int i = 0; i < constraints.size(); i++) {
            result[i] = solution.get(i, 0);
        }
        return result;
    }
}
