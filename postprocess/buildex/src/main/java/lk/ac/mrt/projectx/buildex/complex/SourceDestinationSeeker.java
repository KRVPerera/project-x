package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author Yasiru Kassapa
 */
public class SourceDestinationSeeker {
    private final Logger logger = LogManager.getLogger(SourceDestinationSeeker.class);

    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(BufferedImage sourceImg, BufferedImage filteredImage) throws Exception {
        Map<Integer, List<CartesianCoordinate>> colorLocationDirectory = new HashMap<>();
        List<Pair<CartesianCoordinate, CartesianCoordinate>> mappings = new ArrayList<>();
        int height = sourceImg.getHeight();
        int width = sourceImg.getWidth();

        if (filteredImage.getHeight() != height || filteredImage.getWidth() != width) {
            throw new Exception("Image dimensions mismatch");
        }

        /*Scanning filtered image instead of the original image*/
        logger.info("Scanning output image");
        int black = new Color(0, 0, 0).getRGB();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = filteredImage.getRGB(j, i);
                if (color == black) {//skip black since it is every where
                    continue;
                }
                if (!colorLocationDirectory.containsKey(color)) {
                    colorLocationDirectory.put(color, new ArrayList<CartesianCoordinate>());
                }
                colorLocationDirectory.get(color).add(new CartesianCoordinate(j, i));
            }
        }

        //System.exit(0);

        logger.info("Generating examples");
        /*Check from this pixels comes from in the original image*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = sourceImg.getRGB(j, i);
                List<CartesianCoordinate> cartesianCoordinates = colorLocationDirectory.get(color);
                if (cartesianCoordinates != null) {
                    for (CartesianCoordinate c : cartesianCoordinates) {
                        mappings.add(new Pair(new CartesianCoordinate(j, i), c));
                    }
                }
            }
        }

        //todo should add failing conditions
        return mappings;
    }

}
