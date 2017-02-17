package lk.ac.mrt.projectx.buildex.trees;

import lk.ac.mrt.projectx.buildex.DefinesDotH;
import lk.ac.mrt.projectx.buildex.models.Pair;
import lk.ac.mrt.projectx.buildex.models.common.CommonUtil;
import lk.ac.mrt.projectx.buildex.models.common.StaticInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemDirection;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryInfo;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.PCMemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.MemoryType;
import lk.ac.mrt.projectx.buildex.models.output.Operand;
import lk.ac.mrt.projectx.buildex.models.output.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static lk.ac.mrt.projectx.buildex.models.common.CommonUtil.getExtents;
import static lk.ac.mrt.projectx.buildex.models.common.CommonUtil.getStride;
import static lk.ac.mrt.projectx.buildex.models.common.StaticInfoUtil.getStaticInfo;
import static lk.ac.mrt.projectx.buildex.models.memoryinfo.MemDirection.*;

/**
 * Created by Lasantha on 04-Jan-17.
 */
public class MemoryRegionUtils {

    private static final Logger logger = LogManager.getLogger(MemoryRegionUtils.class);

    //region public methods

    // stride was a pointer. - Not considered it as a pointer here. if needs, it should be implemented
    public static ArrayList<Long> getNbdOfRandomPoints2(ArrayList<MemoryRegion> memoryRegions, int seed, int stride) {

        MemoryRegion randomMemRegion = getRandomOutputRegion(memoryRegions);
        //cout << hex << random_mem_region->start << endl;
        long memLocation = getRandomMemLocation(randomMemRegion, seed);
        logger.info("random mem location we got - {}", memLocation);
        stride = randomMemRegion.getBytesPerPixel();

        ArrayList<Long> nbdLocations = new ArrayList<>();
        ArrayList<Integer> base = getMemPosition(randomMemRegion, memLocation);
        nbdLocations.add(memLocation);

        /*
        // printing - ignored
        cout << "base : " << endl;
        for (int j = 0; j < base.size(); j++){
            cout << base[j] << ",";
        }
        cout << endl;
        */

        //get a nbd of locations - diagonally choose pixels
        int boundary = (int) ((randomMemRegion.getDimension() + 2) / 2.0);
        logger.info("boundary : {}", boundary);

        int count = 0;

        int[] val = new int[(int) randomMemRegion.getDimension()];

        for (int i = -boundary; i <= boundary; i++) {

            ArrayList<Integer> offset = new ArrayList<>();
            int affected = count % (int) randomMemRegion.getDimension();
            if (count == 4) {
                count++;
                val[affected]++;
                continue;
            }
            for (int j = 0; j < base.size(); j++) {
                if (j == affected) {
                    val[j]++;
                    if (val[j] + base.get(j) < randomMemRegion.getExtents()[j] && val[j] + base.get(j) > 0) {
                        offset.add(val[j]);
                    } else {
                        offset.add(0);
                    }

                } else offset.add(0);
            }

            /*
            cout << "offset" << endl;
            for (int j = 0; j < offset.size(); j++){
                cout << offset[j] << ",";
            }
            cout << endl;
            */


            memLocation = getMemLocation(base, offset, randomMemRegion);

            if (memLocation == 0) {
                logger.error("ERROR: random memory location out of bounds");
            }

            nbdLocations.add(memLocation);
            count++;
        }

        return nbdLocations;


    }

    public static long getRandomMemLocation(MemoryRegion region, int seed) {

        logger.info("selecting a random output location now.....");

        Random rand = new Random();
        rand.setSeed(seed);
        int random_num = rand.nextInt();

        ArrayList<Integer> base = new ArrayList<>();
        ArrayList<Integer> offset = new ArrayList<>();
        for (long i = 0; i < region.getDimension(); i++) {
            offset.add(0);  // TODO this is not sure for 100%
        }
        //vector<int> offset(region->dimensions, 0);

        for (int i = 0; i < region.getDimension(); i++) {
            base.add(random_num % (int) region.getExtents()[i]);
//            cout << dec << base[i] << endl;
//            cout << "reg extents : " << region->extents[i] << endl;
        }

//        for (int i = 0; i < region->dimensions; i++){
//            cout << "strides: " << region->strides[i] << endl;
//            cout << "extent: " << region->extents[i] << endl;
//        }


        long memLocation = getMemLocation(base, offset, region);

        if (memLocation == 0) {
            logger.error("ERROR: random memory location out of bounds");
        }

        return memLocation;
    }

