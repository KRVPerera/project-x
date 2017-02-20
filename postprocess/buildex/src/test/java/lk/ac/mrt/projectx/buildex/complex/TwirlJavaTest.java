package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.filters.TwirlJava;
import org.junit.Test;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Chathura Widanage
 */
public class TwirlJavaTest {
    @Test
    public void filterCartesian() throws Exception {
        BufferedImage read = ImageIO.read(new File("D:\\test\\rgb.bmp"));
        BufferedImage out=new BufferedImage(read.getWidth(),read.getHeight(),read.getType());
        TwirlJava twirlJava=new TwirlJava();
        twirlJava.filterCartesian(read,out);
        ImageIO.write(out,"BMP",new File("D:\\test\\rgb-out.bmp"));
    }

    @Test
    public void filter() throws Exception {
        BufferedImage read = ImageIO.read(new File("D:\\test\\in.jpg"));
        BufferedImage out=new BufferedImage(read.getWidth(),read.getHeight(),read.getType());
        TwirlJava twirlJava=new TwirlJava();
        twirlJava.filter(read,out);
        ImageIO.write(out,"JPG",new File("D:\\test\\out_ir.jpg"));
    }

}