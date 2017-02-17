package lk.ac.mrt.projectx.buildex.complex;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.Arrays;

/**
 * @author Chathura Widanage
 */
public class ComplexSynthesizer {
    public double[] synthesize(double[] y, double x[][], boolean noIntercept, boolean isR) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.setNoIntercept(noIntercept);
        regression.newSampleData(y, x);
        double[] doubles = regression.estimateRegressionParameters();
        /*if (!noIntercept) {//make intercept the last of the array, in default it is the first
            double intercept = doubles[doubles.length - 1];
            for (int i = 0; i < doubles.length - 1; i++) {
                doubles[i] = doubles[i + 1];
            }
            doubles[doubles.length - 1] = intercept;
        }*/


        double[] residuals = regression.estimateResiduals();
        DescriptiveStatistics ds = new DescriptiveStatistics(residuals);
        //System.out.println(ds.getMin() + " : " + ds.getMax());
        if (isR && ds.getMax() < 5 && ds.getMin() > -5) {
            //System.out.println(regression.calculateRSquared());
            return doubles;
        } else if (!isR && ds.getMax() < 0.1 && ds.getMin() > -0.1) {
            //System.out.println(regression.calculateRSquared());
            return doubles;
        } else {
           /* System.out.println(ds.getMin() + " : " + ds.getMax());
            for(double r:residuals){
                System.out.print(r);
            }
            System.out.println();*/
            return null;
        }
       /* System.out.println("####################\n");
        if(regression.calculateResidualSumOfSquares()<1000){
            return null;
        }
        return doubles;*/
    }

    public double[] synthesizeBruteForce(double[] y, double x[][]) {
        return new double[]{};
    }


}
