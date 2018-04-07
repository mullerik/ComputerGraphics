package edu.cg;


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

    private Seam[] foundSeams;


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


        foundSeams =  new Seam[numOfSeams];

        BufferedImage currentImage = workingImage;
        int currentImageWidth = inWidth;
        for(int i = 0; i < numOfSeams; i++) {
            calculateCostAndGreyscaleMatrixes(currentImage, inHeight, currentImageWidth);
            Seam currentSeam = findSeam(inHeight, currentImageWidth);
            foundSeams[i] = currentSeam;

            currentImageWidth--;
            currentImage = removeSeam(currentImage, inHeight, currentImageWidth, currentSeam);
        }
    }

    private BufferedImage removeSeam(BufferedImage currentImage, int height, int width, Seam seam) {
        int[] offsets = seam.getOffsets();

        BufferedImage ans = newEmptyImage(width, height);
        for(int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(x >= offsets[y]) {
                    ans.setRGB(x, y, currentImage.getRGB(x + 1, y));
                } else {
                    ans.setRGB(x, y, currentImage.getRGB(x, y));
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

//    private int getAbsoluteDiffInGrayscale(int y1, int x1, int y2, int x2) {
//        try {
//            return Math.abs(grayscaleMatrix[y1][x1] - grayscaleMatrix[y2][x2]);
//        } catch (IndexOutOfBoundsException e) {
//            return -1;
//        }
//    }

    private void calculateCostAndGreyscaleMatrixes(BufferedImage image, int height, int width) {
        BufferedImage grayscaled = grayscale(image, height, width);
        costMatrix = new long[height][width];
        grayscaleMatrix = new int[height][width];

        for(int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // We can use green since in grayscale red=green=blue.Also avoid creation of `Color` object for performance.
                int weight = grayscaled.getRGB(x, y) & 0xFF;
                int weightNextHorizontal = (x != width - 1) ?
                        (grayscaled.getRGB(x + 1, y) & 0xFF) :
                        (grayscaled.getRGB(x - 1, y) & 0xFF);

                int energy = Math.abs(weightNextHorizontal - weight);
                grayscaleMatrix[y][x] = weight;
                costMatrix[y][x] = energy;
            }
        }
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
        numOfSeams = foundSeams.length; // TODO: remove this line
        logger.log("Preparing for showSeams...");
        BufferedImage imageProcessed = changeHue();
        for(int y = 1; y < inHeight; y++) {
            for (int x = 0; x < numOfSeams; x++) {
                imageProcessed.setRGB(foundSeams[x].getOffsets()[y], y, seamColorRGB);
            }
        }
        logger.log("ShowSeams done!");
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
