package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.complex.langs.HalideGenerator;
import lk.ac.mrt.projectx.buildex.complex.operations.*;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.math.BigInteger;
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

        Operand r = new Operand("R", "r_in") {
            @Override
            public double operate(double r, double theta) {
                return r;
            }
        };
        Operand r2 = new Operand("R2", "pow(r_in,2)") {
            @Override
            public double operate(double r, double theta) {
                return r * r;
            }
        };

        Operand r2sqrt = new Operand("r2sqrt", "(r_in+(1-sqrt(1-(pow(r_in,2)))))") {
            @Override
            public double operate(double r, double theta) {
                return (r + (1 - (Math.sqrt(1 - r * r))));
            }
        };

        Operand rt = new Operand("RT", "(r_in*theta_in)") {
            @Override
            public double operate(double r, double theta) {
                return theta * r;
            }
        };

        final Operand t = new Operand("T", "theta_in") {
            @Override
            public double operate(double r, double theta) {
                return theta;
            }
        };

        Operand t2 = new Operand("T2", "pow(theta_in,2)") {
            @Override
            public double operate(double r, double theta) {
                return theta * theta;
            }
        };

        Operand r2T2Sqrt = new Operand("R2T2Sqrt", "hypot(r_in,theta_in)") {
            @Override
            public double operate(double r, double theta) {
                return Math.hypot(r, theta);
            }
        };

        Operand rOverTheta = new Operand("R/T", "(r_in/theta_in)") {
            @Override
            public double operate(double r, double theta) {
                return r / theta;
            }
        };

        Operand tOverR = new Operand("T/R", "(theta_in/r_in)") {
            @Override
            public double operate(double r, double theta) {
                return theta / r;
            }
        };

        Operand widthOp = new Operand("width", "(width/2)") {
            @Override
            public double operate(double r, double theta) {
                return widthIn / 2;
            }
        };


        Operand widthOpSqr = new Operand("width", "(pow(width/2),2)") {
            @Override
            public double operate(double r, double theta) {
                return Math.pow(widthIn / 2, 2);
            }
        };

        Operand widthTheta = new Operand("widthTheta", "(width*theta_in/2)") {
            @Override
            public double operate(double r, double theta) {
                return theta * (widthIn / 2);
            }
        };

        Operand widthThetaSqr = new Operand("widthTheta", "(pow(width*theta_in/2,2))") {
            @Override
            public double operate(double r, double theta) {
                return Math.pow(theta * (widthIn / 2), 2);
            }
        };

        //System.out.println(heightIn * (Math.PI * 4) / (R * widthIn));
        //System.exit(0);


        List<Operand> operands = new ArrayList<>();
        //operands.add(r);
        operands.add(r2);
        operands.add(t2);
        //operands.add(t);
        //operands.add(rt);
        operands.add(rOverTheta);
        //operations.add(tOverR);
        //operations.add(r2sqrt);
        //operations.add(r2T2Sqrt);
        //operands.add(widthOp);
        //operands.add(widthOpSqr);
        //operands.add(widthTheta);
        //operands.add(widthThetaSqr);

        Attribute one = new Attribute("one", "1f", 1);
        Attribute negOne = new Attribute("negative_one", "(-1f)", -1);
        Attribute width = new Attribute("width", "width", widthIn);
        Attribute height = new Attribute("height", "height", heightIn);
        Attribute maxR = new Attribute("maxR", "(hypot(width/2,height/2))", Math.hypot(widthIn / 2, heightIn / 2));
        Attribute pi = new Attribute("maxR", "(M_PI)", Math.PI);
        Attribute userAttr = new Attribute("attribute1", "(4)", 4);


        List<Attribute> attributes = new ArrayList<>();
        attributes.add(one);
        attributes.add(negOne);
        attributes.add(width);
        attributes.add(height);
        attributes.add(maxR);
        attributes.add(pi);
        attributes.add(userAttr);

        List<OperandDecorator> operandDecorators = new ArrayList<>();

        OperandDecorator none = new OperandDecorator("%s") {
            @Override
            public double operate(double val) {
                return val;
            }

            @Override
            public double operateInv(double val) {
                return val;
            }
        };

        OperandDecorator sqr = new OperandDecorator("sqrt(%s)") {
            @Override
            public double operate(double val) {
                return Math.pow(val, 2);
            }

            @Override
            public double operateInv(double val) {
                return Math.sqrt(val);
            }
        };

        OperandDecorator sqrt = new OperandDecorator("pow(%s,2)") {
            @Override
            public double operate(double val) {
                return Math.sqrt(val);
            }

            @Override
            public double operateInv(double val) {
                return Math.pow(val, 2);
            }
        };

        OperandDecorator tan = new OperandDecorator("atan(%s)") {
            @Override
            public double operate(double val) {
                return Math.tan(val);
            }

            @Override
            public double operateInv(double val) {
                return Math.atan(val);
            }
        };

        operandDecorators.add(none);
        operandDecorators.add(sqr);
        operandDecorators.add(sqrt);
        operandDecorators.add(tan);

        Parameterization parameterization = new Parameterization(attributes, operandDecorators);

        Guess bestGuessR = null;
        Guess bestGuessT = null;

        GuessesValidationServiceNew gvs = new GuessesValidationServiceNew(
                getTestCases(examples, null), widthIn, heightIn, true, null
        );//now using same gvs for all guesses, reducing thread creations and making guesses stop if it goes below current best

        for (OperandDecorator operandDecorator : operandDecorators) {
            logger.info("Trying Guess operator : {}", operandDecorator);
            for (int i = 1; i <= operands.size(); i++) {
                List<List<Operand>> combination = Combinations.combination(operands, i);
                for (int j = 0; j < combination.size(); j++) {
                    List<Operand> ops = combination.get(j);
                    guessR(examples, ops, false, operandDecorator, gvs);
                    logger.info("Current max voters : {}", gvs.getMaxVoters());
                    /*if (guess.getVotes() > maxVotes) {
                        bestGuessR = guess;
                        maxVotes = guess.getVotes();
                    }*/
                }
            }
        }
        try {
            List<Guess> guesses = gvs.awaitTermination();
            if (guesses.size() > 1) {
                logger.info("Tie breaking {} guesses", guesses.size());
                logger.info("Max voted guesses {}", guesses);
                gvs = new GuessesValidationServiceNew(
                        getTestCases(examples, examples.size() / 4), widthIn, heightIn, true, null
                );
                for (Guess guess : guesses) {
                    gvs.submit(guess);
                }
                guesses = gvs.awaitTermination();
                logger.info("{} guesses after tie breaking", guesses.size());
            }
            bestGuessR = guesses.get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("R BEST Guess : {}", bestGuessR);
        parameterization.parameterize(bestGuessR);

        logger.info("R code : {}", bestGuessR.getGeneratedCode());
        //logger.info(Math.sqrt(examples.size()));
        //System.exit(0);

        gvs = new GuessesValidationServiceNew(
                getTestCases(examples, null), widthIn, heightIn, false, bestGuessR
        );

        for (OperandDecorator operandDecorator : operandDecorators) {
            for (int i = 1; i <= operands.size(); i++) {
                List<List<Operand>> combination = Combinations.combination(operands, i);
                for (int j = 0; j < combination.size(); j++) {
                    List<Operand> ops = combination.get(j);
                    guessTheta(examples, ops, false, operandDecorator, gvs);
                    logger.info("Current max voters : {}", gvs.getMaxVoters());
                   /* if (guess.getVotes() > maxVotes) {
                        bestGuessT = guess;
                        maxVotes = guess.getVotes();
                    }*/
                }
            }
        }

        try {
            List<Guess> guesses = gvs.awaitTermination();
            bestGuessT = guesses.get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        parameterization.parameterize(bestGuessT);

        logger.info("T BEST Guess : {}", bestGuessT);

        System.out.println("#####\n");
        System.out.println(bestGuessR.getGeneratedCode());
        System.out.println(bestGuessT.getGeneratedCode());
        System.out.println("\n#####");

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HalideGenerator halideGenerator = new HalideGenerator(bestGuessR, bestGuessT);
        halideGenerator.generate();
    }

    /**
     * Returns the current best guess
     */
    private void guessR(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples,
                        List<Operand> variableCombinationR, boolean constant,
                        OperandDecorator guessOperator, GuessesValidationServiceNew gvs) {
        List<Statistics> statisticsR = new ArrayList<>();
        for (Operand oR : variableCombinationR) {
            statisticsR.add(new Statistics(oR));
        }
        if (constant) {//todo move this to upper part
            statisticsR.add(0, new Statistics(new ConstantOperand("constant")));
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
                boolean noRIntercept = !constant;

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
        logger.debug(statisticsR);

        GuessesGenerator guessesGeneratorR = new GuessesGenerator(statisticsR);
        guessesGeneratorR.setGuessOperator(guessOperator);
        BigInteger totalIterations = guessesGeneratorR.getTotalIterations();
        logger.info("Total iterations : {}", totalIterations);
        if (guessesGeneratorR.getTotalIterations().compareTo(BigInteger.valueOf(100000)) > 0 || totalIterations.compareTo(BigInteger.ZERO) <= 0) {
            logger.info("Too much iterations. Skipping.");
            //return new Guess();
        } else {
            ApproximatorNew.approximate(guessesGeneratorR, gvs);
        }
/*
        Guess approximateR = ApproximatorNew.approximate(guessesGeneratorR, gvs);
        logger.debug("R : {}", approximateR);
        return approximateR;*/
    }

    //todo tow seperate methods for guessR and guessT, since some times they are evaluated differently. (ie : Math.PI adjustment)
    private void guessTheta(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples,
                            List<Operand> variableCombinationT, boolean constant,
                            OperandDecorator guessOperator, GuessesValidationServiceNew gvs) {
        List<Statistics> statisticsT = new ArrayList<>();
        for (Operand oT : variableCombinationT) {
            statisticsT.add(new Statistics(oT));
        }
        if (constant) {//todo move this to upper part
            statisticsT.add(0, new Statistics(new ConstantOperand("constant")));
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

        logger.debug(statisticsT);

        GuessesGenerator guessesGeneratorT = new GuessesGenerator(statisticsT);
        guessesGeneratorT.setGuessOperator(guessOperator);
        BigInteger totalIterations = guessesGeneratorT.getTotalIterations();
        logger.debug("Total iterations : {}", totalIterations);
        if (guessesGeneratorT.getTotalIterations().compareTo(BigInteger.valueOf(100000)) > 0 || totalIterations.compareTo(BigInteger.ZERO) <= 0) {
            logger.info("Too much iterations. Skipping.");
            //return new Guess();
        } else {
            ApproximatorNew.approximate(guessesGeneratorT, gvs);
        }/*

        Guess approximateT = ApproximatorNew.approximate(guessesGeneratorT, gvs);
        logger.debug("T : {}", approximateT);
        return approximateT;*/
    }

    private List<Pair<CartesianCoordinate, CartesianCoordinate>> getTestCases(
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs,
            Integer size
    ) {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> q1 = new ArrayList<>();
        List<Pair<CartesianCoordinate, CartesianCoordinate>> q2 = new ArrayList<>();
        List<Pair<CartesianCoordinate, CartesianCoordinate>> q3 = new ArrayList<>();
        List<Pair<CartesianCoordinate, CartesianCoordinate>> q4 = new ArrayList<>();
        for (Pair<CartesianCoordinate, CartesianCoordinate> p : pairs) {
            CartesianCoordinate first = p.first;
            if (first.getX() > 0 && first.getY() > 0) {
                q1.add(p);
            } else if (first.getX() > 0 && first.getY() < 0) {
                q4.add(p);
            } else if (first.getX() < 0 && first.getY() > 0) {
                q2.add(p);
            } else {
                q3.add(p);
            }
        }

        Collections.shuffle(q1);
        Collections.shuffle(q2);
        Collections.shuffle(q3);
        Collections.shuffle(q4);


        int limit;
        if (size == null) {
            limit = (int) (Math.sqrt(pairs.size() * 2) / 4);
        } else {
            limit = size / 4;
        }
        List<Pair<CartesianCoordinate, CartesianCoordinate>> testCases = new ArrayList<>();
        if (!q1.isEmpty())
            testCases.addAll(q1.subList(0, Math.min(limit, q1.size() - 1)));

        if (!q2.isEmpty())
            testCases.addAll(q2.subList(0, Math.min(limit, q2.size() - 1)));

        if (!q3.isEmpty())
            testCases.addAll(q3.subList(0, Math.min(limit, q3.size() - 1)));

        if (!q4.isEmpty())
            testCases.addAll(q4.subList(0, Math.min(limit, q4.size() - 1)));

        return testCases;
    }

    private int loopBound(double value) {
        return (int) (value * 1000);
    }

    private double round(double value) {
        return (double) Math.round(value * 1000d) / 1000d;
    }


}

