package edu.cg.models;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;

/**
 * A simple axes dummy
 *
 */
public class Locomotive implements IRenderable {

    private boolean isLightSpheres;

    private static final float[] BLACK = { 0, 0, 0 };
    private static final float[] WHITE = { 1, 1, 1 };
    private static final float[] CHASSIS_COLOR = { 0.86f, 0.1f, 0.1f };
    private static final float[] WHEEL_TIRE = { 0.5f, 0.2f, 0.1f };
    private static final float[] WHEEL_DISK = { 0.75f, 0.15f, 0.05f };
    private static final float[] LIGHT_OUTER_RING = { 0.3f, 0.3f, 0.3f };

    private GL2 gl = null;
    private GLU glu = null;
    private GLUquadric quad = null;

    public void render(GL2 gl) {
        this.gl = gl;
        glu = new GLU();
        quad = glu.gluNewQuadric();

        drawChassis();
        drawWheels();
        drawLights();
        drawRoof();

    }

    private void setColor(float[] color) {
        gl.glColor3fv(color, 0);
    }

    private void drawSideWindow() {
        setColor(BLACK);
        gl.glNormal3d(0.0f, 0.0f, 1.0f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(0.0f, 0.0f, 0.0f);
        gl.glVertex3d(0.1f, 0.0f, 0.0f);
        gl.glVertex3d(0.1f, 0.1f, 0.0f);
        gl.glVertex3d(0.0f, 0.1f, 0.0f);
        gl.glEnd();
    }
    private void drawFrontWindow() {
        gl.glPushMatrix();
        setColor(BLACK);
        gl.glTranslated(-0.35f, 0.0f, 0.15f);
        gl.glRotated(90, 0, 1, 0);
        gl.glScaled(3, 1.5,1 );
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawBackWindow() {
        gl.glPushMatrix();
        setColor(BLACK);
        gl.glTranslated(0.5f, -0.05f, -0.15f);
        gl.glRotated(-90, 0, 1, 0);
        gl.glScaled(3, 2,1 );
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawDoor(){
        gl.glPushMatrix();
        setColor(BLACK);
        gl.glTranslated(0.0f, -0.1f, 0.0f); // Move down
        gl.glScaled(1.0f, 2.0f, 1.0f); // Stretch the window
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawOnlySidePanel() {
        setColor(CHASSIS_COLOR);
        // Draw pane with windows
        drawSquare(new Point(-0.35, -0.25, 0.2), new Point(0.5, -0.25, 0.2),
                new Point(0.5, 0.25, 0.2), new Point(-0.35, 0.25, 0.2));

        // Draw pane with chimney
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.35, -0.25, 0.2),
                new Point(-0.35, 0, 0.2), new Point(-0.85, 0, 0.2));

    }
    private void drawEntireSide(boolean haveDoor) {
        if (haveDoor)
            gl.glScaled(1.0f, 1.0f, -1.0f);

        drawOnlySidePanel();

        gl.glPushMatrix();
        gl.glTranslated(-0.2f, -0.05f, 0.202f);
        gl.glScaled(1.5f, 2.0f, 1.0f);

        // If this side has a door, draw the window much longer
        if (haveDoor){
            drawDoor();
        }
        else
            drawSideWindow();
        for (int i = 0; i < 2; i++) {
            gl.glTranslated(0.15f, 0.0f, 0.0f);
            drawSideWindow();
        }
        gl.glPopMatrix();
    }
    private void drawSidePanelsWithWindows() {
        setColor(CHASSIS_COLOR);
        gl.glNormal3f(0.0F, -1.0F, 0.0F);
        // Draw side with no door
        drawEntireSide(false);

        // Draw side with door and windows facing out
        gl.glFrontFace(GL2.GL_CW);
        drawEntireSide(true);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawFrontBackChassisPanels() {
        setColor(CHASSIS_COLOR);
        // Draw floor for chassis
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.85, -0.25, -0.2),
                new Point(0.5, -0.25, -0.2), new Point(0.5, -0.25, 0.2));

        // Draw back panel
        drawSquare(new Point(0.5, -0.25, 0.2), new Point(0.5, -0.25, -0.2),
                new Point(0.5, 0.25, -0.2), new Point(0.5, 0.25, 0.2));

        // Draw headlights panel
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.85, 0, 0.2),
                new Point(-0.85, 0, -0.2), new Point(-0.85, -0.25, -0.2));

