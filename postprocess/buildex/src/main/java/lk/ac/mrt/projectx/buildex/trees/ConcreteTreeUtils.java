package lk.ac.mrt.projectx.buildex.trees;

import lk.ac.mrt.projectx.buildex.GeneralUtils;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.CommonUtil;
import lk.ac.mrt.projectx.buildex.models.common.FuncInfo;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.Operand;
import lk.ac.mrt.projectx.buildex.models.output.Output;
import lk.ac.mrt.projectx.buildex.models.output.ReducedInstruction;
import lk.ac.mrt.projectx.buildex.models.output.ReducedInstructionUtils;
import lk.ac.mrt.projectx.buildex.x86.X86Analysis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krv on 1/21/17.
 */
public class ConcreteTreeUtils {

    private static final Logger logger = LogManager.getLogger( ConcreteTreeUtils.class );

    private static final int FILE_BEGINNING = -2;
    private static final int FILE_ENDING = -1;
    private static final boolean conctreeOpt = true;

    public static List<List<ConcreteTree>> clusterTrees(List<MemoryRegion> memRegions, List<MemoryRegion> totalRegions,
                                                        List<Long> startPoints, List<Pair<Output, StaticInfo>> instrs,
                                                        Long farthest, String outputFolder, List<FuncInfo> funcInfo) {

        logger.debug( "Building trees for all locations in the output and clustering" );
        MemoryRegion mem = MemoryRegionUtils.getRandomOutputRegion( ((ArrayList<MemoryRegion>) memRegions) );

        List<ConcreteTree> trees = new ArrayList<>();
        List<List<Integer>> indexes = mem.getIndexList();
        List<Integer> offset = indexes.get( 0 );

        boolean success = true;

        int i = 0;
        if (mem.getStartMemory() > mem.getEndMemory()) {
            i = indexes.size() - 1;
        }

        boolean done = false;
        long count = 0;

        while (!done) {
            Long location = mem.getMemLocation( indexes.get( i ), offset );
            assert location != null : "Getting mem location error";

            logger.debug( "Building tree for location error" );

            ConcreteTree tree = new ConcreteTree();
            tree.setTreeNum( i );

            ConcreteTree initialTree = buildConcreteTree( location, mem.getBytesPerPixel(), startPoints, FILE_BEGINNING,
                    FILE_ENDING, tree, instrs, farthest, totalRegions, funcInfo );
        }

        throw new NotImplementedException();
//        return null;
    }

