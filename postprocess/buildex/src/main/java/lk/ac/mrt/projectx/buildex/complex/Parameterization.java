package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.operations.*;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class Parameterization {
    private final static Logger logger = LogManager.getLogger(Parameterization.class);

    private List<Attribute> attributes;
    private List<OperandDecorator> operandDecorators;

    public Parameterization(List<Attribute> attributeList, List<OperandDecorator> operandDecorators) {
        this.attributes = attributeList;
        this.operandDecorators = operandDecorators;
    }

    public void parameterize(Guess guess) {
        List<ParameterGuess> guessesParams = guess.getGuessesParams();
        outer:
        for (int i = 0; i < guess.getGuesses().size(); i++) {
            ArrayList<ParameterGuess> parameterGuesses = new ArrayList<>();
            Pair<Operand, Double> gs = guess.getGuesses().get(i);
            for (int neumeratorCoefs = 1; neumeratorCoefs <= attributes.size(); neumeratorCoefs++) {
                List<List<Attribute>> combinationNum = Combinations.combination(attributes, neumeratorCoefs);
                for (List<Attribute> num : combinationNum) {
                    for (int denominatorCoefs = 1; denominatorCoefs <= attributes.size(); denominatorCoefs++) {
                        List<List<Attribute>> combinationDenum = Combinations.combination(attributes, denominatorCoefs);
                        for (List<Attribute> den : combinationDenum) {
                            for (OperandDecorator decorator : operandDecorators) {
                                double numMul = mul(num);
                                double denMul = mul(den);
                                //System.out.println(decorator.operate(gs.second)+":"+(numMul / denMul)+"    "+decorator.toString());
                                if (Math.abs(((gs.second)) - decorator.operateInv(numMul / denMul)) < thresholdValue(gs.second)) {
                                    ParameterGuess parameterGuess = new ParameterGuess();
                                    parameterGuess.setNumerator(num);
                                    parameterGuess.setDenominator(den);
                                    parameterGuess.setDecorator(decorator);
                                    parameterGuess.setError(Math.abs((gs.second) - decorator.operateInv(numMul / denMul)));
                                    parameterGuesses.add(parameterGuess);
                                }
                            }
                        }
                    }
                }
            }
            if (parameterGuesses.isEmpty()) {
                guessesParams.add(null);
            } else {
                //logger.info("Before sort : {}", parameterGuesses);

                Collections.sort(parameterGuesses);
                logger.info("After sort : {}", parameterGuesses.subList(0, 10));
                ParameterGuess parameterGuess = parameterGuesses.get(0);
                System.out.println(gs.second + "=" + String.format(parameterGuess.getDecorator().toString(),
                        codeGen(parameterGuess.getNumerator()) + "/" + codeGen(parameterGuess.getDenominator())) + " : " + (mul(parameterGuess.getNumerator()) / mul(parameterGuess.getDenominator())));
                guessesParams.add(parameterGuess);
            }
        }
    }


    public double thresholdValue(double val) {
        double absVal = Math.abs(val);
        String valString = new Double(absVal).toString();
        if (absVal >= 0.9) {
            int length = valString.substring(0, valString.indexOf('.')).length();
            return Math.pow(10, length - 2);
        } else {
            int length = valString.substring(valString.indexOf('.'), valString.length()).length();
            return Math.pow(10, -length + 1);
        }
    }

  /*  public List<> parameterizeTest(Guess guess) {
        List<ParameterGuess> guessesParams = guess.getGuessesParams();
        outer:
        for (int i = 0; i < guess.getGuesses().size(); i++) {
            ArrayList<ParameterGuess> parameterGuesses = new ArrayList<>();
            Pair<Operand, Double> gs = guess.getGuesses().get(i);
            for (int neumeratorCoefs = 1; neumeratorCoefs <= attributes.size(); neumeratorCoefs++) {
                List<List<Attribute>> combinationNum = Combinations.combination(attributes, neumeratorCoefs);
                for (List<Attribute> num : combinationNum) {
                    for (int denominatorCoefs = 1; denominatorCoefs <= attributes.size(); denominatorCoefs++) {
                        List<List<Attribute>> combinationDenum = Combinations.combination(attributes, denominatorCoefs);
                        for (List<Attribute> den : combinationDenum) {
                            for (OperandDecorator decorator : operandDecorators) {
                                double numMul = mul(num);
                                double denMul = mul(den);
                                //System.out.println(decorator.operate(gs.second)+":"+(numMul / denMul)+"    "+decorator.toString());
                                if (Math.abs(((gs.second)) - decorator.operateInv(numMul / denMul)) < 0.1) {
                                    ParameterGuess parameterGuess = new ParameterGuess();
                                    parameterGuess.setNumerator(num);
                                    parameterGuess.setDenominator(den);
                                    parameterGuess.setDecorator(decorator);
                                    parameterGuess.setError(Math.abs((gs.second) - decorator.operateInv(numMul / denMul)));
                                    parameterGuesses.add(parameterGuess);
                                }
                            }
                        }
                    }
                }
            }
            if (parameterGuesses.isEmpty()) {
                guessesParams.add(null);
            } else {
                //logger.info("Before sort : {}", parameterGuesses);

                Collections.sort(parameterGuesses);
                //logger.info("After sort : {}", parameterGuesses.subList(0, 10));
                ParameterGuess parameterGuess = parameterGuesses.get(0);
                System.out.println(gs.second + "=" + String.format(parameterGuess.getDecorator().toString(),
                        codeGen(parameterGuess.getNumerator()) + "/" + codeGen(parameterGuess.getDenominator())) + " : " + (mul(parameterGuess.getNumerator()) / mul(parameterGuess.getDenominator())));
                guessesParams.add(parameterGuess);
            }
        }
    }*/


    private double round(double value) {
        return (double) Math.round(value * 100d) / 100d;
    }

    private double round(double value, int zeros) {
        double mul = Math.pow(10, zeros);
        return (double) Math.round(value * mul) / mul;
    }

    private double mul(List<Attribute> atts) {
        double val = 1;
        for (Attribute a : atts) {
            val *= a.getValue();
        }
        return val;
    }

    public String codeGen(List<Attribute> atts) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < atts.size(); i++) {
            sb.append(atts.get(i).getCode());
            if (i != atts.size() - 1) {
                sb.append("*");
            }
        }
        return sb.append(")").toString();
    }
}
