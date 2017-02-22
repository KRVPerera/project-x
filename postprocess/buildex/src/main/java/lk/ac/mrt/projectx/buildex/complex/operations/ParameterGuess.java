package lk.ac.mrt.projectx.buildex.complex.operations;

import java.util.Collections;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class ParameterGuess implements Comparable<ParameterGuess> {
    private List<Attribute> numerator;
    private List<Attribute> denominator;
    private OperandDecorator decorator;
    private Double error;

    public List<Attribute> getNumerator() {
        return numerator;
    }

    public void setNumerator(List<Attribute> numerator) {
        this.numerator = numerator;
    }

    public List<Attribute> getDenominator() {
        return denominator;
    }

    public void setDenominator(List<Attribute> denominator) {
        this.denominator = denominator;
    }

    public OperandDecorator getDecorator() {
        return decorator;
    }

    public void setDecorator(OperandDecorator decorator) {
        this.decorator = decorator;
    }

    public Double getError() {
        return error;
    }

    public void setError(Double error) {
        this.error = error;
    }

    public String generateCode() {
        Collections.sort(numerator);//to make all attributes in numerator and denominator in same order
        Collections.sort(denominator);
        if (numerator.equals(denominator)) {
            return "(1)";
        }
        return "(" + String.format(decorator.toString(), (codeGenParams(numerator) + "/" + codeGenParams(denominator))) + ")";
    }

    private String codeGenParams(List<Attribute> atts) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < atts.size(); i++) {
            sb.append(atts.get(i).getCode());
            if (i != atts.size() - 1) {
                sb.append("*");
            }
        }
        return sb.append(")").toString();
    }

    @Override
    public int compareTo(ParameterGuess o) {
        if (this.error.compareTo(o.error) == 0) {
            return -(o.numerator.size() + o.denominator.size()) + (this.numerator.size() + this.denominator.size());
        }
        return this.error.compareTo(o.error);
    }

    @Override
    public String toString() {
        return "ParameterGuess{" +
                "numerator=" + numerator +
                ", denominator=" + denominator +
                ", decorator=" + decorator +
                ", error=" + error +
                '}';
    }

    /* public class ParamaterGuessErrorComparator implements Comparator<ParameterGuess> {
        @Override
        public int compare(ParameterGuess o1, ParameterGuess o2) {
            return o1.error.compareTo(o2.error);
        }
    }

    public class ParameterGuessAttributeNumberComparator implements Comparator<ParameterGuess> {
        @Override
        public int compare(ParameterGuess o1, ParameterGuess o2) {
            return (o2.numerator.size() + o2.denominator.size()) - (o1.numerator.size() + o1.denominator.size());
        }
    }*/
}
