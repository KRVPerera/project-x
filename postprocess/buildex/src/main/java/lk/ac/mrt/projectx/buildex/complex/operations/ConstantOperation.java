package lk.ac.mrt.projectx.buildex.complex.operations;

/**
 * @author Chathura Widanage
 */
public class ConstantOperation extends Operation {

    public ConstantOperation(String name) {
        super(name);
    }

    @Override
    public String getCode() {
        return "1";
    }

    @Override
    public double operate(double r, double theta) {
        return 1;
    }
}
