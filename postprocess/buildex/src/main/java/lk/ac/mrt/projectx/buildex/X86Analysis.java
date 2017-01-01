package lk.ac.mrt.projectx.buildex;

/**
 * Created by krv on 12/5/2016.
 */
public class X86Analysis {

    public static int MAX_SIZE_OF_REG = 32;

    //canonicalized operations
    public enum Operation {
        op_assign,
        op_add,
        op_sub,
        op_mul,
        op_div,
        op_mod,
        op_lsh,
        op_rsh,
        op_not,
        op_xor,
        op_and,
        op_or,

        /*support operations*/
        op_split_h,
        op_split_l,
        op_concat,
        op_signex,

        /* to cater to different widths */
        op_partial_overlap,
        op_full_overlap,

        /* logical operations */
        op_ge,
        op_gt,
        op_le,
        op_lt,
        op_eq,
        op_neq,

        /*address dependancy*/
        op_indirect,

        /*call*/
        op_call,
        op_unknown;

        @Override
        public String toString() {
            String ret;
            switch (this) {
                case op_add:
                    ret = "+";
                    break;
                case op_and:
                    ret = "&";
                    break;
                case op_assign:
                    ret = "=";
                    break;
                case op_call:
                    ret = "call";
                    break;
                case op_concat:
                    ret = ",";
                    break;
                case op_div:
                    ret = "/";
                    break;
                case op_eq:
                    ret = "==";
                    break;
                case op_full_overlap:
                    ret = "FO";
                    break;
                case op_ge:
                    ret = ">=";
                    break;
                case op_gt:
                    ret = ">";
                    break;
                case op_indirect:
                    ret = "indirect";
                    break;
                case op_le:
                    ret = "<=";
                    break;
                case op_lsh:
                    ret = "<<";
                    break;
                case op_lt:
                    ret = "<";
                    break;
                case op_mod:
                    ret = "%";
                    break;
                case op_mul:
                    ret = "*";
                    break;
                case op_neq:
                    ret = "!=";
                    break;
                case op_not:
                    ret = "~";
                    break;
                case op_or:
                    ret = "||";
                    break;
                case op_partial_overlap:
                    ret = "PO";
                    break;
                case op_rsh:
                    ret = ">>";
                    break;
                case op_signex:
                    ret = "SE";
                    break;
                case op_split_h:
                    ret = "SH";
                    break;
                case op_split_l:
                    ret = "SL";
                    break;
                case op_sub:
                    ret = "-";
                    break;
                case op_unknown:
                    ret = "__";
                    break;
                case op_xor:
                    ret = "^";
                    break;
                default:
                    ret = "__";
                    break;
            }
            return ret;
        }
    }
}
