package lk.ac.mrt.projectx.buildex.trees;

import lk.ac.mrt.projectx.buildex.MemoryRegion;
import lk.ac.mrt.projectx.buildex.X86Analysis;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by krv on 12/30/16.
 * Depends on classes: Node, MemoryRegion
 */
public class AbstractNode <T> extends Node<T> implements Comparable {

    public static enum AbstractNodeType {
        OPERATION_ONLY,
        INPUT_NODE,
        OUTPUT_NODE,
        INTERMEDIATE_NODE,
        IMMEDIATE_INT,
        IMMEDIATE_FLOAT,
        PARAMETER,
        UNRESOLVED_SYMBOL,
        SUBTREE_BOUNDARY,
    }

    //region private variables

    private AbstractNodeType type;
    private Integer dimensions;
    private Integer headDiemensions;
    private ArrayList<ArrayList<Integer>> indexes;
    private ArrayList<Integer> pos;
    private MemoryRegion associatedMem;

    //endregion private variables

    //region public methods

    public String GetMemString() {
        StringBuilder memSt = new StringBuilder();
        memSt.append(this.associatedMem.getName());
        memSt.append("(");
        for (int i = 0 ; i < this.dimensions - 1 ; i++) {
            memSt.append(this.pos.get(i));
            memSt.append(",");
        }
        memSt.append(this.pos.get(this.dimensions - 1));
        memSt.append(")");
        return memSt.toString();
    }

    public String GetSymbolicString(List<String> vars) {
        throw new NotImplementedException();
    }

    //region overridden methods

    @Override
    public int compareTo(Object o) {
        AbstractNode node = (AbstractNode) o;
        if (this.type == node.type) {
            if (type == AbstractNodeType.OPERATION_ONLY) {
                return (this.operation == node.operation) &&
                        (this.srcs.size() == node.srcs.size()) ? 1 : 0;
            } else {
                if (this.type == AbstractNodeType.IMMEDIATE_INT) {
//                    return this.symbol.value == node.symbol.value;
                    return 1;
                } else if (this.type == AbstractNodeType.IMMEDIATE_FLOAT) {
                    return Float.compare((Float) node.symbol.value, (Float) this.symbol.value);
                } else if (this.type == AbstractNodeType.INPUT_NODE || this.type == AbstractNodeType.OUTPUT_NODE
                        || this.type == AbstractNodeType.INTERMEDIATE_NODE) {
                    if (node.srcs.size() > 0 &&
                            ((Node) node.srcs.get(0)).operation == X86Analysis.Operation.op_indirect) {
                        return 1;
                    }
                }
                return 1;

            }
        } else {
            return 0;
        }
    }

    @Override
    public String getNodeString() {
        if (this.type == AbstractNodeType.INPUT_NODE || this.type == AbstractNodeType.OUTPUT_NODE
                || this.type == AbstractNodeType.INTERMEDIATE_NODE) {
            return GetMemString();
        } else if (this.type == AbstractNodeType.IMMEDIATE_INT || this.type == AbstractNodeType.IMMEDIATE_FLOAT) {
            return this.symbol.value.toString();
        } else if (this.type == AbstractNodeType.OPERATION_ONLY) {
            return this.operation.toString();
        } else if (this.type == AbstractNodeType.PARAMETER) {
            return "p_" + this.para_num.toString();
        }
        return "";
    }

    @Override
    public String getDotString() {
        return getDotString();
    }

    @Override
    public String getSimpleString() {
        throw new NotImplementedException();
    }

    //endregion overridden methods

    //endregion public methods

    //region private methods

    private String getMemString(List<String> vars) {
        StringBuilder stringBuilder = new StringBuilder();
        String in_string = (this.associatedMem.getDirection() == MemoryRegion.Direction.MEM_OUTPUT) ? "_buf_in" : "";

        stringBuilder.append(this.associatedMem.getName());
        stringBuilder.append(in_string);
        stringBuilder.append("(");
        boolean first = true;

        for (int i = 0 ; i < this.dimensions ; i++) {
            for (int j = 0 ; j < this.headDiemensions ; j++) {
                if (this.indexes.get(i).get(j) == 1) {
                    if (!first) {
                        stringBuilder.append("+");
                    }
                    stringBuilder.append(vars.get(j));
                    first = false;
                } else if (this.indexes.get(i).get(j) != 0) {
                    if (!first) {
                        stringBuilder.append("+");
                    }
                    stringBuilder.append("(");
                    stringBuilder.append(this.indexes.get(i).get(j).toString());
                    stringBuilder.append(")");
                    stringBuilder.append("*");
                    stringBuilder.append(vars.get(j));
                    first = false;

                }
            }

            if (!first) {
                if (this.indexes.get(i).get(this.headDiemensions) != 0) {
                    stringBuilder.append("+");
                    stringBuilder.append(this.indexes.get(i).get(this.headDiemensions).toString());
                }
            } else {
                stringBuilder.append(this.indexes.get(i).get(this.headDiemensions).toString());
            }

            if (this.dimensions - 1 != i) {
                stringBuilder.append(",");
            } else {
                stringBuilder.append(")");
            }
        }
        return stringBuilder.toString();
    }

    private String getImmediateString(List<String> vars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0 ; i < dimensions ; i++) {
            if (headDiemensions == i) {
                stringBuilder.append(indexes.get(0).get(i).toString());
            } else if (0 != indexes.get(0).get(i)) {
                stringBuilder.append(indexes.get(0).get(i).toString());
                stringBuilder.append(" * ");
                stringBuilder.append(vars.get(i));
                stringBuilder.append(" + ");
            }
        }
        return stringBuilder.toString();
    }

    private String getSymbolicString(List<String> vars) {
        String stRet;
        if (this.type == AbstractNodeType.INPUT_NODE || this.type == AbstractNodeType.OUTPUT_NODE
                || this.type == AbstractNodeType.INTERMEDIATE_NODE) {
            stRet = getMemString(vars);
        } else if (this.type == AbstractNodeType.IMMEDIATE_INT) {
            stRet = getImmediateString(vars);
        } else {
            stRet = getNodeString();
        }
        return stRet;
    }

    //endregion private methods

}