    private static ConcreteTree buildConcreteTree(Long destination, Integer stride, List<Long> startPoints,
                                                  Integer startTrace, Integer endTrace, ConcreteTree tree,
                                                  List<Pair<Output, StaticInfo>> instrs, Long farthest,
                                                  List<MemoryRegion> regions, List<FuncInfo> funcInfo) {
        logger.debug( "Build tree multi  " );
        Integer initialEntracne = endTrace;

        Pair<Integer, Integer> points = getStartAndEndPoints( startPoints, destination, stride, startTrace,
                endTrace, instrs );

        startTrace = points.first;
        endTrace = points.second;

        if (endTrace == FILE_ENDING) {
            endTrace = instrs.size();
        }

        if (startTrace == FILE_BEGINNING) {
            startTrace = 0;
        }

        assert endTrace >= startTrace : "Trace end should be greater than the trace start";

        Long initialStart = (Long) GeneralUtils.deepCopy( startTrace );
        Integer curPos = (Integer) GeneralUtils.deepCopy( startTrace );

        // now we need to read the next lint adn start from the correct destination
        Output instr = instrs.get( curPos ).first;
        List<ReducedInstruction> rinstr = ReducedInstructionUtils.cinstrToRinstrsEflags( instr,
                instrs.get( curPos ).second.getDissasembly(), curPos );
        int index = -1;
        boolean destPresent = false;
        for (int i = 0 ; i < rinstr.size() ; i++) {
            ReducedInstruction rins = rinstr.get( i );
            if (rins.getDst().getValue() == destination) {
                index = i;
                destPresent = true;
                break;
            }
        }

        if (!destPresent || index < 0) {
            return null;
        }

        // build the initial part of the tree
        for (int i = index ; i >= 0 ; i--) {
            tree.updateDependencyBackward( rinstr.get( i ), instrs.get( curPos ).first, instrs.get( curPos ).second,
                    curPos, regions, funcInfo );
        }

        startTrace++;
        Integer startToInitial = (Integer) GeneralUtils.deepCopy( startTrace );
        index = -1;

//        for (int i = 0 ; i < startPoints.size() ; i++) {
//            if (startPoints.get( i ).intValue() == endTrace) {
//                index = i;
//            }
//        }

        for (int i = startPoints.size() - 1 ; i >= 0 ; i--) {
            if (startPoints.get( i ).intValue() == endTrace) {
                index = i;
                break;
            }
        }

        for (int i = startPoints.size() - 1 ; i >= 0 ; i--) {
            if (startPoints.get( i ).intValue() < startToInitial) {
                startToInitial = startPoints.get( i ).intValue();
                break;
            }
        }

        if (startToInitial == startTrace) {
            startToInitial = 0;
        }

        if (index == -1) {
            endTrace = instrs.size();
        }

        // now build the tree
//      while ((startTrace != instrs.size()) && (startTrace != initialEntracne)) {
        if ((startTrace != instrs.size()) && (startTrace != initialEntracne)) {
            logger.debug( "%d - %d", startTrace, endTrace );
            buildConcTreeHelper( startTrace, endTrace, tree, instrs, regions, funcInfo );
////             startTrace = endTrace - affects initial tree building
////             if(index + 1 < start_points.size()) endTrace = start_points[++index]
//            break;
//        }
        }
        ConcreteTree initialTree = null;
        if (conctreeOpt) {
            tree.removeAssignedNodes();
            tree.removeMultiplication();
            tree.removePoNodes();
            tree.canonicalizeTree();
            tree.simplifyImmediates();
            tree.removeOrMinus1();
            tree.removeIdentities();
            tree.numberParameters( regions );
            tree.setRecursive( false );
            tree.markRecursive();
        }

        if (tree.isRecursive()) {
            initialTree = new ConcreteTree();
            // we need to build a tree for the initial udate definition
            buildTreeInitialUpdate( destination, stride, startToInitial, endTrace, initialTree, instrs, initialStart,
                    regions, funcInfo );
            if (initialTree.getHead() == null) {
                // there is not initial first create a new region if the dummy region is not built
                MemoryRegion region = MemoryRegionUtils.getMemRegion( farthest.intValue(), regions );
                if (region == null) {
                    // dummy region
                    ConcreteNode concreteNode = ((ConcreteNode) tree.getHead());
                    region = ((MemoryRegion) GeneralUtils.deepCopy( concreteNode.getRegion() ));
                    region.setStartMemory( farthest );
                    region.setEndMemory( concreteNode.getRegion().getEndMemory() - concreteNode.getRegion()
                            .getStartMemory() + farthest );
                    region.setName( concreteNode.getRegion().getName() + "_in" );
                    // need a check for integer overflow
                    regions.add( region );
                }

                // dummy tree
                buildDummyInitialTree( initialTree, tree, region );
                initialTree.dummyTree = true;
            }
            if (conctreeOpt) {
                initialTree.removeAssignedNodes();
                initialTree.removeMultiplication();
                initialTree.removePoNodes();
                ;
                initialTree.canonicalizeTree();
                initialTree.simplifyImmediates();
                initialTree.numberParameters( regions );
            }
        }

        return initialTree;
    }

    private static void buildDummyInitialTree(ConcreteTree initialTree, ConcreteTree redTree, MemoryRegion newRegion) {
        logger.debug( "Dummy initial tree" );
        ConcreteNode node = ((ConcreteNode) redTree.getHead());
        MemoryRegion region = node.getRegion();

        assert region != null : "Error: the head should have an output region";
        ConcreteNode dupNode = new ConcreteNode( node.getSymbol() );
        initialTree.setHead( dupNode );
        dupNode.setOperation( X86Analysis.Operation.op_assign );

        ConcreteNode newNode = new ConcreteNode( node.getSymbol() );
        newNode.getSymbol().setValue( node.getSymbol().getValue().longValue() - region.getStartMemory() + newRegion
                .getStartMemory() );
        newNode.setRegion( newRegion );

        dupNode.addForwardReference( newNode );
    }

