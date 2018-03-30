package edu.cg;


import javafx.collections.transformation.SortedList;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class SeamsCarver extends ImageProcessor {

	//MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage apply();
	}

	//MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;

	private int[][] costMatrix;
	private int[][] grayscaleMatrix;

	// TODO: make sure that the first seam is the cheapest
//	private SortedList<Seam> seams;


	//MARK: Constructor
	public SeamsCarver(Logger logger, BufferedImage workingImage,
					   int outWidth, RGBWeights rgbWeights) {
		super(logger, workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);

		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		//Sets resizeOp with an appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		//TODO: Initialize your additional fields and apply some preliminary calculations:
//		calculateCostMatrix();
//		findSeams();
	}
/**
	private void findSeams() {
		// TODO: maybe at some cases we only need one seam when starting;

		// Since each Seam contains history, wen only need 1 dimension of seams.
		Seam[] prevLine = new Seam[inWidth];
		Seam[] currLine;

		// First line
		forEachWidth(x -> prevLine[x] = new Seam(inHeight).addOffsetAndSetCost(x, costMatrix[x][0]));

		for(int y = 1; y < inHeight; y++) {
			for(int x = 0; x < inWidth; x++){
				boolean isCorner = (x == 0) || (x == inWidth - 1);
				// TODO: current logic is not to include edges in calculation whe we are next to corners.
				if(isCorner) {
					// TODO
				} else {
					int edgeCreatedDown = getAbsoluteDiffInGrayscale(y, x-1, y, x+1);
					int edgeCreatedLeft = getAbsoluteDiffInGrayscale(y-1, x, y, x-1);
					int edgeCreatedRight = getAbsoluteDiffInGrayscale(y-1, x, y, x+1);

//					int left = prevLine

				}




				int rawCost = costMatrix[x][y];

//				int costMid =

//				accumulators[x].addOffsetAndSetCost(x, costMatrix[x][0]);
			};
		}
	}

	private int getAbsoluteDiffInGrayscale(int y1, int x1, int y2, int x2) {
		try {
			return Math.abs(grayscaleMatrix[y1][x1] - grayscaleMatrix[y2][x2]);
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
	}

	private void calculateCostMatrix() {
		BufferedImage grayscaled = grayscale();
		costMatrix = new int[inHeight][inWidth];
		grayscaleMatrix = new int[inHeight][inWidth];

		forEach((y, x) -> {
			// We can use green since in grayscale red=green=blue.Also avoid creation of `Color` object for performance.
			int weight = grayscaled.getRGB(x, y) & 0xFF;
			int weightNextHorizontal = (x != inWidth - 1) ?
					(grayscaled.getRGB(x + 1, y) & 0xFF) :
					(grayscaled.getRGB(x - 1, y) & 0xFF);

			int energy = Math.abs(weightNextHorizontal - weight);
			grayscaleMatrix[y][x] = weight;
			costMatrix[y][x] = energy;
		});
	}*/

	//MARK: Methods
	public BufferedImage resize() {
		return resizeOp.apply();
	}

	//MARK: Unimplemented methods
	private BufferedImage reduceImageWidth() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("reduceImageWidth");
	}

	private BufferedImage increaseImageWidth() {
		//TODO: Implement this method, remove the exception.
		throw new UnimplementedMethodException("increaseImageWidth");
	}

	public BufferedImage showSeams(int seamColorRGB) {
		//TODO: Implement this method (bonus), remove the exception.
		throw new UnimplementedMethodException("showSeams");
	}

	private class SeamPoint {
		private long cost;
		private int cameFrom;

		/**
		 * Defines a point in the seam
		 * @param cost
		 * @param cameFrom
		 */
		SeamPoint(long cost, int cameFrom){
			this.cost = cost;
			this.cameFrom = cameFrom;
		}

		public long getCost() {
			return cost;
		}

		public int getCameFrom() {
			return cameFrom;
		}
	}
//	private class ImmutableSeam {
//		private int[] offsets;
//		private long seamCost;
//
//		ImmutableSeam(){
//			offsets = new int[0];
//			seamCost = 0;
//		}
//
////		ImmutableSeam(int offset, int cost){
////			offsets = new int[1];
////			offsets[0] = offset;
////			SeamCost = cost;
////		}
//
//		ImmutableSeam addOffset(int offset, long updatedCost){
//			int[] newOffsets = Arrays.copyOf(offsets, offsets.length + 1);
//			newOffsets[newOffsets.length - 1] = offset;
//			return new ImmutableSeam();
//			offsets[currentLengthOfOffsets] = offset;
//			currentLengthOfOffsets++;
//			SeamCost = cost;
//			return this;
//		}
//
//		public long getCost() {
//			return SeamCost;
//		}
//
//		public int getOffset(int index) {
//			return offsets[index];
//		}
//	}
}
