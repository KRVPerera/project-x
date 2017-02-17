package lk.ac.mrt.projectx.buildex.complex.operations;

import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class Guess {
    private List<Pair<Operation, Double>> guesses = new ArrayList<>();

    private long votes;

    private GuessOperator guessOperator;

    public GuessOperator getGuessOperator() {
        return guessOperator;
    }

    public void setGuessOperator(GuessOperator guessOperator) {
        this.guessOperator = guessOperator;
    }

    public long getVotes() {
        return votes;
    }

    public void incrementVote() {
        votes++;
    }

    public void addGuess(Pair<Operation, Double> guess) {
        guesses.add(guess);
    }

    public double getProcessedValue(double r, double theta) {
        double sum = 0;
        for (Pair<Operation, Double> guess : guesses) {
            sum += (guess.first.operate(r, theta) * guess.second);
        }
        return sum;
    }

    public String getGeneratedCode() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < guesses.size(); i++) {
            Pair<Operation, Double> p = guesses.get(i);
            stringBuilder.append(p.second + "*" + p.first.getCode());
            if (i != guesses.size() - 1) {
                stringBuilder.append("+");
            }
        }
        if (this.guessOperator != null) {
            return String.format(this.guessOperator.operation, stringBuilder.toString());
        }

        return stringBuilder.append(";").toString();
    }

    public enum GuessOperator {//should do the inverse of each operator
        NONE("%s"), TAN("Math.atan(%s)"), //ATAN("Math.tan(%s)"),
        SQUARE("Math.sqrt(%s)");//,
        /* SQRT("Math.pow(%s,2)"),
         SIN("Math.asin(%s)"),
         COS("Math.acos(%s)");*/
        String operation;

        GuessOperator(String operation) {
            this.operation = operation;
        }

        public double operate(double val) {
            if (this.equals(SQUARE)) {
                return Math.pow(val, 2);
            } else if (this.equals(TAN)) {
                return Math.tan(val);
            }/* else if (this.equals(ATAN)) {
                return Math.atan(val);
            }*//* else if (this.equals(SQRT)) {
                return Math.sqrt(val);
            } else if (this.equals(SIN)) {
                return Math.sin(val);
            } else if (this.equals(COS)) {
                return Math.cos(val);
            }*/ else {
                return val;
            }
        }

        public double operateInv(double val) {
            if (this.equals(SQUARE)) {
                return Math.sqrt(val);
            } else if (this.equals(TAN)) {
                return Math.atan(val);
            }/*else if (this.equals(ATAN)) {
                return Math.tan(val);
            }*/ /*else if (this.equals(SQRT)) {
                return Math.pow(val, 2);
            } else if (this.equals(SIN)) {
                return Math.asin(val);
            } else if (this.equals(COS)) {
                return Math.acos(val);
            }*/ else {
                return val;
            }
        }

        @Override
        public String toString() {
            return this.operation;
        }
    }

    @Override
    public String toString() {
        return "Guess{" +
                "guesses=" + guesses +
                ", votes=" + votes +
                '}';
    }
}
