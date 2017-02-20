package lk.ac.mrt.projectx.buildex.complex.filters;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Chathura Widanage
 */
public class PolarJavaTest {
    @Test
    public void filter() throws Exception {
        BufferedImage read = ImageIO.read(new File("D:\\test\\fb.jpg"));
        BufferedImage out=new BufferedImage(read.getWidth(),read.getHeight(),read.getType());
        PolarJava polarJava=new PolarJava();
        polarJava.filter(read,out);
        ImageIO.write(out,"JPG",new File("D:\\test\\polar-cart-gen-fb.jpg"));
    }

    @Test
    public void filterCartesian() throws Exception {
        BufferedImage read = ImageIO.read(new File("D:\\test\\input.png"));
        BufferedImage out=new BufferedImage(read.getWidth(),read.getHeight(),read.getType());
        PolarJava polarJava=new PolarJava();
        polarJava.filterCartesian(read,out);
        ImageIO.write(out,"PNG",new File("D:\\test\\input-polar.png"));
    }

}