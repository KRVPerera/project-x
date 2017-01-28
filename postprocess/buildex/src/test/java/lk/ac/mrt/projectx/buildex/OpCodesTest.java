package lk.ac.mrt.projectx.buildex;

import junit.framework.TestCase;

/**
 * Created by krv on 1/28/17.
 */
public class OpCodesTest extends TestCase {

    public void testIsJmpConditionalAffected() throws Exception {
        DefinesDotH.OpCodes op = DefinesDotH.OpCodes.values()[ 31 ];
        Boolean ans =   op.isJmpConditionalAffected( 1367867392 );
        assertTrue( ans );
    }

}