package edu.cg.models;

import com.jogamp.opengl.*;

/**
 * A simple axes dummy
 *
 */
public class Locomotive implements IRenderable {

    private boolean isLightSpheres;

    public void render(GL2 gl) {

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
