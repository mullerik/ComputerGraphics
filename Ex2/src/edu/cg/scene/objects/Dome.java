package edu.cg.scene.objects;

import edu.cg.algebra.*;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + 
				sphere + plain + endl;
	}

	/**
	 * Intersect ray with dome by splitting to cases.
	 * @param ray
	 * @return
	 */
    @Override
    public Hit intersect(Ray ray) {
        Hit hitPlain;
        Hit hitSphere = this.sphere.intersect(ray);
        // Continue if you miss sphere
        if (hitSphere == null)
            return null;

        Point hitPoint = ray.getHittingPoint(hitSphere);
        // Check if Hit is coming from within
        if (hitSphere.isWithinTheSurface()){
            // Avoid problems with inaccuracy of floating point representation
            if (this.plain.subsForP(ray.source()) > Ops.epsilon) {
                if (this.plain.subsForP(hitPoint) > 0.0)
                    return hitSphere;

                // Continue if you miss plain
                if ((hitPlain = this.plain.intersect(ray)) == null)
                    return null;

                return hitPlain.setWithin();
            }
            if (this.plain.subsForP(hitPoint) > 0.0)
                return this.plain.intersect(ray);
        }

        // Hit is from outside
        else {
            if (this.plain.subsForP(hitPoint) > 0.0)
                return hitSphere;

            // Continue if you miss plain
            if ((hitPlain = this.plain.intersect(ray)) == null)
                return null;

            if (this.sphere.subsForP(ray.getHittingPoint(hitPlain)) < 0.0)
                return hitPlain;

        }
        // Either way - fallback to null if necessary
        return null;
    }

}
