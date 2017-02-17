package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.generators.Generator;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wik2kassa on 2/17/2017.
 */
public class EluwaEngine {
    List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(BufferedImage sourceImg, BufferedImage filteredImage) throws Exception {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> mappings = new ArrayList<>();
        int height = sourceImg.getHeight();
        int width = sourceImg.getWidth();
        int rgb, index, x, y, total = 0, matches = 0;
        if(filteredImage.getHeight() != height || filteredImage.getWidth() != width) {
            throw new Exception("Image dimensions mismatch");
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb = filteredImage.getRGB(j, i);
                index = (((rgb >> 16) & 255) << 16) +
                        (((rgb >> 8) & 255) << 8) +
                        (((rgb) & 255));

                x = index % width;
                y = index / width;

                total++;
                if(sourceImg.getRGB(x, y) == filteredImage.getRGB(j, i)) {
                    mappings.add(new Pair<CartesianCoordinate, CartesianCoordinate>(new CartesianCoordinate(x, y),
                            new CartesianCoordinate(j, i)));
                    matches++;
                }
            }
        }
        return mappings;
    }

}
