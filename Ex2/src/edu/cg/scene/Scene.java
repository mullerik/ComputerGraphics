package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.algebra.*;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;
	
	private Point camera = new Point(0, 0, 5);
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();
	
	
	//MARK: initializers
	public Scene initCamera(Point camera) {
		this.camera = camera;
		return this;
	}
	
	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}
	
	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}
	
	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}
	
	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}
	
	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}
	
	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}
	
	public Scene initName(String name) {
		this.name = name;
		return this;
	}
	
	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}
	
	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}
	
	//MARK: getters
	public String getName() {
		return name;
	}
	
	public int getFactor() {
		return antiAliasingFactor;
	}
	
	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}
	
	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}
	
	public boolean getRenderReflections() {
		return renderReflections;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator(); 
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}
	
	private static class IndexTransformer {
		private final int max;
		private final int deltaX;
		private final int deltaY;
		
		IndexTransformer(int width, int height) {
			max = Math.max(width, height);
			deltaX = (max - width) / 2;
			deltaY = (max - height) / 2;
		}
		
		Point transform(int x, int y) {
			double xPos = (2*(x + deltaX) - max) / ((double)max);
			double yPos = (max - 2*(y + deltaY)) / ((double)max);
			return new Point(xPos, yPos, 0);
		}
	}
	
	private transient IndexTransformer transformaer = null;
	private transient ExecutorService executor = null;
	private transient Logger logger = null;
	
	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		//TODO: initialize your additional field here.
	}
	
	
	public BufferedImage render(int imgWidth, int imgHeight, Logger logger)
			throws InterruptedException, ExecutionException {
		
		initSomeFields(imgWidth, imgHeight, logger);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		transformaer = new IndexTransformer(imgWidth, imgHeight);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);
		
		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);
		
		this.logger.log("Starting to shoot " +
			(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
			" rays over " + name);
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);
		
		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");
		
		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}
		
		executor.shutdown();
		
		this.logger.log("Ray tracing of " + name + " has been completed.");
		
		executor = null;
		transformaer = null;
		this.logger = null;
		
		return img;
	}
	
	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			//TODO: change this method implementation to implement super sampling

			// If antialiasing factor is 1, there's no need to continue further
			if (this.getFactor() == 1){
				Point pointOnScreenPlain = transformaer.transform(x, y);
				Ray ray = new Ray(camera, pointOnScreenPlain);
				return calcColor(ray, 0).toColor();
			}

			// Find boundaries of the pixel
			Point cornerLeft = this.transformaer.transform(x, y);
			Point cornerRight = this.transformaer.transform(x + 1, y + 1);

			// Define a new result vector to hold the average
			Vec result = new Vec();

			// To divide for each "new pixel"
			double newPixelWeight = 1.0 / (double)this.antiAliasingFactor;
			// To divide the overall result and get the color average
			double allPixelWeight = 1.0 / Math.pow(this.antiAliasingFactor, 2);

			// Iterate over the new rays shooting through the "smaller pixels"
			for (int i = 0; i < this.antiAliasingFactor; i++) {
				for (int j = 0; j < this.antiAliasingFactor; j++) {
					// Calculate new coordinates
					double xCoords = this.antiAliasingFactor - j;
					double yCoords = this.antiAliasingFactor - i;
					// New left up corner weight
					Point cornerLeftWeight = new Point(xCoords, yCoords, 0.0);
					cornerLeftWeight = cornerLeftWeight.mult(newPixelWeight);

					// New right down corner weight (coordinates are already fine)
					Point cornerRightWeight = new Point(j, i, 0.0);
					cornerRightWeight = cornerRightWeight.mult(newPixelWeight);

					// Find the middle of both corners (of the new smaller "pixel")
					// Multiple the left upper corner with the calculated weight,
					// do the same for the lower right corner and add them together
					Point pointOnScreenPlain = cornerLeft.mult(cornerLeftWeight).add(cornerRight.mult(cornerRightWeight));

					// Define a new ray according to the pointOnScreenPlain
					Ray ray = new Ray(this.camera, pointOnScreenPlain);

					// Add the calculated color of the give ray to the result
					result = result.add(this.calcColor(ray, 0));
				}
			}

			// Return the average color according to antialiasing factor
			return result.mult(allPixelWeight).toColor();
		});
	}
	
	private Vec calcColor(Ray ray, int recusionLevel) {
		// Stop from continuing to infinite loop
		if (this.maxRecursionLevel <= recusionLevel) return new Vec();

		Surface closestSurface = null;
        double minT = Double.MAX_VALUE;
        Hit bestHit = null;
        for(Surface surface: surfaces) {
            Hit hit = surface.intersect(ray);
            if(hit != null && hit.t() < minT) {
                closestSurface = surface;
                minT = hit.t();
                bestHit = hit;
            }
        }

        if(closestSurface == null) {
            return backgroundColor;
        }

        Vec result = closestSurface.Ka().mult(ambient);//.mult(new Vec(surfaces.indexOf(closestSurface)));

		Point point = ray.add(bestHit.t());
        for(Light light: lightSources) {
            if(isShadowed(point, bestHit.getNormalToSurface(), surfaces, light)) {
                continue;
            }

            // From slide 58
            double cosAngleBetweenNornalAndLight = light.calculateCosAngleBetweenNormalAndLight(bestHit.getNormalToSurface(), point);
            boolean isLightRelevant = cosAngleBetweenNornalAndLight > 0;
            if(isLightRelevant) {
                Vec intensityForPoint = (light.intensityForPoint(point));

                // diffuse
                Vec Kd = bestHit.getSurface().Kd(point);
                Vec diffuse = Kd.mult(cosAngleBetweenNornalAndLight).mult(intensityForPoint);

                // From slide 52
                Vec Ks = bestHit.getSurface().Ks();
                Vec R = light.getReflectedFromSurface(bestHit.getNormalToSurface(), point);
                Vec V = ray.direction().neg();
                double vDotR = V.normalize().dot(R.normalize());
                double vDotRPowerN = vDotR > 0 ? Math.pow(vDotR, bestHit.getSurface().shininess()) : 0;
                Vec specular = Ks.mult(vDotRPowerN).mult(intensityForPoint);

                result = result.add(diffuse).add(specular);
            }
        }

        // Add reflections if specified
		if (this.getRenderReflections()) {
        	// Create a new ray with the point & direction
			Vec reflection = Ops.reflect(ray.direction(), bestHit.getNormalToSurface());
			Ray newRay = new Ray(point, reflection);

			// Calculate intensity for reflection
			Vec refIntensity = closestSurface.Ks().mult(closestSurface.reflectionIntensity());

			// Calculate the reflective color recursively and add it to result
			Vec tmpResult = this.calcColor(newRay, recusionLevel + 1).mult(refIntensity);

			// Add tmp result to our result
			result = result.add(tmpResult);
		}

		// Add refractions if specified and surface is transparent
		if (this.getRenderRefarctions() && closestSurface.isTransparent()) {
			double n1 = closestSurface.n1(bestHit);
			double n2 = closestSurface.n2(bestHit);

			// Create a new ray with the point & direction
			Vec refraction = Ops.refract(ray.direction(), bestHit.getNormalToSurface(), n1, n2);
			Ray newRay = new Ray(point, refraction);

			// Calculate intensity for refraction
			Vec refIntensity = closestSurface.Kt().mult(closestSurface.refractionIntensity());

			// Calculate the refraction color recursively and add it to result
			Vec tmpResult = this.calcColor(newRay, recusionLevel + 1).mult(refIntensity);

			// Add tmp result to our result
			result = result.add(tmpResult);
			}

		// Return final color
		return result;
	}

    /**
     * Check if there are surfaces between a point and a light
     * @param point
     * @param normalToPointsSurface
     * @param surfaces
     * @param light
     * @return
     */
    private static boolean isShadowed(Point point, Vec normalToPointsSurface, List<Surface> surfaces, Light light) {
        Point rayOrigin = point.add(normalToPointsSurface.mult(Ops.epsilonVec));
        Vec rayDirection = light.fromPointToLightNormalized(rayOrigin);
        Ray ray = new Ray(rayOrigin, rayDirection);
        // Find if there is a surface between the point to the light

        double distanceToLight = light.distanceToLight(rayOrigin);
        for(Surface surface: surfaces) {
            Hit hit = surface.intersect(ray);
            if(hit != null && hit.t() < distanceToLight) {
                return true;
            }
        }
        return false;
    }
}
