package com.company;

import java.io.*;
import java.util.*;

class BPlusTree{

    private BPTNode ROOT_NODE = null;

    void create_BPlus_Tree(int degree){
        BPTNode root = bptCreate(degree);
        createIndexFile(root);
        System.out.println("create");
    }

    void search_single(int key){
        BPTNode root = loadIndexFile();
        bptSingleSearch(root,key);
    }

    void search_range(int start, int end){
        BPTNode root = loadIndexFile();
        bptRangeSearch(root,start,end);
    }

    void insert_CSV(){
        BPTNode root = loadIndexFile();

        try{
            File file = new File("input.csv");
            FileReader reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(reader);
            String curLine = "";

            while((curLine = bufReader.readLine()) != null){
                String[] curData = curLine.split(",");

                Integer curKey = Integer.parseInt(curData[0]);
                Integer curValue = Integer.parseInt(curData[1]);

                System.out.println("INPUT : [ " + curKey + " , " + curValue + " ]");
                root = bptInsert(root,curKey,curValue,root.getDegree());

            }

            bufReader.close();

        } catch (IOException e){
            System.out.println(e);
        }

        createIndexFile(root);
        createOutputCSV(root);
    }

    void delete_node(int key){
        BPTNode root = loadIndexFile();
        bptDelete(root,key);
    }

    BPTNode bptCreate(int size){
        File file = new File("output.csv");
        BPTNode root = new BPTNode();// Initially the root node is a leaf.
        root.determineLeaf(true);
        root.setNodeInfo(size);
        setRoot(root);
        return root;
    }

    BPTNode bptInsert(BPTNode root, Integer key, BPTNode leftChild, BPTNode rightChild, int size){ //Insert Index
        System.out.println("Key " + key + "become index!");
        boolean isBiggest = true;

        if(!root.isLeaf){//Current node is index node
            Set<Integer> keySet = root.p.keySet();
            for(Integer i : keySet){
                System.out.println(i);
                if(key < i) {
                    System.out.println("CASE 2");
                    //Case 2: The target key will be inserted to the middle of the node
                    root.p.put(key, leftChild);
                    root.setIndexElementNum();
                    isBiggest = false;

                    System.out.println("Change Right KEY : " + i);
                    root.p.put(i,rightChild);
                    System.out.println("End of Operation");
                    break;
                }
            }
            if(isBiggest){ // Case 1 : The target key will be inserted to the rightmost location
                System.out.println("Rightmost Index");
                root.p.put(key,leftChild);
                root.setIndexElementNum();
                System.out.println("Current Index Element: " + root.checkElementNum());

                root.setRightChild(rightChild);
            }
        }
        System.out.println("Index Node Set");
        return root;
    }

    BPTNode bptInsert(BPTNode root, Integer key, Integer value, int size){ //Insert Leaf
        if(root.isLeaf){//Current node is leaf node
            //System.out.println("SET: [ " + key +" , " + value + " ]");

            Set<Integer> keySet = root.v.keySet();
            if(keySet!=null){
                for(Integer i : keySet) {
                    if(i > key) break;
                    else System.out.println("Leaf Traverse: " + i);
                }
            }

            root.v.put(key,value);
            root.setLeafElementNum();
            System.out.println("Leaf Element : " + root.checkElementNum());

            if(root.checkElementNum() > root.getMaxKeys()){
                System.out.println("Leaf Overflow! " + root.checkElementNum());
                root = bptLeafSplit(root,size);

//                if(getRoot() != null){
//                    System.out.println("NO ERROR on getRoot");
//                    root = getRoot();
//                }
//                    return root;
            }
        } else { //Current node isn't leaf node
            boolean isRecursiveCall = false;
            Set<Integer> keySet = root.p.keySet();
            for(Integer i : keySet){
                System.out.println("Traverse: " + i);
                if(key < i){
                    bptInsert(root.p.get(i),key,value,size);
                    System.out.println("Goes to current child!");
                    isRecursiveCall = true;
                    break;
                }
            }
            if(root.hasRightChild() && !isRecursiveCall){
                System.out.println("Goes to Rightmost Child!");
                bptInsert(root.getRightChild(),key,value,size);
            }
        }

        if(getRoot() != null){
            return getRoot();
        }else return root;
    }

