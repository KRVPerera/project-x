package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.complex.operations.Operand;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author Chathura Widanage
 */
public class InductiveSynthesizer {
    private final static Logger logger = LogManager.getLogger(InductiveSynthesizer.class);

    public void solve(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples, BufferedImage imageIn, BufferedImage imageOut) {
        logger.debug("Synthesizing using {} examples", examples.size());
        int widthIn = imageIn.getWidth();
        int heightIn = imageIn.getHeight();
        int widthOut = imageOut.getWidth();
        int heightOut = imageOut.getHeight();

        CoordinateTransformer.cartesianToCenter(widthIn, heightIn, examples);//making all points center originated

        /*Filtering good pairs*/
        List<Pair<CartesianCoordinate, CartesianCoordinate>> goodExamples = new ArrayList<>();
        Map<Double, List<Pair<CartesianCoordinate, CartesianCoordinate>>> thetaExamples = new HashMap<>();
        for (Pair<CartesianCoordinate, CartesianCoordinate> e : examples) {

            if (e.first.getX() == widthOut - 1 || e.first.getX() == 0
                    || e.first.getY() == heightOut - 1 || e.first.getY() == 0) {//to get rid of errors due to clamping
                //System.out.println("drop");
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(e.first);
                double theta = round(polarCoordinate.getTheta(), 2);
                List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs = thetaExamples.get(theta);
                if (pairs == null) {
                    pairs = new ArrayList<>();
                    thetaExamples.put(theta, pairs);
                }
                pairs.add(e);
            } else {

                goodExamples.add(e);
            }
        }

        Operand rOnly = new Operand("R") {
            @Override
            public double operate(double r, double theta) {
                return r;
            }
        };

        Operand tOnly = new Operand("T") {
            @Override
            public double operate(double r, double theta) {
                return theta;
            }
        };

        /*Iterator<Double> thetaExamplesIterator = thetaExamples.keySet().iterator();
        int s = 0;
        while (thetaExamplesIterator.hasNext()) {
            double t = thetaExamplesIterator.next();
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs = thetaExamples.get(t);
            if (pairs.size() > s) {
                examples = pairs;
                s = pairs.size();
            }
        }

        System.out.println(examples);*/

        //logger.debug(rExamples.keySet());

        logger.debug("Dropped {} example pairs", examples.size() - goodExamples.size());
        //examples = goodExamples;

        boolean variableCombinationR[] = {true, true, false, false, false};//R,T,R2,T2,RT,C
        boolean constantR = false;

        boolean variableCombinationT[] = {true, true, false, false, true};//R,T,R2,T2,RT,C
        boolean constantT = false;

        int variablesCountR = 0;
        for (boolean x : variableCombinationR) {
            if (x) {
                variablesCountR++;
            }
        }


        int variablesCountT = 0;
        for (boolean x : variableCombinationT) {
            if (x) {
                variablesCountT++;
            }
        }


        DescriptiveStatistics rComponentRCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics rComponentTCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics rComponentR2Coefficient = new DescriptiveStatistics();
        DescriptiveStatistics rComponentT2Coefficient = new DescriptiveStatistics();
        DescriptiveStatistics rComponentRTCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics rComponentConstCoefficient = new DescriptiveStatistics();

        DescriptiveStatistics tComponentRCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics tComponentTCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics tComponentR2Coefficient = new DescriptiveStatistics();
        DescriptiveStatistics tComponentT2Coefficient = new DescriptiveStatistics();
        DescriptiveStatistics tComponentRTCoefficient = new DescriptiveStatistics();
        DescriptiveStatistics tComponentConstCoefficient = new DescriptiveStatistics();

        int window = widthIn * widthOut / 5;//(int) Math.sqrt(width * height);
        for (int i = 0; i < examples.size() - window; i += window) {
            int size = window;
            double xR[][] = new double[size][variablesCountR];
            double yR[] = new double[size];

            double xT[][] = new double[size][variablesCountT + 1];//+1 for Math.PI*n constant normalization
            double yT[] = new double[size];
            for (int j = i; j < i + size; j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                /*Operations to R*/
                double varValues[] = new double[variableCombinationR.length];
                varValues[0] = polarCoordinateS.getR();
                varValues[1] = polarCoordinateS.getTheta();
                varValues[2] = Math.pow(polarCoordinateS.getR(), 2);
                varValues[3] = Math.pow(polarCoordinateS.getTheta(), 2);
                varValues[4] = polarCoordinateS.getR() * polarCoordinateS.getTheta();

                int pivot = 0;
                for (int comb = 0; comb < variableCombinationR.length; comb++) {
                    if (variableCombinationR[comb]) {
                        xR[j - i][pivot++] = varValues[comb];
                    }
                }

                yR[j - i] = polarCoordinateD.getR();


                /*Operations to T*/
                // varValues[1] = polarCoordinateS.getTheta();
                // varValues[4] = polarCoordinateS.getR() * polarCoordinateS.getTheta();


                pivot = 0;
                for (int comb = 0; comb < variableCombinationT.length; comb++) {
                    if (variableCombinationT[comb]) {
                        xT[j - i][pivot++] = varValues[comb];
                    }
                }
                xT[j - i][pivot] = Math.PI;//to avoid the error due to normalizing angle to 360deg
                yT[j - i] = polarCoordinateD.getTheta();
                //   System.out.println(polarCoordinateD.getTheta()+":"+(polarCoordinateS.getR()+polarCoordinateS.getTheta()));
            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
                boolean noRIntercept = !constantR;

                double[] synthesizeR = complexSynthesizer.synthesize(yR, xR, noRIntercept, true);
                double rVarCoefs[] = new double[variableCombinationR.length + 1];//+1 for constant
                int pivot = 0;
                for (int l = 0; l < variableCombinationR.length; l++) {
                    if (variableCombinationR[l]) {
                        rVarCoefs[l] = synthesizeR[pivot++];
                    } else {
                        rVarCoefs[l] = 0;
                    }
                }
                if (constantR) {
                    rVarCoefs[rVarCoefs.length - 1] = synthesizeR[pivot];
                }
                int coefficientIndex = 0;
                rComponentRCoefficient.addValue(rVarCoefs[coefficientIndex++]);
                rComponentTCoefficient.addValue(rVarCoefs[coefficientIndex++]);
                rComponentR2Coefficient.addValue(rVarCoefs[coefficientIndex++]);
                rComponentT2Coefficient.addValue(rVarCoefs[coefficientIndex++]);
                rComponentRTCoefficient.addValue(rVarCoefs[coefficientIndex++]);
                rComponentConstCoefficient.addValue(rVarCoefs[coefficientIndex++]);
                //System.out.println(round(synthesizeR[2])+","+synthesizeR[3]);


                //assuming T has no coefficient term. Might have to change in future
                double[] synthesizeT = complexSynthesizer.synthesize(yT, xT, true, false);
                //System.out.println(synthesizeT[2]);
                double normalizedValue = synthesizeT[synthesizeT.length - 1];
                for (int k = 0; k < yT.length; k++) {
                    yT[k] -= normalizedValue;
                }
                double xTnew[][] = new double[xT.length][xT[0].length - 1];
                for (int k = 0; k < xTnew.length; k++) {
                    for (int l = 0; l < xT[0].length - 1; l++) {
                        xTnew[k][l] = xT[k][l];
                    }
                }
                synthesizeT = complexSynthesizer.synthesize(yT, xTnew, true, false);

                double tVarCoefs[] = new double[variableCombinationR.length + 1];//+1 for constant

                pivot = 0;
                for (int l = 0; l < variableCombinationT.length; l++) {
                    if (variableCombinationT[l]) {
                        tVarCoefs[l] = synthesizeT[pivot++];
                    } else {
                        tVarCoefs[l] = 0;
                    }
                }

//                System.out.println(synthesizeT[0]);

                coefficientIndex = 0;
                tComponentRCoefficient.addValue(tVarCoefs[coefficientIndex++]);
                tComponentTCoefficient.addValue(tVarCoefs[coefficientIndex++]);
                tComponentR2Coefficient.addValue(tVarCoefs[coefficientIndex++]);
                tComponentT2Coefficient.addValue(tVarCoefs[coefficientIndex++]);
                tComponentRTCoefficient.addValue(tVarCoefs[coefficientIndex++]);
                tComponentConstCoefficient.addValue(tVarCoefs[coefficientIndex++]);
                //dsT3.addValue(round());
            } catch (Exception e) {
                logger.error(e.getMessage());//no need to print stack trace
            }
        }


        int loweP = 25;
        int higherP = 75;

        /*Approximating R coefficients*/

        LoopBounds loopBoundsR = new LoopBounds();
        loopBoundsR.r.low = loopBound(rComponentRCoefficient.getPercentile(loweP));
        loopBoundsR.r.high = loopBound(rComponentRCoefficient.getPercentile(higherP));

        logger.debug("R->r : {},{},{}", rComponentRCoefficient.getPercentile(loweP),
                rComponentRCoefficient.getPercentile(50), rComponentRCoefficient.getPercentile(higherP));

        loopBoundsR.t.low = loopBound(rComponentTCoefficient.getPercentile(loweP));
        loopBoundsR.t.high = loopBound(rComponentTCoefficient.getPercentile(higherP));
        logger.debug("R->t : {},{},{}", rComponentTCoefficient.getPercentile(loweP),
                rComponentTCoefficient.getPercentile(50), rComponentTCoefficient.getPercentile(higherP));

        loopBoundsR.r2.low = loopBound(rComponentR2Coefficient.getPercentile(loweP));
        loopBoundsR.r2.high = loopBound(rComponentR2Coefficient.getPercentile(higherP));
        logger.debug("R->r2 : {},{},{}", rComponentR2Coefficient.getPercentile(loweP),
                rComponentR2Coefficient.getPercentile(50), rComponentR2Coefficient.getPercentile(higherP));

        loopBoundsR.rt.low = loopBound(rComponentRTCoefficient.getPercentile(loweP));
        loopBoundsR.rt.high = loopBound(rComponentRTCoefficient.getPercentile(higherP));
        logger.debug("R->rt : {},{},{}", rComponentRTCoefficient.getPercentile(loweP),
                rComponentRTCoefficient.getPercentile(50), rComponentRTCoefficient.getPercentile(higherP));

        loopBoundsR.c.low = loopBound(rComponentConstCoefficient.getPercentile(loweP));
        loopBoundsR.c.high = loopBound(rComponentConstCoefficient.getPercentile(higherP));
        logger.debug("R->const : {},{},{}", rComponentConstCoefficient.getPercentile(loweP),
                rComponentConstCoefficient.getPercentile(50), rComponentConstCoefficient.getPercentile(higherP));

        loopBoundsR.t2.low = loopBound(rComponentT2Coefficient.getPercentile(loweP));
        loopBoundsR.t2.high = loopBound(rComponentT2Coefficient.getPercentile(higherP));
        logger.debug("R->t2 : {},{},{}", rComponentT2Coefficient.getPercentile(loweP),
                rComponentT2Coefficient.getPercentile(50), rComponentT2Coefficient.getPercentile(higherP));

        logger.debug("iterations : {}", loopBoundsR.getIterations());
        logger.debug("Loop bounds R {}", loopBoundsR);
        Guesses approximateR = Approximator.approximate(loopBoundsR, examples, true, null, widthIn, heightIn);
        logger.debug("R : {}", approximateR);

        /*Approximating T coefficients*/
        LoopBounds loopBoundsT = new LoopBounds();
        loopBoundsT.r.low = loopBound(tComponentRCoefficient.getPercentile(loweP));
        loopBoundsT.r.high = loopBound(tComponentRCoefficient.getPercentile(higherP));
        logger.debug("T->r : {},{},{}", tComponentRCoefficient.getPercentile(loweP),
                tComponentRCoefficient.getPercentile(50), tComponentRCoefficient.getPercentile(higherP));

        loopBoundsT.t.low = loopBound(tComponentTCoefficient.getPercentile(loweP));
        loopBoundsT.t.high = loopBound(tComponentTCoefficient.getPercentile(higherP));
        logger.debug("T->t : {},{},{}", tComponentTCoefficient.getPercentile(loweP),
                tComponentTCoefficient.getPercentile(50), tComponentTCoefficient.getPercentile(higherP));

        loopBoundsT.r2.low = loopBound(tComponentR2Coefficient.getPercentile(loweP));
        loopBoundsT.r2.high = loopBound(tComponentR2Coefficient.getPercentile(higherP));
        logger.debug("T->r2 : {},{},{}", tComponentR2Coefficient.getPercentile(loweP),
                tComponentR2Coefficient.getPercentile(50), tComponentR2Coefficient.getPercentile(higherP));

        loopBoundsT.rt.low = loopBound(tComponentRTCoefficient.getPercentile(loweP));
        loopBoundsT.rt.high = loopBound(tComponentRTCoefficient.getPercentile(higherP));
        logger.debug("T->rt : {},{},{}", tComponentRTCoefficient.getPercentile(loweP),
                tComponentRTCoefficient.getPercentile(50), tComponentRTCoefficient.getPercentile(higherP));

        loopBoundsT.c.low = loopBound(tComponentConstCoefficient.getPercentile(loweP));
        loopBoundsT.c.high = loopBound(tComponentConstCoefficient.getPercentile(higherP));
        logger.debug("T->const : {},{},{}", tComponentConstCoefficient.getPercentile(loweP),
                tComponentConstCoefficient.getPercentile(50), tComponentConstCoefficient.getPercentile(higherP));

        loopBoundsT.t2.low = loopBound(tComponentT2Coefficient.getPercentile(loweP));
        loopBoundsT.t2.high = loopBound(tComponentT2Coefficient.getPercentile(higherP));
        logger.debug("T->t2 : {},{},{}", tComponentT2Coefficient.getPercentile(loweP),
                tComponentT2Coefficient.getPercentile(50), tComponentT2Coefficient.getPercentile(higherP));

        logger.debug("iterations : {}", loopBoundsT.getIterations());
        logger.debug("Loop bounds T {}", loopBoundsT);
        Guesses approximateT = Approximator.approximate(loopBoundsT, examples, false, approximateR, widthIn, heightIn);
        logger.debug("T : {}", approximateT);

    }

    private int loopBound(double value) {
        return (int) (value * 1000);
    }

    private double round(double value) {
        return (double) Math.round(value * 1000d) / 1000d;
    }

    private double round(double value, int zeros) {
        double mul = Math.pow(10, zeros);
        return (double) Math.round(value * mul) / mul;
    }
}

