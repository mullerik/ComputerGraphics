package edu.cg.curves;


import java.util.ArrayList;
import java.util.List;

class Constraint {

    /** consider 3 points: [p1]----[p2]-----[p3]
     *  Constraints when iterating p2:
     *  1 - curve ab must reach point p2
     *  2 - curve ab must have tangent f' on point p2
     *  3 - curve bc must have tangent f' on point p2
     *  4 - curve bc must reach point p2
     */
    private static final double[] c1 = {0d, 0d, 0d, 0d,/**/ 0d, 0d, 0d, 1d};
    private static final double[] c2 = {1d, 1d, 1d, 1d,/**/ 0d, 0d, 0d, -1d};
    private static final double[] c3 = {3d, 2d, 1d, 0d,/**/ 0d, 0d, -1d, 0d};
    private static final double[] c4 = {3d, 1d, 0d, 0d,/**/ 0d, -1d, 0d, 0d};

    public double b;
    public double[] values;

    private Constraint(int length, int index, double[] constants, double b) {

        // Assign values to implement the constraint
        values = new double[length * 4];
        for(int i = index * 4; i < index * 4 + 8; i++) {
            values[i % values.length] = constants[i - index * 4];
        }
        this.b = b;
    }

    public static List<Constraint> createConstraints(double point, int i, int length) {
        List<Constraint> list = new ArrayList<>(4);
        list.add(new Constraint(length, i, c1, point));
        list.add(new Constraint(length, i, c2, 0d));
        list.add(new Constraint(length, i, c3, 0d));
        list.add(new Constraint(length, i, c4, 0d));
        return list;
    }
}