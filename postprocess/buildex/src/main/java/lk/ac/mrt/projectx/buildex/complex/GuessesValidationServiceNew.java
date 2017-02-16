package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.complex.operations.Guess;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

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
    private Queue<Guess> guesses = new LinkedList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
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
        if (executingCounter.intValue() < 400) {
            Guess poll;
            synchronized (guesses) {
                poll = guesses.poll();
            }
            if (poll != null) {
                submitNewTask(poll);
            }
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
                    PolarCoordinate newPolar;
                    if (isR) {
                        newPolar = new PolarCoordinate(polarCoordinateFirst.getTheta(), newValue);
                    } else {
                        double rVal = rGuess.getProcessedValue(polarCoordinateFirst.getR(), polarCoordinateFirst.getTheta());

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
                    if (isR && distance <= Math.sqrt(2)) {
                        guess.incrementVote();
                    } else if (!isR && distance <= 25) {
                        guess.incrementVote();
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
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executingCounter.decrementAndGet();
                checkAndExecute();
            }
        });
    }
}
