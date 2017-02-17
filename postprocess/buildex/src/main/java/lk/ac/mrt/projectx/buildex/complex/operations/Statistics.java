package lk.ac.mrt.projectx.buildex.complex.operations;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * @author Chathura Widanage
 */
public class Statistics {
    private DescriptiveStatistics statistics = new DescriptiveStatistics();
    private Operation operation;

    private boolean changed = true;

    private int lowerPercentage = 25;
    private int higherPercentage = 75;

    private int low, high;

    public int getLow() {
        generateHighLow();
        return low;
    }

    public int getHigh() {
        generateHighLow();
        return high;
    }

    private void generateHighLow() {
        if (changed) {
            setChanged(false);
            low = (int) (statistics.getPercentile(lowerPercentage) * 1000d);//todo make 1000 a parameter
            high = (int) (statistics.getPercentile(higherPercentage) * 1000d);
        }
    }

    public int getTotalIterations() {
        return (this.getHigh() - this.getLow())+1;
    }

    private void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Statistics(Operation operation) {
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

    public void addValue(double val) {
        setChanged(true);
        statistics.addValue(val);
    }

    @Override
    public String toString() {
        return String.format("Operation : %s [%d - %d] : %d", operation.getName(), this.getLow(), this.getHigh(), this.getTotalIterations());
    }
}
