package lk.ac.mrt.projectx.buildex;

import lk.ac.mrt.projectx.buildex.files.MemoryDumpFile;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryDumpType;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.PCMemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Chathura Widanage
 * @see https://msdn.microsoft.com/en-us/library/windows/desktop/aa473780(v=vs.85).aspx
 */
public class MemoryAnalyser {
    private static final Logger logger = LogManager.getLogger(MemoryAnalyser.class);

    private static MemoryAnalyser memoryAnalyser = new MemoryAnalyser();

    private MemoryAnalyser() {

    }

    public static MemoryAnalyser getInstance() {
        return memoryAnalyser;
    }

    public static List<MemoryRegion> getInputOutputRegions(List<MemoryRegion> imageRegions, List<MemoryRegion> totalMemRegions, List<PCMemoryRegion> pcMems, List<Long> appPc, List<Pair<Output, StaticInfo>> instrs, List<Long> startPoints) {
        logger.debug("Getting input output region for further analysis");
        List<MemoryRegion> ret = new ArrayList<>();
        MemoryRegion inputMemRegion = null;
        MemoryRegion outputMemRegion = null;
        for (MemoryRegion imageRegion : imageRegions) {
            if ((imageRegion.getMemoryDumpType() == MemoryDumpType.INPUT_BUFFER) && (inputMemRegion == null)) {
                inputMemRegion = imageRegion;
                if (outputMemRegion != null) {
                    break;
                }
            } else if ((imageRegion.getMemoryDumpType() == MemoryDumpType.OUTPUT_BUFFER) && (outputMemRegion == null)) {
                outputMemRegion = imageRegion;
                if (inputMemRegion != null) {
                    break;
                }
            }
        }

        if ((inputMemRegion != null) && (outputMemRegion != null)) {
            logger.debug("Both input and output regions are found from dump");
            ret.add(inputMemRegion);
            ret.add(outputMemRegion);
        } else {

            if (inputMemRegion == null) {

            }

        }
        return ret;
    }

    public List<MemoryRegion> getImageRegions(List<MemoryDumpFile> memoryDumpFiles, ProjectXImage inputImage, ProjectXImage outputImage) throws IOException {
        /*if (this.isEqualImages(inputImage, outputImage)) {
            logger.error("Input and output images are equal");
            //throw new Exception("Input and output images are equal");
        }*/
        List<MemoryRegion> memoryRegions = new ArrayList<>();

        for (MemoryDumpFile memoryDumpFile : memoryDumpFiles) {
            logger.info("Analyzing file : {}", memoryDumpFile.toString());
            List<MemoryRegion> startPoints = null;
            if (!memoryDumpFile.isWrite()) {
                startPoints = forwardAnalysis(memoryDumpFile, inputImage);
                if (startPoints == null)
                    startPoints = backwardAnalysis(memoryDumpFile, inputImage, false);
            } else {
                startPoints = forwardAnalysis(memoryDumpFile, outputImage);
                if (startPoints == null)
                    startPoints = backwardAnalysis(memoryDumpFile, outputImage, true);
            }

            if (startPoints != null && !startPoints.isEmpty()) {
                memoryRegions.addAll(startPoints);
            }
        }
        logger.debug("Found total of {} memory regions {}", memoryRegions.size(), memoryRegions.toString());
        return memoryRegions;
    }

    private void findAnyWhere(MemoryDumpFile memoryDumpFile, ProjectXImage image) throws IOException {
        int[] imageBuffer = image.getImageBuffer(ProjectXImage.BufferLayout.PLANAR);
        byte[] memoryBuffer = memoryDumpFile.getMemoryBuffer();

        ArrayList<Integer> locs = new ArrayList<>();
        for (int pix : imageBuffer) {
            boolean found = false;
            int pos = 0;
            for (byte x : memoryBuffer) {
                if ((x & 0xff) == pix) {
                    locs.add(pos);
                    found = true;
                    break;
                }
                pos++;
            }
            if (!found) {
                System.out.println("Pixel not found");
            }
        }
        Collections.sort(locs);
    }

