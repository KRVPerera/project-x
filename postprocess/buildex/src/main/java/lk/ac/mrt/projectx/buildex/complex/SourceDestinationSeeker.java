package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yasiru Kassapa
 * @author Lasantha Ekanayake
 */
public class SourceDestinationSeeker {
    private final Logger logger = LogManager.getLogger(SourceDestinationSeeker.class);

    public static void main(String[] args) throws Exception {
        SourceDestinationSeeker seeker = new SourceDestinationSeeker();
        BufferedImage sourceImage, filteredImage;
        sourceImage = ImageIO.read(new File("E:\\FYP\\AtanGenerator\\rgb.bmp"));
        filteredImage = ImageIO.read(new File("E:\\FYP\\AtanGenerator\\input-polar.bmp"));

        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = seeker.generate(sourceImage, filteredImage);

        ExamplesFile examplesFile = new ExamplesFile();
        examplesFile.write(examples);
    }

    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(BufferedImage sourceImg, BufferedImage filteredImage) throws Exception {
        Map<Integer, CartesianCoordinate> colorLocationDirectory = new HashMap<>();
        List<Pair<CartesianCoordinate, CartesianCoordinate>> mappings = new ArrayList<>();
        int height = sourceImg.getHeight();
        int width = sourceImg.getWidth();

        if (filteredImage.getHeight() != height || filteredImage.getWidth() != width) {
            throw new Exception("Image dimensions mismatch");
        }

        /*Scanning filtered image instead of the original image*/
        logger.info("Scanning input image");

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = sourceImg.getRGB(x, y);

                // we assumed that all pixels are different
                colorLocationDirectory.put(color,new CartesianCoordinate(x,y));
            }
        }


        logger.info("Generating examples");
        int black = new Color(0, 0, 0).getRGB();

        /*Check from this pixels comes from in the original image*/

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = filteredImage.getRGB(x, y);
                if (color == black) {//skip black since it is every where
                    continue;
                }

                // this color pixel must be the in the source image
                CartesianCoordinate c = colorLocationDirectory.get(color);
                mappings.add(new Pair<>(new CartesianCoordinate(x, y), c));
                //System.out.println(x+","+y+","+c.getX()+","+c.getY());
            }
        }

        //todo should add failing conditions
        return mappings;
    }


}
