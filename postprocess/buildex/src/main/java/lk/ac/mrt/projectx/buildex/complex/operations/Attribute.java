package lk.ac.mrt.projectx.buildex.complex.operations;

/**
 * @author Chathura Widanage
 */
public class Attribute implements Comparable<Attribute> {
    private String name;
    private String code;
    private double value;
    private boolean arguement;

    public Attribute(String name, String code, double value) {
        this.name = name;
        this.code = code;
        this.value = value;
    }

    public Attribute(String name, String code, double value, boolean arguement) {
        this.name = name;
        this.code = code;
        this.value = value;
        this.arguement = arguement;
    }

    public boolean isArguement() {
        return arguement;
    }

    public String getCode() {
        return code;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attribute) {
            return this.name.equals(((Attribute) obj).name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public int compareTo(Attribute o) {
        return this.name.compareTo(o.name);
    }
}
