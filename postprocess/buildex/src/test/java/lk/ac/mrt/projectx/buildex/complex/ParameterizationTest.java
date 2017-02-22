package lk.ac.mrt.projectx.buildex.complex;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Chathura Widanage
 */
public class ParameterizationTest {
    @Test
    public void tresholdValue() throws Exception {
        Parameterization parameterization = new Parameterization(null, null);
        assertEquals(100, parameterization.thresholdValue(4000.25), 0.00001);
        assertEquals(1000, parameterization.thresholdValue(10000.25), 0.00001);
        assertEquals(10, parameterization.thresholdValue(100.25), 0.00001);
        assertEquals(1, parameterization.thresholdValue(10.25), 0.00001);
        assertEquals(0.1, parameterization.thresholdValue(1.25), 0.00001);
        assertEquals(0.01, parameterization.thresholdValue(0.25), 0.00001);
        assertEquals(0.001, parameterization.thresholdValue(0.025), 0.00001);
    }

}