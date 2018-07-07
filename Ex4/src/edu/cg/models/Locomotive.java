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

    // COLORS & LIGHTS
    private static final float[] BLACK = { 0, 0, 0 };
    private static final float[] WHITE = { 1, 1, 1 };
    private static final float[] CHASSIS_COLOR = { 0.86f, 0.1f, 0.1f };
    private static final float[] WHEEL_TIRE = { 0.5f, 0.2f, 0.1f };
    private static final float[] WHEEL_DISK = { 0.75f, 0.15f, 0.05f };
    private static final float[] LIGHT_OUTER_RING = { 0.3f, 0.3f, 0.3f };
    private static final float[] CHIMNEY_BOTTOM = { 0.7f, 0.15f, 0.05f };

    private static final float[] LIGHT1COLOR = { 0.1f, 0.3f, 0.1f };
    private static final float[] LIGHT2COLOR = { 0.1f, 0.1f, 0.3f };



    // OpenGL stuff
    private GL2 gl = null;
    private GLU glu = null;
    private GLUquadric quad = null;

    public void render(GL2 gl) {
        // Initiate OpenGL stuff
        this.gl = gl;
        glu = new GLU();
        quad = glu.gluNewQuadric();

        // Add lights
        addLightSources();

        // Start drawing the locomotive according to pdf order
        drawChassis();
        drawWheels();
        drawLights();
        drawRoof();
        drawChimney();

        // Disable Lights
        gl.glDisable(gl.GL_LIGHT1);
        gl.glDisable(gl.GL_LIGHT2);

    }
    // Copied drawSquare from House.java
    // Yes - it's a duplicated code :(
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

    private void setColor(float[] color) {
        gl.glColor3fv(color, 0);
    }

    private void drawSideWindow() {
//        setColor(BLACK);
        setLightningBlack();
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
//        setColor(BLACK);
        setLightningBlack();
        gl.glTranslated(-0.35f, 0.0f, 0.15f);
        gl.glRotated(90, 0, 1, 0);
        gl.glScaled(3, 1.5,1 );
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawBackWindow() {
        gl.glPushMatrix();
//        setColor(BLACK);
        gl.glTranslated(0.5f, -0.05f, -0.15f);
        gl.glRotated(-90, 0, 1, 0);
        gl.glScaled(3, 2,1 );
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawDoor(){
        gl.glPushMatrix();
//        setColor(BLACK);
        setLightningBlack();
        gl.glTranslated(0.0f, -0.1f, 0.0f); // Move down
        gl.glScaled(1.0f, 2.0f, 1.0f); // Stretch the window
        drawSideWindow();
        gl.glPopMatrix();
    }
    private void drawOnlySidePanel() {
//        setColor(CHASSIS_COLOR);
        setLightningChassis();
        // Draw pane with windows
        drawSquare(new Point(-0.35, -0.25, 0.2), new Point(0.5, -0.25, 0.2),
                new Point(0.5, 0.25, 0.2), new Point(-0.35, 0.25, 0.2));

        // Draw pane with chimney
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.35, -0.25, 0.2),
                new Point(-0.35, 0, 0.2), new Point(-0.85, 0, 0.2));

    }
    private void drawEntireSide(boolean isDoorSide) {
        if (isDoorSide)
            // Flip drawing if it's door side
            gl.glScaled(1.0f, 1.0f, -1.0f);

        // Basically draw 2 rectangles and add windows afterwards
        drawOnlySidePanel();

        gl.glPushMatrix();
        gl.glTranslated(-0.2f, -0.05f, 0.202f);
        gl.glScaled(1.5f, 2.0f, 1.0f);

        // If this side has a door, draw the window much longer
        if (isDoorSide){
            drawDoor();
        }
        // Otherwise, draw a normal window
        else
            drawSideWindow();
        // Either way - add 2 more windows
        for (int i = 0; i < 2; i++) {
            gl.glTranslated(0.15f, 0.0f, 0.0f);
            drawSideWindow();
        }
        gl.glPopMatrix();
    }

    private void drawSidePanelsWithWindows() {
        // Replace setColor with setMaterial
        // setColor(CHASSIS_COLOR);
        gl.glNormal3f(0.0F, -1.0F, 0.0F);
        // Draw side with no door
        drawEntireSide(false);

        // Draw side with door and windows facing out
        gl.glFrontFace(GL2.GL_CW);
        drawEntireSide(true);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawChassisOtherPanels() {
//        setColor(CHASSIS_COLOR);
        setLightningChassis();
        // Draw floor for chassis
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.85, -0.25, -0.2),
                new Point(0.5, -0.25, -0.2), new Point(0.5, -0.25, 0.2));

        // Draw back panel (will hold back window)
        drawSquare(new Point(0.5, -0.25, 0.2), new Point(0.5, -0.25, -0.2),
                new Point(0.5, 0.25, -0.2), new Point(0.5, 0.25, 0.2));

        // Draw headlights panel
        drawSquare(new Point(-0.85, -0.25, 0.2), new Point(-0.85, 0, 0.2),
                new Point(-0.85, 0, -0.2), new Point(-0.85, -0.25, -0.2));

        // Draw hood (chimney base)
        drawSquare(new Point(-0.85, 0, 0.2), new Point(-0.35, 0, 0.2),
                new Point(-0.35, 0, -0.2), new Point(-0.85, 0, -0.2));

        // Draw front panel (for front window)
        drawSquare(new Point(-0.35, 0, 0.2), new Point(-0.35, 0.25, 0.2),
                new Point(-0.35, 0.25, -0.2), new Point(-0.35, 0, -0.2));

    }

    private void drawChassis() {
        drawSidePanelsWithWindows();
        // We're drawing windows first to avoid layering problems (z-index)
        drawFrontWindow();
        drawBackWindow();
        setLightningChassis();
        drawChassisOtherPanels();
    }
    private void drawWheels() {
        gl.glPushMatrix();

        // Draw front wheels (move forward on the x-axis)
        gl.glTranslated(-0.55f, -0.25f, 0.2f);
        // Each wheel is rendered is only once and each pair as well!!
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
//        setColor(WHEEL_TIRE);
        setLightningTire();
        gl.glTranslated(0.0f, 0.0f, -0.05f);
        // Side wheel is how to wheel looks if you're looking from the side
        drawSideWheel();
        gl.glTranslated(0.0f, 0.0f, 0.1f);
        gl.glRotated(180.0f, 1.0f, 0.0f, 0.0f);
        drawSideWheel();
        // The outer part is only the tire
        drawWheelOuterTire();
    }
    private void drawSideWheel(){
//        setColor(WHEEL_TIRE);
        setLightningTire();
        glu.gluDisk(quad, 0.1f, 0.15f, 20, 1);
//        setColor(WHEEL_DISK);
        setLightningDisk();
        glu.gluDisk(quad, 0.0f, 0.1f, 20, 1);
    }
    private void drawWheelOuterTire(){
//        setColor(WHEEL_TIRE);
        setLightningTire();
        // Color the outside of the tire
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.15, 0.15f, 0.1f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawLights(){
        gl.glPushMatrix();
        gl.glTranslated(-0.875f, -0.125f, 0.1f);
        gl.glRotated(90.0f, 0.0f, 1.0f, 0.0f);
        // Render light once and move on the x-axis to render the same light symmetrically
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
//        setColor(WHITE);
        setLightningWhite();
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);

    }
    private void drawLightOuterRing(){
//        setColor(LIGHT_OUTER_RING);
        setLightningRing();
        // Color the outside of the tire
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.05f, 0.05f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawRoof(){
        gl.glPushMatrix();
        // Move+rotate+stretch the basic roof to fit the chassis
        // We're using epsilon to avoid strange effects of two layers one on another
        gl.glTranslated(-0.35f + Ops.epsilon, 0.25f, 0.0f);
        gl.glRotated(90.0f, 0.0f, 1.0f, 0.0f);
        gl.glScaled(4, 1, 1);
        // Draw the basic roof - only the cylinder with disks
        drawBasicRoof();
        gl.glPopMatrix();
    }
    private void drawBasicRoof(){
//        setColor(BLACK);
        setLightningBlack();
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.05f, 0.05f, 0.85f - Ops.epsilon, 20, 1);
        gl.glTranslated(0.0f, 0.0f, 0.85f- Ops.epsilon);
        glu.gluDisk(quad, 0.0f, 0.05f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }

    private void drawChimney(){
        gl.glPushMatrix();
        gl.glTranslated(-0.6f, 0.25f, 0.0f);
        gl.glRotated(90.0f, 1.0f, 0.0f, 0.0f);
        // This is the bottom part of the chimney with no disks
        drawChimneyBottom();
        gl.glTranslated(0.0f, 0.0f, -0.1f);
        // The upper part has 2 disks - one on each side
        drawChimneyUpper();
        gl.glPopMatrix();
    }
    private void drawChimneyBottom(){
//        setColor(CHIMNEY_BOTTOM);
        setLightningChimney();
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.08f, 0.08f, 0.25f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }
    private void drawChimneyUpper(){
//        setColor(CHIMNEY_BOTTOM);
        setLightningChimney();
        glu.gluDisk(quad, 0.0f, 0.1f, 20, 1);
        gl.glFrontFace(GL2.GL_CW);
        glu.gluCylinder(quad, 0.1f, 0.1f, 0.1f, 20, 1);
        gl.glTranslated(0.0f, 0.0f, 0.1f);
        glu.gluDisk(quad, 0.0f, 0.1f, 20, 1);
        gl.glFrontFace(GL2.GL_CCW);
    }

    // functions for setting lighting & materials effects
    private void addLightSources(){
        gl.glEnable(gl.GL_LIGHTING);
        setLight1();
        setLight2();
        if (this.isLightSpheres)
            addLightSourceSpheres();
    }

    private void setLight1() {
        gl.glEnable(gl.GL_LIGHT1);
        gl.glLightfv(gl.GL_LIGHT1, gl.GL_AMBIENT, WHITE, 0);
        gl.glLightfv(gl.GL_LIGHT1, gl.GL_SPECULAR, LIGHT1COLOR, 0);
        gl.glLightfv(gl.GL_LIGHT1, gl.GL_POSITION, new float[]{0.7f, 0.7f, 0.0f, 1.0f}, 0);
    }

    private void setLight2() {
        gl.glEnable(gl.GL_LIGHT2);
        gl.glLightfv(gl.GL_LIGHT2, gl.GL_DIFFUSE, LIGHT2COLOR, 0);
        gl.glLightfv(gl.GL_LIGHT2, gl.GL_EMISSION, LIGHT2COLOR, 0);
        gl.glLightfv(gl.GL_LIGHT2, gl.GL_SPECULAR, LIGHT2COLOR, 0);
        gl.glLightfv(gl.GL_LIGHT2, gl.GL_POSITION, new float[]{-0.8f, 0.2f, 0.5f, 1.0f}, 0);
    }

    private void addLightSourceSpheres(){
        // Light spheres should not be effected by lighting
        gl.glDisable(gl.GL_LIGHTING);

        // Sphere for light 1
        gl.glPushMatrix();
        gl.glTranslated(0.7f, 0.7f, 0.0f);
        gl.glColor4fv(LIGHT1COLOR, 0);
        glu.gluSphere(quad, 0.1, 10, 5);
        gl.glPopMatrix();

        // Sphere for light 2
        gl.glPushMatrix();
        gl.glTranslated(-0.8f, 0.2f, 0.5f);
        gl.glColor4fv(LIGHT2COLOR, 0);
        glu.gluSphere(quad, 0.1, 10, 5);
        gl.glPopMatrix();

        // Bring back lighting
        gl.glEnable(gl.GL_LIGHTING);
    }

    private void setLightningChassis() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, CHASSIS_COLOR, 0);
        this.setDiffuseAndSpecular();
    }
    private void setLightningBlack() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, BLACK, 0);
        this.setDiffuseAndSpecular();
    }
    private void setLightningWhite() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, WHITE, 0);
        this.setDiffuseAndSpecular();
    }

    private void setLightningTire() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, WHEEL_TIRE, 0);
        this.setDiffuseAndSpecular();
    }

    private void setLightningRing() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, LIGHT_OUTER_RING, 0);
        this.setDiffuseAndSpecular();
    }
    private void setLightningChimney() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, CHIMNEY_BOTTOM, 0);
        this.setDiffuseAndSpecular();
    }
    private void setLightningDisk() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT, WHEEL_DISK, 0);
        this.setDiffuseAndSpecular();
    }
    private void setDiffuseAndSpecular() {
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_DIFFUSE, BLACK, 0);
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, WHITE, 0);
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

