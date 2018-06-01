package edu.cg.models;

import com.jogamp.opengl.GL2;

/**
 * A simple axes dummy 
 *
 */
public class Cube implements IRenderable {
	
	private boolean isLightSpheres;
	
	public void render(GL2 gl) {
        gl.glBegin(GL2.GL_QUADS);

        double r = 0.7;

        gl.glVertex3d(-r,-r,+r);
        gl.glVertex3d(+r,-r,+r);
        gl.glVertex3d(+r,+r,+r);
        gl.glVertex3d(-r,+r,+r);

        gl.glColor3d(0,0,0);
        gl.glVertex3d(-r,-r,-r);
        gl.glColor3d(0,0,1);
        gl.glVertex3d(-r,-r,+r);
        gl.glColor3d(0,1,1);
        gl.glVertex3d(-r,+r,+r);
        gl.glColor3d(0,1,0);
        gl.glVertex3d(-r,+r,-r);

        gl.glColor3d(1,0,1);
        gl.glVertex3d(+r,-r,+r);
        gl.glColor3d(1,0,0);
        gl.glVertex3d(+r,-r,-r);
        gl.glColor3d(1,1,0);
        gl.glVertex3d(+r,+r,-r);
        gl.glColor3d(1,1,1);
        gl.glVertex3d(+r,+r,+r);

        gl.glColor3d(1,1,0);
        gl.glVertex3d(+r,+r,-r);
        gl.glColor3d(1,0,0);
        gl.glVertex3d(+r,-r,-r);
        gl.glColor3d(0,0,0);
        gl.glVertex3d(-r,-r,-r);
        gl.glColor3d(0,1,0);
        gl.glVertex3d(-r,+r,-r);

        gl.glColor3d(0,1,1);
        gl.glVertex3d(-r,+r,+r);
        gl.glColor3d(1,1,1);
        gl.glVertex3d(+r,+r,+r);
        gl.glColor3d(1,1,0);
        gl.glVertex3d(+r,+r,-r);
        gl.glColor3d(0,1,0);
        gl.glVertex3d(-r,+r,-r);

        gl.glColor3d(0,0,0);
        gl.glVertex3d(-r,-r,-r);
        gl.glColor3d(1,0,0);
        gl.glVertex3d(+r,-r,-r);
        gl.glColor3d(1,0,1);
        gl.glVertex3d(+r,-r,+r);
        gl.glColor3d(0,0,1);
        gl.glVertex3d(-r,-r,+r);

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
