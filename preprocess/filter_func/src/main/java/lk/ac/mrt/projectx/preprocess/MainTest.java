package lk.ac.mrt.projectx.preprocess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import lk.ac.mrt.projectx.buildex.ProjectXImage;

import javax.imageio.ImageIO;

/**
 * Created by Lasantha on 02-Dec-16.
 */

public class MainTest {
    private static final Logger logger = LogManager.getLogger(MainTest.class);

    public static void main(String[] args) {
        MainTest mainTest = new MainTest();

        mainTest.setOutputFolderPath("E:\\FYP\\Java Ported\\Test Files\\output_files");
        mainTest.setImageFolderPath("E:\\FYP\\Java Ported\\Test Files\\images");
        mainTest.setFilterFilesFolderPath("E:\\FYP\\Java Ported\\Test Files\\filter_files");
        mainTest.setInImageFileName("arith.png");
        mainTest.setOutImageFileName("aritht.png");
        mainTest.setExeFileName("halide_threshold_test.exe");
        mainTest.setThreshold(80);
        mainTest.setFilterMode(1);
        mainTest.setBufferSize(0);

        try {
            mainTest.runAlgorithmDiffMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainTest() {
        this.profileData = new ArrayList<byte[]>();
        this.memtraceData = new ArrayList<byte[]>();
    }


    private final int DIFF_MODE = 1;
    private final int TWO_IMAGE_MODE = 2;
    private final int ONE_IMAGE_MODE = 3;

    private String outputFolderPath;
    private String filterFilesFolderPath;
    private String imageFolderPath;
    private String inImageFileName;
    private String outImageFileName;
    private String exeFileName;
    private int filterMode;
    private int bufferSize;
    private int threshold;  // continuous chunck % of image
    private ArrayList<byte[]> profileData;
    private ArrayList<byte[]> memtraceData;

    public void setOutputFolderPath(String outputFolderPath) {
        this.outputFolderPath = outputFolderPath;
    }

    public void setImageFolderPath(String imageFolderPath) {
        this.imageFolderPath = imageFolderPath;
    }

    public void setFilterFilesFolderPath(String filterFilesFolderPath) {
        this.filterFilesFolderPath = filterFilesFolderPath;
    }

    public void setInImageFileName(String inImageFileName) {
        this.inImageFileName = inImageFileName;
    }

    public void setOutImageFileName(String outImageFileName) {
        this.outImageFileName = outImageFileName;
    }

    public void setExeFileName(String exeFileName) {
        this.exeFileName = exeFileName;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setFilterMode(int filterMode) {
        this.filterMode = filterMode;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private void readMemtraceAndProfileFiles() {

        String profileFileNameFormat = "profile_" + exeFileName + "_" + inImageFileName;
        String memtraceFileNameFormat = "memtrace_" + exeFileName + "_" + inImageFileName;

        // go through all the files in the output folder to find matches for profile and memtrace
        File folder = new File(outputFolderPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                byte[] data;
                if (fileName.contains(profileFileNameFormat)) {
                    // read profile files
                    data = getFileContent(listOfFiles[i].getPath());
                    profileData.add(data);
                } else if (fileName.contains(memtraceFileNameFormat)) {
                    // read memtrace files
                    data = getFileContent(listOfFiles[i].getPath());
                    memtraceData.add(data);
                }
            }
        }

    }

    public void runAlgorithmDiffMode() throws IOException {

        readMemtraceAndProfileFiles();

        logger.info("Filter function DIFF MODE");

        ModuleInfo module = ModuleInfo.getPopulatedModuleInfo(profileData.get(0));

        ProjectXImage inImage = new ProjectXImage(ImageIO.read(new File(imageFolderPath + "\\" + inImageFileName)));
        logger.info("Input Image Read Done! - {}", imageFolderPath + "\\" + inImageFileName);

        ProjectXImage outImage = new ProjectXImage(ImageIO.read(new File(imageFolderPath + "\\" + outImageFileName)));
        logger.info("Output Image Read Done! - {}", imageFolderPath + "\\" + outImageFileName);

        // getting the highest executed basic block
        logger.info("Finding the highest executed basic block...");

        ModuleInfo maxModule = null;
        ModuleInfo tempModule = module;
        int maxFrequency = 0;
        BasicBlockInfo maxBasicBlock = null;
        while (tempModule != null) {
            ArrayList<FunctionInfo> functions = tempModule.getFunctions();
            for (int i = 0; i < functions.size(); i++) {
                ArrayList<BasicBlockInfo> bbs = functions.get(i).getBasicBlocks();
                for (int j = 0; j < bbs.size(); j++) {
                    BasicBlockInfo bb = bbs.get(j);
                    if (bb.getFrequency() > maxFrequency) {
                        maxFrequency = bb.getFrequency();
                        maxBasicBlock = bb;
                        maxModule = tempModule;
                    }
                }
            }
            tempModule = tempModule.getNext();
        }
        logger.info("max module - {}, max start addr - {}", maxModule.getName(), maxBasicBlock.getStartAddress());

        logger.info("Finding the probable function...");

        long maxFunction = getProbableFunction(maxModule, maxBasicBlock.getStartAddress());

        logger.info("Enclosed function = {}", maxFunction);

        /* parsing memtrace files to pc_mem_regions */

        logger.info("Getting memory region information...");
        ArrayList<PcMemoryRegion> pcMems = PcMemoryRegion.getMemRegionFromMemTrace(memtraceData, module);

        logger.info("linking memory regions together...");
        PcMemoryRegion.linkMemRegions(pcMems, 1);    //TODO not tested

        logger.info("filtering out insignificant regions...");
        /* all memory related information */
        /************* Skipped because not using this ***********/

        if (bufferSize == 0) {
            PcMemoryRegion.filterMemRegions(pcMems, inImage, outImage, threshold);
        } else {
            //TODO not implemented because not using here
            //filter_mem_regions_total(pc_mems, total_size, threshold);
        }

        logger.info("Memory regions filtering - DONE!");

        ArrayList<InternalFunctionInfo> funcInfo = new ArrayList<>();

        /* get the pc_mems and there functional info as well as filter the pc_mems which are not in the func */
        for (int i = 0; i < pcMems.size(); i++) {
            logger.info("Entered finding funcs...");
            ModuleInfo md = ModuleInfo.findModuleByName(module, pcMems.get(i).getModule());
            if (md == null) {
                logger.error("ERROR: the module should be present");
            }

            BasicBlockInfo bbInfo = BasicBlockInfo.findBasicBlock(md, pcMems.get(i).getPc());
            if (bbInfo == null) {
                logger.error("ERROR: bbinfo should be present");
            }

            long funcStart = getProbableFunction(md, bbInfo.getStartAddress());

            if (funcStart == 0) {
                continue;
            }

            logger.info("module - {}, start - {} (in dec)", pcMems.get(i).getModule(), funcStart);

            boolean isThere = false;
            int index = 0;
            for (int j = 0; j < funcInfo.size(); j++) {
                InternalFunctionInfo func = funcInfo.get(j);
                if (func.address == funcStart && func.name.equals(md.getName())) {
                    isThere = true;
                    index = j;
                    break;
                }
            }

            if (!isThere) {
                InternalFunctionInfo newFunc = new InternalFunctionInfo();
                newFunc.name = md.getName();
                newFunc.address = funcStart;
                newFunc.frequency = 1;
                newFunc.candidateInstructions.add(pcMems.get(i).getPc());
                newFunc.bbStart.add(bbInfo.getStartAddress());
                funcInfo.add(newFunc);
            } else {
                funcInfo.get(index).frequency++;
                boolean found = false;
                for (int j = 0; j < funcInfo.get(index).candidateInstructions.size(); j++) {
                    if (pcMems.get(i).getPc() == funcInfo.get(index).candidateInstructions.get(j)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    funcInfo.get(index).candidateInstructions.add(pcMems.get(i).getPc());
                    funcInfo.get(index).bbStart.add(bbInfo.getStartAddress());
                }
            }
        }

        /* sort the probable function locations */
        Collections.sort(funcInfo);
        logger.info("Sorting the probable function locations - DONE!");

        // app pc data file writing.
        AppPcData appPcData = new AppPcData(filterFilesFolderPath + "\\" + exeFileName + "_app_pc.log");
        appPcData.setModuleName(funcInfo.get(0).name);
        appPcData.setCandidateInstructions(funcInfo.get(0).candidateInstructions);
        appPcData.saveDataToFile();

        // filter data file writing
        FilterData filterData = new FilterData(filterFilesFolderPath + "\\" + exeFileName + ".log");
        filterData.setModuleName(funcInfo.get(0).name);
        filterData.setFunctionAddress(funcInfo.get(0).address);
        filterData.saveDataToFile();

        /* print out the summary */
        System.out.println("**********************Summary of localization**************************************");

		/* print out the maximum executed basic block summary */
        System.out.println("1. maximum executed basic block summary");
        System.out.println(" module name - " + maxModule.getName());
        System.out.println(" bb start addr - " + maxBasicBlock.getStartAddress());
        System.out.println(" bb freq - " + maxBasicBlock.getFrequency());
        System.out.println(" enclosed function - " + maxFunction);

        System.out.println("2. functions accessing candidate instructions ");
        /*print out the function with the most number of candidate instructions */
        for (int i = 0; i < funcInfo.size(); i++) {
            System.out.println((i + 1) + " - function");
            System.out.println(" func addr - " + funcInfo.get(i).address);
            System.out.println(" module name - " + funcInfo.get(i).name);
            System.out.println(" amount of candidate instructions - " + funcInfo.get(i).frequency);
            System.out.println(" candidate instructions - ");

            ArrayList<Integer> candidateInstructions = funcInfo.get(i).candidateInstructions;
            for (int j = 0; j < candidateInstructions.size(); j++) {
                System.out.println("\t" + candidateInstructions.get(j) + " - " + funcInfo.get(i).bbStart.get(j));
            }
        }

    }


    private static final int MAX_RECURSE = 200;

    private static long getProbableFuncEntrypoint(ModuleInfo current, LinkedList<RetAddress> bbStart, ArrayList<Integer> processed, int maxRecurse) {
        if (maxRecurse > MAX_RECURSE) {
            logger.warn("WARNING: max recursion limit reached!");
            return 0;
        }

        if (bbStart.isEmpty()) {
            return 0;
        }

        RetAddress retAddress = bbStart.poll();
        processed.add(retAddress.address);

        BasicBlockInfo bbinfo = findBbExact(current, retAddress.address);
        if (bbinfo == null) {
            return getProbableFuncEntrypoint(current, bbStart, processed, maxRecurse + 1);
        }

        if (bbinfo.isCallTarget()) {
            retAddress.ret--;
        }
        if (retAddress.ret < 0) {
            return bbinfo.getStartAddress();
        }

        if (bbinfo.isRet() && maxRecurse > 0) {
            retAddress.ret++;
        }

        logger.info("Addr : {} , ret : {} , Freq : {}", retAddress.address, retAddress.ret, bbinfo.getFrequency());

        for (int i = 0; i < bbinfo.getFromBasicBlocks().size(); i++) {
            if (!processed.contains(bbinfo.getFromBasicBlocks().get(i).getTarget())) {
                bbStart.add(new RetAddress(bbinfo.getFromBasicBlocks().get(i).getTarget(), retAddress.ret));
            }
        }
        return getProbableFuncEntrypoint(current, bbStart, processed, maxRecurse + 1);
    }

    private static BasicBlockInfo findBbExact(ModuleInfo module, long addr) {
        for (int i = 0; i < module.getFunctions().size(); i++) {
            FunctionInfo func = module.getFunctions().get(i);
            for (int j = 0; j < func.getBasicBlocks().size(); j++) {
                BasicBlockInfo bb = func.getBasicBlocks().get(j);
                if (bb.getStartAddress() == addr) {
                    return bb;
                }
            }
        }
        return null;
    }

    private static class RetAddress {
        int ret;
        int address;

        public RetAddress(int address, int ret) {
            this.ret = ret;
            this.address = address;
        }
    }

    private static class InternalFunctionInfo implements Comparable {
        String name;
        long address;
        int frequency;
        ArrayList<Integer> candidateInstructions;
        ArrayList<Long> bbStart;

        public InternalFunctionInfo() {
            this.candidateInstructions = new ArrayList<>();
            this.bbStart = new ArrayList<>();
        }

        @Override
        public int compareTo(Object o) {
            InternalFunctionInfo other = (InternalFunctionInfo) o;
            if (this.frequency > other.frequency) {
                return -1;
            } else if (this.frequency < other.frequency) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static long getProbableFunction(ModuleInfo current, long startAddr) {

        BasicBlockInfo bb = BasicBlockInfo.findBasicBlock(current, startAddr);
        if (bb != null) {
            RetAddress retAddress = new RetAddress((int) bb.getStartAddress(), 0);
            LinkedList<RetAddress> queue = new LinkedList<>();
            queue.add(retAddress);
            ArrayList<Integer> processed = new ArrayList<>();

            return getProbableFuncEntrypoint(current, queue, processed, 0);
        } else {
            return 0;
        }
    }

    private byte[] getFileContent(String filename) {
        byte[] data = null;
        logger.info("Reading file {}", filename);
        try {
            data = Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return data;
    }

}
