package lk.ac.mrt.projectx.buildex.models.memoryinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class MemoryRegion {

    private final static int DIMENSIONS = 3;

    private int bytesPerPixel;

    private long type;//memory region type based on dependency analysis
    private MemoryDumpType memoryDumpType;//memory region type based on dump
    private long treeDirections;//indirect or not
    private long dimension;

    /* indirect or not */
    private boolean dependant;

    private MemDirection memDirection;

    /* physical demarcations of the memory regions */
    private long startMemory, endMemory;

    private String name; // krv - needed in AbstractNode

    //halide buffer_t emulation
    private long extents[];
    private long strides[];
    private long min[];

    private long paddingField;//right left up and down
    private long padding[];//padding for four directions

    private List<Long> referingPCs;

    public MemoryRegion() {
        extents = new long[ DIMENSIONS ];
        strides = new long[ DIMENSIONS ];
        min = new long[ DIMENSIONS ];

        type = 0;
        memDirection = MemDirection.READ;
        memoryDumpType = MemoryDumpType.OUTPUT_BUFFER;
        treeDirections = 0;
        dependant = false;
    }

    public static int getDIMENSIONS() {
        return DIMENSIONS;
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
    }

    public void setBytesPerPixel(int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public MemoryDumpType getMemoryDumpType() {
        return memoryDumpType;
    }

    public void setMemoryDumpType(MemoryDumpType memoryDumpType) {
        this.memoryDumpType = memoryDumpType;
    }

    public long getTreeDirections() {
        return treeDirections;
    }

    public void setTreeDirections(long treeDirections) {
        this.treeDirections = treeDirections;
    }

    public long getDimension() {
        return dimension;
    }

    public void setDimension(long dimension) {
        this.dimension = dimension;
    }

    public boolean isDependant() {
        return dependant;
    }

    public void setDependant(boolean dependant) {
        this.dependant = dependant;
    }

    public MemDirection getMemDirection() {
        return memDirection;
    }

    public void setMemDirection(MemDirection memDirection) {
        this.memDirection = memDirection;
    }

    public long getStartMemory() {
        return startMemory;
    }

    public void setStartMemory(long startMemory) {
        this.startMemory = startMemory;
    }

    public long getEndMemory() {
        return endMemory;
    }

    public void setEndMemory(long endMemory) {
        this.endMemory = endMemory;
    }

    public long[] getExtents() {
        return extents;
    }

    public void setExtents(long[] extents) {
        this.extents = extents;
    }

    public long[] getStrides() {
        return strides;
    }

    public void setStrides(long[] strides) {
        this.strides = strides;
    }

    public long[] getMin() {
        return min;
    }

    public void setMin(long[] min) {
        this.min = min;
    }

    public long getPaddingField() {
        return paddingField;
    }

    public void setPaddingFilled(long paddingField) {
        this.paddingField = paddingField;
    }

    public long[] getPadding() {
        return padding;
    }

    public void setPadding(long[] padding) {
        this.padding = padding;
    }

    public List<Long> getReferingPCs() {
        return referingPCs;
    }

    public void setReferingPCs(List<Long> referingPCs) {
        this.referingPCs = referingPCs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MemoryRegion{" +
                "bytesPerPixel=" + bytesPerPixel +
                ", type=" + type +
                ", dumpType=" + memoryDumpType +
                ", treeDirections=" + treeDirections +
                ", dimension=" + dimension +
                ", dependant=" + dependant +
                ", direction=" + memDirection +
                ", startMemory=" + startMemory +
                ", endMemory=" + endMemory +
                ", name='" + name + '\'' +
                ", extents=" + Arrays.toString( extents ) +
                ", strides=" + Arrays.toString( strides ) +
                ", min=" + Arrays.toString( min ) +
                ", paddingField=" + paddingField +
                ", padding=" + Arrays.toString( padding ) +
                ", referingPCs=" + referingPCs +
                '}';
    }

    public List<List<Integer>> getIndexList() {
        boolean finished = false;
        List<List<Integer>> ret = new ArrayList<>();
        List<Integer> currentIndex = new ArrayList<>();

        for (int i = 0 ; i < this.dimension ; i++) {
            currentIndex.add( 0 );
        }

        while (!finished) {
            ret.add( currentIndex );
            finished = true;

            for (int i = 0 ; i < dimension ; i++) {
                if (currentIndex.get( i ) < extents[ i ] - 1) {
                    Integer val = currentIndex.get( i );
                    val++;
                    currentIndex.set( i, val );
                    for (int j = 0 ; j < i ; j++) {
                        currentIndex.set( j, 0 );
                    }
                    finished = false;
                    break;
                }
            }
        }

        return ret;
    }
}
