package edu.cg.models;

import com.jogamp.opengl.*;

/**
 * A simple axes dummy 
 *
 */
public class Empty implements IRenderable {	
	
	private boolean isLightSpheres;
	
	public void render(GL2 gl) {
		gl.glBegin(GL2.GL_QUADS);
		// bottom cube
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);

		// left corner
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, 0.5f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);

		// back corner
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);


		// right corner
		gl.glColor3f(1.0f, 0.0f,1.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glColor3f(1.0f, 0.0f,1.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glColor3f(1.0f, 0.0f,1.0f);
		gl.glVertex3f(-0.5f, 0.5f, -0.5f);
		gl.glColor3f(1.0f, 0.0f,1.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);


		// upper corner
		gl.glColor3f(0.0f, 0.0f,1.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glColor3f(0.0f, 0.0f,1.0f);
		gl.glVertex3f(0.5f, -0.5f, 0.5f);
		gl.glColor3f(0.0f, 0.0f,1.0f);
		gl.glVertex3f(-0.5f, -0.5f, 0.5f);
		gl.glColor3f(0.0f, 0.0f,1.0f);
		gl.glVertex3f(-0.5f, 0.5f, 0.5f);


		//front corner
		gl.glColor3f(0.0f, 1.0f,1.0f);
		gl.glVertex3f(0.5f, 0.5f, 0.5f);
		gl.glColor3f(0.0f, 1.0f,1.0f);
		gl.glVertex3f(0.5f, 0.5f, -0.5f);
		gl.glColor3f(0.0f, 1.0f,1.0f);
		gl.glVertex3f(0.5f, -0.5f, -0.5f);
		gl.glColor3f(0.0f, 1.0f,1.0f);
		gl.glVertex3f(0.5f, -0.5f, 0.5f);

		gl.glEnd();
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
