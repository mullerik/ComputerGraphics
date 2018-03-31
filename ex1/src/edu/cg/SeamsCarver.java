package edu.cg;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
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

    private long[][] costMatrix;
    private int[][] grayscaleMatrix;
    private Seam[] seams;


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

        calculateCostMatrix();
        findSeams();
    }

    private void findSeams() {
        SeamPoint[][] dynamicMatrix = new SeamPoint[inHeight][inWidth];

        // First line
        forEachWidth(x -> dynamicMatrix[0][x] = new SeamPoint(costMatrix[0][x], -1));
        for(int y = 1; y < inHeight; y++) {
            for(int x = 0; x < inWidth; x++){
                SeamPoint currentPoint = null;
                long costAtCurrentPoint = costMatrix[y][x];

                boolean isCorner = (x == 0) || (x == inWidth - 1);
                if(isCorner) {
                    // TODO: current logic is not to include edges in calculation whe we are next to corners.
                    //       solution: change `todo` variable (and make sure that leftmost/rightmost seams can be sometimes chosen!)
                    long todo = costAtCurrentPoint;

                    int sideIndex = (x == 0) ? x + 1 : x - 1;
                    long midCost = dynamicMatrix[y-1][x].cost + costAtCurrentPoint + todo;
                    long sideCost = dynamicMatrix[y-1][sideIndex].cost + costAtCurrentPoint + todo;
                    if(midCost <= sideCost) {
                        currentPoint = new SeamPoint(midCost, x);
                    } else {
                        currentPoint = new SeamPoint(sideCost, sideIndex);
                    }
                } else {
                    int edgeCreatedDown = Math.abs(grayscaleMatrix[y][x-1] - grayscaleMatrix[y][x+1]);
                    int edgeCreatedLeft = Math.abs(grayscaleMatrix[y-1][x] - grayscaleMatrix[y][x-1]);
                    int edgeCreatedRight = Math.abs(grayscaleMatrix[y-1][x] - grayscaleMatrix[y][x+1]);

                    long leftCost = dynamicMatrix[y-1][x-1].cost + edgeCreatedDown + edgeCreatedLeft;
                    long midCost = dynamicMatrix[y-1][x].cost + edgeCreatedDown;
                    long rightCost = dynamicMatrix[y-1][x+1].cost + edgeCreatedDown + edgeCreatedRight;

                    if( leftCost <= midCost && leftCost <= rightCost) {
                        currentPoint = new SeamPoint(costAtCurrentPoint + leftCost, x-1);
                    } else {
                        if(midCost <= rightCost) {
                            currentPoint = new SeamPoint(costAtCurrentPoint + midCost, x);
                        } else {
                            currentPoint = new SeamPoint(costAtCurrentPoint + rightCost, x+1);
                        }
                    }
                }
                dynamicMatrix[y][x] = currentPoint;
            };
        }

        // convert to seams:
        seams = new Seam[inWidth];
        forEachWidth(x -> seams[x] = recreateSeam(dynamicMatrix, x));

        // Sort seams
        Arrays.sort(seams, (o1, o2) -> (int) (o1.cost - o2.cost));
    }

    private Seam recreateSeam(SeamPoint[][] dynamicMatrix, Integer x) {
        int[] offsets = new int[inHeight];
        recreateSeamRecursion(dynamicMatrix, inHeight - 1, x, offsets);
        return new Seam(dynamicMatrix[inHeight - 1][x].cost, offsets);
    }

    private void recreateSeamRecursion(SeamPoint[][] dynamicMatrix, int y, int x, int[] offsets) {
        if(y >= 0) {
            offsets[y] = x;
            recreateSeamRecursion(dynamicMatrix, y-1, dynamicMatrix[y][x].cameFromOffset, offsets);
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
        costMatrix = new long[inHeight][inWidth];
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
    }

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
        logger.log("Preparing for showSeams...");

        BufferedImage imageProcessed = changeHue();

        int r = rgbWeights.redWeight;
        int g = rgbWeights.greenWeight;
        int b = rgbWeights.blueWeight;
        int sumWeights = (r + g + b);


        forEach((y, x) -> {
            if( x < numOfSeams) {
                Color color = new Color(255, 0, 0);
                imageProcessed.setRGB(seams[x].getOffsets()[y], y, color.getRGB());
            }
        });

        logger.log("Image grayscale done!");

        return imageProcessed;
    }

    private class SeamPoint {
        private long cost;
        private int cameFromOffset;

        /**
         * Defines a point in the seam
         * @param cost
         * @param cameFrom
         */
        SeamPoint(long cost, int cameFrom){
            this.cost = cost;
            this.cameFromOffset = cameFrom;
        }

        public long getCost() {
            return cost;
        }

        public int getCameFromOffset() {
            return cameFromOffset;
        }
    }
    private class Seam {
        private long cost;
        private int[] offsets;
        Seam(long cost, int[] offsets){
            this.cost = cost;
            this.offsets = offsets;
        }

        public long getCost() {
            return cost;
        }

        public int[] getOffsets() {
            return offsets;
        }
    }

}
