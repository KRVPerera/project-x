package lk.ac.mrt.projectx.buildex.complex.operations;

import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class Guess {
    private List<Pair<Operand, Double>> guesses = new ArrayList<>();
    private List<ParameterGuess> guessesParams = new ArrayList<>();

    private long votes;

    private OperandDecorator guessOperator;

    public OperandDecorator getGuessOperator() {
        return guessOperator;
    }

    public List<ParameterGuess> getGuessesParams() {
        return guessesParams;
    }

    public void setGuessOperator(OperandDecorator guessOperator) {
        this.guessOperator = guessOperator;
    }

    public long getVotes() {
        return votes;
    }

    public void incrementVote() {
        votes++;
    }

    public void addGuess(Pair<Operand, Double> guess) {
        guesses.add(guess);
    }

    public double getProcessedValue(double r, double theta) {
        double sum = 0;
        for (Pair<Operand, Double> guess : guesses) {
            sum += (guess.first.operate(r, theta) * guess.second);
        }
        return sum;
    }

    public String getGeneratedCode() {
        return getGeneratedCode(false);
    }

    public String getGeneratedCode(boolean withParams) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < guesses.size(); i++) {
            Pair<Operand, Double> p = guesses.get(i);
            stringBuilder.append("(");

            if (guessesParams.get(i) == null || !withParams) {
                stringBuilder.append(p.second).append("f");
            } else {
                stringBuilder.append(guessesParams.get(i).generateCode());
            }
            stringBuilder.append("*")
                    .append(p.first.getCode())
                    .append(")");
            if (i != guesses.size() - 1) {
                stringBuilder.append("+");
            }
        }
        if (this.guessOperator != null) {
            return String.format(this.guessOperator.getCode(), stringBuilder.toString());
        }
        stringBuilder.append(";");

        return stringBuilder.toString();
    }

    public List<Pair<Operand, Double>> getGuesses() {
        return guesses;
    }

    @Override
    public String toString() {
        return "Guess{" +
                "guesses=" + guesses +
                ", votes=" + votes +
                '}';
    }
}
