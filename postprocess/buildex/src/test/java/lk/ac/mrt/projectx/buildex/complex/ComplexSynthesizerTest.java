package lk.ac.mrt.projectx.buildex.complex;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Chathura Widanage
 */
public class ComplexSynthesizerTest {
    @Test
    public void synthesize() throws Exception {
        ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
        double y[] = {7, 6, 5, 16};
        double x[][] = {{1, 3}, {2, 2}, {3, 1}, {4, 6}};
        double[] synthesize = complexSynthesizer.synthesize(y, x,true,true);
        for (double d : synthesize) {
            System.out.print(d + ",");
        }
        System.out.println();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                double answer = i + (2 * j) + 0;
                double a = calculateEstimation(new double[]{i, j}, synthesize);
                //System.out.println(a);
                assertEquals(Math.round(answer),Math.round(a));
            }
        }
    }

    private double calculateEstimation(double[] x, double[] coe) {
        double result = 0;//coe[0];
        for (int i = 0; i < coe.length; ++i)
            result += coe[i] * x[i ]; // 1
        return result;
    }

}