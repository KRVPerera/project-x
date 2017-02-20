package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.generators.CylinderGenerator;
import lk.ac.mrt.projectx.buildex.complex.generators.PolarGenerator;
import lk.ac.mrt.projectx.buildex.complex.generators.TwirlGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * @author Chathura Widanage
 */
public class TestSolver {
    private final static Logger logger = LogManager.getLogger(TestSolver.class);

    public static void main(String[] args) throws Exception {
        int x = 800;
        int y = 600;
        BufferedImage inImg = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage outImg = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
        InductiveSynthesizerNew inductiveSynthesizer = new InductiveSynthesizerNew();
//        inductiveSynthesizer.solve(new TwirlGenerator().generate(x, x), inImg, outImg);

        /*Images*/
        BufferedImage in = ImageIO.read(new File("D:\\test\\rgb.bmp"));
        BufferedImage out = ImageIO.read(new File("D:\\test\\polar-cart-fb.bmp"));

        SourceDestinationSeeker eg = new SourceDestinationSeeker();
        //eg.generate(in,out);



        long startT = System.currentTimeMillis() / (1000 * 60);
        try {
            inductiveSynthesizer.solve(new PolarGenerator().generate(x,y)/*eg.generate(in,out)*/, in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Synthesized in {}minutes", System.currentTimeMillis() / (1000 * 60) - startT);
    }

    private static Random random = new Random();

    private static int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private static double round(double value) {
        return (double) Math.round(value * 1000d) / 1000d;
    }


    private static CartesianCoordinate clamp(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round(cartesianCoordinate.getX());
        x = x >= width ? width - 1 : x;
        x = x < 0 ? 0 : x;

        int y = (int) Math.round(cartesianCoordinate.getY());
        y = y >= height ? height - 1 : y;
        y = y < 0 ? 0 : y;

        return new CartesianCoordinate(x, y);
    }


    private static final BigDecimal SQRT_DIG = new BigDecimal(150);
    private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

    /**
     * Private utility method used to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn, BigDecimal precision) {
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(new BigDecimal(2));
        BigDecimal xn1 = fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c);
        currentPrecision = currentPrecision.abs();
        if (currentPrecision.compareTo(precision) <= -1) {
            return xn1;
        }
        return sqrtNewtonRaphson(c, xn1, precision);
    }

    /**
     * Uses Newton Raphson to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    public static BigDecimal bigSqrt(BigDecimal c) {
        return sqrtNewtonRaphson(c, new BigDecimal(1), new BigDecimal(1).divide(SQRT_PRE));
    }
}
