package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Triangle extends Shape {
	private Point p1, p2, p3;
	private transient Plain trianglesPlain = null;
	
	public Triangle() {
		p1 = p2 = p3 = null;
	}

	private Plain getTrianglesPlain() {
	    if(trianglesPlain != null) {
	        return trianglesPlain;
        }
        synchronized(this) {
            if(trianglesPlain == null) {
                Vec v1 = p2.sub(p1);
                Vec v2 = p2.sub(p3);
                Vec normal = v1.cross(v2).normalize();
                trianglesPlain = new Plain(normal, p2);
            }
        }
        return trianglesPlain;
    }
	
	public Triangle(Point p1, Point p2, Point p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Triangle:" + endl +
				"p1: " + p1 + endl + 
				"p2: " + p2 + endl +
				"p3: " + p3 + endl;
	}

    /**
     * Ray and triangle intersection
     * Based on http://geomalgorithms.com/a06-_intersect-2.html
     * @param ray
     * @return
     */
	@Override
	public Hit intersect(Ray ray) { // TODO: make sure it works
	    Hit plainHit = getTrianglesPlain().intersect(ray);

	    // Check that the ray intersects the plain
	    if(plainHit == null){
	        return null;
        }

        // Check that the ray is inside the triangle
        Point intersectionPoint = ray.add(plainHit.t());
	    Vec u = p2.sub(p1);
        Vec v = p3.sub(p1);
        Vec w = intersectionPoint.sub(p1);

        double k = Math.pow(u.dot(v), 2) - (u.dot(u) * v.dot(v));
        double s1 = ((u.dot(v) * w.dot(v)) - (v.dot(v) * w.dot(u))) / k;
        double t1 = ((u.dot(v) * w.dot(u)) - (u.dot(u) * w.dot(v))) / k;

        // Conditions: s1 <= 1, t1 <= 1, s1+t1 <= 1
        if(s1 <= 1 && t1 <= 1 && s1+t1 <= 1) {
            return plainHit;
        }
        return null;
	}
}
