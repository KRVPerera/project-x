package lk.ac.mrt.projectx.buildex.models.output;

import lk.ac.mrt.projectx.buildex.DefinesDotH;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by krv on 1/20/17.
 */
public class OutputInstructionUtils {

    final static Logger logger = LogManager.getLogger( OutputInstructionUtils.class );

    //region Public Methods

    public static void updateFloatingPointRegs(List<Pair<Output, StaticInfo>> instrs, Integer direction,
                                               List<StaticInfo> statidInfo, List<Integer> pc) {
        logger.debug( "updating floating point regs" );
        int tos = DefinesDotH.DR_REG.DR_REG_ST8.ordinal();

        for (int i = 0 ; i < instrs.size() ; i++) {
            Output cinstr = instrs.get( i ).first;
            boolean unhandled = false;
            String disasm = getDisasmString( statidInfo, cinstr.getPc() );
            int line = i + 1;

            // this loop has no effect since tos is already set to the value changed by the loop
//            for (int j = 0 ; j < pc.size() ; j++) {
//                if (cinstr.getPc() == pc.get( j )) {
//                    tos = DefinesDotH.DR_REG.DR_REG_ST8.ordinal();
//                    break;
//                }
//            }
            if (!cinstr.getOpcode().isFloatingPointIns()) {
                cinstr.updateFPReg( disasm, i + 1 );
            } else {
                switch (cinstr.getOpcode()) {
                    case OP_fld: //Push m32fp onto the FPU register stack.
                    case OP_fld1: //Push +1.0 onto the FPU register stack
                    case OP_fild: //Push m32int onto the FPU register stack.
                    case OP_fldz: //Push +0.0 onto the FPU register stack.
                        if (direction == 1) { // FORWARDS_ANALYSIS
                            cinstr.updateFPSrc( disasm, line );
                            tos = updateTos( tos, true, disasm, line, direction );
                            cinstr.updateFPDest( disasm, line );
                        }
                }
            }
        }
    }

    /**
     * Update tos
     *
     * @param tos       int current tos value
     * @param push      Whether the type is a push = true, or pop = false
     * @param disasm
     * @param line
     * @param direction whether forward analysis = 1, or backwards analysis = 2
     * @return ::int updated tos value
     */
    private static int updateTos(int tos, boolean push, String disasm, int line, Integer direction) {
        if (direction == 2) { // BACKWARD_ANALYSIS
            assert tos >= DefinesDotH.DR_REG.DR_REG_ST0.ordinal() : "Floating point stack overflow";
            assert tos < DefinesDotH.DR_REG.DR_REG_ST15.ordinal() : "Floating point stack underflow";
            if (push) {
                tos--;
            } else { // pop
                tos++;
            }
        } else if (direction == 1) { // FORWARDS_ANALYSIS
            assert tos >= DefinesDotH.DR_REG.DR_REG_ST0.ordinal() : "Floating point stack overflow";
            assert tos < DefinesDotH.DR_REG.DR_REG_ST15.ordinal() : "Floating point stack underflow";
            if (push) { // push
                tos++;
            } else { // pop
                tos--;
            }
        }
        return tos;
    }

    private static String getDisasmString(List<StaticInfo> statidInfo, long pc) {
        String disasm = null;
        for (int i = 0 ; i < statidInfo.size() ; i++) {
            StaticInfo staticInfo = statidInfo.get( i );
            if (staticInfo.getPc() == pc) {
                disasm = staticInfo.getDissasembly();
            }
        }
        return disasm;
    }

    //endregion Public Methods

    //region Private Methods

    public static void updateRegsToMemRange(List<Pair<Output, StaticInfo>> instrs) {
        logger.debug( "Coverting reg to memory" );
        for (int i = 0 ; i < instrs.size() ; i++) {
            Output instr = instrs.get( i ).first;
            for (int j = 0 ; j < instr.getSrcs().size() ; j++) {
                Operand srcOp = instr.getSrcs().get( j );
                updateRegsToMemRangeHelper( srcOp, j );
            }

            for (int j = 0 ; j < instr.getSrcs().size() ; j++) {
                Operand dstOp = instr.getDsts().get( j );
                updateRegsToMemRangeHelper( dstOp, j );
            }
        }
    }

    private static void updateRegsToMemRangeHelper(Operand op, int j) {
        if ((op.getType() == MemoryType.REG_TYPE) &&
                (((Integer) op.getValue()) > DefinesDotH.DR_REG.DR_REG_ST7.ordinal())) {
            op.regToMemRange();
            if (!op.getAddress().isEmpty()) { // TODO : check the logic x86_analysis.cpp line 1181 - 1185
                for (int k = 0 ; k < 4 ; k++) {
                    if ((op.getAddress().get( k ).getType() == MemoryType.REG_TYPE)
                            && (((Integer) op.getAddress().get( j ).getValue()) == 0)) {
                        continue;
                    }
                    op.getAddress().get( j ).regToMemRange();
                }
            }
        }
    }

    //endregion Private Methods
}
