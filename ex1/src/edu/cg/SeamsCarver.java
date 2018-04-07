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


        foundSeams =  new Seam[numOfSeams];

        int[][] currentImageMatrix = imageToMatrix(workingImage, inHeight, inWidth);
        int currentImageWidth = inWidth;
        long k = System.currentTimeMillis();
        for(int i = 0; i < numOfSeams; i++) {
//            long k = System.currentTimeMillis();
            grayscaleMatrix = toGreyscale(currentImageMatrix, inHeight, currentImageWidth);
            costMatrix = calculateCostMatrix(grayscaleMatrix, inHeight, currentImageWidth);
//            System.out.print(">>>> " + (System.currentTimeMillis() - k));
            Seam currentSeam = findSeam(inHeight, currentImageWidth);
            foundSeams[i] = currentSeam;

            currentImageWidth--;
            currentImageMatrix = removeSeam(currentImageMatrix, inHeight, currentImageWidth, currentSeam);
//            System.out.println(" >>>> " + (System.currentTimeMillis() - k));
        }
        System.out.println(">>>>>>>> " + (System.currentTimeMillis() - k));

        // TODO: move the following line to its revevant method
        shrinkedImage = matrixToImage(currentImageMatrix, inHeight, inWidth - numOfSeams);
    }

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

    private Seam findSeam(int height, int width) {
        SeamPoint[][] dynamicMatrix = new SeamPoint[height][width];

        // First line
        for(int x = 0; x < width; x++){
            dynamicMatrix[0][x] = new SeamPoint(costMatrix[0][x], -1);
        }

        for(int y = 1; y < height; y++) {
            for(int x = 0; x < width; x++) {
                SeamPoint currentPoint = null;
                long costAtCurrentPoint = costMatrix[y][x];

                boolean isCorner = (x == 0) || (x == width- 1);
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
            }
        }

        // convert to seams:
        Seam[] possibleSeams = new Seam[width];
        for(int x = 0; x < width; x++){
            possibleSeams[x] = recreateSeam(dynamicMatrix, x, height);
        }

        // Sort seams
        // TODO: no need to sort all, just find the minimum seam
        Arrays.sort(possibleSeams, (o1, o2) -> (int) (o1.cost - o2.cost));
        return possibleSeams[0];
    }

    private Seam recreateSeam(SeamPoint[][] dynamicMatrix, int x, int height) {
        int[] offsets = new int[height];
        recreateSeamRecursion(dynamicMatrix, height - 1, x, offsets);
        return new Seam(dynamicMatrix[height - 1][x].cost, offsets);
    }

    private void recreateSeamRecursion(SeamPoint[][] dynamicMatrix, int y, int x, int[] offsets) {
        if(y >= 0) {
            offsets[y] = x;
            recreateSeamRecursion(dynamicMatrix, y-1, dynamicMatrix[y][x].cameFromOffset, offsets);
        }
    }

    private static long[][] calculateCostMatrix(int[][] greyscaleMatrix, int height, int width) {
        long[][] costMatrix = new long[height][width];

        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int weight = greyscaleMatrix[y][x];
                int weightNextHorizontal = (x != width - 1) ?
                        (greyscaleMatrix[y][x+1]) :
                        (greyscaleMatrix[y][x-1]);

                // We choose to use squared for better output
                int temp = weightNextHorizontal - weight;
                costMatrix[y][x] = temp * temp;
            }
        }
        return costMatrix;
    }

    //MARK: Methods
    public BufferedImage resize() {
        return resizeOp.apply();
    }

    //MARK: Unimplemented methods
    private BufferedImage reduceImageWidth() {
        logger.log("reduceImageWidth done!");
        return shrinkedImage;
    }

    private BufferedImage increaseImageWidth() { // TODO: organzie code in this function
        normalizeSeams();
        BufferedImage ans = newEmptyOutputSizedImage();

        int[][] accumulators = new int[inHeight][inWidth];

        for(int i = 0; i < numOfSeams; i++) {
            int[] offsetsOfCurrentSeam = foundSeams[i].getOffsets();
            int height = offsetsOfCurrentSeam.length; // TODO: maybe use outHeight instead of this variable

            for (int y = 0; y < height; y++) {
                accumulators[y][offsetsOfCurrentSeam[y]]++;
            }
        }

        int[][] originalImageAsMatrix = imageToMatrix(workingImage, inHeight, inWidth);
        ColorAndCounter[][] matrixAsColorAndCounter = new ColorAndCounter[inHeight][inWidth];

        for (int y = 0; y < inHeight; y++) {
            for(int x = 0; x < inWidth; x++){
                matrixAsColorAndCounter[y][x] = new ColorAndCounter(originalImageAsMatrix[y][x], accumulators[y][x]);
            }
        }
        for (int y = 0; y < inHeight; y++) {
            ColorAndCounter[] currentLine = matrixAsColorAndCounter[y];


            List<Integer> l = Arrays.stream(currentLine).flatMap(colorAndCounter -> {
                List<Integer> colors = new ArrayList<>();
                for (int i = 0; i < colorAndCounter.counter + 1; i++){
                    colors.add(colorAndCounter.color);
                }
                return colors.stream();
            }).collect(Collectors.toList());

            for(int x = 0; x < outWidth; x++){
                ans.setRGB(x, y, l.get(x));
            }
        }
        return ans;
    }

    public BufferedImage showSeams(int seamColorRGB) {
        logger.log("Preparing for showSeams...");
        normalizeSeams();

        // TODO: maybe not need changeHue here?
        BufferedImage imageProcessed = changeHue();
        // TODO: maybe start y from 0
        for(int y = 0; y < inHeight; y++) {
            for (int x = 0; x < numOfSeams; x++) {
                imageProcessed.setRGB(foundSeams[x].getOffsets()[y], y, seamColorRGB);
            }
        }
        logger.log("ShowSeams done!");
        return imageProcessed;
    }

    /**
     * Move the found seams to be in the corresponding indexes in the original image
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

    private static int[][] imageToMatrix(BufferedImage image, int height, int width) {
        int[][] matrix = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = image.getRGB(x, y);
            }
        }
        return matrix;
    }

    private static int[][] toGreyscale(int[][] original, int height, int width) {
        int[][] matrix = new int[height][width];
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = original[y][x] & 0xFF;
            }
        }
        return matrix;
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

    private class ColorAndCounter {
        int color;
        int counter;
        ColorAndCounter(int color, int counter){
            this.color = color;
            this.counter = counter;
        }
    }

}
