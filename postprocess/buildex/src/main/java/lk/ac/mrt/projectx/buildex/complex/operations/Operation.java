package lk.ac.mrt.projectx.buildex.complex.operations;

/**
 * @author Chathura Widanage
 */
public abstract class Operation {
    private String name;
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operation(String name) {
        this.name = name;
    }

    public Operation(String name,String code) {
        this.name = name;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract double operate(double r, double theta);
}
