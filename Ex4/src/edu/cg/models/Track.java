package edu.cg.models;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import edu.cg.CyclicList;
import edu.cg.TrackPoints;
import edu.cg.algebra.Point;
import edu.cg.curves.Axis;
import edu.cg.curves.Spline;
import edu.cg.curves.SplineHelper;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class Track implements IRenderable {
    private IRenderable vehicle;
    private CyclicList<Point> trackPoints;
    private Texture texGrass = null;
    private Texture texTrack = null;
    private Spline spline;
    private int trainPositionInSpline = 1;
    private int speed = 300; // Steps / sec
    private long lastTrainUpdateTime;

    public Track(IRenderable vehicle, CyclicList<Point> trackPoints) {
        this.vehicle = vehicle;
        this.trackPoints = trackPoints;
        lastTrainUpdateTime = System.currentTimeMillis();
    }

    public Track(IRenderable vehicle) {
        this(vehicle, TrackPoints.track1());
    }

    public Track() { }

    @Override
    public void init(GL2 gl) {

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

        // Update train's position according to the speed
        long time = System.currentTimeMillis();
        long timeElapsed = time - lastTrainUpdateTime;
        int steps = (int) (timeElapsed * speed / 1000);
        if(steps != 0) {
            trainPositionInSpline = (trainPositionInSpline + steps);
            trainPositionInSpline %= spline.maxPossiblePostions();
            lastTrainUpdateTime = time;
        }

        gl.glPushMatrix();

        Axis trainAxis = spline.getTrainPosition(trainPositionInSpline * -1);
        MoveGlToAxis(gl, trainAxis);

        gl.glScaled(.15, .15, .15);
        gl.glTranslated(0,.35,0);

        vehicle.render(gl);
        gl.glPopMatrix();
    }

    private static void MoveGlToAxis(GL2 gl, Axis axis) {
        gl.glTranslatef(axis.getPosition().x, axis.getPosition().y, axis.getPosition().z);

        // Create the rotation matrix as we learned in class
        double[] rotationMatrix = new double[]{
                (double) axis.getForward().x, (double) axis.getForward().y, (double) axis.getForward().z, 0d,
                (double) axis.getUp().x, (double) axis.getUp().y, (double) axis.getUp().z, 0d,
                (double) axis.getRight().x, (double) axis.getRight().y, (double) axis.getRight().z, 0d,
                0d, 0d, 0d, 1.0d};
        gl.glMultMatrixd(rotationMatrix, 0);
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

            // 4 Points that represent each rail texture
            Point a = curr.getPosition().add(curr.getRight().mult(-0.05f));
            Point b = curr.getPosition().add(curr.getRight().mult(0.05f));
            Point c = next.getPosition().add(next.getRight().mult(0.05f));
            Point d = next.getPosition().add(next.getRight().mult(-0.05f));
            drawRail(gl, a, b, c, d);
        }
        gl.glEnd();

        if(lightningEnabled)
            gl.glEnable(GL2.GL_LIGHTING);

        gl.glDisable(GL2.GL_BLEND);
        gl.glDisable(GL2.GL_TEXTURE_2D);
    }

    // Draw with triangles
    private void drawRail(GL2 gl, Point a, Point b, Point c, Point d) {
        final Point[] pointsToDraw = {
                b, c, d,
                b, d, a,
                b, d, c,
                b, a, d
        };
        final float[] textureXCoords = {
                0f, 1f, 0f,
                1f, 1f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f
        };
        final float[] textureYCoords = {
                1f, 1f, 0f,
                1f, 0f, 0f,
                1f, 1f, 0f,
                0f, 1f, 0f
        };

        for(int i=0; i < 12; i++) {
            gl.glVertex3f(pointsToDraw[i].x, pointsToDraw[i].y, pointsToDraw[i].z);
            gl.glTexCoord2d(textureXCoords[i], textureYCoords[i]);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void control(int type, Object params) {
        switch(type) {
            case KeyEvent.VK_UP:
                speed+= 50;
                break;

            case KeyEvent.VK_DOWN:
                speed-= 50;
                break;

            case KeyEvent.VK_ENTER:
                try {
                    Method m = TrackPoints.class.getMethod("track" + params);
                    trackPoints = (CyclicList<Point>)m.invoke(null);
                    spline = SplineHelper.createSpline(trackPoints);
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
    // Set camera to the train's location.
    public void setCamera(GL2 gl) {
        GLU glu = new GLU();
        Axis trainAxis = spline.getTrainPosition(trainPositionInSpline * -1);
        Point cameraLocation = trainAxis.getPosition()
                .add(trainAxis.getUp().mult(0.2f))
                .add(trainAxis.getForward().mult(0.3f))
                .add(trainAxis.getRight().mult(0.14f));
        Point cameraZVector = cameraLocation.add(trainAxis.getForward().mult(-1));
        glu.gluLookAt(cameraLocation.x, cameraLocation.y, cameraLocation.z,
                cameraZVector.x, cameraZVector.y, cameraZVector.z,
                trainAxis.getUp().x, trainAxis.getUp().y, trainAxis.getUp().z);
    }

    @Override
    public void destroy(GL2 gl) {
        texGrass.destroy(gl);
        texTrack.destroy(gl);
        vehicle.destroy(gl);
    }

}
