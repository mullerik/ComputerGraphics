package edu.cg;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private Seam[] foundSeams;
    private int[][] originalImageAsMatrix = null;
    private BufferedImage shrinkedImage = null;

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

        originalImageAsMatrix = imageToMatrix(workingImage, inHeight, inWidth);

        foundSeams =  new Seam[numOfSeams];

        // Convert to matrix for performance
        int[][] currentImageMatrix = originalImageAsMatrix;

        // Iterate to find the required number of seams
        int currentImageWidth = inWidth;
        for(int i = 0; i < numOfSeams; i++) {
            grayscaleMatrix = toGreyscale(currentImageMatrix, inHeight, currentImageWidth);
            costMatrix = calculateCostMatrix(grayscaleMatrix, inHeight, currentImageWidth);

            // Find next seam
            Seam currentSeam = findSeam(inHeight, currentImageWidth);
            foundSeams[i] = currentSeam;

            // Update width and change the current image
            currentImageWidth--;
            currentImageMatrix = removeSeam(currentImageMatrix, inHeight, currentImageWidth, currentSeam);
        }

        shrinkedImage = matrixToImage(currentImageMatrix, inHeight, inWidth - numOfSeams);
    }

    /**
     * Convert matrix to BufferedImage object
     * @param imageMatrix A give matrix of pixels
     * @param height
     * @param width
     * @return Image object of the given pixel matrix
     */
    private BufferedImage matrixToImage(int[][] imageMatrix, int height, int width) {
        BufferedImage ans = newEmptyImage(width, height);
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ans.setRGB(x, y, imageMatrix[y][x]);
            }
        }
        return ans;
    }

    private int[][] removeSeam(int[][] currentImageMatrix, int height, int width, Seam seam) {
        int[] offsets = seam.getOffsets();

        int[][] ans = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(x >= offsets[y]) {
                    ans[y][x] = currentImageMatrix[y][x+1];
                } else {
                    ans[y][x] = currentImageMatrix[y][x];
                }
            }
        }
        return ans;
    }

    /**
     * Find the next seam using the costMatrix and the greyscale image matrix
     * @param height
     * @param width
     * @return The cheapest seam that was found
     */
    private Seam findSeam(int height, int width) {

        // Initialize the dynamic programming matrix that will be used
        SeamPoint[][] dynamicMatrix = new SeamPoint[height][width];

        // First line
        for(int x = 0; x < width; x++){
            dynamicMatrix[0][x] = new SeamPoint(costMatrix[0][x], -1);
        }

        // Iterate the matrix and fill all
        for(int y = 1; y < height; y++) {
            for(int x = 0; x < width; x++) {
                SeamPoint currentPoint = null;
                long costAtCurrentPoint = costMatrix[y][x];

                // Corners are treated differently
                boolean isCorner = (x == 0) || (x == width- 1);
                if(isCorner) {
                    int sideIndex = (x == 0) ? x + 1 : x - 1;
                    long midCost = dynamicMatrix[y-1][x].cost + costAtCurrentPoint;
                    long sideCost = dynamicMatrix[y-1][sideIndex].cost + costAtCurrentPoint;

                    // Choose the cheaper: side pixel or mid pixel
                    if(midCost <= sideCost) {
                        currentPoint = new SeamPoint(midCost, x);
                    } else {
                        currentPoint = new SeamPoint(sideCost, sideIndex);
                    }
                } else {
                    int edgeCreatedDown = Math.abs(grayscaleMatrix[y][x-1] - grayscaleMatrix[y][x+1]);
                    int edgeCreatedLeft = Math.abs(grayscaleMatrix[y-1][x] - grayscaleMatrix[y][x-1]);
                    int edgeCreatedRight = Math.abs(grayscaleMatrix[y-1][x] - grayscaleMatrix[y][x+1]);

                    // Calculate for each of the 3 possibilities
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
            }
        }

        // convert to seams:
        Seam[] possibleSeams = new Seam[width];
        for(int x = 0; x < width; x++){
            possibleSeams[x] = recreateSeam(dynamicMatrix, x, height);
        }

        // Return the cheapest seam
        return Arrays.stream(possibleSeams).min((seam1, seam2) -> (int) (seam1.cost - seam2.cost)).get();
    }

    /**
     * Recreate the seam that ends at offset x
     * @param dynamicMatrix The matrix to recreate from
     * @param x the offset that the seam ends at
     * @param height
     * @return The calculated seam
     */
    private Seam recreateSeam(SeamPoint[][] dynamicMatrix, int x, int height) {
        int[] offsets = new int[height];
        recreateSeamRecursion(dynamicMatrix, height - 1, x, offsets);
        return new Seam(dynamicMatrix[height - 1][x].cost, offsets);
    }

    /**
     * Fill the offsets array using recursion
     * @param dynamicMatrix The calcualted dynamic programming matrix
     * @param y The current height being checked
     * @param x The current offset being checked
     * @param offsets The array of offsets for the seam that is being filled
     */
    private void recreateSeamRecursion(SeamPoint[][] dynamicMatrix, int y, int x, int[] offsets) {
        if(y >= 0) {
            offsets[y] = x;
            recreateSeamRecursion(dynamicMatrix, y-1, dynamicMatrix[y][x].cameFromOffset, offsets);
        }
    }

    /**
     * Calculate the cost of each pixel by the horizontal gradient. Use te squared difference as it
     * was found by us to give the best output.
     * @param greyscaleMatrix a greyscaled matrix
     * @param height
     * @param width
     * @return The cost matrix for the given image matrix
     */
    private static long[][] calculateCostMatrix(int[][] greyscaleMatrix, int height, int width) {
        long[][] costMatrix = new long[height][width];

        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int weight = greyscaleMatrix[y][x];
                int weightNextHorizontal = (x != width - 1) ?
                        (greyscaleMatrix[y][x+1]) :
                        (greyscaleMatrix[y][x-1]);

                // We choose to use squared for better output
                int horizontalDifference = weightNextHorizontal - weight;

                // We chose to use the squared since it has a better output than using absolute value
                costMatrix[y][x] = horizontalDifference * horizontalDifference;
            }
        }
        return costMatrix;
    }

    //MARK: Methods
    public BufferedImage resize() {
        return resizeOp.apply();
    }

    /**
     * Find the resized image in case of reducing the width
     * @return The resized image
     */
    private BufferedImage reduceImageWidth() {
        logger.log("reduceImageWidth done!");
        return shrinkedImage;
    }

    /**
     * Find the resized image in case of increasing the width
     * @return The resized image
     */
    private BufferedImage increaseImageWidth() {
        normalizeSeams();
        BufferedImage ans = newEmptyOutputSizedImage();

        // Create an array for accumulators that count pixel duplications.
        // Cells are automatically initialized to 0

        int[][] accumulators = new int[inHeight][inWidth];

        for(int i = 0; i < numOfSeams; i++) {
            int[] offsetsOfCurrentSeam = foundSeams[i].getOffsets();

            // Increment the accumulator indexes that correspond to current seam
            for (int y = 0; y < inHeight; y++) {
                accumulators[y][offsetsOfCurrentSeam[y]]++;
            }
        }

        // Create a copy of the original image such that each cell also have a counter for the number
        // of times to duplicate each pixel.
        ColorAndCounter[][] matrixAsColorAndCounter = new ColorAndCounter[inHeight][inWidth];

        for (int y = 0; y < inHeight; y++) {
            for(int x = 0; x < inWidth; x++){
                matrixAsColorAndCounter[y][x] = new ColorAndCounter(originalImageAsMatrix[y][x], accumulators[y][x]);
            }
        }

        // Extend each line to its new width
        for (int y = 0; y < inHeight; y++) {
            ColorAndCounter[] currentLine = matrixAsColorAndCounter[y];

            // Use flatMap to extend the number of pixels and duplicate pixels
            List<Integer> newLineOfPixels = Arrays.stream(currentLine).flatMap(colorAndCounter -> {
                List<Integer> colors = new ArrayList<>();
                for (int i = 0; i < colorAndCounter.counter + 1; i++){
                    colors.add(colorAndCounter.color);
                }
                return colors.stream();
            }).collect(Collectors.toList());

            // Put the calculated pixels for output
            for(int x = 0; x < outWidth; x++){
                ans.setRGB(x, y, newLineOfPixels.get(x));
            }
        }
        logger.log("increase ImageWidth done!");
        return ans;
    }

    /**
     * Show the calculated seams for the image
     * @param seamColorRGB Color to show seams
     * @return The image with seams colored in the given color
     */
    public BufferedImage showSeams(int seamColorRGB) {
        logger.log("Preparing for showSeams...");
        normalizeSeams();

        BufferedImage imageToProcess = duplicateWorkingImage();
        for(int y = 0; y < inHeight; y++) {
            for (int x = 0; x < numOfSeams; x++) {
                imageToProcess.setRGB(foundSeams[x].getOffsets()[y], y, seamColorRGB);
            }
        }
        logger.log("ShowSeams done!");
        return imageToProcess;
    }

    /**
     * Move the found seams to be in their corresponding indexes in the original image
     */
    private void normalizeSeams() {
        for(int i = 0; i < numOfSeams; i++){
            for(int j = i + 1; j < numOfSeams; j++){

                // push all seams that came after i and have x position > x position of i
                for(int y = 0; y < inHeight; y++){
                    if(foundSeams[j].getOffsets()[y] >= foundSeams[i].getOffsets()[y]) {
                        foundSeams[j].getOffsets()[y]++;
                    }
                }
            }
        }
    }

    /**
     * Convert BufferedImage object to matrix
     * @param image given BufferedImage
     * @param height image height
     * @param width image width
     * @return A matrix representation of the image
     */
    private static int[][] imageToMatrix(BufferedImage image, int height, int width) {
        int[][] matrix = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = image.getRGB(x, y);
            }
        }
        return matrix;
    }

    /**
     * Convert image matrix to greyscale
     * @param original given image matrix
     * @param height height
     * @param width width
     * @return
     */
    private static int[][] toGreyscale(int[][] original, int height, int width) {
        int[][] matrix = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = original[y][x] & 0xFF;
            }
        }
        return matrix;
    }

    // A class for representing a tuple of cost and offset for a point in seam
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
    }

    // A class for representing Seam
    private class Seam {
        private long cost;
        private int[] offsets;

        Seam(long cost, int[] offsets){
            this.cost = cost;
            this.offsets = offsets;
        }

        public int[] getOffsets() {
            return offsets;
        }
    }

    // A class for representing a tuple of color and counter
    private class ColorAndCounter {
        int color;
        int counter;
        ColorAndCounter(int color, int counter){
            this.color = color;
            this.counter = counter;
        }
    }

}
