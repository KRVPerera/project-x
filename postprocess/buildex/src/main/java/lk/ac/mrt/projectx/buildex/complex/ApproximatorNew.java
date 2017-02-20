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

    public static void approximate(GuessesGenerator guessesGenerator, GuessesValidationServiceNew gvs) {
        gvs.newBatch();
        while (guessesGenerator.hasNext()) {
            Guess guess = guessesGenerator.next();
            try {
                if (guess != null) {
                    gvs.submit(guess);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while submitting a guess", e);
            }
        }/*

        try {
            logger.debug("Waiting for test termination");
            List<Guess> maxVoters = gvs.awaitTermination();
            //logger.debug("Maximum voters {}", maxVoters);
            logger.debug("Maximum votes is {} out of {}", maxVoters.get(0).getVotes(), gvs.getTestsSize());
            return maxVoters.get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }*/
    }
}