    /* abstracting memory locations from mem_regions */
    public static long getMemLocation(ArrayList<Integer> base, ArrayList<Integer> offset, MemoryRegion memRegion) {

        // success boolean parameter ignored.

        if (base.size() != memRegion.getDimension()) {
            logger.error("ERROR: dimensions dont match up");
        }

        for (int i = 0; i < base.size(); i++) {
            base.set(i, base.get(i) + offset.get(i));
        }

        for (int i = 0; i < base.size(); i++) {
            if (base.get(i) >= memRegion.getExtents()[i]) {
                return 0;
            }
        }

        long retAddr;
        if (memRegion.getStartMemory() < memRegion.getEndMemory()) {
            retAddr = memRegion.getStartMemory();
            for (int i = 0; i < base.size(); i++) {
                retAddr += memRegion.getStrides()[i] * base.get(i);
            }
        } else {
            retAddr = memRegion.getStartMemory();
            for (int i = 0; i < base.size(); i++) {
                retAddr -= memRegion.getStrides()[i] * base.get(i);
            }
        }

        return retAddr;

    }

    public static MemoryRegion getRandomOutputRegion(ArrayList<MemoryRegion> regions) {

        logger.info("selecting a random output region now.......");

	    /*get the number of intermediate and output regions*/
        int noRegions = 0;
        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i).getMemDirection() == MemDirection.MEM_INTERMEDIATE || regions.get(i).getMemDirection() == MEM_OUTPUT) {
                noRegions++;
            }
        }

        Random rand = new Random();
        int random = rand.nextInt(noRegions);

        noRegions = 0;

        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i).getMemDirection() == MemDirection.MEM_INTERMEDIATE || regions.get(i).getMemDirection() == MEM_OUTPUT) {
                if (noRegions == random) {
                    logger.info("random output region seleted");
                    return regions.get(i);
                }
                noRegions++;
            }
        }

        return null; /*should not reach this point*/


    }

    public static ArrayList<Integer> getMemPosition(MemoryRegion memoryRegion, long memValue) {

        ArrayList<Integer> pos = new ArrayList<>();
        ArrayList<Integer> rPos = new ArrayList<>();

	/* dimensions would always be width dir(x), height dir(y) */

	/*get the row */

        long offset;

        if (memoryRegion.getStartMemory() < memoryRegion.getEndMemory()) {
            offset = memValue - memoryRegion.getStartMemory();
        } else {
            offset = memoryRegion.getStartMemory() - memValue;
        }

        for (int i = (int) memoryRegion.getDimension() - 1; i >= 0; i--) {
            int pointOffset = (int) (offset / memoryRegion.getStrides()[i]);
            if (pointOffset >= memoryRegion.getExtents()[i]) {
                pointOffset = -1;
            }
            rPos.add(pointOffset);

            offset -= pointOffset * memoryRegion.getStrides()[i];

        }

        for (int i = 0; i < rPos.size(); i++) {
            pos.add(rPos.get(i));
        }

        return pos;

    }

    public static long getRegionSize(MemoryRegion region) {

        long size = 1;
        int dimension = (int) region.getDimension();
        for (int i = 0; i < dimension; i++) {
            size *= region.getExtents()[i];
        }
        return size;

    }

    public static MemoryRegion getMemRegion(Integer value, List<MemoryRegion> memoryRegions) {
        MemoryRegion region = null;
        for (MemoryRegion memRegion : memoryRegions) {
            if (memRegion.getStartMemory() < memRegion.getEndMemory()) {
                // start <= value <= end
                if ((memRegion.getStartMemory() <= value) && memRegion.getEndMemory() >= value) {
                    region = memRegion;
                    break;
                }
            } else {
                // end <= value <= start
                if ((memRegion.getStartMemory() >= value) && (memRegion.getEndMemory() <= value)) {
                    region = memRegion;
                    break;
                }
            }
        }
        return region;
    }

    public static boolean isWithinMemRegion(MemoryRegion memoryRegion, int value) {

        if (memoryRegion.getStartMemory() < memoryRegion.getEndMemory()) {
            return (value >= memoryRegion.getStartMemory()) && (value <= memoryRegion.getEndMemory());
        } else {
            return (value >= memoryRegion.getEndMemory()) && (value <= memoryRegion.getStartMemory());
        }
    }

    public static Long getFarthestMemAccessPoint(List<MemoryRegion> regions) {
        Long maxAddr = 0L;
        for (MemoryRegion region : regions) {
            if ((region.getStartMemory() < region.getEndMemory()) && (maxAddr < region.getEndMemory())) {
                maxAddr = region.getEndMemory();
            } else if ((region.getStartMemory() > region.getEndMemory()) && (maxAddr < region.getStartMemory())) {
                maxAddr = region.getStartMemory();
            }
        }
        return maxAddr + 16;
    }

    public static void removePossibleStackFrames(List<PCMemoryRegion> pcMem, List<MemoryInfo> mem, List<StaticInfo>
            info, List<Pair<Output, StaticInfo>> instrs) {
        logger.debug("Removing stack frames and out of scope mem infos");
        logger.debug("Mem info size - %d", mem.size());

        for (Iterator<MemoryInfo> memoryInfoIterator = mem.iterator(); memoryInfoIterator.hasNext(); ) {
            boolean found = false;
            MemoryInfo curMem = memoryInfoIterator.next();
            for (PCMemoryRegion pcMemRegion : pcMem) {
                List<MemoryInfo> memoryInfos = pcMemRegion.getRegions();
                for (MemoryInfo memInfo : memoryInfos) {
                    if (CommonUtil.isOverlapped(curMem.getStart(), curMem.getEnd(), memInfo.getStart(), memInfo.getEnd())) {
                        StaticInfo staticInfo = getStaticInfo(info, pcMemRegion.getPc());
                        if (staticInfo.getExampleLine() == -1) {
                            continue;
                        }
                        Output instr = instrs.get(staticInfo.getExampleLine()).first;
                        if ((memInfo.getDirection() & MemDirection.MEM_OUTPUT.ordinal()) == MemDirection.MEM_OUTPUT.ordinal()) {
                            for (Operand opnd : instr.getDsts()) {
                                if ((opnd.getType() == MemoryType.MEM_HEAP_TYPE) || (opnd.getType() == MemoryType.MEM_STACK_TYPE)) {
                                    // TODO : why only two addresses
                                    for (int addr = 0; addr < 2; addr++) {
                                        if (opnd.getAddress().get(addr).getValue().intValue() != 0) {
                                            if ((opnd.getAddress().get(addr).memRangeToRegister() != DefinesDotH
                                                    .DR_REG.DR_REG_RBP) && (opnd.getAddress().get(addr).memRangeToRegister() != DefinesDotH
                                                    .DR_REG.DR_REG_RSP)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    } // for (int addr = 0 ; addr < 2 ; addr++)
                                }
                            } // for (Operand opnd : instr.getDsts())
                        } //if ((memInfo.getDirection() & MemDirection.MEM_OUTPUT.ordinal()) == MemDirection.MEM_OUTPUT.ordinal())

                        if ((memInfo.getDirection() & MEM_INPUT.ordinal()) == MEM_INPUT.ordinal()) {
                            for (Operand opnd : instr.getSrcs()) {
                                if ((opnd.getType() == MemoryType.MEM_HEAP_TYPE) || (opnd.getType() == MemoryType.MEM_STACK_TYPE)) {
                                    for (int addr = 0; addr < 2; addr++) {
                                        if (opnd.getAddress().get(addr).getValue().intValue() != 0) {
                                            if ((opnd.getAddress().get(addr).memRangeToRegister() != DefinesDotH
                                                    .DR_REG.DR_REG_RBP) && (opnd.getAddress().get(addr).memRangeToRegister() != DefinesDotH
                                                    .DR_REG.DR_REG_RSP)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

            if (!found) {
                mem.remove(curMem);
            }
        }

        logger.debug("mem infor size after - %d", mem.size());
    }

    public static List<MemoryRegion> mergeInstraceAndDumpRegions(List<MemoryRegion> totalRegions, List<MemoryInfo>
            memInfos, List<MemoryRegion> memoryRegions) {

        logger.info("Merge instrace and dump regions");

        List<MemoryRegion> finalRegions = new ArrayList<>();

        Boolean[] mergedMemInfo = new Boolean[memInfos.size()];
        for (int i = 0; i < memInfos.size(); i++) {
            mergedMemInfo[i] = false;
        }

        // mem regions are from dumps and check whether overlap with instrace regions
        for (MemoryRegion memoryRegion : memoryRegions) {
            Long regionStart = 0L;
            Long regionEnd = 0L;
            if (memoryRegion.getStartMemory() > memoryRegion.getEndMemory()) {
                regionStart = memoryRegion.getEndMemory();
                regionEnd = memoryRegion.getStartMemory();
                logger.debug("Region start is greater than end");
            } else {
                regionStart = memoryRegion.getStartMemory();
                regionEnd = memoryRegion.getEndMemory();
                logger.debug("Region start is less than end");
            }

            for (int j = 0; j < memInfos.size(); j++) {
                if (CommonUtil.isOverlapped(regionStart, regionEnd - 1, memInfos.get(j).getStart(),
                        memInfos.get(j).getEnd() - 1)) {
                    mergedMemInfo[j] = true;
                    memoryRegion.setMemDirection(MemDirection.values()[((int) memInfos.get(j).getDirection())]);

                    // not printing the debug information

                    MemoryInfo info = getDeepestEnclosing(memoryRegion, memInfos.get(j));
                    // If the memory region is completely contained in the region constructed by meminfo
                    if (info != null) {
                        logger.debug("Found deepest enclosing : start : %d end : %d ", info.getStart(), info.getEnd());
                        if (memoryRegion.getStartMemory() < memoryRegion.getEndMemory()) {


                            Long start = memoryRegion.getStartMemory();
                            Long end = memoryRegion.getEndMemory();
                            // how much to the left of start is the meminfo spread
                            List<Long> leftSpread = new ArrayList<>();
                            // how much to the right of the end of the meminfo are we spread
                            List<Long> rightSpread = new ArrayList<>();

                            for (int k = ((int) memoryRegion.getDimension()) - 1; k >= 0; k--) {
                                long stride = memoryRegion.getStrides()[k];
                                Long lSpread = (start - info.getStart()) / stride;
                                start = memoryRegion.getStartMemory() - lSpread * stride;
                                leftSpread.add(lSpread);

                                Long rSpread = (info.getEnd() - end) / stride;
                                end = memoryRegion.getEndMemory() + rSpread * stride;
                                rightSpread.add(rSpread);
                            }

                            logger.debug("Left spread ------------");
                            logger.debug("Right spread ------------");

                        }
                        if (memoryRegion.getStartMemory() < memoryRegion.getEndMemory()) {
                            memoryRegion.setStartMemory(info.getStart());
                            memoryRegion.setEndMemory(info.getEnd());
                        } else {
                            memoryRegion.setStartMemory(info.getEnd());
                            memoryRegion.setEndMemory(info.getStart());
                        }

                        logger.debug("dims : ", info.getNumberDimensions());
                        logger.debug("new start : ", memInfos.get(j).getStart());
                        logger.debug("new end : ", memInfos.get(j).getEnd());

                        if (memoryRegion.getDimension() == info.getNumberDimensions()) {
                            /*
                                if we get the dimensionality correct on out memory analysys we should
                                use it instead of the memory dump information
                             */
                            for (int k = 0; k < memoryRegion.getDimension(); k++) {
                                memoryRegion.getExtents()[k] = getExtents(info, k + 1, info.getNumberDimensions());
                            }
                        }

                        // added - for invert
                        if (memoryRegion.getBytesPerPixel() != info.getProbStride()) {
                            Long factor = info.getProbStride() / memoryRegion.getBytesPerPixel();
                            // TODO : check casting data loss
                            memoryRegion.setBytesPerPixel(((int) info.getProbStride()));
                            memoryRegion.getStrides()[0] = info.getProbStride();
                            memoryRegion.getExtents()[0] /= factor;
                        }

                        finalRegions.add(memoryRegion);
                        totalRegions.add(memoryRegion);
                        memoryRegion.setOrder(memInfos.get(j).getOrder());
                        break;
                    } else { // TODO : FIX ME
                        logger.warn("Regions is greater than what is accesses; may be not whole image accessed");
                        finalRegions.add(memoryRegion);
                        totalRegions.add(memoryRegion);
                        memoryRegion.setOrder(memInfos.get(j).getOrder());
                        break;
                    }
                }
            }
        }

        // create new mem_regions for the remaining mem_info which of type MEM_HEAP - postpone the implementation;
        // these are intermediate nodes
        for (int i = 0; i < memInfos.size(); i++) {
            MemoryInfo memoryInfo = memInfos.get(i);
            if (mergedMemInfo[i] == false) { // if not merged
                if (memInfos.get(i).getType() == MemoryType.MEM_HEAP_TYPE) {
                    MemoryRegion mem = new MemoryRegion();
                    mem.setStartMemory(memoryInfo.getStart());
                    mem.setEndMemory(memoryInfo.getEnd());
                    mem.setDimension(memoryInfo.getNumberDimensions()); // we don't know the dimension of this yet
                    mem.setBytesPerPixel(((int) memoryInfo.getProbStride()));
                    for (int j = 1; j < mem.getDimension(); j++) {
                        mem.getStrides()[j - 1] = getStride(memoryInfo, j, mem.getDimension());
                        mem.getExtents()[j - 1] = getExtents(memoryInfo, j, mem.getDimension());
                    }
                    mem.setPaddingFilled(0);
                    mem.setMemDirection(MemDirection.values()[memoryInfo.getDirection()]);
                    mem.setOrder(memoryInfo.getOrder());
                    totalRegions.add(mem);
                }
            }
        }

        // naming the memory region
        int inputs = 0;
        int intermediates = 0;
        int outputs = 0;

        for (MemoryRegion totalRegion : totalRegions) {
            if (totalRegion.getMemDirection() == MEM_INPUT) {
                inputs++;
                totalRegion.setName("input_" + inputs);
            } else if (totalRegion.getMemDirection() == MEM_OUTPUT) {
                outputs++;
                totalRegion.setName("output_" + outputs);
            } else if (totalRegion.getMemDirection() == MEM_INTERMEDIATE) {
                intermediates++;
                totalRegion.setName("inter_" + intermediates);
            }
        }

        logger.debug("No of image mem regions after merging - %d", finalRegions.size());
        logger.debug("Total number of mem regions (from instrace) - %d", totalRegions.size());
        logger.debug("mergeInstraceAndDumpRegions - done");

        return finalRegions;
    }

    private static MemoryInfo getDeepestEnclosing(MemoryRegion region, MemoryInfo info) {
        Long regionStart = region.getStartMemory() > region.getEndMemory() ? region.getEndMemory() : region
                .getStartMemory();
        Long regionEnd = region.getStartMemory() > region.getEndMemory() ? region.getStartMemory() : region.getEndMemory();
        MemoryInfo mem = null;
        if (info.getStart() <= regionStart && info.getEnd() >= regionEnd) {
            mem = info;
        }

        for (MemoryInfo memoryInfo : info.getMergedMemoryInfos()) {
            MemoryInfo ret = getDeepestEnclosing(region, memoryInfo);
            if (ret != null) {
                mem = ret;
            }
        }
        return mem;
    }

    public static void markPossibleBuffers(List<PCMemoryRegion> pcMem, List<MemoryRegion> memoryRegions,
                                           List<StaticInfo> info, List<Pair<Output, StaticInfo>> instrs) {
        for (MemoryRegion memoryRegion : memoryRegions) {
            boolean found = false;
            for (PCMemoryRegion pcMemoryRegion : pcMem) {
                boolean printed = true;
                List<MemoryInfo> memoryInfos = pcMemoryRegion.getRegions();
                for (MemoryInfo memoryInfo : memoryInfos) {
                    if (CommonUtil.isOverlapped(memoryRegion.getStartMemory(),
                            memoryRegion.getEndMemory(), memoryInfo.getStart(),
                            memoryInfo.getEnd())) {
                        StaticInfo ind = getStaticInfo(info, pcMemoryRegion.getPc());
                        if (ind.getExampleLine() == -1) {
                            continue;
                        }
                        Output instr = instrs.get(ind.getExampleLine()).first;
                        if (!printed) {
                            logger.debug("%s", instr);
                            printed = true;
                        }

                        if ((memoryInfo.getDirection() & MEM_OUTPUT.ordinal()) == MEM_OUTPUT.ordinal()) {
                            for (Operand operand : instr.getDsts()) {
                                if (operand.getType() == MemoryType.MEM_HEAP_TYPE || operand.getType() == MemoryType.MEM_STACK_TYPE) {
                                    for (int addr = 0; addr < 2; addr++) {
                                        if (operand.getAddress().get(addr).getValue().intValue() != 0) {
                                            if (operand.getAddress().get(addr).memRangeToRegister() != DefinesDotH.DR_REG.DR_REG_RBP &&
                                                    operand.getAddress().get(addr).memRangeToRegister() != DefinesDotH.DR_REG.DR_REG_RSP)
                                                found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if ((memoryInfo.getDirection() & MEM_INPUT.ordinal()) == MEM_INPUT.ordinal()) {
                            for (Operand operand : instr.getSrcs()) {
                                if (operand.getType() == MemoryType.MEM_HEAP_TYPE || operand.getType() == MemoryType.MEM_STACK_TYPE) {
                                    for (int addr = 0; addr < 2; addr++) {
                                        if (operand.getAddress().get(addr).getValue().intValue() != 0) {
                                            if (operand.getAddress().get(addr).memRangeToRegister() != DefinesDotH.DR_REG.DR_REG_RBP &&
                                                    operand.getAddress().get(addr).memRangeToRegister() != DefinesDotH.DR_REG.DR_REG_RSP)
                                                found = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(found) break;
                    }
                    if(found) break;
                }
                if (!found) {
                    logger.debug("Region not found to be a buffer");
                    logger.debug("Start : %d End : %d", memoryRegion.getStartMemory(), memoryRegion.getEndMemory());
                }
                assert found : "ERROR : all buffers should be captured";
            }
        }
    }

    /* extracting random locations from the mem regions */
    ArrayList<Long> getNbdOfRandomPoints(ArrayList<MemoryRegion> memoryRegions, int seed, int stride) {

	    /*ok we need find a set of random locations */
        MemoryRegion randomMemRegion = getRandomOutputRegion(memoryRegions);
        //cout << hex << random_mem_region->start << endl;
        long memLocation = getRandomMemLocation(randomMemRegion, seed);
        logger.info("random mem location we got - {}", memLocation);
        stride = randomMemRegion.getBytesPerPixel();

        ArrayList<Long> nbdLocations = new ArrayList<>();
        ArrayList<Integer> base = getMemPosition(randomMemRegion, memLocation);
        nbdLocations.add(memLocation);

        //get a nbd of locations - diagonally choose pixels
        int boundary = (int) ((randomMemRegion.getDimension() + 2) / 2.0);
        logger.info("boundary : {}", boundary);

        int count = 0;

        int[] val = new int[(int) randomMemRegion.getDimension()];

        for (int i = -boundary; i <= boundary; i++) {

            if (i == 0) {
                continue;
            }

            ArrayList<Integer> offset = new ArrayList<>();
            int affected = count % (int) randomMemRegion.getDimension();
            for (int j = 0; j < base.size(); j++) {
                if (j == affected) {
                    if (base.get(j) + i < 0 || base.get(j) + i >= randomMemRegion.getExtents()[j]) {
                        offset.add(0);
                    } else {
                        offset.add(i);
                    }

                } else offset.add(0);
            }

            memLocation = getMemLocation(base, offset, randomMemRegion);

            if (memLocation == 0) {
                logger.error("ERROR: random memory location out of bounds");
            }

            nbdLocations.add(memLocation);
            count++;
        }

        return nbdLocations;
    }
    //endregion public methods

}