    BPTNode bptIndexSplit(BPTNode index, int size){
        int mid = (int)Math.ceil((double)size/2);
        int idx = 0;
        BPTNode leftChild = new BPTNode();
        BPTNode rightChild = new BPTNode();
        BPTNode returnIndex, parentIndex = null; //newIndexNode

        if(index.hasParent()){
            parentIndex = index.parent;
        } else {
            System.out.println("Parent Created!");
            parentIndex = new BPTNode();
            parentIndex.setNodeInfo(size);
            parentIndex.determineLeaf(false);
            parentIndex.setParent(null);

            setRoot(parentIndex);
        }
        rightChild.setNodeInfo(size);
        leftChild.setNodeInfo(size);
        rightChild.setParent(parentIndex);
        leftChild.setParent(parentIndex);
        leftChild.determineLeaf(false);
        rightChild.determineLeaf(false);

        System.out.println("Index Split!");

        Set<Integer> keySet = index.p.keySet();
        for(Integer i : keySet){
            if(++idx < mid){
                System.out.println("Index " + idx + " - Key " + i + "goes leftTree");
                BPTNode element = index.p.get(i);
                leftChild = leftChild.bptInsert(leftChild,i,element,null,size);
                element.setParent(leftChild);
            }else if(idx == mid){
                System.out.println("Index " + idx  + " - Key " + i + " Became mid node");
                parentIndex = parentIndex.bptInsert(parentIndex,i,leftChild,rightChild,size); //Middle node will promoted to index node
                BPTNode element = index.p.get(i);
                leftChild.setRightChild(element); // Current Index child became left's right child
                element.setParent(leftChild);
            } else {
                System.out.println("Index " + idx + " - Key " + i + "goes rightTree");
                BPTNode element = index.p.get(i);
                rightChild = rightChild.bptInsert(rightChild,i,element,null,size);
                element.setParent(rightChild);
            }
        }
        BPTNode element = index.getRightChild();
        rightChild.setRightChild(element);
        element.setParent(rightChild);

        if(parentIndex.checkElementNum() >= size) {
            returnIndex = bptIndexSplit(parentIndex,size);
        }
        else{
            returnIndex = parentIndex;
        }

        index = null;

        return returnIndex;
    }

    BPTNode bptLeafSplit(BPTNode leaf, int size){
        int mid = (int)Math.ceil((double)size/2);
        int idx = 0;
        int midIdx = 0;
        BPTNode tempLeaf = new BPTNode();
        BPTNode rightLeaf = new BPTNode();
        BPTNode indexNode, returnNode = null;

        if(leaf.hasParent()){
            System.out.println("Parent Exists!");
            indexNode = leaf.parent;
        } else {
            System.out.println("Null Parent!");
            indexNode = new BPTNode();
            indexNode.setNodeInfo(size);
            indexNode.determineLeaf(false);
            indexNode.setParent(null);

            setRoot(indexNode);
        }
        if(!leaf.hasParent()){
            System.out.println("Set Parent at leaf!");
            leaf.setParent(indexNode);
        }
        if(leaf.hasRightChild()){
            System.out.println("This Leaf Have Right child!");
            rightLeaf.setRightChild(leaf.getRightChild());
        }
        rightLeaf.setNodeInfo(size);
        tempLeaf.setNodeInfo(size);
        rightLeaf.setParent(indexNode);//Set this node as parent
        tempLeaf.determineLeaf(true);
        rightLeaf.determineLeaf(true);

        System.out.println("Leaf Split! Mid: " + mid);

        Set<Integer> keySet = leaf.v.keySet();
        for(Integer i : keySet){
            if(++idx < mid){
                System.out.println("Index " + idx + " - Key " + i + "goes leftTree");
                tempLeaf = tempLeaf.bptInsert(tempLeaf,i, leaf.v.get(i),size);
            }else{ //split by mid

                if(idx == mid) midIdx = i;//Middle node will promoted to index node

                System.out.println("Index " + idx + " - Key " + i + "goes rightTree");
                rightLeaf = rightLeaf.bptInsert(rightLeaf,i,leaf.v.get(i),size);
            }
        }

        leaf.clearElement();
        //Assign Mid node to index node
        Set<Integer> tempKeySet = tempLeaf.v.keySet();
        for(Integer curItem : tempKeySet){
            leaf.bptInsert(leaf,curItem,tempLeaf.v.get(curItem),size);
        }
        indexNode = indexNode.bptInsert(indexNode,midIdx,leaf,rightLeaf,size);

        tempLeaf = null;
        //Connecting generated nodes
        leaf.setRightChild(rightLeaf);

        //Check whether the index overflows.
        if(indexNode.checkElementNum() >= size){
            System.out.println("Index OverFlows!");
            returnNode = bptIndexSplit(indexNode,size);
            for(Integer i : returnNode.p.keySet()){
                System.out.println("Key: " + i);
            }
        } else {
            returnNode = indexNode;
        }

        return returnNode;
    }

