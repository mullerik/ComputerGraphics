package edu.cg.curves;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class Polynom3d {
    float[] x;
    float[] y;
    float[] z;

    public Polynom3d(float[] x, float[] y, float[] z) {
        assert(x.length == 4);
        assert(y.length == 4);
        assert(z.length == 4);

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point get(float t) {
        // t^2, t^3
        double t2 = t * t;
        double t3 = t2 * t;
        return new Point(
                x[0] * t3 + x[1] * t2 + x[2] * t + x[3],
                y[0] * t3 + y[1] * t2 + y[2] * t + y[3],
                z[0] * t3 + z[1] * t2 + z[2] * t + z[3]
        );
    }

    public Point getdt(float t) {
        // t^2
        double t2 = t * t;
        return new Point(
                3f * x[0] * t2 + 2f * x[1] * t + x[2],
                3f * y[0] * t2 + 2f * y[1] * t + y[2],
                3f * z[0] * t2 + 2f * z[1] * t + z[2]
        );
    }

    public Point getdtdt(float t) {
        return new Point(
                6f * x[0] * t + 2f * x[1],
                6f * y[0] * t + 2f * y[1],
                6f * z[0] * t + 2f * z[1]
        );
    }

    public Axis getAxis(float distance) {
        Point position = get(distance);
        Vec forward = getdt(distance).toVec().normalize();
        Vec right = getdtdt(distance).toVec().cross(forward).normalize();
        Vec up = forward.cross(right).normalize();
        return new Axis(position, forward, up, right);
    }
}
