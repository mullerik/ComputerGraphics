package edu.cg.models;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import javafx.scene.effect.Light;

/**
 * A simple axes dummy 
 *
 */
public class Auto implements IRenderable {
	
	private boolean isLightSpheres;
	private static final float[] BLACK = { 0, 0, 0 };
	private static final float[] CHASSIS_COLOR = { 0.8f, 0, 0.2f };
	private static final float[] DOORS_OUTLINE_COLOR = { 0, 0, 0 };
	private static final float[] WINDOWS_COLOR = { 0.1f, 0.5f, 0.1f };
	private static final float[] WHEELS_HUBCAP_COLOR = { 0, 1, 1 };
	private static final float[] WHEELS_TIRES_COLOR = { 0, 0, 0 };
	private static final float[] WHEELS_CYLINDER_COLOR = { 0, 0, 0 };
	private static final float[] LIGHTS_CYLINDER_COLOR = { 0, 1, 1 };
	private static final float[] LIGHTS_DISK_COLOR = { 0.88f, 0.89f, 0.3f };
	private static final double WIDTH = 0.3;

	private GL2 gl = null;
	private GLU glu = null;
	GLUquadric quad = null;
	
	
	public void render(GL2 gl) {
		this.gl = gl;


		// Set lights
//		int firstLightNumber = GL2.GL_LIGHT0;
//		Light[] lights = {
//				new Light(new float[] { 0.5f, 0.5f, -0.5f, 1.5f }, new float[] { 0.7f, 0.8f, 0.5f, 1 }),
//				new Light(new float[] { -1f, 0.4f, 0.5f, 1.5f }, new float[] { 0.2f, 0.7f, 0.8f, 1 }),
//				new Light(new float[] { 0, 0.3f, 1f, 0f }, new float[] {0.6f, 0.1f, 0.5f, 1 }),
//				new Light(new float[] { 0, -0.5f, -0.5f, 0f }, new float[] {0.5f, 0.5f, 0.5f, 1 })
//		};
//		for (int i = 0; i < lights.length; i++) {
//			gl.glLightfv(firstLightNumber, GL2.GL_DIFFUSE, lights[i].color, 0);
//			gl.glLightfv(firstLightNumber, GL2.GL_SPECULAR, lights[i].color, 0);
//			gl.glLightfv(firstLightNumber, GL2.GL_POSITION, lights[i].position, 0);
//			gl.glEnable(firstLightNumber);
//			firstLightNumber++;
//		}

		glu = new GLU();
		quad = glu.gluNewQuadric();

		// Draws light spheres if needed
//		if (isLightSpheres) {
//			drawLightsSpheres(lights);
//		}

		// Set global GL_SHININESS
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, new float[] { 10, 20, 30, 1.0f }, 0);

		// Draw car parts
		drawChassis();
		drawFrontLights();
		drawAllTires();
	}

	// Helper function to draw the lights.