    boolean bptSingleSearch(BPTNode root, int key){

        boolean recursiveCall = false;

        if(root != null) {
            if(root.isLeaf){ // Current node is leaf node
                Set<Integer> keySet = root.v.keySet();
                for(Integer i : keySet){
                    System.out.println("[Leaf] Searching for key " + key + "... Current key: " + i);
                    if(i == key){
                        System.out.println("Key Found!\nValue: " + root.v.get(i));
                        return true; // Matching key found
                    }
                }
                System.out.println("Key " + key + " not found");
                return true;
            } else { //Current node is index node;
                Set<Integer> keySet = root.p.keySet();
                for(Integer i: keySet){
                    if(!recursiveCall){
                        System.out.println("[Index] Searching for key " + key +"... Current key: " + i);
                        if(key < i){
                            System.out.println("Goes to Left Child!");
                            recursiveCall = bptSingleSearch(root.p.get(i),key);//Search its child if i < key
                        }
                    }
                }
                if(root.hasRightChild() && ! recursiveCall){
                    System.out.println("Goes to Right Child!");
                    bptSingleSearch(root.getRightChild(),key);//If the node has right child
                }
            }
        }
        return true;
    }

    void bptRangeSearch(BPTNode root, int start, int end){

        System.out.println("***Performing Range Search...***");

        BPTNode firstLeaf = root;
        while(!firstLeaf.isLeaf){ //Search for leftmost leaf (smallest data)
            Set<Integer> keySet = firstLeaf.p.keySet();
            System.out.println("Cur Leaf : " + keySet.iterator().next());
            firstLeaf = firstLeaf.p.get(keySet.iterator().next()); //Reach to the leftmost data
        }

        System.out.println("Starting Operation: Searching for values ranges in " + start + " and " + end);

        while (firstLeaf != null){ //Search all leaf nodes
            for(Integer i : firstLeaf.v.keySet()){
                //System.out.println("Traverse: " + i);
                if(i >= start && i <= end) System.out.println("Value found: Key: " + i + " Value: " +  firstLeaf.v.get(i));
            }
            firstLeaf = firstLeaf.r;
        }

    }