    //todo invalid implementation
    private List<MemoryRegion> backwardAnalysis(MemoryDumpFile memoryDumpFile, ProjectXImage image, boolean write) throws IOException {
        logger.info("Backward analyzing {}", memoryDumpFile.getName());
        int[] imageBuffer = image.getImageBuffer(ProjectXImage.BufferLayout.PLANAR);
        int[] reversedImageBuffer = new int[imageBuffer.length];

        //todo this is just for PLANAR, will not work for INTERLEAVED
        for (int i = 0; i < imageBuffer.length; i += image.getImage().getWidth()) {
            int swapPosition = (image.getImage().getWidth() * image.getImage().getHeight() * 3) - i - image.getImage().getWidth();
            System.arraycopy(imageBuffer, i, reversedImageBuffer, swapPosition, image.getImage().getWidth());
        }
     /*   if (true) {
            int[] revereImageBufferCopy = Arrays.copyOf(reversedImageBuffer, reversedImageBuffer.length);
            int channelGap = (image.getImage().getWidth() * image.getImage().getHeight());
            int channelLevelIndex = 0;
            for (int i = 0; i < reversedImageBuffer.length; ) {
                reversedImageBuffer[i++] = revereImageBufferCopy[channelLevelIndex];
                reversedImageBuffer[i++] = revereImageBufferCopy[channelLevelIndex + channelGap];
                reversedImageBuffer[i++] = revereImageBufferCopy[channelLevelIndex + (2 * channelGap)];
                channelLevelIndex++;
            }
        }*/

        return findRegions(image.getImage().getWidth(),
                image.getImage().getHeight(),
                reversedImageBuffer,
                memoryDumpFile.getBaseProgramCounter(),
                memoryDumpFile.getMemoryBuffer(),
                memoryDumpFile.isWrite()
        );
    }

    private List<MemoryRegion> forwardAnalysis(MemoryDumpFile memoryDumpFile, ProjectXImage image) throws IOException {
        logger.info("Forward analyzing {}", memoryDumpFile.getName());
        return findRegions(image.getImage().getWidth(),
                image.getImage().getHeight(),
                image.getImageBuffer(ProjectXImage.BufferLayout.PLANAR),
                memoryDumpFile.getBaseProgramCounter(),
                memoryDumpFile.getMemoryBuffer(),
                memoryDumpFile.isWrite()
        );
    }

    private List<MemoryRegion> findRegions(int imageWidth, int imageHeight, int[] imageBuffer,
                                           long basePC, byte[] memoryBuffer, boolean write) {
        ArrayList<MemoryRegion> memoryRegions = new ArrayList<>();
        ArrayList<Integer> startPoints = new ArrayList<>();
        int last = 0;
        for (int j = 0; j < memoryBuffer.length; ) {
            boolean found = true;
            for (int k = last; k < last + (imageWidth); k++) {
                if (j + k - last < memoryBuffer.length && (memoryBuffer[j + k - last] & 0xff) != imageBuffer[k]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                last += (imageWidth);
                startPoints.add(j);
                j += (imageWidth - 1);
                //todo backward memory region implementation
                if (last == imageWidth * imageHeight) {//todo multiply by 3?? OK for planar, interleaved not required
                    logger.info("Scanned whole image with {} start points", startPoints.size());
                    List<Integer> gaps = new ArrayList<>();
                    for (int i = 1; i < startPoints.size(); i++) {
                        gaps.add(startPoints.get(i) - startPoints.get(i - 1));
                    }
                    logger.info("Gaps size : {}", gaps.size());
                    MemoryRegion memoryRegion = new MemoryRegion();
                    memoryRegion.setBytesPerPixel(1);
                    memoryRegion.setDimension(2);
                    memoryRegion.setExtents(new long[]{imageWidth, imageHeight});

                    memoryRegion.setMemoryDumpType(write ? MemoryDumpType.OUTPUT_BUFFER :
                            MemoryDumpType.INPUT_BUFFER);
                    if (gaps.size() == 0) {
                        memoryRegion.setStrides(new long[]{1, imageWidth});
                        memoryRegion.setPaddingFilled(1);
                        memoryRegion.setPadding(new long[]{0});

                        memoryRegions.add(memoryRegion);
                    } else {
                        //check gaps are equal
                        boolean foundEqualGaps = true;
                        int expectedGap = gaps.get(0);
                        for (int gap : gaps) {
                            if (gap != expectedGap) {
                                foundEqualGaps = false;
                                break;
                            }
                        }
                        if (foundEqualGaps) {
                            memoryRegion.setStrides(new long[]{1, expectedGap});
                            memoryRegion.setPaddingFilled(1);
                            memoryRegion.setPadding(new long[]{expectedGap - imageWidth});

                            memoryRegions.add(memoryRegion);
                        }
                    }
                    memoryRegion.setStartMemory(startPoints.get(0) + basePC);
                    memoryRegion.setEndMemory(startPoints.get(startPoints.size() - 1)
                            + memoryRegion.getStrides()[1] + basePC);

                    //reset
                    last = 0;
                    startPoints.clear();
                }
            } else {
                j++;
            }
        }
        logger.info("Found {} memory regions", memoryRegions.size());
        return memoryRegions;
    }

    private boolean isEqualImages(ProjectXImage i1, ProjectXImage i2) {
        BufferedImage image1 = i1.getImage();
        BufferedImage image2 = i2.getImage();
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            return false;
        }
        for (int x = 1; x < image2.getWidth(); x++) {
            for (int y = 1; y < image2.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