    private static void buildTreeInitialUpdate(Long destination, Integer stride, Integer start, Integer end,
                                               ConcreteTree tree, List<Pair<Output, StaticInfo>> instrs,
                                               Long originalStart, List<MemoryRegion> regions, List<FuncInfo>
                                                       funcInfo) {
        logger.debug( "Initial update tree building" );
        List<ReducedInstruction> rinstr = null;
        Output instr = null;
        int index = -1;
        int curpos = -1;
        // get the first assignment to the destination
        for (int i = end - 1 ; i >= 0 ; i--) {
            instr = instrs.get( i ).first;
            boolean found = false;
            for (int j = 0 ; j < instr.getNumOfDestinations() ; j++) {
                Operand opnd = instr.getDsts().get( j );
                if (CommonUtil.isOverlapped( destination, destination + stride - 1, opnd.getValue().longValue(), opnd
                        .getValue().longValue() + opnd.getWidth() - 1 )) {
                    rinstr = ReducedInstructionUtils.cinstrToRinstrsEflags( instr, instrs.get( i ).second
                            .getDissasembly(), i );
                    for (int k = rinstr.size() ; k >= 0 ; k--) {
                        if (rinstr.get( k ).getDst().getValue().longValue() == opnd.getValue().longValue() &&
                                rinstr.get( k ).getDst().getWidth() == opnd.getWidth()) {
                            found = true;
                            index = k;
                            break;
                        }
                    }
                    break;
                }
            }
            if (found) {
                curpos = i;
                break;
            }
        }

        if (curpos == originalStart) {
            return;
        }

        assert index != -1 : "Error : could not find the destination or overlap";
        for (int i = index ; i >= 0 ; i--) {
            tree.updateDependencyBackward( rinstr.get( i ), instr, instrs.get( curpos ).second, curpos, regions, funcInfo );
        }

        Integer startTrace = curpos + 1;
        Integer endTrace = end;

        assert index != -1 : "Error : Should find end trace in start points";

        // build the tree
        buildConcTreeHelper( startTrace, endTrace, tree, instrs, regions, funcInfo );
    }

    private static void buildConcTreeHelper(Integer startTrace, Integer endTrace, ConcreteTree tree,
                                            List<Pair<Output, StaticInfo>> instrs, List<MemoryRegion> regions,
                                            List<FuncInfo> funcInfo) {
        List<Pair<Long, Long>> lines = new ArrayList<>();
        // build the rest of expression tree building
        Integer curpos = startTrace;
        List<ReducedInstruction> rinstr = null;
        for ( ; curpos < endTrace ; curpos++) {
            Output instr = instrs.get( curpos ).first;
            rinstr = ReducedInstructionUtils.cinstrToRinstr( instr, instrs.get( curpos )
                    .second.getDissasembly(), curpos );

            boolean updated = false;
            boolean affected = false;

            for (int i = rinstr.size() - 1 ; i >= 0 ; i--) {
                updated = tree.updateDependencyBackward( rinstr.get( i ), instrs.get( curpos ).first,
                        instrs.get( curpos ).second, curpos, regions, funcInfo );

                if (updated) {
                    affected = true;
                    lines.add( new Pair( curpos, instr.getPc() ) );
                }
            }
            if (affected) { // this is this instr affect the frontier
                tree.updateJumpConditionals( instrs, curpos );
            }
        }
    }

    /**
     * Start is the one after the Output which points to the destination given
     * end is the next largest or equal value in startPoints list
     *
     * @param startPoints
     * @param destination
     * @param stride
     * @param startTrace
     * @param endTrace
     * @param instrs
     * @return Pair <start, end> Integer values
     */
    private static Pair<Integer, Integer> getStartAndEndPoints(List<Long> startPoints, Long destination, Integer stride,
                                                               Integer startTrace, Integer endTrace, List<Pair<Output, StaticInfo>> instrs) {
        Integer start = startTrace;
        Integer end = endTrace;

        if (startTrace == FILE_BEGINNING) {
            boolean found = false;
            for (int i = 0 ; i < instrs.size() ; i++) {
                Output opt = instrs.get( i ).first;
                for (int j = 0 ; j < opt.getDsts().size() ; j++) {
                    Operand dstOperand = opt.getDsts().get( j );
                    if (dstOperand.getValue() == destination && dstOperand.getWidth() == stride) {
                        start = i;
                        found = true;
                        break;
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }

        // TODO : Unsafe casting
        if (endTrace == FILE_ENDING) {
            for (int i = 0 ; i < startPoints.size() ; i++) {
                if (start <= startPoints.get( i )) {
                    end = startPoints.get( i ).intValue();
                    break;
                }
            }
        }

        return new Pair<>( start, end );
    }
}