    ArrayList<Integer> traverseIndex(BPTNode root, ArrayList<Integer> indexSet) {
        if(root.checkElementNum() > 0){
            Queue<BPTNode> childQueue = new LinkedList<>(); //This saves the list of the child
            boolean isLastElement = false;

            do {
                if (!root.isLeaf) {

                    indexSet.add(root.checkElementNum());

                    Set<Integer> keySet = root.p.keySet();
                    for (Integer i : keySet) {
                        System.out.println(i);
                        indexSet.add(i);
                        indexSet.add(root.p.get(i).checkElementNum());
                        if (!root.p.get(i).isLeaf) {
                            childQueue.offer(root.p.get(i));
                        }
                    }
                    indexSet.add(root.getRightChild().checkElementNum());
                    indexSet.add(null); //Classifies nodes.
                    if(!root.getRightChild().isLeaf){
                        System.out.println("RIGHT!");
                        childQueue.offer(root.getRightChild());
                    }

                    isLastElement = childQueue.isEmpty();

                    root = childQueue.poll();
                }
            }while (!isLastElement) ;
        }

        return indexSet;
    }

    ArrayList<Integer> traverseLeaf(BPTNode root,ArrayList<Integer> leafSet){
        BPTNode firstLeaf = root;

        while(!firstLeaf.isLeaf){
            Set<Integer> keySet = firstLeaf.p.keySet();
            firstLeaf = firstLeaf.p.get(keySet.iterator().next());
        }

        while(firstLeaf != null){ //Print all leaf nodes
            for(Integer i : firstLeaf.v.keySet()){
                leafSet.add(i);
                leafSet.add(firstLeaf.v.get(i));
                System.out.println("Print Value");
            }
            firstLeaf = firstLeaf.r;
        }
        return leafSet;
    }

