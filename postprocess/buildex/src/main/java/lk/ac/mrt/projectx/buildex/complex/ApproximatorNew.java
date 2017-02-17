package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.operations.Guess;
import lk.ac.mrt.projectx.buildex.complex.operations.GuessesGenerator;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author Chathura Widanage
 */
public class ApproximatorNew {
    private static final Logger logger = LogManager.getLogger(ApproximatorNew.class);

    public static Guess approximate(final GuessesGenerator guessesGenerator, final List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs, final boolean isR, Guess rGuess, final int width, final int height) {
        final List<Pair<CartesianCoordinate, CartesianCoordinate>> tests = getTestCases(pairs);
        GuessesValidationServiceNew gvs = new GuessesValidationServiceNew(tests, width, height, isR, rGuess);
        while (guessesGenerator.hasNext()) {
            Guess guess = guessesGenerator.next();
            try {
                gvs.submit(guess);
            } catch (InterruptedException e) {
                logger.error("Interrupted while submitting a guess", e);
            }
        }

        try {
            logger.debug("Waiting for test termination");
            List<Guess> maxVoters = gvs.awaitTermination();
            //logger.debug("Maximum voters {}", maxVoters);
            logger.debug("Maximum votes is {} out of {}", maxVoters.get(0).getVotes(), tests.size());
            return maxVoters.get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<Pair<CartesianCoordinate, CartesianCoordinate>> getTestCases(
            List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs) {
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

        List<Pair<CartesianCoordinate, CartesianCoordinate>> testCases = new ArrayList<>();
        testCases.addAll(q1.subList(0, Math.min(26, q1.size())));
        testCases.addAll(q2.subList(0, Math.min(26, q1.size())));
        testCases.addAll(q3.subList(0, Math.min(26, q1.size())));
        testCases.addAll(q4.subList(0, Math.min(26, q1.size())));

        return testCases;
    }
}