        // Draw hood (chimney base)
        drawSquare(new Point(-0.85, 0, 0.2), new Point(-0.35, 0, 0.2),
                new Point(-0.35, 0, -0.2), new Point(-0.85, 0, -0.2));

        // Draw front panel (window base)
        drawSquare(new Point(-0.35, 0, 0.2), new Point(-0.35, 0.25, 0.2),
                new Point(-0.35, 0.25, -0.2), new Point(-0.35, 0, -0.2));

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


    private void drawChassis() {
        drawSidePanelsWithWindows();
        drawFrontWindow();
        drawBackWindow();
        drawFrontBackChassisPanels();
    }
    private void drawWheels() {
        gl.glPushMatrix();

        // Draw front wheels (move forward on the x-axis)
        gl.glTranslated(-0.55f, -0.25f, 0.2f);
        drawPairOfWheels();

        // Draw back wheels (move backwards on the x-axis)
        gl.glTranslated(0.75f, 0.0f, 0.0f);
        drawPairOfWheels();
        gl.glPopMatrix();
    }

    private void drawPairOfWheels() {
        gl.glPushMatrix();
        drawWheel();
        gl.glTranslated(0.0f, 0.0f, 0.45f);
        drawWheel();
        gl.glTranslated(0.0f, 0.0f, -0.2f);
        // Return to beginning
        gl.glPopMatrix();
    }

    private void drawWheel() {
        setColor(WHEEL_TIRE);
        gl.glTranslated(0.0f, 0.0f, -0.05f);
        drawSideWheel();
        gl.glTranslated(0.0f, 0.0f, 0.1f);
        gl.glRotated(180.0f, 1.0f, 0.0f, 0.0f);
        drawSideWheel();
        drawWheelOuterTire();
    }
    private void drawSideWheel(){
        setColor(WHEEL_TIRE);
        glu.gluDisk(quad, 0.1f, 0.15f, 20, 1);
        setColor(WHEEL_DISK);
        glu.gluDisk(quad, 0.0f, 0.1f, 20, 1);
    }
    private void drawWheelOuterTire(){
        setColor(WHEEL_TIRE);
        // Color the outside of the tire
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.15, 0.15f, 0.1f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawLights(){
        gl.glPushMatrix();
        gl.glTranslated(-0.875f, -0.125f, 0.1f);
        gl.glRotated(90.0f, 0.0f, 1.0f, 0.0f);
        drawLight();
        gl.glTranslated(0.2f, 0.0f, 0.0f);
        drawLight();
        gl.glPopMatrix();
    }
    private void drawLight(){
        gl.glPushMatrix();
        drawLightOuterRing();
        drawLightInnerDisk();
        gl.glPopMatrix();
    }
    private void drawLightInnerDisk(){
        setColor(WHITE);
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);

    }
    private void drawLightOuterRing(){
        setColor(LIGHT_OUTER_RING);
        // Color the outside of the tire
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.05f, 0.05f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawRoof(){
        gl.glPushMatrix();
        // Move+rotate+stretch the basic roof to fit hte chassis
        // We're using epsilon to avoid strange effects of two layers one on another
        gl.glTranslated(-0.35f + Ops.epsilon, 0.25f, 0.0f);
        gl.glRotated(90.0f, 0.0f, 1.0f, 0.0f);
        gl.glScaled(4, 1, 1);
        // Draw the basic roof
        drawBasicRoof();
        gl.glPopMatrix();
    }
    private void drawBasicRoof(){
        setColor(BLACK);
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.05f, 0.05f, 0.85f - Ops.epsilon, 20, 1);
        gl.glTranslated(0.0f, 0.0f, 0.85f- Ops.epsilon);
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }


    @Override
    public String toString() {
        return "Locomotive";
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

