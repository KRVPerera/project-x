package lk.ac.mrt.projectx.buildex;

import lk.ac.mrt.projectx.buildex.exceptions.NoSuitableFileFoundException;
import lk.ac.mrt.projectx.buildex.files.AppPCFile;
import lk.ac.mrt.projectx.buildex.files.InstructionTraceFile;
import lk.ac.mrt.projectx.buildex.files.MemoryDumpFile;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.JumpInfo;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.PCMemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.Output;
import lk.ac.mrt.projectx.buildex.models.output.OutputInstructionUtils;
import lk.ac.mrt.projectx.buildex.trees.ConcreteTree;
import lk.ac.mrt.projectx.buildex.trees.MemoryRegionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * @author Chathura Widanage
 */
public class Buildex {

    private static final Logger logger = LogManager.getLogger( Buildex.class );

    public static void main(String[] args) throws IOException, NoSuitableFileFoundException {
        String inImage = "a.png";
        String exec = "halide_blur_hvscan_test.exe";

        File outputFolder = Configurations.getOutputFolder();//new File("generated_files_test\\working\\output_files");//Configurations.getOutputFolder();
        List<File> outputFilesList = Arrays.asList( outputFolder.listFiles() );

        File filterFolder = Configurations.getFilterFolder();
        List<File> filterFileList = Arrays.asList( filterFolder.listFiles() );

        ProjectXImage inputImage = new ProjectXImage( ImageIO.read( new File( Configurations.getImagesFolder(), "a.png" ) ) );
        ProjectXImage outputImage = new ProjectXImage( ImageIO.read( new File( Configurations.getImagesFolder(), "ablur.png" ) ) );

        InstructionTraceFile instructionTraceFile = InstructionTraceFile.filterLargestInstructionTraceFile( outputFilesList, inImage, exec, false );
        logger.info( "Found instrace file {}", instructionTraceFile.getName() );

        InstructionTraceFile disAsmFile = InstructionTraceFile.filterLargestInstructionTraceFile( outputFilesList, inImage, exec, true );
        logger.info( "Found memory dump file {}", disAsmFile.getName() );

        List<MemoryDumpFile> memoryDumpFileList = MemoryDumpFile.filterMemoryDumpFiles( outputFilesList, exec );
        logger.info( "Found {} memory dump files {}", memoryDumpFileList.size(), memoryDumpFileList.toString() );

        AppPCFile appPCFile = AppPCFile.filterAppPCFile( filterFileList, exec );
        logger.info( "Found app pc file {}", appPCFile.toString() );

        /*MEMORY INFO STAGE*/
        MemoryAnalyser memoryAnalyser = MemoryAnalyser.getInstance();
        List<MemoryRegion> dumpRegions = memoryAnalyser.getImageRegions( memoryDumpFileList, inputImage, outputImage );
        logger.info( "Found {} image regions", dumpRegions.size() );
        logger.debug( dumpRegions.toString() );

        List<MemoryInfo> memoryLayoutMemoryInfo = MemoryLayoutOps.createMemoryLayoutMemoryInfo( instructionTraceFile, 1 );
        logger.info( "Found {} memory infos", memoryLayoutMemoryInfo.size() );

        List<PCMemoryRegion> memoryLayoutPCMemoryRegion = MemoryLayoutOps.createMemoryLayoutPCMemoryRegion( instructionTraceFile, 1 );
        logger.info( "Found {} PC Memory Regions", memoryLayoutPCMemoryRegion.size() );
        logger.debug( memoryLayoutPCMemoryRegion.toString() );

        logger.debug( "Linking memory regions. Size : {}", memoryLayoutMemoryInfo.size() );
        MemoryLayoutOps.linkMemoryRegionsGreedy( memoryLayoutMemoryInfo, 0 );
        logger.debug( "Linked memory regions. Size : {}", memoryLayoutMemoryInfo.size() );

        MemoryLayoutOps.mergeMemoryInfoPCMemoryRegion( memoryLayoutMemoryInfo, memoryLayoutPCMemoryRegion );
        logger.info( "Merged memory regions {}", memoryLayoutMemoryInfo.toString() );
        /*END OF MEMORY INFO STAGE*/

        /* GATHERING INSTRUCTION TRACE */
        List<StaticInfo> staticInfos = new ArrayList<>();
        InstructionTracer ir = InstructionTracer.getInstance();
        staticInfos = ir.parseDebugDisassembly( disAsmFile );

        List<Long> startPcs = new ArrayList<>();
        List<Long> endPcs = new ArrayList<>();
        if (startPcs.isEmpty()) {
            StaticInfo first = staticInfos.get( 0 );
            // TODO [YASIRU] : -- KRV
            Pair<Long, Long> locs = ir.getStartEndPcs( staticInfos, first );
            startPcs.add( locs.first );
            endPcs.add( locs.second );
        }

        List<javafx.util.Pair<InstructionTraceUnit, StaticInfo>> lis = ir.walkFileAndGetInstructions( instructionTraceFile,
                staticInfos, 1 );
        /* need to filter unwanted instrs from the file we got */
        lis = ir.filterInstructionTrace( startPcs, endPcs, lis );

        List<Pair<Output, StaticInfo>> instrsForward = OutputInstructionUtils.instraceToOutputAdabpter( lis );
        List<Pair<Output, StaticInfo>> instrsBackward = new ArrayList<>();

        for (Pair<Output, StaticInfo> outputStaticInfoPair : instrsForward) {
            instrsBackward.add( 0, outputStaticInfoPair );
        }

        // TODO [YASIRU] : -- KRV
        OutputInstructionUtils.updateRegsToMemRange( instrsForward );
        OutputInstructionUtils.updateRegsToMemRange( instrsBackward );

        // TODO [YASIRU] : -- KRV
        List<Integer> startPcsInteger = new ArrayList<>();
        for (Iterator<Long> it = startPcs.iterator() ; it.hasNext() ; ) {
            Long num = it.next();
            Integer numInt = num.intValue();
            startPcsInteger.add( numInt );
        }
        OutputInstructionUtils.updateFloatingPointRegs( instrsBackward, 2, staticInfos, startPcsInteger );
        OutputInstructionUtils.updateFloatingPointRegs( instrsForward, 1, staticInfos, startPcsInteger );
        /* ---------------------------- memory input output selection---------------------*/
        logger.debug( "Memory input output selection" );
        List<Long> candidateIns = new ArrayList<>();
        List<Long> startPointMem = InstructionTracer.getInstance().getInstraceStartpoints( instrsForward, startPcs );

        MemoryRegionUtils.removePossibleStackFrames( memoryLayoutPCMemoryRegion, memoryLayoutMemoryInfo, staticInfos,
                instrsForward );

        /* merge these two information - instrace mem info + mem dump info */
        List<MemoryRegion> totalMemRegions = new ArrayList<>();
        List<MemoryRegion> imageRegions = MemoryRegionUtils.mergeInstraceAndDumpRegions( totalMemRegions,
                memoryLayoutMemoryInfo, dumpRegions );

        /* get possible buffers - should be able to merge these analysis */
        MemoryRegionUtils.markPossibleBuffers(memoryLayoutPCMemoryRegion, totalMemRegions, staticInfos, instrsForward);
        List<MemoryRegion> regions = MemoryAnalyser.getInstance().getInputOutputRegions(imageRegions, totalMemRegions, memoryLayoutPCMemoryRegion,
                candidateIns, instrsForward, startPointMem);

        MemoryRegion inputMemoryRegion = regions.get(0);
        MemoryRegion outputMemoryRegion = regions.get(1);

        List<MemoryRegion> inputRegions = new ArrayList<>();

        /* ---------------------------- forward analysis -------------------------------*/

        ArrayList<Long> appPc = new ArrayList<>();
        ArrayList<Long> appPcIndirect;
        ArrayList<Long> appPcTotal = new ArrayList<>();
        ArrayList<ArrayList<Long>> appPcVec = new ArrayList<>();
        ArrayList<JumpInfo> condAppPc;

        logger.info( "before filter static ins : {}", staticInfos.size() );
        Preprocess.filterDisamVector( instrsForward, staticInfos );
        logger.info( "after filter static ins : {}", staticInfos.size() );

        appPcVec.add( appPc );
        appPcVec.add( appPcTotal );

        // others seems to be useless - to check

        /* ---------------------------- Tree Construction ------------------------------*/
        logger.debug( "Tree Building" );

        List<List<ConcreteTree>> clusteredTrees = null;
        List<ConcreteTree> concreteTrees = null;

        // capture the function start points if the end trace is not given specifically
        List<Long> startPoints = null;
        List<Long> startPointsForward = null;

//        if (true) { // endTrace == FILE_ENDING
        startPoints = InstructionTracer.getInstance().getInstraceStartpoints( instrsBackward, startPcs );
        startPointsForward = InstructionTracer.getInstance().getInstraceStartpoints( instrsForward, startPcs );
//        }


//        if (true) { // tree_build == BUILD_CLUSTERS = 4
        Long farthest = MemoryRegionUtils.getFarthestMemAccessPoint( totalMemRegions );
//        clusteredTrees = ConcreteTreeUtils.clusterTrees( imageRegions, totalMemRegions, startPoints,
//                instrsBackwards, farthest, outputFolder + fileSubString, funcReplacements );
//        }

        // number the trees - for all conc tree built
//        if (true) { // tree_build == BUILD_CLUSTERS = 4
        for (List<ConcreteTree> clusteredTree : clusteredTrees) {
            for (ConcreteTree concreteTree : clusteredTree) {
                concreteTree.numberTreeNodes();
            }
        }
//        }

        /*Halide generation*/
        /*HalideProgram halideProgram=new HalideProgram();
        halideProgram.generateHalide();*/
    }


}
