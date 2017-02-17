package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.complex.operations.*;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author Chathura Widanage
 */
public class InductiveSynthesizerNew {
    private final static Logger logger = LogManager.getLogger(InductiveSynthesizerNew.class);

    public void solve(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples, BufferedImage imageIn, BufferedImage imageOut) {
        logger.debug("Synthesizing using {} examples", examples.size());
        final int widthIn = imageIn.getWidth();
        int heightIn = imageIn.getHeight();
        int widthOut = imageOut.getWidth();
        int heightOut = imageOut.getHeight();

        CoordinateTransformer.cartesianToCenter(widthIn, heightIn, examples);//making all points center originated

        /*Filtering good pairs*/
       /* List<Pair<CartesianCoordinate, CartesianCoordinate>> goodExamples = new ArrayList<>();
        Map<Double, List<Pair<CartesianCoordinate, CartesianCoordinate>>> thetaExamples = new HashMap<>();
        Map<Double, List<Pair<CartesianCoordinate, CartesianCoordinate>>> rExamples = new HashMap<>();
        for (Pair<CartesianCoordinate, CartesianCoordinate> e : examples) {

            PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(e.first);
            double theta = round(polarCoordinate.getTheta(), 1);
            double r = Math.round(polarCoordinate.getR());
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairsTheta = thetaExamples.get(theta);
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairsR = rExamples.get(r);

            if (pairsTheta == null) {
                pairsTheta = new ArrayList<>();
                thetaExamples.put(theta, pairsTheta);
            }
            pairsTheta.add(e);

            if (pairsR == null) {
                pairsR = new ArrayList<>();
                rExamples.put(r, pairsR);
            }
            pairsR.add(e);
        }*/

        Operation r = new Operation("R", "polarCoordinate.getR()") {
            @Override
            public double operate(double r, double theta) {
                return r;
            }
        };
        Operation r2 = new Operation("R2", "Math.pow(polarCoordinate.getR(),2)") {
            @Override
            public double operate(double r, double theta) {
                return r * r;
            }
        };

        Operation r2sqrt = new Operation("r2sqrt", "(polarCoordinate.getR()+(1-Math.sqrt(1-(Math.pow(polarCoordinate.getR(),2)))))") {
            @Override
            public double operate(double r, double theta) {
                return (r + (1 - (Math.sqrt(1 - r * r))));
            }
        };

        Operation rt = new Operation("RT", "(polarCoordinate.getR()*polarCoordinate.getTheta())") {
            @Override
            public double operate(double r, double theta) {
                return theta * r;
            }
        };

        Operation t = new Operation("T", "polarCoordinate.getTheta()") {
            @Override
            public double operate(double r, double theta) {
                return theta;
            }
        };

        Operation t2 = new Operation("T2", "Math.pow(polarCoordinate.getTheta(),2)") {
            @Override
            public double operate(double r, double theta) {
                return theta * theta;
            }
        };

        final double h = imageIn.getHeight();
        final double R = Math.hypot(imageIn.getWidth() / 2, imageIn.getHeight() / 2);

        Operation r2T2Sqrt = new Operation("R2T2Sqrt", "Math.hypot(polarCoordinate.getR(),polarCoordinate.getTheta())") {
            @Override
            public double operate(double r, double theta) {
                return Math.hypot(r, theta);
            }
        };

        Operation rOverTheta = new Operation("R/T", "(polarCoordinate.getR()/polarCoordinate.getTheta())") {
            @Override
            public double operate(double r, double theta) {
                return r / theta;
            }
        };

        Operation tOverR = new Operation("T/R", "(polarCoordinate.getTheta()/polarCoordinate.getR())") {
            @Override
            public double operate(double r, double theta) {
                return theta / r;
            }
        };

        //System.out.println(heightIn * (Math.PI * 4) / (R * widthIn));
        //System.exit(0);


        List<Operation> operations = new ArrayList<>();
        operations.add(r);
        operations.add(r2);
        operations.add(t2);
        operations.add(t);
        operations.add(rt);

        /*Guess guess2 = guessR(examples, operations, widthIn, heightIn, false, Guess.GuessOperator.SQUARE);
        System.out.println(guess2);
        System.exit(0);
*/
        operations.add(rOverTheta);
        //operations.add(tOverR);
        //operations.add(r2sqrt);
        //operations.add(r2T2Sqrt);

        Guess bestGuessR = null;
        Guess bestGuessT = null;

        List<Guess.GuessOperator> guessOperators = Arrays.asList(Guess.GuessOperator.values());

        long maxVotes = 0;
        for (Guess.GuessOperator gops : guessOperators) {
            logger.info("Trying Guess operator : {}", gops);
            for (int i = 1; i <= operations.size(); i++) {
                List<List<Operation>> combination = Combinations.combination(operations, i);
                for (int j = 0; j < combination.size(); j++) {
                    List<Operation> ops = combination.get(j);
                    Guess guess = guessR(examples, ops, widthIn, heightIn, false, gops);
                    if (guess.getVotes() > maxVotes) {
                        bestGuessR = guess;
                        maxVotes = guess.getVotes();
                    }
                }
            }
        }
        logger.info("R BEST Guess : {}", bestGuessR);
        logger.info("R code : {}", bestGuessR.getGeneratedCode());

        maxVotes = 0;
        for (Guess.GuessOperator gops : guessOperators) {
            for (int i = 1; i < operations.size(); i++) {
                List<List<Operation>> combination = Combinations.combination(operations, i);
                for (int j = 0; j < combination.size(); j++) {
                    List<Operation> ops = combination.get(j);
                    Guess guess = guessTheta(examples, ops, widthIn, heightIn, bestGuessR, gops);
                    if (guess.getVotes() > maxVotes) {
                        bestGuessT = guess;
                        maxVotes = guess.getVotes();
                    }
                }
            }
        }
        logger.info("T BEST Guess : {}", bestGuessT);

        System.out.println("#####\n");
        System.out.println(bestGuessR.getGeneratedCode());
        System.out.println(bestGuessT.getGeneratedCode());
        System.out.println("\n#####");

        System.exit(0);


        Operation variableCombinationR[] = {r2, r, t};//R,T,R2,T2,RT,C
        boolean constantR = false;

        Operation variableCombinationT[] = {t, r};//R,T,R2,T2,RT,C
        boolean constantT = false;

        List<Statistics> statisticsR = new ArrayList<>();
        List<Statistics> statisticsT = new ArrayList<>();

        for (Operation oR : variableCombinationR) {
            statisticsR.add(new Statistics(oR));
        }

        for (Operation oT : variableCombinationT) {
            statisticsT.add(new Statistics(oT));
        }

       /* Iterator<Double> thetaIterator = thetaExamples.keySet().iterator();
        Iterator<Double> rIterator = rExamples.keySet().iterator();*/

       /* while (thetaIterator.hasNext()) {
            double t=thetaIterator.next();
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs = thetaExamples.get(t);
            int size=pairs.size();
            double xR[][] = new double[size][variableCombinationR.length];
            double yR[] = new double[size];

            for (int j = 0; j < pairs.size(); j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                *//*Operations to R*//*
                for (int comb = 0; comb < variableCombinationR.length; comb++) {
                    xR[j][comb] = variableCombinationR[comb].operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }

                yR[j] = polarCoordinateD.getR();


            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
                boolean noRIntercept = !constantR;

                double[] synthesizeR = complexSynthesizer.synthesize(yR, xR, noRIntercept);
                if (synthesizeR != null) {
                    assert synthesizeR.length == variableCombinationR.length;
                    for (int l = 0; l < synthesizeR.length; l++) {
                        statisticsR.get(l).addValue(synthesizeR[l]);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in regression process", e);//no need to print stack trace
            }
        }

        //System.exit(0);

        while (rIterator.hasNext()) {
            double r=rIterator.next();
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs = rExamples.get(r);
            int size=pairs.size();

            if(size<=variableCombinationT.length+1){
                continue;
            }

            double xT[][] = new double[size][variableCombinationT.length+0];//+1 for Math.PI*n constant normalization
            double yT[] = new double[size];
            for (int j = 0; j < pairs.size(); j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                *//*Operations to T*//*
                for (int comb = 0; comb < variableCombinationT.length; comb++) {
                    xT[j][comb] = variableCombinationT[comb].operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }
                //xT[j][variableCombinationT.length] = 2*Math.PI;//to avoid the error due to normalizing angle to 360deg
                yT[j] = polarCoordinateD.getTheta();
                //   System.out.println(polarCoordinateD.getTheta()+":"+(polarCoordinateS.getR()+polarCoordinateS.getTheta()));
            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
                //assuming T has no coefficient term. Might have to change in future
                double[] synthesizeT = complexSynthesizer.synthesize(yT, xT, true);
                if (synthesizeT != null) {
                    *//*double normalizedValue = synthesizeT[synthesizeT.length - 1]*Math.PI*2;//PI
                    for (int k = 0; k < yT.length; k++) {
                        yT[k] -= normalizedValue;
                    }
                    double xTnew[][] = new double[xT.length][xT[0].length - 1];
                    for (int k = 0; k < xTnew.length; k++) {
                        for (int l = 0; l < xT[0].length - 1; l++) {
                            xTnew[k][l] = xT[k][l];
                        }
                    }
                    synthesizeT = complexSynthesizer.synthesize(yT, xTnew, true);*//*
                    if (synthesizeT != null) {
                        for (int l = 0; l < synthesizeT.length; l++) {
                            statisticsT.get(l).addValue(synthesizeT[l]);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error in regression process", e);//no need to print stack trace
            }
        }
*/

      /*  int window = 64;//(int) Math.sqrt(width * height);
        for (int i = 0; i < examples.size() - window; i += window) {
            int size = window;
            double xR[][] = new double[size][variableCombinationR.length];
            double yR[] = new double[size];

            double xT[][] = new double[size][variableCombinationT.length + 1];//+1 for Math.PI*n constant normalization
            double yT[] = new double[size];
            for (int j = i; j < i + size; j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                *//*Operations to R*//*
                for (int comb = 0; comb < variableCombinationR.length; comb++) {
                    xR[j - i][comb] = variableCombinationR[comb].operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }

                yR[j - i] = polarCoordinateD.getR();

                *//*Operations to T*//*
                for (int comb = 0; comb < variableCombinationT.length; comb++) {
                    xT[j - i][comb] = variableCombinationT[comb].operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }
                xT[j - i][variableCombinationT.length] = Math.PI;//to avoid the error due to normalizing angle to 360deg
                yT[j - i] = polarCoordinateD.getTheta();
                //   System.out.println(polarCoordinateD.getTheta()+":"+(polarCoordinateS.getR()+polarCoordinateS.getTheta()));
            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
                boolean noRIntercept = !constantR;

                double[] synthesizeR = complexSynthesizer.synthesize(yR, xR, noRIntercept, true);
                if (synthesizeR != null) {
                    assert synthesizeR.length == variableCombinationR.length;
                    for (int l = 0; l < synthesizeR.length; l++) {
                        statisticsR.get(l).addValue(synthesizeR[l]);
                    }
                }

                //assuming T has no coefficient term. Might have to change in future
                double[] synthesizeT = complexSynthesizer.synthesize(yT, xT, true, false);
                if (synthesizeT != null) {
                    double normalizedValue = synthesizeT[synthesizeT.length - 1] * Math.PI;//PI
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
                    if (synthesizeT != null) {
                        for (int l = 0; l < synthesizeT.length; l++) {
                            statisticsT.get(l).addValue(synthesizeT[l]);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error in regression process", e);//no need to print stack trace
            }
        }

        *//*Approximating R coefficients*//*
        for (Statistics sR : statisticsR) {
            logger.debug(sR.toString());
        }

        GuessesGenerator guessesGeneratorR = new GuessesGenerator(statisticsR);
        logger.debug("Total iterations : {}", guessesGeneratorR.getTotalIterations());

        Guess approximateR = ApproximatorNew.approximate(guessesGeneratorR, examples, true, null, widthIn, heightIn);
        logger.debug("R : {}", approximateR);
        System.out.println("\n" + approximateR.getGeneratedCode());

        *//*Approximating T coefficients*//*
        for (Statistics sT : statisticsT) {
            logger.debug(sT.toString());
        }
        GuessesGenerator guessesGeneratorT = new GuessesGenerator(statisticsT);
        logger.debug("Total iterations : {}", guessesGeneratorT.getTotalIterations());
        Guess approximateT = ApproximatorNew.approximate(guessesGeneratorT, examples, false, approximateR, widthIn, heightIn);
        logger.debug("T : {}", approximateT);
        System.out.println("\n" + approximateT.getGeneratedCode());*/


    }

