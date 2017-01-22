package lk.ac.mrt.projectx.buildex.trees;

import javafx.util.Pair;
import lk.ac.mrt.projectx.buildex.GeneralUtils;
import lk.ac.mrt.projectx.buildex.models.memoryinfo.MemoryRegion;
import lk.ac.mrt.projectx.buildex.models.output.MemoryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static lk.ac.mrt.projectx.buildex.x86.X86Analysis.Operation.*;

/**
 * Created by krv on 1/2/17.
 */
public abstract class Tree implements Comparable {

    final static Logger logger = LogManager.getLogger(Tree.class);

    //region private variables

    private static Integer numParas;
    protected boolean dummyTree;
    private boolean recursive;
    private Integer numNodes;
    private Integer treeNum;
    private Node head;

    //endregion private variables

    //region public constructors

    public Tree() {
        head = null;
        numNodes = 0;
        treeNum = -1;
        recursive = false;
    }

    //endregion public constructors

    //region protected methods

    public static Integer getNumParas() {
        return numParas;
    }

    //TODO : move to util
    public static boolean areTreesSimilar(List<Tree> trees) {
        List<Node> nodes = new ArrayList<>();
        for (Tree tree : trees) {
            nodes.add(tree.getHead());
        }
        return areTreeNodesSimilar(nodes) == 1 ? true : false;
    }

    /**
     * No idea what this do
     *
     * @param nodes
     * @return
     */
    private static int areTreeNodesSimilar(List<Node> nodes) {
        if (!Node.isNodesSimilar(nodes)) return 0;

        if (!nodes.isEmpty()) {
            for (int i = 0; i < nodes.get(0).srcs.size(); i++) {
                List<Node> nodesList = new ArrayList<>();
                for (int j = 0; j < nodes.size(); j++) {
                    //TODO : find the reason for needing to cast to Node
                    nodesList.add((Node) nodes.get(j).srcs.get(i));
                }
                if (areTreeNodesSimilar(nodesList) != 1) return 0;
            }
        }

        return 1;
    }


    //endregion protected methods

    //region public methods

    protected void copyUnrolledTreeStructure(Tree tree, Object peripheralData, NodeToNode NodeCreation) {
        throw new NotImplementedException();
    }

    protected void copyUnrolledTreeStructure(Node head, Node from, Node to, Object peripheralData, NodeToNode NodeCreation) {
        throw new NotImplementedException();
    }

