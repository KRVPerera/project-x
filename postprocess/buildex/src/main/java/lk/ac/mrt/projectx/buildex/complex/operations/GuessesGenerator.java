package lk.ac.mrt.projectx.buildex.complex.operations;

import lk.ac.mrt.projectx.buildex.models.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class GuessesGenerator {
    private List<Integer> currentValues = new ArrayList<>();
    private List<Statistics> statisticsList;

    private boolean hasNext = true;

    private Guess.GuessOperator guessOperator;

    public GuessesGenerator(List<Statistics> statisticsList) {
        this.statisticsList = statisticsList;
        for (Statistics s : statisticsList) {
            currentValues.add(s.getLow());
        }
    }

    public void setGuessOperator(Guess.GuessOperator guessOperator) {
        this.guessOperator = guessOperator;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public Guess next() {
        if (!hasNext) {
            return null;
        }
        /*Creating teh new guess from current values*/
        Guess guess = new Guess();
        if (guessOperator != null) {
            guess.setGuessOperator(guessOperator);
        }

        for (int i = 0; i < statisticsList.size(); i++) {
            guess.addGuess(new Pair<>(statisticsList.get(i).getOperation(),
                    currentValues.get(i) * 1.0d / 1000d));//todo make 1000 a parameter
        }
        hasNext = false;
        /*Generate the next set of values*/
        for (int i = 0; i < statisticsList.size(); i++) {
            Statistics s = statisticsList.get(i);
            if (currentValues.get(i) < s.getHigh()) {
                currentValues.set(i, currentValues.get(i) + 1);
                hasNext = true;
                break;
            } else {
                currentValues.set(i, s.getLow());
            }
        }
        return guess;
    }

    public BigInteger getTotalIterations() {
        BigInteger totalIterations = BigInteger.ONE;
        for (Statistics s : statisticsList) {
            totalIterations = totalIterations.multiply(BigInteger.valueOf(s.getTotalIterations()));
        }
        return totalIterations;
    }
}
