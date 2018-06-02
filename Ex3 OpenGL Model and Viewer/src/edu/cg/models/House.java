package edu.cg.models;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

public class House implements IRenderable {

    private boolean isLightSpheres;
    private static final float[] BLACK = { 0, 0, 0 };
    private static final float[] GRASS_COLOR = { 0.1f, 0.7f, 0.2f };
    private static final float[] HOUSE_COLOR = { 0.65f, 0.6f, 0.6f };
    private static final float[] HOUSE_CORNERS_COLOR = { 0.85f, 0.85f, 0.8f };
    private static final float[] ROOF_COLOR = { 0.6f, 0.04f, 0.08f };
    private static final float[] ROOF_COLOR_2 = { 0.3f, 0.01f, 0.02f };
    private static final float[] TREE_WOOD_COLOR = {  0.5f, 0.33f, 0.18f};
    private static final float[] TREE_COLOR = {  0f, 0.75f, 0.25f};
    private static final float[][] TREES_VARIANCE = {
            {0.1f, -0.1f, 0.05f},
            {0.0f, 0.0f, -0.05f},
            {0.05f, -0.1f, 0.1f},
            {0.15f, -0.1f, 0.02f},
            {0.02f, -0.15f, 0.04f},
    };

    private static final Point GRASS_A = new Point(0,0,0);
    private static final Point GRASS_B = new Point(1,0,0);
    private static final Point GRASS_C = new Point(1,0.6,0);
    private static final Point GRASS_D = new Point(0,0.6,0);

    private static final Point[] HOUSE_COORDINATES = {
            new Point(0,0.6,0),
            new Point(0,0.2,0),
            new Point(0.2,0.2,0),
            new Point(0.2,0.1,0),
            new Point(0.4,0.1,0),
            new Point(0.4,0.2,0),
            new Point(0.6,0.2,0),
            new Point(0.6,0.6,0),
    };

    private static final Point ROOF_A = new Point(0.0,0.4,0.55);
    private static final Point ROOF_B = new Point(0.3,0.4,0.55);
    private static final Point ROOF_C = new Point(0.6,0.4,0.55);
    private static final Point ROOF_D = new Point(0.3,0.2,0.55);

    private static final Vec WALLS_VEC = new Vec(0, 0, 0.4);

    private GL2 gl = null;
    private GLU glu = null;
    private GLUquadric quad = null;

    public void render(GL2 gl) {
        this.gl = gl;
        glu = new GLU();
        quad = glu.gluNewQuadric();

        gl.glTranslated(-0.5, -0.3, 0);
        drawTrees();
        drawRoof();

        // Draw grass
        setColor(GRASS_COLOR);
        drawSquare(GRASS_A, GRASS_B, GRASS_C, GRASS_D);

        drawHouseWalls();
        gl.glEnd();
    }

    private void drawTrees() {
        gl.glTranslated(0.9,0.1,0);
        drawTree(1, 0);
        gl.glTranslated(-0.9,-0.1,0);

        gl.glTranslated(0.7,0.1,0);
        drawTree(1, 1);
        gl.glTranslated(-0.7,-0.1,0);

        gl.glTranslated(0.6,0.1,0);
        drawTree(2, 2);
        gl.glTranslated(-0.6,-0.1,0);

        gl.glTranslated(0.8,0.3,0);
        drawTree(4, 3);
        gl.glTranslated(-0.8,-0.3,0);

        gl.glTranslated(0.8,0.5,0);
        drawTree(3, 4);
        gl.glTranslated(-0.8,-0.5,0);

    }

    private void drawTree(int layers, int treeIndex) {
        setColor(TREE_WOOD_COLOR);
        glu.gluCylinder(quad, 0.02, 0.02, 0.2, 50, 1);
        for(int i = layers - 1; i >= 0; i--) {
            gl.glTranslated(0,0,0.1 + (0.05*i));

            float[] color = {
                    (float) ((TREE_COLOR[0] + TREES_VARIANCE[treeIndex][0]) * Math.pow(0.8,layers-i)),
                    (float) ((TREE_COLOR[1] + TREES_VARIANCE[treeIndex][1]) * Math.pow(0.8,layers-i)),
                    (float) ((TREE_COLOR[2] + TREES_VARIANCE[treeIndex][2]) * Math.pow(0.8,layers-i))};

            setColor(color);
            glu.gluCylinder(quad, 0.1, 0, 0.2 * Math.pow(0.85,i), 50, 1);
            gl.glTranslated(0,0,-0.1 + (-0.05*i));
        }
        gl.glRotated(180, 1, 0, 0);
        gl.glEnd();
        gl.glRotated(-180, 1, 0, 0);
    }

