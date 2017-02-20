package lk.ac.mrt.projectx.buildex.complex.operations;

/**
 * @author Chathura Widanage
 */
public abstract class OperandDecorator {//should do the inverse of each operator
    private String code;

    public OperandDecorator(String inverseCode) {
        this.code = inverseCode;
    }

    public abstract double operate(double val);

    public abstract double operateInv(double val);

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
