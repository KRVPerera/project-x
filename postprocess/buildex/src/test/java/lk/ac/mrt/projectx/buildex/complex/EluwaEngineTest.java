package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by wik2kassa on 2/17/2017.
 */
public class EluwaEngineTest {
    @Test
    public void fishEyeTest() throws Exception {
        EluwaEngine eluwaEngine = new EluwaEngine();
        BufferedImage sourceImage, filteredImage;
        sourceImage = ImageIO.read(new File("G:\\out.bmp"));
        filteredImage = ImageIO.read(new File("G:\\out_fisheye.bmp"));

        List<Pair<CartesianCoordinate, CartesianCoordinate>> mappings = eluwaEngine.generate(sourceImage, filteredImage);

        System.out.println(mappings.size());
        System.out.println(sourceImage.getHeight()*sourceImage.getWidth());
    }

}