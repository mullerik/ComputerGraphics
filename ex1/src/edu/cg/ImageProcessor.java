package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {

	//MARK: Fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;
	
	//MARK: Constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage,
			RGBWeights rgbWeights, int outWidth, int outHeight) {
		super(); //Initializing for each loops...
		
		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}
	
	public ImageProcessor(Logger logger,
			BufferedImage workingImage,
			RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights,
				workingImage.getWidth(), workingImage.getHeight());
	}
	
	//MARK: Change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Preparing for hue changing...");
		
		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;
		
		BufferedImage ans = newEmptyInputSizedImage();
		
		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r*c.getRed() / max;
			int green = g*c.getGreen() / max;
			int blue = b*c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});
		
		logger.log("Changing hue done!");
		
		return ans;
	}

    /**
     * Calculate greyscale for the working image.
     * @return
     */
	public BufferedImage grayscale() {
		logger.log("Preparing for grayscale...");
		BufferedImage ans = grayscale(workingImage, inHeight, inWidth);
		logger.log("Image grayscale done!");
		return ans;
	}

    /**
     * Calculate greyscale for a given image
     * @param inputImage image
     * @param height image height
     * @param width image width
     * @return
     */
    BufferedImage grayscale(BufferedImage inputImage, int height, int width) {

	    // Calculate sumWeights
	    int r = rgbWeights.redWeight;
        int g = rgbWeights.greenWeight;
        int b = rgbWeights.blueWeight;
        int sumWeights = (r + g + b);

        BufferedImage ans = newEmptyImage(width, height);

        // Iterate all pixels change to the correct gray level
        for(int y = 0 ; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color c = new Color(inputImage.getRGB(x, y));
				int grayColor = (r * c.getRed() + g * c.getGreen() + b * c.getBlue()) / sumWeights;
				Color color = new Color(grayColor, grayColor, grayColor);
				ans.setRGB(x, y, color.getRGB());
			}
		}
        return ans;
    }

    /**
     * Calculate the gradient magnitude for the working image
     * @return An image that represents the gradient magnitude at each pixel
     */
	public BufferedImage gradientMagnitude() {
        // If the image dimensions are too small, throw an appropriate exception"
        if(inWidth < 3 || inHeight < 3) {
            throw new RuntimeException("Can not apply gradientMagnitude: Image is too small");
        }

		logger.log("Preparing for gradientMagnitude...");
        BufferedImage ans = newEmptyInputSizedImage();
        BufferedImage grayscaled = grayscale();

		forEach((y, x) -> {

			// We can use green since in grayscale red=green=blue.Also avoid creation of `Color` object for performance.
			int weight = grayscaled.getRGB(x, y) & 0xFF;
			int weightNextHorizontal = (x != inWidth - 1) ?
					(grayscaled.getRGB(x + 1, y) & 0xFF) :
					(grayscaled.getRGB(x - 1, y) & 0xFF);
			int weightNextVertical = (y != inHeight - 1) ?
					(grayscaled.getRGB(x, y + 1) & 0xFF) :
					(grayscaled.getRGB(x, y - 1) & 0xFF);
			int dx = weightNextHorizontal - weight;
			int dy = weightNextVertical - weight;

			// Calculate the gradient magnitude using the formula
			int gradientMagnitude = (int) Math.sqrt((dx * dx + dy * dy)/2);
			Color color = new Color(gradientMagnitude, gradientMagnitude, gradientMagnitude);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("GradientMagnitude done!");
		return ans;
	}

    /**
     * Resize the working image using Nearest Neighbor algorithm
     * @return The resized image
     */
	public BufferedImage nearestNeighbor() {
        logger.log("Preparing for nearestNeighbor...");
        BufferedImage ans = newEmptyOutputSizedImage();

        // Calculate the resizing ratio
        double widthRatio = ((double) inWidth) / ((double) outWidth);
        double heightRatio = ((double) inHeight) / ((double) outHeight);

        setForEachOutputParameters();
        forEach((y, x) -> {
            int nearestY = (int) Math.round(y * heightRatio);
            int nearestX = (int) Math.round(x * widthRatio);
            ans.setRGB(x, y, workingImage.getRGB(nearestX, nearestY));
        });

        logger.log("NearestNeighbor done!");
        return ans;
	}

    /**
     * Apply the Bilinear algorithm to resize the working image
     * @return The resized image
     */
	public BufferedImage bilinear() {
		logger.log("Preparing for bilinear...");
		BufferedImage ans = newEmptyOutputSizedImage();
		double widthRatio = ((double) inWidth) / ((double) outWidth);
		double heightRatio = ((double) inHeight) / ((double) outHeight);

		setForEachOutputParameters();
		forEach((y, x) -> {

			// Find coordinates of the 4 pixels around the new point
			// Variable names match the way pixels were named in the presentation
			int v12 = (int) (x * widthRatio);
			int v22 = Math.min(v12 + 1, inWidth - 1);
			int v11 = (int) (y * heightRatio);
			int v21 = Math.min(v11 + 1, inHeight - 1);

			// Find distance "t" for x-axis
			double tx = (double) v22 - (x * widthRatio);

			// Calculate linear interpolation twice (upper and lower bounds)
			Color clr1 = new Color(workingImage.getRGB(v12, v11));
			Color clr2 = new Color(workingImage.getRGB(v22, v11));
			Color v2 = ImageProcessor.linearInterpolation(clr1, clr2, tx);

			clr1 = new Color(workingImage.getRGB(v12, v21));
			clr2 = new Color(workingImage.getRGB(v22, v21));
			Color v1 = ImageProcessor.linearInterpolation(clr1, clr2, tx);

			// Find distance "t" for y-axis
			double ty = (double) v21 - (y * heightRatio);

			// Calculate linear interpolation with the new points
			ans.setRGB(x, y, ImageProcessor.linearInterpolation(v2, v1, ty).getRGB());
		});

		logger.log("Bilinear done!");
		return ans;
	}

    /**
     * Calculate the new color to be used as linear interpolation. This function calculates for two colors.
     * @param clr1
     * @param clr2
     * @param t
     * @return The interpolation color of two colors
     */
	private static Color linearInterpolation(Color clr1, Color clr2, double t) {
		double r1 = clr1.getRed();
		double g1 = clr1.getGreen();
		double b1 = clr1.getBlue();
		double r2 = clr2.getRed();
		double g2 = clr2.getGreen();
		double b2 = clr2.getBlue();
		return new Color(ImageProcessor.weightedAvgByDistance(r1, r2, t),
				ImageProcessor.weightedAvgByDistance(g1, g2, t),
				ImageProcessor.weightedAvgByDistance(b1, b2, t));

	}

    /**
     * Calculate the weighted average distance
     * @param n1
     * @param n2
     * @param t
     * @return The weighted average of the two values
     */
	private static int weightedAvgByDistance(double n1, double n2, double t) {
		int avg = (int) ((1 - t) * n2 + t * n1);
		return Math.min(Math.max(avg, 0), 255);
	}
	
	//MARK: Utilities
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}
	
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}
	
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}
	
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		
		forEach((y, x) -> 
			output.setRGB(x, y, workingImage.getRGB(x, y))
		);
		
		return output;
	}
}
