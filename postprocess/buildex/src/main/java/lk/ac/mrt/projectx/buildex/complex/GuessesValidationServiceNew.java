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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Chathura Widanage
 */
public class GuessesValidationServiceNew {
    private final static Logger logger = LogManager.getLogger(GuessesValidationServiceNew.class);

    private Queue<Guess> guesses = new ConcurrentLinkedQueue<>();//LinkedList<>();
    private int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    private ExecutorService executorService;
    private List<Pair<CartesianCoordinate, CartesianCoordinate>> testCases;
    private AtomicLong itCount = new AtomicLong();

    Semaphore maxVoteAccessSem = new Semaphore(1);
    Semaphore taskSubmitSem = new Semaphore(10000);
    //Lock lock = new ReentrantLock();
    private long maxVotes = 0;
    private List<Guess> maxVoters = new ArrayList<>();

    private int submits = 0;

    private int width, height;
    private boolean isR;

    private Guess rGuess;

    private Thread statusPrintThread;
    private boolean running = true;

    public GuessesValidationServiceNew(List<Pair<CartesianCoordinate,
            CartesianCoordinate>> testCases, int width,
                                       int height, boolean isR, Guess rGuess) {
        this.testCases = testCases;
        this.width = width;
        this.height = height;
        this.isR = isR;
        this.rGuess = rGuess;

        executorService = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            spawnNewThread();
        }

        statusPrintThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (itCount.get() != 0)
                        System.out.print("\r" + itCount.incrementAndGet());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        statusPrintThread.start();

    }

    public void newBatch() {
        this.itCount.set(0);
    }

    public int getTestsSize() {
        return testCases.size();
    }

    public List<Guess> getMaxVoters() {
        try {//prevent concurrent exception while logging
            maxVoteAccessSem.acquire();
            List<Guess> mv = new ArrayList<>(maxVoters);
            return mv;
        } catch (InterruptedException e) {
            return null;
        } finally {
            maxVoteAccessSem.release();
        }
    }

    public void submit(Guess guess) throws InterruptedException {
        if (guess == null) {
            return;
        }
        //lock.lock();
        taskSubmitSem.acquire();//control memory usage by trying to keep queue size at a constant
        guesses.add(guess);
      /*  if (guesses.size() < 5000) {
            guesses.add(guess);
        } else {

        }*/
    }

    public List<Guess> awaitTermination() throws InterruptedException {
        int spawned = -1;
        while (guesses.peek() != null) {
            if (spawned++ < 4) {
                spawnNewThread();
            } else {
                break;
            }
            Thread.sleep(10000);//allow 10secs before recheck
        }
        running = false;
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
        return maxVoters;
    }

    /**
     * This methods limits memory usage by limiting number of submitted runnables
     */
    private void checkAndExecute() {
        /*try {
            taskSubmitSem.acquire();
            spawnNewThread();
        } catch (InterruptedException e) {
            spawnNewThread();
        } finally {
            taskSubmitSem.release();
        }*/
    }

    private void spawnNewThread() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    Guess guess = guesses.poll();
                    if (guess == null) {
                        taskSubmitSem.release();
                        continue;
                    }
                    boolean skipped = false;
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


                        double distance = 500;
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
                        } else if (!isR && distance <= 15) {
                            guess.incrementVote();
                        }

                        //stop if not going to make it better than the current best
                        if ((testCases.size() - maxVotes) < (p - guess.getVotes())) {//no point of continuing
                            //logger.debug("Abandoning guess due to no possible winning : {}", guess);
                            skipped = true;
                            continue;
                        }
                    }
                    if (!skipped) {
                        try {
                            maxVoteAccessSem.acquire();
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
                            maxVoteAccessSem.release();
                        }
                    }
                    itCount.incrementAndGet();
                    taskSubmitSem.release();
                }
            }
        });
    }
}
