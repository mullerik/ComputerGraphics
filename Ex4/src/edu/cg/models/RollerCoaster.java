package edu.cg.models;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;

public class RollerCoaster implements IRenderable{

    Texture grassTexture = null;


    @Override
    public void init(GL2 gl) {

        // Load the grass texture
        try {
            File grassFile = new File("textures" + File.separator + "grass.jpg");
            grassTexture = TextureIO.newTexture(grassFile, true);
            gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
        } catch (Exception ex) {
            System.err.println("Error: RollerCoaster init failed " + ex.getMessage());
        }
    }

    @Override
    public void render(GL2 gl) {
        renderGrass(gl);

    }

    // Render the grass texture
    private void renderGrass(GL2 gl) {
        gl.glEnable(GL2.GL_TEXTURE_2D);

        // Bind grass texture.
        grassTexture.bind(gl);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        gl.glBegin(GL2.GL_QUADS);

        // Draw by vertices
        gl.glTexCoord3f(0, 2, 0);
        gl.glVertex3f(-4, -4, -0.05f);
        gl.glTexCoord3f(4, 0, 0);
        gl.glVertex3f(4, -4, -0.05f);
        gl.glTexCoord3f(4, 4, 0);
        gl.glVertex3f(4, 4, -0.05f);
        gl.glTexCoord3f(0, 4, 0);
        gl.glVertex3f(-4, 4, -0.05f);

        gl.glEnd();
        gl.glDisable(GL2.GL_TEXTURE_2D);
    }

    @Override
    public void control(int type, Object params) {

    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void setCamera(GL2 gl) {

    }
}
