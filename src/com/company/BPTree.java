package com.company;

import java.io.*;
import java.util.*;

class BPlusTree{

    private BPTNode ROOT_NODE = null;

    BPTNode bptCreate(int size){
        BPTNode root = new BPTNode();// Initially the root node is a leaf.
        root.determineLeaf(true);
        setRoot(root);
        return root;
    }

    BPTNode bptInsert(BPTNode root, Integer key, BPTNode leftChild, BPTNode rightChild, int size){ //Insert Index
        System.out.println("Key " + key + "become index!");
        boolean isBiggest = true;
        Map<Integer, BPTNode> temp_p = new TreeMap<>();
//        boolean changeRight = false;
        //Set new map, copy to the new map.
        if(!root.isLeaf){//Current node is index node
            Set<Integer> keySet = root.p.keySet();
            for(Integer i : keySet){
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
            root.v.put(key,value);
            root.setLeafElementNum();
            System.out.println("Leaf Element : " + root.checkElementNum());

            if(root.m >= size){
                System.out.println("Leaf Overflow!");
                root = bptLeafSplit(root,size);
                System.out.println(root.m);

                for(Integer i : root.p.keySet()) System.out.println("From Insert - Key: " + i);
//                if(getRoot() != null){
//                    System.out.println("NO ERROR on getRoot");
//                    root = getRoot();
//                }
//                    return root;
            }
        } else { //Current node isn't leaf node
            System.out.println("ELSE CALLED");
            boolean isRecursiveCall = false;
            Set<Integer> keySet = root.p.keySet();
            for(Integer i : keySet){
                System.out.println("Traverse: " + i);
                if(key < i){
                    bptInsert(root.p.get(i),key,value,size);
                    isRecursiveCall = true;
                    break;
                }
            }
            if(root.hasRightChild() && !isRecursiveCall){
                bptInsert(root.r,key,value,size);
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
            parentIndex.determineLeaf(false);
            parentIndex.setParent(null);

            setRoot(parentIndex);
        }
        rightChild.setParent(parentIndex);
        leftChild.setParent(parentIndex);
        leftChild.determineLeaf(false);
        rightChild.determineLeaf(false);

        System.out.println("Index Split!");

        Set<Integer> keySet = index.p.keySet();
        for(Integer i : keySet){
            if(++idx < mid){
                System.out.println("Index " + idx + " - Key " + i + "goes leftTree");
                leftChild = leftChild.bptInsert(leftChild,i,index.p.get(i),null,size);
            }else if(idx == mid){
                System.out.println("Index " + idx  + " - Key " + i + " Became mid node");
                parentIndex = parentIndex.bptInsert(parentIndex,i,leftChild,rightChild,size); //Middle node will promoted to index node
                leftChild.setRightChild(index.p.get(i)); // Current Index child became left's right child
            } else {
                   System.out.println("Index " + idx + " - Key " + i + "goes rightTree");
                   rightChild = rightChild.bptInsert(rightChild,i,index.p.get(i),null,size);
            }
        }
        rightChild.setRightChild(index.getRightChild());

        System.out.println("Nodes Connected");

        if(parentIndex.checkElementNum() >= size) {
            System.out.println("Returning Parent Node");
            returnIndex = bptIndexSplit(parentIndex,size);
        }
        else{
            System.out.println("Returning newly Created Parent Node");
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
        System.out.println("Nodes Connected");
        System.out.println("Current Parent node: " + indexNode.checkElementNum());

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
                    bptSingleSearch(root.r,key);//If the node has right child
                }
            }
        }
        return true;
    }

    BPTNode bptRangeSearch(BPTNode root, int start, int end){

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

        return null;
    }

    void setRoot(BPTNode root){this.ROOT_NODE = root;}
    BPTNode getRoot(){return ROOT_NODE;}

}

class BPTNode extends BPlusTree{
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

    void setLeafElementNum() {this.m = this.v.size();}
    void setIndexElementNum() {this.m = this.p.size();}

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

    void clearElement(){
        this.m = 0;
        this.p.clear();
        this.v.clear();
    }

}


public class BPTree {

    public static void main(String[] args) {

        BPlusTree bPlusTree = new BPlusTree();
        BPTNode root = bPlusTree.bptCreate(3);

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
                root = bPlusTree.bptInsert(root,curKey,curValue,3);
                System.out.println("Inserted!");

            }

            bufReader.close();

        } catch (IOException e){
            System.out.println(e);
        }

        for(Integer i : root.p.keySet())
            System.out.println("Key in Main :" + i);

        System.out.println("***Test 1: Searching key 26***");
        bPlusTree.bptSingleSearch(root,26);
        System.out.println("***Test 2: Searching key 10***");
        bPlusTree.bptSingleSearch(root,10);
        System.out.println("***Test 3: Searching key 87***");
        bPlusTree.bptSingleSearch(root,87);
        System.out.println("***Test 4: Searching key 86***");
        bPlusTree.bptSingleSearch(root,86);
        System.out.println("***Test 5: Searching key 20***");
        bPlusTree.bptSingleSearch(root,20);
        System.out.println("***Test 6: Searching key 9***");
        bPlusTree.bptSingleSearch(root,9);
        System.out.println("***Test 7: Searching key 68***");
        bPlusTree.bptSingleSearch(root,68);
        System.out.println("***Test 8: Searching key 84***");
        bPlusTree.bptSingleSearch(root,84);
        System.out.println("***Test 9: Searching key 37***");
        bPlusTree.bptSingleSearch(root,37);

        System.out.println("***Ranged Search 25~75***");
        bPlusTree.bptRangeSearch(root,0,100);
    }
}
