package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * @author Yasiru Kassapa
 */
public class SourceDestinationSeekerTest {
    @Test
    public void fishEyeTest() throws Exception {
        SourceDestinationSeeker eluwaEngine = new SourceDestinationSeeker();
        BufferedImage sourceImage, filteredImage;
        sourceImage = ImageIO.read(new File("G:\\out.bmp"));
        filteredImage = ImageIO.read(new File("G:\\out_fisheye.bmp"));

        List<Pair<CartesianCoordinate, CartesianCoordinate>> mappings = eluwaEngine.generate(sourceImage, filteredImage);

        System.out.println(mappings.size());
        System.out.println(sourceImage.getHeight()*sourceImage.getWidth());
    }

}