    private Guess guessR(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples, List<Operation> variableCombinationR, int widthIn, int heightIn, boolean constantR, Guess.GuessOperator guessOperator) {
        List<Statistics> statisticsR = new ArrayList<>();
        for (Operation oR : variableCombinationR) {
            statisticsR.add(new Statistics(oR));
        }
        if (constantR) {//todo move this to upper part
            statisticsR.add(new Statistics(new ConstantOperation("constant")));
        }
        int window = 64;//(int) Math.sqrt(width * height);
        for (int i = 0; i < examples.size() - window; i += window) {
            int size = window;
            double xR[][] = new double[size][variableCombinationR.size()];
            double yR[] = new double[size];

            for (int j = i; j < i + size; j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                /*Operations to R*/
                for (int comb = 0; comb < variableCombinationR.size(); comb++) {
                    xR[j - i][comb] = variableCombinationR.get(comb).operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }
                if (guessOperator != null) {
                    yR[j - i] = guessOperator.operate(polarCoordinateD.getR());
                } else {
                    yR[j - i] = polarCoordinateD.getR();
                }
            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();
                boolean noRIntercept = !constantR;

                double[] synthesizeR = complexSynthesizer.synthesize(yR, xR, noRIntercept, true);
                //System.out.println(synthesizeR[0]);
                if (synthesizeR != null) {
                    assert synthesizeR.length == variableCombinationR.size();
                    for (int l = 0; l < synthesizeR.length; l++) {
                        statisticsR.get(l).addValue(synthesizeR[l]);
                    }
                }
            } catch (Exception e) {
                //logger.error("Error in regression process", e);//no need to print stack trace
            }
        }

        /*Approximating R coefficients*/
        for (Statistics sR : statisticsR) {
            logger.debug(sR.toString());
        }

        GuessesGenerator guessesGeneratorR = new GuessesGenerator(statisticsR);
        guessesGeneratorR.setGuessOperator(guessOperator);
        logger.debug("Total iterations : {}", guessesGeneratorR.getTotalIterations());
        if (guessesGeneratorR.getTotalIterations().longValue() > 1000000 || guessesGeneratorR.getTotalIterations().longValue() < 0) {
            logger.info("Too much iterations. Skipping.");
            return new Guess();
        }

        Guess approximateR = ApproximatorNew.approximate(guessesGeneratorR, examples, true, null, widthIn, heightIn);
        logger.debug("R : {}", approximateR);
        return approximateR;
    }

