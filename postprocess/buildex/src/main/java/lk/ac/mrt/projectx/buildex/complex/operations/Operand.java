package lk.ac.mrt.projectx.buildex.complex.operations;

/**
 * @author Chathura Widanage
 */
public abstract class Operand {
    private String name;
    private String code;
    private Integer family;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Operand(String name) {
        this.name = name;
    }

    public Operand(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Operand(String name, String code, Integer family) {
        this.name = name;
        this.code = code;
        this.family = family;
    }

    public Integer getFamily() {
        if (family == null) {
            return -1;
        }
        return family;
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
