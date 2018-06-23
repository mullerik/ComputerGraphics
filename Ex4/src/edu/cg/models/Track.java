package edu.cg.models;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import edu.cg.CyclicList;
import edu.cg.algebra.Vec;
import edu.cg.curves.Axis;
import edu.cg.curves.SplineHelper;
import edu.cg.TrackPoints;
import edu.cg.algebra.Point;
import edu.cg.curves.Spline;

public class Track implements IRenderable {
	private IRenderable vehicle;
	private CyclicList<Point> trackPoints;
	private Texture texGrass = null;
	private Texture texTrack = null;
	private Spline spline;

	public Track(IRenderable vehicle, CyclicList<Point> trackPoints) {
		this.vehicle = vehicle;
		this.trackPoints = trackPoints;
	}
	
	public Track(IRenderable vehicle) {
		this(vehicle, TrackPoints.track1());
	}
	
	public Track() {
		//TODO: uncomment this and change it if for your needs.
//		this(new Locomotive());
	}

	@Override
	public void init(GL2 gl) {
		//TODO: Build your track splines here.
		//Compute the length of each spline.
		//Do not repeat those calculations over and over in the render method.
		//It will make the application to run not smooth.

		spline = SplineHelper.createSpline(trackPoints);

		loadTextures(gl);
		vehicle.init(gl);
	}
	
	private void loadTextures(GL2 gl) {
		File fileGrass = new File("grass.jpg");
		File fileRoad = new File("track.png");
		try {
			texTrack = TextureIO.newTexture(fileRoad, true);
			texGrass = TextureIO.newTexture(fileGrass, false);
		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(GL2 gl) {
		renderVehicle(gl);
		renderField(gl);
		renderTrack(gl);
	}

	private void renderVehicle(GL2 gl) {
		gl.glPushMatrix();
		
		//TODO: implement vehicle translations and rotations here...
		
		gl.glScaled(.15, .15, .15);
		gl.glTranslated(0,.35,0);
		
		vehicle.render(gl);
		gl.glPopMatrix();
	}

	private void renderField(GL2 gl) {
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texGrass.getTextureObject());
		
		boolean lightningEnabled;
		if((lightningEnabled = gl.glIsEnabled(GL2.GL_LIGHTING)))
			gl.glDisable(GL2.GL_LIGHTING);

		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LOD, 1);
		
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(-1.2, -1.2, -.02);
		gl.glTexCoord2d(4, 0);
		gl.glVertex3d(1.2, -1.2, -.02);
		gl.glTexCoord2d(4, 4);
		gl.glVertex3d(1.2, 1.2, -.02);
		gl.glTexCoord2d(0, 4);
		gl.glVertex3d(-1.2, 1.2, -.02);
		
		gl.glEnd();
		
		if(lightningEnabled)
			gl.glEnable(GL2.GL_LIGHTING);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}

	private void renderTrack(GL2 gl) {
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texTrack.getTextureObject());
		
		boolean lightningEnabled;
		if((lightningEnabled = gl.glIsEnabled(GL2.GL_LIGHTING)))
			gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LOD, 2);

        gl.glBegin(GL2.GL_TRIANGLES);
        for(int i = 0; i < spline.getChainOfRails().size(); i++) {
            Axis curr = spline.getChainOfRails().get(i);
            Axis next = spline.getChainOfRails().get(i + 1);
            drawTrackPiece(gl, curr.getRight(), curr.getPosition(), next.getRight(), next.getPosition());
        }
        gl.glEnd();
		
		if(lightningEnabled)
			gl.glEnable(GL2.GL_LIGHTING);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}

    // TODO TODO  TODO: copied:
    private void drawTrackPiece(GL2 gl, Vec l0Right, Point l0Pos, Vec l1Right, Point l1Pos) {
        Vec r0 = l0Right;
        Vec r1 = l1Right;
        Point p0 = l0Pos.add(r0.mult(0.05F));
        Point p1 = l1Pos.add(r1.mult(0.05F));
        Point p2 = l1Pos.add(r1.mult(-0.05F));
        Point p3 = l0Pos.add(r0.mult(-0.05F));
        gl.glTexCoord2d(0.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p0));
        gl.glTexCoord2d(0.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p1));
        gl.glTexCoord2d(1.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p2));
        gl.glTexCoord2d(0.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p0));
        gl.glTexCoord2d(1.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p2));
        gl.glTexCoord2d(0.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p1));
        gl.glTexCoord2d(0.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p0));
        gl.glTexCoord2d(1.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p2));
        gl.glTexCoord2d(1.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p3));
        gl.glTexCoord2d(0.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p0));
        gl.glTexCoord2d(1.0D, 0.0D);
        gl.glVertex3fv(toGlFloat(p3));
        gl.glTexCoord2d(1.0D, 1.0D);
        gl.glVertex3fv(toGlFloat(p2));
    }
    // TODO TODO TODO: copied!
    private static FloatBuffer toGlFloat(Point p) {
        return FloatBuffer.wrap(new float[] {p.x, p.y, p.z});
    }


	@SuppressWarnings("unchecked")
	@Override
	public void control(int type, Object params) {
		switch(type) {
		case KeyEvent.VK_UP:
			//TODO: increase the locomotive velocity
			break;
			
		case KeyEvent.VK_DOWN:
			//TODO: decrease the locomotive velocity
			break;
			
		case KeyEvent.VK_ENTER:
			try {
				Method m = TrackPoints.class.getMethod("track" + params);
				trackPoints = (CyclicList<Point>)m.invoke(null);
				//TODO: replace the track with the new one...
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
			
			
		case IRenderable.TOGGLE_LIGHT_SPHERES:
			vehicle.control(type, params);
			break;
			
		default:
			System.out.println("Unsupported operation for Track control");
		}
	}

	@Override
	public boolean isAnimated() {
		return true;
	}

	@Override
	public void setCamera(GL2 gl) {
		//You should use:
//		GLU glu = new GLU();
//		glu.gluLookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z);
		//TODO: set the camera here to follow the locomotive...
	}
	
	@Override
	public void destroy(GL2 gl) {	
		texGrass.destroy(gl);
		texTrack.destroy(gl);
		vehicle.destroy(gl);
	}

}