    private void drawRoof() {
        setColor(ROOF_COLOR);
        drawTriangle(HOUSE_COORDINATES[0].add(WALLS_VEC), ROOF_A, HOUSE_COORDINATES[1].add(WALLS_VEC));
        drawTriangle(HOUSE_COORDINATES[6].add(WALLS_VEC), ROOF_C, HOUSE_COORDINATES[7].add(WALLS_VEC));

        drawTriangle(HOUSE_COORDINATES[0].add(WALLS_VEC), ROOF_A, HOUSE_COORDINATES[1].add(WALLS_VEC));
        drawTriangle(HOUSE_COORDINATES[6].add(WALLS_VEC), ROOF_C, HOUSE_COORDINATES[7].add(WALLS_VEC));

        drawSquare(HOUSE_COORDINATES[0].add(WALLS_VEC), HOUSE_COORDINATES[7].add(WALLS_VEC), ROOF_C, ROOF_A);
        drawSquare(HOUSE_COORDINATES[1].add(WALLS_VEC), HOUSE_COORDINATES[6].add(WALLS_VEC), ROOF_C, ROOF_A);

        drawTriangle(HOUSE_COORDINATES[2].add(WALLS_VEC), HOUSE_COORDINATES[5].add(WALLS_VEC), ROOF_D);

        // Draw small triangle
        setColor(ROOF_COLOR_2);
        drawTriangle(HOUSE_COORDINATES[2].add(WALLS_VEC), ROOF_B, ROOF_D);
        drawTriangle(HOUSE_COORDINATES[5].add(WALLS_VEC), ROOF_B, ROOF_D);

        // Draw balcony
        setColor(new float[] {0.5f, 0.5f, 0.5f});
        drawSquare(HOUSE_COORDINATES[2].add(WALLS_VEC),
                HOUSE_COORDINATES[3].add(WALLS_VEC),
                HOUSE_COORDINATES[4].add(WALLS_VEC),
                HOUSE_COORDINATES[5].add(WALLS_VEC));
    }

    private void drawSquare(Point a, Point b, Point c, Point d) {
        Vec normal = (b.sub(a).cross(c.sub(a))).normalize();
        gl.glNormal3d(normal.x, normal.y, normal.z);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(a.x, a.y, a.z);
        gl.glVertex3d(b.x, b.y, b.z);
        gl.glVertex3d(c.x, c.y, c.z);
        gl.glVertex3d(d.x, d.y, d.z);
        gl.glEnd();

        // To show both sides of the square
        gl.glNormal3d(normal.x, normal.y, normal.z);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(d.x, d.y, d.z);
        gl.glVertex3d(c.x, c.y, c.z);
        gl.glVertex3d(b.x, b.y, b.z);
        gl.glVertex3d(a.x, a.y, a.z);
        gl.glEnd();
    }

    private void drawTriangle(Point a, Point b, Point c) {
        drawSquare(a,b,c,c);
    }

    private void drawHouseWalls() {
        setColor(HOUSE_COLOR);
        for(int i = 0; i < HOUSE_COORDINATES.length; i++) {
            Point a = HOUSE_COORDINATES[i];
            Point b = HOUSE_COORDINATES[(i+1) % HOUSE_COORDINATES.length];
            Point c = a.add(WALLS_VEC);
            Point d = b.add(WALLS_VEC);
            drawSquare(a, b, c, d);
        }

        // Draw corners
        setColor(HOUSE_CORNERS_COLOR);
        for(int i = 0; i < HOUSE_COORDINATES.length; i++) {
            gl.glTranslated(HOUSE_COORDINATES[i].x,HOUSE_COORDINATES[i].y,HOUSE_COORDINATES[i].z);
            glu.gluCylinder(quad, 0.01, 0.01, 0.4, 50, 1);
            gl.glRotated(180, 1, 0, 0);
            gl.glEnd();
            gl.glRotated(-180, 1, 0, 0);
            gl.glTranslated(-HOUSE_COORDINATES[i].x,-HOUSE_COORDINATES[i].y,-HOUSE_COORDINATES[i].z);
        }
    }

    private void setColor(float[] color) {
        gl.glColor3fv(color, 0);
    }


    @Override
    public String toString() {
        return "Empty";
    }


    //If your scene requires more control (like keyboard events), you can define it here.
    @Override
    public void control(int type, Object params) {
        switch (type) {
            case IRenderable.TOGGLE_LIGHT_SPHERES:
            {
                isLightSpheres = ! isLightSpheres;
                break;
            }
            default:
                System.out.println("Control type not supported: " + toString() + ", " + type);
        }
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void init(GL2 gl) {

    }

    @Override
    public void setCamera(GL2 gl) {

    }
}
