package lk.ac.mrt.projectx.buildex;

import lk.ac.mrt.projectx.buildex.exceptions.NoSuitableFileFoundException;
import lk.ac.mrt.projectx.buildex.files.AppPCFile;
import lk.ac.mrt.projectx.buildex.files.InstructionTraceFile;
import lk.ac.mrt.projectx.buildex.files.MemoryDumpFile;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.PCMemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.Output;
import lk.ac.mrt.projectx.buildex.models.output.OutputInstructionUtils;
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
        List<MemoryRegion> imageRegions = memoryAnalyser.getImageRegions( memoryDumpFileList, inputImage, outputImage );
        logger.info( "Found {} image regions", imageRegions.size() );
        logger.debug( imageRegions.toString() );

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

        // TODO [YASIRU] : -- KRV
        OutputInstructionUtils.updateRegsToMemRange( instrsForward );
        // TODO [YASIRU] : -- KRV
        List<Integer> startPcsInteger = new ArrayList<>();
        for (Iterator<Long> it = startPcs.iterator() ; it.hasNext() ; ) {
            Long num = it.next();
            Integer numInt = num.intValue();
            startPcsInteger.add( numInt );
        }
        OutputInstructionUtils.updateFloatingPointRegs( instrsForward, 1, staticInfos, startPcsInteger );

    }
}
