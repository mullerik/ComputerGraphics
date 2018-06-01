package edu.cg;

import java.awt.Component;
import java.awt.Point;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;

import edu.cg.algebra.Ops;
import edu.cg.algebra.Vec;
import edu.cg.models.IRenderable;

/**
 * An OpenGL model viewer 
 *
 */
public class Viewer implements GLEventListener {

	private double zoom = 0.0; //How much to zoom in? >0 mean magnify, <0 means shrink
	private Point mouseFrom, mouseTo; //From where to where was the mouse dragged between the last redraws?
	private boolean isWireframe = false; //Should we display wireframe or not?
	private boolean isAxes = true; //Should we display axes or not?
	private IRenderable model; //Model to display
	private FPSAnimator ani; //This object is responsible to redraw the model with a constant FPS
	private Component glPanel; //We store the OpenGL panel component object to refresh the scene
	private boolean isModelCamera = false; //Whether the camera is relative to the model, rather than the world (ex6)
	private boolean isModelInitialized = false; //Whether model.init() was called.
	
	// Store the width and height of the canvas:\
	private int width = 0;
	private int height = 0;

	// Rotation transformation matrix - set to identity
	private final double[] rotationMatrix = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};


    public Viewer(Component glPanel) {
		this.glPanel = glPanel;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		if (!isModelInitialized) {
			initModel(gl);
			isModelInitialized = true;
		}
		//TODO: uncomment the following line to clear the window before drawing
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

		setupCamera(gl);
		if (isAxes)
			renderAxes(gl);
		
		//TODO: set wireframe mode

		model.render(gl);
		
		// Polygon mode needs to be set back to GL_FILL to work around a bug on some platforms.
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}

	private void setupCamera(GL2 gl) {
		if (!isModelCamera) { //Camera is in an absolute location

			gl.glLoadIdentity();
			setTrackball(gl);

            // Zoom
            gl.glLoadIdentity();
            gl.glTranslated(0, 0, -2 - zoom);
            gl.glMultMatrixd(rotationMatrix, 0);

			//By this point, we should have already changed the point of view, now set these to null
			//so we don't change it again on the next redraw.
			mouseFrom = null;
			mouseTo = null;
		} else { //Camera is relative to the model
			gl.glLoadIdentity();
			model.setCamera(gl);
		}
	}

	private void setTrackball(GL2 gl) {
        if(mouseFrom != null && mouseTo != null) {
            Vec mouseOrigin = mouseCordinatesToSphereVec(mouseFrom).normalize();
            Vec mouseDestination = mouseCordinatesToSphereVec(mouseTo).normalize();

            Vec axis = mouseOrigin.cross(mouseDestination).normalize();
            if (axis.length() != 0 && axis.isFinite()) {

                // Calculate rotation angle
                double angle = Math.acos(mouseOrigin.dot(mouseDestination));
                angle = angle * (180 / Math.PI); // converts from radians to degrees
                angle = (angle >= 0 && angle < Ops.infinity) ? angle : 0;

                // Calculate rotation
                gl.glRotated(angle, axis.x, axis.y, axis.z);
            }
        }

        gl.glMultMatrixd(rotationMatrix, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, rotationMatrix, 0);


	}

	// TODO: remove this method (used for debugging)
    private String getMatStr() {
        java.lang.StringBuilder builder = new StringBuilder();
	    builder.append("[");
        for(int i  = 0; i < rotationMatrix.length; i++){

            if(i > 0 && i % 4 == 0) {
                builder.append(" | ");
            }
            builder.append(rotationMatrix[i]).append(",");
            if(Double.isNaN(rotationMatrix[i])) {
                System.out.println(i);
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private Vec mouseCordinatesToSphereVec(Point p) {
		double x = 2 * (double)p.x / width - 1;
		double y = 1 - (double)(2 * p.y) / height;
		double z = 2 - x * x - y * y;
		if (z >= 0) {
			z = Math.sqrt(z);
		} else {
			z = 0;
		}
		Vec vec = new Vec(x, y, z);

		try{
			vec.normalize();
		} catch (ArithmeticException e) {}

		return vec;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Typically there's nothing to do here
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Initialize display callback timer
		//TODO on hw4 Uncomment the following lines to create an animator object and attach it to the canvas.
//		ani = new FPSAnimator(30, true);
//		ani.add(drawable);
		
		glPanel.repaint();
		
		initModel(gl);
	}
	
	public void initModel(GL2 gl) {
		//TODO: on hw4 light model, normal normalization, depth test, back face culling, ...
		
		//gl.glCullFace(GL2.GL_BACK);    // Set Culling Face To Back Face
        //gl.glEnable(GL2.GL_CULL_FACE); // Enable back face culling
		
		model.init(gl);
		
		if (model.isAnimated()) //Start animation (for ex6)
			startAnimation();
		else
			stopAnimation();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.width = width;
		this.height = height;

		GL2 gl = drawable.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        double ratio = (double) height / width;
        gl.glFrustum(-0.15, 0.15, ratio * (-0.15), ratio * 0.15, 0.15, 1000);
	}

	/**
	 * Stores the mouse coordinates for trackball rotation.
	 * 
	 * @param from
	 *            2D canvas point of drag beginning
	 * @param to
	 *            2D canvas point of drag ending
	 */
	public void storeTrackball(Point from, Point to) {
		//The following lines store the rotation for use when next displaying the model.
		//After you redraw the model, you should set these variables back to null.
		if (!isModelCamera) {
			if (null == mouseFrom)
				mouseFrom = from;
			mouseTo = to;
			glPanel.repaint();
		}
	}

	/**
	 * Zoom in or out of object. s<0 - zoom out. s>0 zoom in.
	 * 
	 * @param s
	 *            Scalar
	 */
	public void zoom(double s) {
		if (!isModelCamera) {
			zoom += s*0.1;
			glPanel.repaint();
		}
	}

	/**
	 * Toggle rendering method. Either wireframes (lines) or fully shaded
	 */
	public void toggleRenderMode() {
		isWireframe = !isWireframe;
		glPanel.repaint();
	}
	
	/**
	 * Toggle whether little spheres are shown at the location of the light sources (ex6).
	 */
	public void toggleLightSpheres() {
		model.control(IRenderable.TOGGLE_LIGHT_SPHERES, null);
		glPanel.repaint();
	}

	/**
	 * Toggle whether axes are shown.
	 */
	public void toggleAxes() {
		isAxes = !isAxes;
		glPanel.repaint();
	}
	
	public void toggleModelCamera() {
		isModelCamera =! isModelCamera;
		glPanel.repaint();
	}
	
	/**
	 * Start redrawing the scene with 30 FPS
	 */
	public void startAnimation() {
		//TODO Uncomment these lines to animate the model 
		//if (!ani.isAnimating())
		//	ani.start();
	}
	
	/**
	 * Stop redrawing the scene with 30 FPS
	 */
	public void stopAnimation() {
		//TODO Uncomment these lines to stop animating the model 
		//if (ani.isAnimating())
		//	ani.stop();
	}
	
	private void renderAxes(GL2 gl) {
		gl.glLineWidth(2);
		boolean flag = gl.glIsEnabled(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3d(1, 0, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(10, 0, 0);
		
		gl.glColor3d(0, 1, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 10, 0);
		
		gl.glColor3d(0, 0, 1);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 0, 10);
		
		gl.glEnd();
		if(flag)
			gl.glEnable(GL2.GL_LIGHTING);
	}

	public void setModel(IRenderable model) {
		this.model = model;
		isModelInitialized = false;
	}
	
}