    private Guess guessTheta(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples, List<Operation> variableCombinationT, int widthIn, int heightIn, Guess rGuess, Guess.GuessOperator guessOperator) {
        List<Statistics> statisticsT = new ArrayList<>();
        for (Operation oT : variableCombinationT) {
            statisticsT.add(new Statistics(oT));
        }
        int window = 64;//(int) Math.sqrt(width * height);
        for (int i = 0; i < examples.size() - window; i += window) {
            int size = window;

            double xT[][] = new double[size][variableCombinationT.size() + 0];//+1 for Math.PI*n constant normalization
            double yT[] = new double[size];
            for (int j = i; j < i + size; j++) {
                Pair<CartesianCoordinate, CartesianCoordinate> p = examples.get(j);

                PolarCoordinate polarCoordinateS = CoordinateTransformer.cartesian2Polar(p.first);
                PolarCoordinate polarCoordinateD = CoordinateTransformer.cartesian2Polar(p.second);

                /*Operations to T*/
                for (int comb = 0; comb < variableCombinationT.size(); comb++) {
                    xT[j - i][comb] = variableCombinationT.get(comb).operate(polarCoordinateS.getR(), polarCoordinateS.getTheta());
                }
                //xT[j - i][variableCombinationT.size()] = Math.PI;//to avoid the error due to normalizing angle to 360deg
                if (guessOperator != null) {
                    yT[j - i] = guessOperator.operate(polarCoordinateD.getTheta());
                } else {
                    yT[j - i] = polarCoordinateD.getTheta();
                }
                //yT[j - i] = polarCoordinateD.getTheta();
                //   System.out.println(polarCoordinateD.getTheta()+":"+(polarCoordinateS.getR()+polarCoordinateS.getTheta()));
            }
            try {
                ComplexSynthesizer complexSynthesizer = new ComplexSynthesizer();

                //assuming T has no coefficient term. Might have to change in future
                double[] synthesizeT = complexSynthesizer.synthesize(yT, xT, true, false);
                if (synthesizeT != null) {
                    /*double normalizedValue = synthesizeT[synthesizeT.length - 1] * Math.PI;//PI
                    for (int k = 0; k < yT.length; k++) {
                        yT[k] -= normalizedValue;
                    }
                    double xTnew[][] = new double[xT.length][xT[0].length - 1];
                    for (int k = 0; k < xTnew.length; k++) {
                        for (int l = 0; l < xT[0].length - 1; l++) {
                            xTnew[k][l] = xT[k][l];
                        }
                    }
                    synthesizeT = complexSynthesizer.synthesize(yT, xTnew, true, false);*/
                    if (synthesizeT != null) {
                        for (int l = 0; l < synthesizeT.length; l++) {
                            statisticsT.get(l).addValue(synthesizeT[l]);
                        }
                    }
                }
            } catch (Exception e) {
                //logger.error("Error in regression process", e);//no need to print stack trace
            }
        }

        GuessesGenerator guessesGeneratorT = new GuessesGenerator(statisticsT);
        guessesGeneratorT.setGuessOperator(guessOperator);
        logger.debug("Total iterations : {}", guessesGeneratorT.getTotalIterations());
        if (guessesGeneratorT.getTotalIterations().longValue() > 1000000 || guessesGeneratorT.getTotalIterations().longValue() < 0) {
            logger.info("Too much iterations. Skipping.");
            return new Guess();
        }

        Guess approximateT = ApproximatorNew.approximate(guessesGeneratorT, examples, false, rGuess, widthIn, heightIn);
        logger.debug("T : {}", approximateT);
        return approximateT;
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

