package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.complex.operations.Guess;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Chathura Widanage
 */
public class GuessesValidationServiceNew {
    private final static Logger logger = LogManager.getLogger(GuessesValidationServiceNew.class);

    private Queue<Guess> guesses = new LinkedList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private List<Pair<CartesianCoordinate, CartesianCoordinate>> testCases;
    private AtomicInteger executingCounter = new AtomicInteger(0);
    private AtomicLong itCount = new AtomicLong();

    Semaphore semaphore = new Semaphore(1);
    Lock lock = new ReentrantLock();
    private long maxVotes = 0;
    private List<Guess> maxVoters = new ArrayList<>();

    private int width, height;
    private boolean isR;

    private Guess rGuess;

    public GuessesValidationServiceNew(List<Pair<CartesianCoordinate,
            CartesianCoordinate>> testCases, int width,
                                       int height, boolean isR, Guess rGuess) {
        this.testCases = testCases;
        this.width = width;
        this.height = height;
        this.isR = isR;
        this.rGuess = rGuess;
    }

    public void submit(Guess guess) throws InterruptedException {
        lock.lock();
        guesses.add(guess);
        checkAndExecute();
    }

    public List<Guess> awaitTermination() throws InterruptedException {
        while (!guesses.isEmpty()) {

        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        return maxVoters;
    }

    private void checkAndExecute() {
        if (guesses.size() < 100) {
            lock.unlock();
        }
        Guess poll = guesses.poll();
        if (poll != null) {
            submitNewTask(poll);
        }
    }

    private void submitNewTask(final Guess guess) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                executingCounter.incrementAndGet();
                System.out.print("\r" + itCount.incrementAndGet());
                for (int p = 0; p < testCases.size(); p++) {
                    Pair<CartesianCoordinate, CartesianCoordinate> pair = testCases.get(p);
                    CartesianCoordinate cartesianCoordinateFirst = pair.first;
                    CartesianCoordinate cartesianCoordinateSecond = pair.second;

                    PolarCoordinate polarCoordinateFirst = CoordinateTransformer.cartesian2Polar(cartesianCoordinateFirst);
                    PolarCoordinate polarCoordinateSecond = CoordinateTransformer.cartesian2Polar(cartesianCoordinateSecond);


                    double newValue = guess.getProcessedValue(polarCoordinateFirst.getR(), polarCoordinateFirst.getTheta());
                    if (guess.getGuessOperator() != null) {
                        newValue = guess.getGuessOperator().operateInv(newValue);
                    }
                    PolarCoordinate newPolar;
                    if (isR) {
                        newPolar = new PolarCoordinate(polarCoordinateFirst.getTheta(), newValue);
                    } else {
                        double rVal = rGuess.getProcessedValue(polarCoordinateFirst.getR(), polarCoordinateFirst.getTheta());
                        if (rGuess.getGuessOperator() != null) {
                            rVal = rGuess.getGuessOperator().operateInv(rVal);
                        }

                        newValue = MathUtils.normalizeAngle(newValue, FastMath.PI);
                        //System.out.println(newValue);
                        //System.out.println(polarCoordinateSecond.getR()+","+rVal);
                        newPolar = new PolarCoordinate(newValue, rVal);
                    }


                    double distance = 5;
                    if (isR) {
                        distance = Math.abs(polarCoordinateSecond.getR() - newPolar.getR());
                    } else {
                        CartesianCoordinate genertaed = CoordinateTransformer.polar2Cartesian(newPolar);
                        double x = pair.second.getX();
                        double y = pair.second.getY();
                        distance = Math.sqrt(
                                Math.pow(genertaed.getX() - x, 2)
                                        + Math.pow(genertaed.getY() - y, 2)
                        );// / newPolar.getTheta();
                        //System.out.println(x+","+genertaed.getX());
                        //System.out.println(distance);
                    }
                    if (isR && distance <= Math.sqrt(2) * 3) {
                        guess.incrementVote();
                    } else if (!isR && distance <= 15) {
                        guess.incrementVote();
                    }

                    //stop if not going to make it better than the current best
                    if ((testCases.size() - maxVotes) < (p - guess.getVotes())) {//no point of continuing
                        //logger.debug("Abandoning guess due to no possible winning : {}", guess);
                        continue;
                    }

                }
                try {
                    semaphore.acquire();
                    if (maxVotes < guess.getVotes()) {
                        maxVotes = guess.getVotes();
                        maxVoters.clear();
                        maxVoters.add(guess);
                    } else if (maxVotes == guess.getVotes()) {
                        maxVoters.add(guess);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
                executingCounter.decrementAndGet();
                checkAndExecute();
            }
        });
    }
}