    protected void copyExactTreeStructure(Tree tree, Object data, NodeToNode NodeCreation) {
        GeneralUtils.assertAndFail(tree.getHead().order_num != -1,
                "TODO add error message");
        GeneralUtils.assertAndFail(tree.getHead().isVisited() == false,
                "TODO add error message");

        // Get all the nodes and the nodes to which it is connected
        List<Pair<Node, List<Integer>>> treeMap = new ArrayList<>(tree.getNumNodes());
        traverseTree(tree.getHead(), treeMap, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                List<Pair<Node, List<Integer>>> map = (List<Pair<Node, List<Integer>>>) value;
                Pair<Node, List<Integer>> mapElement = map.get(node.order_num);
                if (mapElement.getKey() == null) { //todo : == null in Helium
                    for (Iterator<Node> srcIter = node.srcs.iterator() ; srcIter.hasNext() ; ) {
                        Node srcNode = srcIter.next();
                        mapElement.getValue().add(srcNode.order_num);
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });

        // create the new tree as a vector
        List<Pair<Node, List<Integer>>> newTreeMap = new ArrayList<>(tree.getNumNodes());


    }

    protected Object traverseTree(Object nde, Object value, NodeMutator nodeMutator, NodeReturnMutator nodeReturnMutator) {
        Node node = (Node) nde;
        Object nodeVal = nodeMutator.mutate(node, value);
        List<Object> traverseValue = new ArrayList<>();

        for (int i = 0; i < node.srcs.size(); i++) {
            traverseValue.add(traverseTree(node.srcs.get(i), value, nodeMutator, nodeReturnMutator));
        }

        return nodeReturnMutator.mutate(nodeVal, traverseValue, value);
    }

    public Node getHead() {
        return head;
    }

    public void setHead(Node head) {
        this.head = head;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isDummyTree() {
        return dummyTree;
    }

    public void setDummyTree(boolean dummyTree) {
        this.dummyTree = dummyTree;
    }

    public Integer getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(Integer numNodes) {
        this.numNodes = numNodes;
    }

    public Integer getTreeNum() {
        return treeNum;
    }

    public void setTreeNum(Integer treeNum) {
        this.treeNum = treeNum;
    }

    @Override
    public int compareTo(Object o) {
        Tree tree = (Tree) o;
        List<Node> nodes = new ArrayList<>();
        nodes.add(head);
        nodes.add(tree.getHead());
        return areTreeNodesSimilar(nodes);
    }

    public abstract void simplifyTree();

    public void canonicalizeTree() {
        boolean changedG = true;
        while (changedG) {

            changedG = (boolean) traverseTree(head, this, new NodeMutator() {
                @Override
                public Object mutate(Node node, Object value) {
                    Tree tree = (Tree) value;
                    Node headNode = tree.getHead();
                    Boolean changed = headNode.congregateNode();
                    return changed;
                }
            }, new NodeReturnMutator() {
                @Override
                public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                    boolean changed = (boolean) nodeValue;
                    if (changed) {
                        return changed;
                    }
                    for (int i = 0; i < traverseValue.size(); i++) {
                        changed = (boolean) traverseValue.get(i);
                        if (changed) {
                            return (boolean) changed;
                        }
                    }
                    return null;
                }
            });

        }

        traverseTree(head, null, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                node.orderNode();
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void changeHeadNode() {
        if (head.operation == op_assign) {
            GeneralUtils.assertAndFail(head.srcs.size() == 1,"TODO add proper error");
            Node newHead = (Node) head.srcs.get(0);
            newHead.prev.clear();
            newHead.pos.clear();
            setHead(newHead);
        }
    }

    public void numberTreeNodes() {
        logger.debug("Start tree numbering");
        traverseTree(head, this, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                Tree tr = (Tree) value;
                tr.numNodes = tr.numNodes + 1;
                if (node.order_num == -1) {
                    node.order_num = tr.numNodes;
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
        logger.debug("Number of trees %d", numNodes);
    }

    public void printTree(String file) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            printTree(bufferedWriter);
        } finally {
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    public void printTree(BufferedWriter file) throws IOException {
        printTreeRecursive(this.head, file);
    }

    private void printTreeRecursive(Node node, BufferedWriter bufferedWriter) throws IOException {
        Integer numSrcs = node.srcs.size();
        if (numSrcs == 0) { // this is a leaf ndoe
            bufferedWriter.write(node.symbol.toString());
        } else if (numSrcs == 1) { // unary operation
            if (node.operation == op_full_overlap) {
                bufferedWriter.write("{");
                bufferedWriter.write(node.symbol.toString());
                bufferedWriter.write(" -> ");
                bufferedWriter.write(((Node) node.srcs.get(0)).symbol.toString());
                bufferedWriter.write("}");
                bufferedWriter.write("(");
                printTreeRecursive((Node) node.srcs.get(0), bufferedWriter);
                bufferedWriter.write(")");
            } else if (node.operation == op_assign) {
                printTreeRecursive((Node) node.srcs.get(0), bufferedWriter);
            } else {
                bufferedWriter.write(node.operation.toString());
                bufferedWriter.write(" ");
                printTreeRecursive((Node) node.srcs.get(0), bufferedWriter);
            }
        } else if ((numSrcs == 2) && (node.operation != op_partial_overlap)) {
            bufferedWriter.write("(");
            printTreeRecursive((Node) node.srcs.get(0), bufferedWriter);
            bufferedWriter.write(" ");
            bufferedWriter.write(node.operation.toString());
            bufferedWriter.write(" ");
            printTreeRecursive((Node) node.srcs.get(1), bufferedWriter);
            bufferedWriter.write(")");
        } else {
            bufferedWriter.write("(");
            bufferedWriter.write(node.operation.toString());
            bufferedWriter.write(" ");
            for (int i = 0; i < numSrcs - 1; i++) {
                //TODO : Helium always calls oth srcNdoe
                printTreeRecursive((Node) node.srcs.get(i), bufferedWriter);
                bufferedWriter.write(",");
            }
            printTreeRecursive((Node) node.srcs.get(numSrcs - 1), bufferedWriter);
            bufferedWriter.write(")");
        }
    }

    //region Tree Transformationus

    public void printDot(String file, String name, int number) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            printDot(bufferedWriter, name, number);
        } finally {
            bufferedWriter.close();
            fileWriter.close();
        }
    }

    public void printDot(BufferedWriter file, String name, int number) throws IOException {
        logger.debug("Printing tree to dot file");
        StringBuilder nodesStBlder = new StringBuilder();
        StringBuilder headerStBlder = new StringBuilder();
        StringBuilder edgeStBlder = new StringBuilder();
        headerStBlder.append("digraph G_");
        headerStBlder.append(name);
        headerStBlder.append("_");
        headerStBlder.append(number);
        headerStBlder.append(" {");

        cleanupVisit();
        traverseTree(head, nodesStBlder, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                StringBuilder nodesStBlder = (StringBuilder) value;
                if (!node.isVisited()) {
                    // this implementations is from Helium "dot_get_node_string" in print_helper.cpp
                    nodesStBlder.append(node.getOrderNum());
                    nodesStBlder.append(" [label=\"");
                    nodesStBlder.append(node.getDotString());
                    nodesStBlder.append("\"];");
                    node.setVisited();
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
        cleanupVisit();

        traverseTree(head, edgeStBlder, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                if (node.isVisited()) {
                    StringBuilder edgeStBlder = ((StringBuilder) value);
                    for (Iterator<Node> srcIter = node.srcs.iterator() ; srcIter.hasNext() ; ) {
                        Node srcNode = srcIter.next();
                        edgeStBlder.append(node.getOrderNum());
                        edgeStBlder.append(" -> ");
                        edgeStBlder.append(srcNode.getOrderNum());
                        edgeStBlder.append(";");
                        edgeStBlder.append("\n");
                        node.setVisited();
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
        file.write(headerStBlder.toString());
        file.write("\n");
        file.write(nodesStBlder.toString());
        file.write("\n");
        file.write(edgeStBlder.toString());
        file.write("}");
        file.write("\n");
    }

    public void cleanupVisit() {
        logger.debug("cleaning up all the visited states");
        traverseTree(head, numNodes, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                node.setVisited(false);
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void removeAssignedNodes() {
        cleanupVisit();
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node dst, Object value) {
                Node headNode = (Node) value;
                if (dst.operation == op_assign && dst != headNode && dst.srcs.size() == 1) {
                    Node srcNode = (Node) dst.srcs.get(0);
                    for (int i = 0; i < dst.prev.size(); i++) {
                        Node preNode = (Node) dst.prev.get(i);
                        int idx = (Integer) dst.pos.get(i);
                        srcNode.prev.add(preNode);
                        srcNode.pos.add(idx);
                        preNode.srcs.remove(idx);
                        preNode.srcs.add(idx, srcNode);
                    }
                }

                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public List<MemoryRegion> identifyIntermediateBuffers(List<MemoryRegion> mem) {
        throw new NotImplementedException();
    }

    public void removeMultiplication() {
        cleanupVisit();
        removeMultiplication(head);
        cleanupVisit();
    }

    public void simplifyImmediates() {
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                List<Integer> indexes = new ArrayList<Integer>();
                if (node.operation == op_mul || node.operation == op_add) {

                    Integer val = 0;

                    for (int i = 0; i < node.srcs.size(); i++) {
                        Node loopNode = (Node) node.srcs.get(i);
                        val += (Integer) loopNode.symbol.getValue();
                        indexes.add(i);
                    }
                }

                if (!indexes.isEmpty()) {
                    logger.debug("First value : %d ", indexes.get(0));
                    Node nde = (Node) node.srcs.get(0);
                    nde.symbol.setValue((Number) value);
                    for (int i = 0; i < indexes.size(); i++) {
                        node.removeForwardReference((Node) node.srcs.get(indexes.get(i) - (i - 1)));
                    }
                }

                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void removeMinusNodes() {
        throw new NotImplementedException();
    }

    public void removeRedundantNodes() {
        removeRedundant(head);
    }

    public void convertSubNodes() {
        convertNodeSub(head);
        propogateMinus(head);
    }

    public void simplifyMinus() {
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                List<Node> postitive;
                List<Node> negative;
                if (node.operation == op_add) {
                    postitive = new ArrayList<Node>();
                    negative = new ArrayList<Node>();
                    for (int i = 0; i < node.srcs.size(); i++) {
                        if (((Node) node.srcs.get(i)).minus) {
                            negative.add((Node) node.srcs.get(i));
                        } else {
                            postitive.add((Node) node.srcs.get(i));
                        }
                    }
                    logger.debug("Minus node size : %d, positive node size : %d", negative.size(), postitive.size());
                    for (Iterator<Node> posIter = postitive.iterator(); posIter.hasNext(); ) {
                        Node posNode = posIter.next();
                        for (Iterator<Node> negIter = negative.iterator(); negIter.hasNext(); ) {
                            Node negNode = negIter.next();
                            List<Node> removallist = new ArrayList<Node>();
                            removallist.add(negNode);
                            if (posNode.symbol.getType() == negNode.symbol.getType() &&
                                    posNode.symbol.getWidth() == negNode.symbol.getWidth() &&
                                    posNode.symbol.getValue().doubleValue() == negNode.symbol.getValue().doubleValue()) {
                                node.removeForwardReference(posNode);
                                node.removeForwardReference(negNode);
                                negative.removeAll(removallist);
                                break;
                            }
                        }
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void verifyMinus() {
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                if (node.operation == op_sub) {
                    GeneralUtils.assertAndFail(node.srcs.size() == 2,"TODO add peoper error");
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void removePoNodes() {
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                if (node.operation == op_partial_overlap) {
                    for (Iterator<Node> prevIter = node.prev.iterator(); prevIter.hasNext(); ) {
                        Node prevNode = prevIter.next();
                        if (prevNode.symbol.getWidth() == node.symbol.getWidth()) {
                            for (Iterator<Node> srcIter = node.srcs.iterator(); srcIter.hasNext(); ) {
                                Node srcNode = srcIter.next();
                                node.changeReference(prevNode, srcNode);
                            }
                        }
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void removeOrMinus1() {
        traverseTree(head, head, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                if (node.operation == op_or) {
                    Integer idx = -1;
                    for (int i = 0; i < node.srcs.size(); i++) {
                        Node srcNode = (Node) node.srcs.get(i);
                        if (srcNode.symbol.getType() == MemoryType.IMM_INT_TYPE && (srcNode.symbol.getValue().intValue() == -1)) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx != -1) {
                        node.srcs.clear();
                        node.symbol.setType(MemoryType.IMM_INT_TYPE);
                        node.symbol.setValue(255);
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    public void markRecursive() {
        traverseTree(head, this, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {
                Tree tree = (Tree) value;
                Node head = tree.getHead();

                MemoryRegion head_region = ((ConcreteNode) head).getRegion();
                MemoryRegion conc_region = ((ConcreteNode) node).getRegion();

                if (head_region == null || conc_region == null) {
                    return null;
                }
                //TODO : head_region == conc_region in c++ check the pointer value whether
                // pointing to same place
                if (head_region == conc_region && head.symbol.getValue().doubleValue() != node.symbol.getValue().doubleValue()) {
                    tree.recursive = true;
                }

                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    //endregion Tree Transformations

    //endregion public methods

    //region private methods

    public void removeIdentities() {
        traverseTree(head, null, new NodeMutator() {
            @Override
            public Object mutate(Node node, Object value) {

                if (node.operation == op_add) {
                    for (Iterator<Node> srcIter = node.srcs.iterator(); srcIter.hasNext(); ) {
                        Node srcNode = srcIter.next();
                        if (srcNode.symbol.getType() == MemoryType.IMM_INT_TYPE && srcNode.symbol.getValue().intValue() == 0) {
                            node.removeForwardReference(srcNode);
                        }
                    }
                }
                return null;
            }
        }, new NodeReturnMutator() {
            @Override
            public Object mutate(Object nodeValue, List<Object> traverseValue, Object value) {
                return null;
            }
        });
    }

    //TODO : move to util
    public boolean isRecursive(Node node, List<MemoryRegion> regions) {
        if (head != node) {
            if (node.symbol.getType() == MemoryType.MEM_HEAP_TYPE ||
                    node.symbol.getType() == MemoryType.MEM_STACK_TYPE) {
                if (MemoryRegionUtils.getMemRegion(head.symbol.getValue().intValue(), regions) ==
                        MemoryRegionUtils.getMemRegion(node.symbol.getValue().intValue(), regions)) {
                    return true;
                }
            }
        }

        boolean isRec = false;
        Iterator<Node> nodeIterator = head.srcs.listIterator();
        while (nodeIterator.hasNext()) {
            Node nde = nodeIterator.next();
            isRec = isRecursive(nde, regions);
            if (isRec) {
                break;
            }
        }

        return recursive;
    }

    private boolean removeMultiplication(Node node) {
        boolean mul = false;
        if (node.isVisited()) {
            return false;
        } else {
            mul = false;
            node.setVisited(true);
            if (node.operation == op_mul) {
                Integer index = -1;
                Integer imm_value = 0;
                for (int i = 0; i < node.srcs.size(); i++) {
                    if (((Node) node.srcs.get(i)).symbol.getType() == MemoryType.IMM_INT_TYPE) {
                        imm_value = ((Node) node.srcs.get(i)).symbol.getValue().intValue();
                        index = i;
                        break;
                    }
                }

                if (index != -1 && imm_value >= 0) {
                    mul = true;
                    for (int i = 0; i < node.prev.size(); i++) {
                        Node nde = (Node) node.prev.get(i);
                        for (int j = 0; j < node.srcs.size(); j++) {
                            if (index != j) {
                                for (int k = 0; k < imm_value; k++) {
                                    nde.addForwardReference((Node) node.srcs.get(j));
                                }
                            }
                        }
                        nde.removeForwardReference(node);
                    }

                    node.removeForwardReferenceAll();
                }
            }
        }

        for (int i = 0; i < node.srcs.size(); i++) {
            Node src_node = (Node) node.srcs.get(i);
            if (removeMultiplication(src_node)) {
                i = i - 1;
            }
        }

        return mul;
    }

    private boolean removeRedundant(Node node) {
        boolean changed = false;
        if (node.operation == op_add) {
            if (node.srcs.size() == 1) {
                for (int i = 0; i < node.prev.size(); i++) {
                    node.changeReference((Node) node.prev.get(i), (Node) node.srcs.get(0));
                    changed = true;
                }
            }
        } else if (node.operation == op_full_overlap) {
            for (int i = 0; i < node.srcs.size(); i++) {
                Node loopSrcNode = (Node) node.srcs.get(i);
                if (loopSrcNode.symbol.getType() == MemoryType.IMM_INT_TYPE) {
                    for (int j = 0; j < node.prev.size(); j++) {
                        Node loopPreNode = (Node) node.prev.get(j);
                        node.changeReference(loopPreNode, loopSrcNode);
                        changed = true;
                    }
                }
            }
        } else if (node.symbol.getType() == MemoryType.IMM_INT_TYPE && node.symbol.getValue().intValue() == 0) {
            Integer count = 0;
            for (int i = 0; i < node.prev.size(); i++) {
                Node loopPreNode = (Node) node.prev.get(i);
                if (loopPreNode.operation == op_add) {
                    loopPreNode.removeForwardReference(node);
                    changed = true;
                    count++;
                }

            }
        }

        for (int i = 0; i < node.srcs.size(); i++) {
            Node loopSrxNode = (Node) node.srcs.get(i);
            if (removeRedundant(loopSrxNode)) {
                i = ((i - 1) >= 0) ? i - 1 : 0;
            }
        }
        return changed;
    }

    private void propogateMinus(Node node) {
        if (node.minus && node.operation == op_add) {
            for (int i = 0; i < node.srcs.size(); i++) {
                Node loopSrcNode = (Node) node.srcs.get(i);
                loopSrcNode.minus = !loopSrcNode.minus;
                propogateMinus(loopSrcNode);
            }
        }
    }

    private boolean convertNodeSub(Node node) {
        boolean changed = false;
        if (node.operation == op_sub) {
            Node srcNode0 = (Node) node.srcs.get(0);
            Node srcNode1 = (Node) node.srcs.get(1);

            for (Iterator<Node> preIter = node.prev.iterator(); preIter.hasNext(); ) {
                Node preNode = preIter.next();
                if (preNode.operation == op_add) {
                    GeneralUtils.assertAndFail(node.srcs.size() == 2,"TODO add error message");
                    preNode.addForwardReference(srcNode0);
                    preNode.addForwardReference(srcNode1);

                    srcNode1.minus = true;
                    changed = true;

                    //TODO : Helium original has two duplicate lines changed to srcNode1
                    node.removeForwardReference(srcNode0);
                    node.removeForwardReference(srcNode1);
                    preNode.removeForwardReference(node);
                }
            }
        }

        for (Iterator<Node> srcIter = node.srcs.iterator(); srcIter.hasNext(); ) {
            //TODO : need testing
            convertNodeSub(srcIter.next());
        }
        return changed;
    }

    //endregion private methods

}
