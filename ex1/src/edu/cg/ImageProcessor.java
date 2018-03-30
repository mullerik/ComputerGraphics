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
	
	
	//MARK: Unimplemented methods
	public BufferedImage grayscale() {
		logger.log("Preparing for grayscale...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int sumWeights = (r + g + b);

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int grayColor = (r*c.getRed() + g*c.getGreen() + b*c.getBlue()) / sumWeights;
			Color color = new Color(grayColor, grayColor, grayColor);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Image grayscale done!");

		return ans;
	}

	public BufferedImage gradientMagnitude() {
		logger.log("Preparing for gradientMagnitude...");
        BufferedImage ans = newEmptyInputSizedImage();
        BufferedImage grayscaled =grayscale();

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

			int gradientMagnitude = (int) Math.sqrt((dx * dx + dy * dy)/2);
			Color color = new Color(gradientMagnitude, gradientMagnitude, gradientMagnitude);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("GradientMagnitude done!");
		return ans;
	}
	
	public BufferedImage nearestNeighbor() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("nearestNeighbor");
	}
	
	public BufferedImage bilinear() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("bilinear");
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