    void createIndexFile(BPTNode root){

        ArrayList<Integer> indexSet = new ArrayList<>();
        ArrayList<Integer> leafSet = new ArrayList<>();
        int curElementNum = 0;
        boolean isChildNum = false;
        boolean isRootNum = true;

        try{
            FileWriter writer = new FileWriter("index.dat");
            writer.append("D:").append(String.valueOf(root.getDegree())).append('\n');

            indexSet = traverseIndex(root,indexSet);
            leafSet = traverseLeaf(root,leafSet);

            if(!indexSet.isEmpty()){
                System.out.println(indexSet);

                for(Integer i : indexSet){
                    // Records current element number in nodes.
                    if(i == null){//End of current node
                        writer.append('\n');
                        curElementNum = 0;
                        isChildNum = false;
                        isRootNum = true;
                    }
                    else {
                        if(isRootNum){
                            writer.append('#');
                            writer.append(i.toString());
                            writer.append('/');
                            isRootNum =false;
                        }
                        else if(!isChildNum){
                            writer.append(i.toString());
                            ++curElementNum;
                            isChildNum = true;
                        } else {
                            writer.append("-");
                            writer.append(i.toString());
                            writer.append('/');
                            isChildNum = false;

                        }
                    }
                }
            }

            //Leaf Node
            if(!leafSet.isEmpty()){
                writer.append("L:\n");
                boolean isValue = false;
                for(Integer i : leafSet){
                    if(!isValue){
                        writer.append(i.toString());
                        writer.append(",");
                    } else{
                        writer.append(i.toString());
                        writer.append("\n");
                    }

                    isValue = !isValue;
                }
            }

            System.out.println("Printed!");
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

    BPTNode loadIndexFile(){

        Queue<BPTNode> children = new LinkedList<>();
        BPTNode root = null;
        BPTNode container = null;
        boolean setLeaf = false;
        try{
            File file = new File("index.dat");
            FileReader reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(reader);
            String curLine = "";
            int degreeInfo = Integer.parseInt(bufReader.readLine().replace("D:","")); //Extract degree Information
            root = new BPTNode();
            root.setNodeInfo(degreeInfo);
            root.determineLeaf(true); //If it doesn't have any indexes, it initially become leaf.
            root.setRoot(root);
            System.out.println("Root set");

            while((curLine = bufReader.readLine()) != null){
                if(!curLine.equals("L:")){
                    System.out.println("Index exists");
                    if(children.peek() == null) { //Current Element is root node
                        root.determineLeaf(false);
                        int idx = 1;
                        String[] dataSet = curLine.split("/");
                        int curNodeElement = Integer.parseInt(dataSet[0].replace("#",""));
                        while(idx <= curNodeElement) {
                            String[] elementInformation = dataSet[idx].split("-");
                            BPTNode curElementChild = new BPTNode();
                            curElementChild.setNodeInfo(degreeInfo);
                            curElementChild.setParent(root);
                            curElementChild.setElementNum(Integer.parseInt(elementInformation[1])); // Second Element contains current index number of curKey's child
                            root.p.put(Integer.parseInt(elementInformation[0]), curElementChild); //First element: key
                            children.offer(curElementChild); //Put cur child inside the queue
                            idx++;
                        }
                        if(dataSet[idx] != null){ //Current dataset contains right Child Information.
                            BPTNode curElementRightChild = new BPTNode();
                            curElementRightChild.setNodeInfo(degreeInfo);
                            curElementRightChild.setParent(root);
                            curElementRightChild.setElementNum(Integer.parseInt(dataSet[idx]));
                            root.setRightChild(curElementRightChild);
                            children.offer(curElementRightChild);
                        }
                    }else { //Index nodes in the middle.
                        BPTNode curElement = children.poll();
                        curElement.determineLeaf(false);

                        int idx = 1;
                        String[] dataSet = curLine.split("/");
                        int curNodeElement = Integer.parseInt(dataSet[0].replace("#",""));
                        while(idx <= curNodeElement) {
                            String[] elementInformation = dataSet[idx].split("-");
                            BPTNode curElementChild = new BPTNode();
                            curElementChild.setNodeInfo(degreeInfo);
                            curElementChild.setParent(curElement);
                            curElementChild.setElementNum(Integer.parseInt(elementInformation[1])); // Second Element contains current index number of curKey's child
                            curElement.p.put(Integer.parseInt(elementInformation[0]), curElementChild); //First element: key
                            children.offer(curElementChild); //Put cur child inside the queue
                            idx++;
                        }
                        if(dataSet[idx] != null){ //Current dataset contains right Child Information.
                            BPTNode curElementRightChild = new BPTNode();
                            curElementRightChild.setNodeInfo(degreeInfo);
                            curElementRightChild.setParent(curElement);
                            curElementRightChild.setElementNum(Integer.parseInt(dataSet[idx]));
                            curElement.setRightChild(curElementRightChild);
                            children.offer(curElementRightChild);
                        }
                    }
                } else { //Pause Reading
                    bufReader.mark(0);
                    break;
                }
            }
            
            //Set Leaves
            bufReader.reset();
            while(children.peek()!=null){
                BPTNode leaf = children.poll();
                leaf.determineLeaf(true);
                int curElementNum = leaf.checkElementNum();
                int idx = 0;
                while(idx < curElementNum){
                    if((curLine = bufReader.readLine()) != null){
                        String[] leafData = curLine.split(",");
                        leaf.v.put(Integer.parseInt(leafData[0]),Integer.parseInt(leafData[1]));
                    }
                    idx++;
                }
                if(container!=null) container.setRightChild(leaf);

                container = leaf;
            }


            bufReader.close();
        } catch (IOException e){
            System.out.println(e);
        }

        return root;
    }

    BPTNode reachToTarget(BPTNode root, Integer key){
        boolean recursiveCall = false;

        if(root != null) {
            if(root.isLeaf){
                System.out.println("Return Root");
                return root;
            } else { //Current node is index node;
                Set<Integer> keySet = root.p.keySet();
                for(Integer i: keySet){
                    if(!recursiveCall){
                        System.out.println("[Index] Searching for key " + key +"... Current key: " + i);
                        if(key < i){
                            System.out.println("Goes to Left Child!");
                            recursiveCall = true;
                            root = reachToTarget(root.p.get(i),key);//Search its child if i < key
                        }
                    }
                }
                if(root.hasRightChild() && ! recursiveCall){
                    System.out.println("Goes to Right Child!");
                    root = reachToTarget(root.getRightChild(),key);//If the node has right child
                }
            }
        }
        return root;
    }

    BPTNode findDuplicates(BPTNode curNode, int key){
        while(curNode.hasParent()) {
            curNode = curNode.getParent();
            Set<Integer> keySet = curNode.p.keySet();
            for (Integer i : keySet) {
                if (key == i) {
                    System.out.println("Key Found");
                    return curNode;
                }
            }
        }

        return null;
    }

    void bptDelete(BPTNode root, Integer key){
        BPTNode target = reachToTarget(root,key);
        //target.v.remove(key);
        if(target != null){
            BPTNode duplicatedIndex = findDuplicates(target,key);
            if(duplicatedIndex != null){
                System.out.println("Duplicated index exists");
            } else {
                System.out.println("No Duplicates");
            }
        }


    }

    void createOutputCSV(BPTNode root){
        BPTNode firstLeaf = root;

        while(!firstLeaf.isLeaf){
            Set<Integer> keySet = firstLeaf.p.keySet();
            firstLeaf = firstLeaf.p.get(keySet.iterator().next());
        }

        try{
            FileWriter writer = new FileWriter("output.csv");

            while(firstLeaf != null){ //Print all leaf nodes
                for(Integer i : firstLeaf.v.keySet()){
                    writer.append(i.toString());
                    writer.append(",");
                    writer.append(firstLeaf.v.get(i).toString());
                    writer.append('\n');

                    System.out.println("Print Value");
                }
                firstLeaf = firstLeaf.r;
            }
            System.out.println("Printed!");
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e);
        }
    }

    void setRoot(BPTNode root){this.ROOT_NODE = root;}
    BPTNode getRoot(){return ROOT_NODE;}

}

class BPTNode extends BPlusTree{
    private int maxChildren;
    private int minChildren;
    private int maxKeys;
    private int minKeys;
    private int degree;
    int m; //Number of Keys
    BPTNode r; // pointer to the rightmost child node
    BPTNode parent; // pointer to its parent
    Map<Integer, BPTNode> p; // p = Array of <>pairs
    Map<Integer,Integer> v; // v = Array of value pairs
    boolean isLeaf; //determine whether the node is index node or leaf node.


