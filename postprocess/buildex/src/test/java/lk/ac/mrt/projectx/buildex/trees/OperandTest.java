package lk.ac.mrt.projectx.buildex.trees;

import junit.framework.TestCase;
import lk.ac.mrt.projectx.buildex.models.output.MemoryType;
import lk.ac.mrt.projectx.buildex.models.output.Operand;

/**
 * Created by krv on 1/1/17.
 */
public class OperandTest extends TestCase {

    public void testConstructorInteger() {
        Operand operand = new Operand(MemoryType.IMM_INT_TYPE, 50, 160);

        assertEquals(50, operand.getValue());
    }

    public void testConstructorFloat() {
        Operand operand = new Operand(MemoryType.IMM_FLOAT_TYPE, 50.0f, 160);

        assertEquals(50.0f, operand.getValue());
    }


    public void testMemRangeToRegName() {
        Operand operand = new Operand();
        operand.setType(MemoryType.REG_TYPE);
        operand.setValue(3*32);
        String string = operand.getRegName();
        assertEquals("rbx", string);
    }
}