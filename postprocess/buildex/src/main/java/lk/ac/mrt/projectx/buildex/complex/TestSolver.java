package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.generators.CylinderGenerator;
import lk.ac.mrt.projectx.buildex.complex.generators.PolarGenerator;
import lk.ac.mrt.projectx.buildex.complex.generators.TwirlGenerator;
import lk.ac.mrt.projectx.buildex.complex.operations.Guess;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class TestSolver {
    private final static Logger logger = LogManager.getLogger(TestSolver.class);

    public static void main(String[] args) throws Exception {
        int x = 800;
        int y = 600;
        BufferedImage in = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage out = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
        InductiveSynthesizerNew inductiveSynthesizer = new InductiveSynthesizerNew();

        ExamplesFile examplesFile = new ExamplesFile();

        List<Pair<CartesianCoordinate, CartesianCoordinate>>
                examples = examplesFile.read("C:\\Users\\chath\\Desktop\\ExampleGenerotor\\examples\\examples_1487701047010.csv");


//        inductiveSynthesizer.solve(new TwirlGenerator().generate(x, x), inImg, outImg);

        /*Images*/
//        BufferedImage in = ImageIO.read(new File("D:\\test\\rgb.bmp"));
//        BufferedImage out = ImageIO.read(new File("D:\\test\\rgb-twirl-out.bmp"));

        //SourceDestinationSeeker eg = new SourceDestinationSeeker();
        //eg.generate(in,out);

        long startT = System.currentTimeMillis() / (1000 * 60);
        try {
            Pair<Guess, Guess> solve = inductiveSynthesizer.solve(examples, in, out);

           /* logger.debug("Solve {}", solve);
            x = 600;
            y = 800;
            in = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
            out = new BufferedImage(x, y, BufferedImage.TYPE_3BYTE_BGR);
            inductiveSynthesizer = new InductiveSynthesizerNew();
            Pair<Guess, Guess> solve2 = inductiveSynthesizer.solve(new PolarGenerator().generate(x, y)*//*eg.generate(in,out)*//*, in, out);
            logger.debug("solve 2 {}",solve2);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Synthesized in {}minutes", System.currentTimeMillis() / (1000 * 60) - startT);
    }
}
