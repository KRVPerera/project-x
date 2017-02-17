package lk.ac.mrt.projectx.buildex.complex.generators;

import lk.ac.mrt.projectx.buildex.complex.filters.FishEyeJava;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Chathura Widanage
 */
public class DistinctRGBGeneratorTest {
    @Test
    public void generate() throws Exception {
        BufferedImage read = ImageIO.read(new File("D:\\test\\in.png"));
        DistinctRGBGenerator distinctRGBGenerator = new DistinctRGBGenerator();
        BufferedImage out = distinctRGBGenerator.generate(read);
        ImageIO.write(out, "BMP", new File("D:\\test\\rgb.bmp"));
    }

}