//	private void drawLightsSpheres(Light[] lights) {
//		boolean isLight = gl.glIsEnabled(GL2.GL_LIGHTING);
//		gl.glDisable(GL2.GL_LIGHTING);
//		for (int i = 0; i < lights.length; i++) {
//			gl.glPushMatrix();
//			gl.glTranslated(lights[i].position[0], lights[i].position[1],
//					lights[i].position[2]);
//			gl.glColor4fv(lights[i].color, 0);
//			glu.gluSphere(quad, 0.03, 8, 8);
//			gl.glPopMatrix();
//		}
//
//		if (isLight) {
//			gl.glEnable(GL2.GL_LIGHTING);
//		}
//	}

	// Draw the chassis of the car.
	private void drawChassis() {
		setGLMaterialAndColor(CHASSIS_COLOR, true, true, false);

		// Draw main parts
		drawQuad(-0.8, -0.4, 0, 0, WIDTH); // engine cover
		drawQuad(-0.2, 0.3, 0.2, 0.2, WIDTH); // roof
		drawQuad(0.6, -0.9, -0.2, -0.2, WIDTH); // bottom
		drawQuad(0.4, 0.5, 0, 0, WIDTH); // trunk
		drawQuad(-0.4, -0.2, 0, 0.2, WIDTH); // // front pane
		drawQuad(0.3, 0.4, 0.2, 0, WIDTH); // rear pane
		drawQuad(-0.9, -0.8, -0.2, 0, WIDTH); // front bumper
		drawQuad(0.5, 0.6, 0, -0.2, WIDTH); // rear bumper

		// Draw Wind shields
		drawWindshields();

		// Draw sides
		drawLeftSide();
		drawRightSide(); // using drawLeftSide
	}

	private void drawWindshields() {
		setGLMaterialAndColor(WINDOWS_COLOR, true, false, false);
		drawQuad(-0.401, -0.25, 0, 0.15, 0.25);
		drawQuad(0.326, 0.41, 0.15, 0, 0.25);
	}

	// Draws the left side of the car
	private void drawLeftSide() {
		setGLMaterialAndColor(CHASSIS_COLOR, true, true, false);

		// sets the normal
		gl.glNormal3d(0, 0, 1);

		gl.glBegin(GL2.GL_QUADS);

		// top
		gl.glVertex3d(0.4, 0, WIDTH);
		gl.glVertex3d(0.3, 0.2, WIDTH);
		gl.glVertex3d(-0.2, 0.2, WIDTH);
		gl.glVertex3d(-0.4, 0, WIDTH);

		// bottom
		gl.glVertex3d(-0.9, -0.2, WIDTH);
		gl.glVertex3d(0.6, -0.2, WIDTH);
		gl.glVertex3d(0.5, 0, WIDTH);
		gl.glVertex3d(-0.8, 0, WIDTH);

		gl.glEnd();

		// Draw door details
		drawDoorOutline();
		drawLeftDoorWindows();
	}

	// Draw left door outline
	private void drawDoorOutline() {
		setGLMaterialAndColor(DOORS_OUTLINE_COLOR, true, false, false);
		int[] polygonMode = new int[1];

		gl.glGetIntegerv(GL2.GL_POLYGON_MODE, polygonMode, 0);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);

		// Front door
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3d(-0.3, -0.15, WIDTH + 0.01);
		gl.glVertex3d(-0.02, -0.15, WIDTH + 0.01);
		gl.glVertex3d(-0.02, 0.15, WIDTH + 0.01);
		gl.glVertex3d(-0.15, 0.15, WIDTH + 0.01);
		gl.glVertex3d(-0.3, 0, WIDTH + 0.01);
		gl.glEnd();

		// Back door
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3d(0.02, -0.15, WIDTH + 0.01);
		gl.glVertex3d(0.15, -0.15, WIDTH + 0.01);
		gl.glVertex3d(0.33, 0, WIDTH + 0.01);
		gl.glVertex3d(0.25, 0.15, WIDTH + 0.01);
		gl.glVertex3d(0.02, 0.15, WIDTH + 0.01);

		gl.glEnd();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, polygonMode[0]);
	}

	// Draws left door windows.
	private void drawLeftDoorWindows() {
		setGLMaterialAndColor(WINDOWS_COLOR, true, true, false);

		// Front window
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3d(-0.25, 0, WIDTH + 0.01);
		gl.glVertex3d(-0.05, 0, WIDTH + 0.01);
		gl.glVertex3d(-0.05, 0.1, WIDTH + 0.01);
		gl.glVertex3d(-0.15, 0.1, WIDTH + 0.01);
		gl.glEnd();

		// Back window
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3d(0.05, 0, WIDTH + 0.01);
		gl.glVertex3d(0.28, 0, WIDTH + 0.01);
		gl.glVertex3d(0.23, 0.1, WIDTH + 0.01);
		gl.glVertex3d(0.05, 0.1, WIDTH + 0.01);
		gl.glEnd();
	}

	// Draw right side of the car, using drawLeftSide()
	private void drawRightSide() {
		gl.glFrontFace(GL2.GL_CW);
		gl.glScaled(1, 1, -1);
		drawLeftSide();
		gl.glScaled(1, 1, -1);
		gl.glFrontFace(GL2.GL_CCW);
	}

	// Draw the front lights
	private void drawFrontLights() {
		gl.glTranslated(-0.9, -0.1, -0.5 * WIDTH);
		drawSingleFrontLight();
		gl.glTranslated(0, 0, WIDTH);
		drawSingleFrontLight();
	}

	// Draws front lights
	private void drawSingleFrontLight() {

		// Draw light cylinder case
		gl.glRotated(90, 0, 1, 0);
		setGLMaterialAndColor(LIGHTS_CYLINDER_COLOR, true, false, false);
		glu.gluCylinder(quad, 0.07, 0.07, 0.05, 50, 1);

		// Draw light disk
		setGLMaterialAndColor(LIGHTS_DISK_COLOR, true, false, true);
		gl.glRotated(180, 1, 0, 0);
		glu.gluDisk(quad, 0, 0.07, 20, 1);
		gl.glRotated(180, 1, 0, 0);
		gl.glRotated(-90, 0, 1, 0);
	}

	// Draws the tires
	private void drawAllTires() {
		gl.glPushMatrix();

		gl.glTranslated(0.3, -0.12, -0.15);

		// draw left side tires
		drawPairOfLeftTires();

		// draw right side tires
		drawPairOfRightTires();

		gl.glPopMatrix();
	}

	// Draw pair of left tires
	private void drawPairOfLeftTires() {
		gl.glPushMatrix();

		// front tire
		gl.glTranslated(0, 0, -WIDTH);
		drawSingleTire();

		// back tire
		gl.glTranslated(0.9, 0, 0);
		drawSingleTire();

		gl.glPopMatrix();
	}

	// Draw pair of right tires using drawPairOfLeftTires()
	private void drawPairOfRightTires() {
		gl.glFrontFace(GL2.GL_CW);
		gl.glScaled(1, 1, -1);
		drawPairOfLeftTires();
		gl.glScaled(1, 1, -1);
		gl.glFrontFace(GL2.GL_CCW);
	}

	// Draws one tire
	private void drawSingleTire() {

		// draw wheel cylinder
		setGLMaterialAndColor(WHEELS_CYLINDER_COLOR, true, true, true);
		gl.glTranslated(0, 0, -0.05);
		glu.gluCylinder(quad, 0.125, 0.125, 0.1, 20, 1);
		gl.glRotated(180, 1, 0, 0);

		// draw wheel hubcap
		setGLMaterialAndColor(WHEELS_HUBCAP_COLOR, true, true, true);
		glu.gluDisk(quad, 0.08, 0.125, 20, 1);

		// draw wheel tire
		setGLMaterialAndColor(WHEELS_TIRES_COLOR, true, true, true);
		glu.gluDisk(quad, 0, 0.08, 20, 1);
		gl.glRotated(-180, 1, 0, 0);
		gl.glTranslated(0, 0, 0.1);

		// draw back of wheel tire
		setGLMaterialAndColor(WHEELS_CYLINDER_COLOR, true, true, true);
		glu.gluDisk(quad, 0.08, 0.125, 20, 1);

		// draw back of wheel cylinder
		setGLMaterialAndColor(WHEELS_HUBCAP_COLOR, true, true, true);
		glu.gluDisk(quad, 0, 0.08, 20, 1);
		gl.glTranslated(0, 0, -0.05);
	}

	// Draws a quad
	private void drawQuad(double x1, double x2, double y1, double y2,
						  double width) {
		double a = x2 - x1;
		double b = y2 - y1;
		double l = 2 * width * Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		gl.glNormal3d(-2 * b * width / l, 2 * a * width / l, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3d(x1, y1, -width);
		gl.glVertex3d(x1, y1, width);
		gl.glVertex3d(x2, y2, width);
		gl.glVertex3d(x2, y2, -width);
		gl.glEnd();
	}

	// Sets GL material and color
	private void setGLMaterialAndColor(float[] color, boolean diffuse,
									   boolean specular, boolean emmision) {
		if (gl == null)
			return;
		gl.glColor3fv(color, 0);
		float[] diffuseColor = (diffuse) ? color : BLACK;
		float[] specularColor = (specular) ? color : BLACK;
		float[] emmisionColor = (emmision) ? color : BLACK;
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuseColor, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specularColor, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emmisionColor, 0);
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