    BPTNode(){
        m = 0;
        r = null;
        parent = null;
        p = new TreeMap<>();
        v = new TreeMap<>();
    }

    void setNodeInfo(int order){
        this.maxKeys = order - 1;
        this.minKeys = (int)Math.ceil((double)order/2) -1;
        this.maxChildren = order;
        this.degree = order;
        this.minChildren = (int)Math.ceil((double)order/2);
    }

    int getMaxKeys(){return this.maxKeys;}
    int getMinKeys(){return this.minKeys;}
    int getDegree(){return this.degree;}

    void setLeafElementNum() {this.m = this.v.size();}
    void setIndexElementNum() {this.m = this.p.size();}
    void setElementNum(Integer num){this.m = num;}

    int checkElementNum(){
        return m;
    }

    void determineLeaf(boolean isLeaf){
        this.isLeaf = isLeaf;
    }

    void setRightChild(BPTNode rightChild){
        this.r = rightChild;
    }

    void setParent(BPTNode parent){
        this.parent = parent;
    }

    boolean hasRightChild(){
        return r != null;
    }

    boolean hasParent(){
        return parent != null;
    }

    BPTNode getRightChild(){ return this.r;}
    BPTNode getParent(){ return this.parent; }

    void clearElement(){
        this.m = 0;
        this.p.clear();
        this.v.clear();
    }

}


public class BPTree {

    public static void main(String[] args) {

        BPlusTree bPlusTree = new BPlusTree();
//        bPlusTree.create_BPlus_Tree(5);
//        bPlusTree.insert_CSV();
        bPlusTree.search_single(13);
//        bPlusTree.search_range(2,4);
//        bPlusTree.delete_node(7);

//        for(Integer i : root.p.keySet())
//            System.out.println("Key in Main :" + i);
//
        //Output
//        bPlusTree.printOutput(root);
    }
}
