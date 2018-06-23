package edu.cg.curves;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class Axis {
    private Point position;
    private Vec forward;
    private Vec up;
    private Vec right;

    public Axis(Point position, Vec forward, Vec up, Vec right) {
        this.position = position;
        this.forward = forward;
        this.up = up;
        this.right = right;
    }

    public Point getPosition() {
        return position;
    }

    public Vec getForward() {
        return forward;
    }

    public Vec getUp() {
        return up;
    }

    public Vec getRight() {
        return right;
    }
}
