package com.company;

import java.io.*;
import java.util.*;

class BPlusTree {

    private BPTNode ROOT_NODE;

    BPlusTree create_BPlus_Tree(BPlusTree bptree, int degree) {
        bptree = bptCreate(bptree, degree);
        bptree.getRoot(bptree);
        createIndexFile(bptree);
        System.out.println("create");

        return bptree;
    }

    BPlusTree insert_CSV(BPlusTree bptree) {
        bptree = loadIndexFile(bptree);
        if (bptree.getRoot(bptree) == null) {
            System.out.println("ERROR");
        }
        BPTNode root = bptree.getRoot(bptree);

        if (root != null) {
            try {
                File file = new File("input.csv");
                FileReader reader = new FileReader(file);
                BufferedReader bufReader = new BufferedReader(reader);
                String curLine = "";

                while ((curLine = bufReader.readLine()) != null) {
                    if (curLine.isEmpty()) break; //Ignore blank
                    root = bptree.getRoot(bptree);
                    String[] curData = curLine.split(",");

                    Integer curKey = Integer.parseInt(curData[0]);
                    Integer curValue = Integer.parseInt(curData[1]);

                    System.out.println("INPUT : [ " + curKey + " , " + curValue + " ]");
                    bptInsert(bptree, root, curKey, curValue, root.getDegree());

                }

                bufReader.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }

        createIndexFile(bptree);
        createOutputCSV(bptree);

        return bptree;
    }

    void search_single(BPlusTree bPlusTree, int key) {
        bPlusTree = loadIndexFile(bPlusTree);
        BPTNode root = getRoot(bPlusTree);
        bptSingleSearch(root, key);
    }

    void search_range(BPlusTree bPlusTree, int start, int end) {
        bPlusTree = loadIndexFile(bPlusTree);
        BPTNode root = getRoot(bPlusTree);
        bptRangeSearch(root, start, end);
    }

    void delete_node(BPlusTree bPlusTree, int key) {
        bPlusTree = loadIndexFile(bPlusTree);
        BPTNode root = getRoot(bPlusTree);
        bptDelete(bPlusTree, root, key);
        createOutputCSV(bPlusTree);
        createIndexFile(bPlusTree);
    }

    BPlusTree bptCreate(BPlusTree bPlusTree, int size) {
        BPTNode root = new BPTNode();// Initially the root node is a leaf.
        root.determineLeaf(true);
        root.setNodeInfo(size);
        bPlusTree.setRoot(bPlusTree, root);
        return bPlusTree;
    }

    BPTNode bptInsert(BPlusTree bPlusTree, BPTNode root, Integer key, BPTNode leftChild, BPTNode rightChild, int size) { //Insert Index
        System.out.println("Key " + key + "become index!");
        boolean isBiggest = true;

        if (!root.isLeaf) {//Current node is index node
            Set<Integer> keySet = root.p.keySet();
            if (!keySet.isEmpty()) {
                for (Integer i : keySet) {
                    System.out.println(i);
                    if (key.equals(i)) {
                        System.out.println("WARNING: Duplicated key is not allowed. - Input Ignored");
                        break;
                    } else if (key < i) {
                        System.out.println("CASE 2");
                        //Case 2: The target key will be inserted to the middle of the node
                        root.p.put(key, leftChild);
                        root.updateElementNum(root);
                        isBiggest = false;

                        System.out.println("Change Right KEY : " + i);
                        root.p.put(i, rightChild);
                        System.out.println("End of Operation");
                        break;
                    }
                }
            } else { //There is nothing; current node is newly created index node
                root.p.put(key, leftChild);
                root.updateElementNum(root);
            }
            if (isBiggest) { // Case 1 : The target key will be inserted to the rightmost location
                System.out.println("Rightmost Index");
                root.p.put(key, leftChild);
                root.updateElementNum(root);
                System.out.println("Current Index Element: " + root.checkElementNum());

                root.setRightChild(rightChild);
            }
        }
        System.out.println("Index Node Set");
        return root;
    }

    BPTNode bptInsert(BPlusTree bPlusTree, BPTNode root, Integer key, Integer value, int size) { //Insert Leaf
        if (root.isLeaf) {//Current node is leaf node
            //System.out.println("SET: [ " + key +" , " + value + " ]");

            Set<Integer> keySet = root.v.keySet();

            if (keySet.contains(key)) {
                System.out.println("WARNING: Duplicated key is not allowed. - Input Ignored");
            } else {
                root.v.put(key, value);
                root.updateElementNum(root);
                System.out.println("Leaf Element : " + root.checkElementNum());
            }

            if (root.checkElementNum() > root.getMaxKeys()) {
                System.out.println("Leaf Overflow! " + root.checkElementNum());
                root = bptLeafSplit(bPlusTree, root, size);

            }
        } else { //Current node isn't leaf node
            boolean isRecursiveCall = false;
            Set<Integer> keySet = root.p.keySet();
            for (Integer i : keySet) {
                System.out.println("Traverse: " + i);
                if (key < i) {
                    bptInsert(bPlusTree, root.p.get(i), key, value, size);
                    isRecursiveCall = true;
                    break;
                }
            }
            if (root.hasRightChild() && !isRecursiveCall) {
                bptInsert(bPlusTree, root.getRightChild(), key, value, size);
            }
        }

        return root;
    }

    BPTNode bptIndexSplit(BPlusTree bPlusTree, BPTNode index, int size) {
        int mid = (int) Math.ceil((double) size / 2);
        if (size % 2 == 0) mid += 1;
        int idx = 0;
        BPTNode leftChild = new BPTNode();
        BPTNode rightChild = new BPTNode();
        BPTNode returnIndex, parentIndex = null; //newIndexNode

        if (index.hasParent()) {
            parentIndex = index.parent;
        } else {
            System.out.println("Parent Created!");
            parentIndex = new BPTNode();
            parentIndex.setNodeInfo(size);
            parentIndex.determineLeaf(false);
            parentIndex.setParent(null);

            bPlusTree.setRoot(bPlusTree, parentIndex);
        }
        rightChild.setNodeInfo(size);
        leftChild.setNodeInfo(size);
        rightChild.setParent(parentIndex);
        leftChild.setParent(parentIndex);
        leftChild.determineLeaf(false);
        rightChild.determineLeaf(false);

        System.out.println("Index Split!");

        Set<Integer> keySet = index.p.keySet();
        for (Integer i : keySet) {
            if (++idx < mid) {
                System.out.println("Index " + idx + " - Key " + i + "goes leftTree");
                BPTNode element = index.p.get(i);
                leftChild = leftChild.bptInsert(bPlusTree, leftChild, i, element, null, size);
                element.setParent(leftChild);
            } else if (idx == mid) {
                System.out.println("Index " + idx + " - Key " + i + " Became mid node");
                parentIndex = parentIndex.bptInsert(bPlusTree, parentIndex, i, leftChild, rightChild, size); //Middle node will promoted to index node
                BPTNode element = index.p.get(i);
                leftChild.setRightChild(element); // Current Index child became left's right child
                element.setParent(leftChild);
            } else {
                System.out.println("Index " + idx + " - Key " + i + "goes rightTree");
                BPTNode element = index.p.get(i);
                rightChild = rightChild.bptInsert(bPlusTree, rightChild, i, element, null, size);
                element.setParent(rightChild);
            }
        }
        BPTNode element = index.getRightChild();
        rightChild.setRightChild(element);
        element.setParent(rightChild);

        if (parentIndex.checkElementNum() >= size) {
            returnIndex = bptIndexSplit(bPlusTree, parentIndex, size);
        } else {
            returnIndex = parentIndex;
        }

        index = null;

        return returnIndex;
    }

    BPTNode bptLeafSplit(BPlusTree bPlusTree, BPTNode leaf, int size) {
        int mid = (int) Math.ceil((double) size / 2);
        if (size % 2 == 0) mid += 1; //If the degree is even number, plus 1 to the pivot
        int idx = 0;
        int midIdx = 0;
        BPTNode tempLeaf = new BPTNode();
        BPTNode rightLeaf = new BPTNode();
        BPTNode indexNode, returnNode = null;

        if (leaf.hasParent()) {
            System.out.println("Parent Exists!");
            indexNode = leaf.parent;
        } else {
            System.out.println("Null Parent!");
            indexNode = new BPTNode();
            indexNode.setNodeInfo(size);
            indexNode.determineLeaf(false);
            indexNode.setParent(null);

            bPlusTree.setRoot(bPlusTree, indexNode);
        }
        if (!leaf.hasParent()) {
            System.out.println("Set Parent at leaf!");
            leaf.setParent(indexNode);
        }
        if (leaf.hasRightChild()) {
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
        for (Integer i : keySet) {
            if (++idx < mid) {
                System.out.println("Index " + idx + " - Key " + i + "goes leftTree");
                tempLeaf = tempLeaf.bptInsert(bPlusTree, tempLeaf, i, leaf.v.get(i), size);
            } else { //split by mid

                if (idx == mid) midIdx = i;//Middle node will promoted to index node

                System.out.println("Index " + idx + " - Key " + i + "goes rightTree");
                rightLeaf = rightLeaf.bptInsert(bPlusTree, rightLeaf, i, leaf.v.get(i), size);
            }
        }

        leaf.clearElement();
        //Assign Mid node to index node
        Set<Integer> tempKeySet = tempLeaf.v.keySet();
        for (Integer curItem : tempKeySet) {
            leaf.bptInsert(bPlusTree, leaf, curItem, tempLeaf.v.get(curItem), size);
        }
        indexNode = bptInsert(bPlusTree, indexNode, midIdx, leaf, rightLeaf, size);

        tempLeaf = null;
        //Connecting generated nodes
        leaf.setRightChild(rightLeaf);

        //Check whether the index overflows.
        if (indexNode.checkElementNum() >= size) {
            System.out.println("Index OverFlows!");
            returnNode = bptIndexSplit(bPlusTree, indexNode, size);
            for (Integer i : returnNode.p.keySet()) {
                System.out.println("Key: " + i);
            }
        } else {
            returnNode = indexNode;
        }

        return returnNode;
    }

    boolean bptSingleSearch(BPTNode root, int key) {

        boolean recursiveCall = false;

        if (root != null) {
            if (root.isLeaf) { // Current node is leaf node
                Set<Integer> keySet = root.v.keySet();
                for (Integer i : keySet) {
                    System.out.println("[Leaf] Searching for key " + key + "... Current key: " + i);
                    if (i == key) {
                        System.out.println("Key Found!\nValue: " + root.v.get(i));
                        return true; // Matching key found
                    }
                }
                System.out.println("Key " + key + " not found");
                return true;
            } else { //Current node is index node;
                Set<Integer> keySet = root.p.keySet();
                for (Integer i : keySet) {
                    if (!recursiveCall) {
                        System.out.println("[Index] Searching for key " + key + "... Current key: " + i);
                        if (key < i) {
                            System.out.println("Goes to Left Child!");
                            recursiveCall = bptSingleSearch(root.p.get(i), key);//Search its child if i < key
                        }
                    }
                }
                if (root.hasRightChild() && !recursiveCall) {
                    System.out.println("Goes to Right Child!");
                    bptSingleSearch(root.getRightChild(), key);//If the node has right child
                }
            }
        }
        return true;
    }

    void bptRangeSearch(BPTNode root, int start, int end) {

        System.out.println("***Performing Range Search...***");

        BPTNode firstLeaf = root;
        while (!firstLeaf.isLeaf) { //Search for leftmost leaf (smallest data)
            Set<Integer> keySet = firstLeaf.p.keySet();
            System.out.println("Cur Leaf : " + keySet.iterator().next());
            firstLeaf = firstLeaf.p.get(keySet.iterator().next()); //Reach to the leftmost data
        }

        System.out.println("Starting Operation: Searching for values ranges in " + start + " and " + end);

        while (firstLeaf != null) { //Search all leaf nodes
            for (Integer i : firstLeaf.v.keySet()) {
                //System.out.println("Traverse: " + i);
                if (i >= start && i <= end)
                    System.out.println("Value found: Key: " + i + " Value: " + firstLeaf.v.get(i));
            }
            firstLeaf = firstLeaf.r;
        }

    }

    ArrayList<Integer> traverseIndex(BPTNode root, ArrayList<Integer> indexSet) {
        System.out.println("Traverse Index");

        if (Objects.isNull(root)) {
            System.out.println("ERROR: Received root is null");
        } else if (root.checkElementNum() == 0) {
            System.out.println("ERROR: No element in root - Case that shouldn't happened");
        }

        if (root.checkElementNum() > 0) {
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
                    if (!root.getRightChild().isLeaf) {
                        System.out.println("RIGHT!");
                        childQueue.offer(root.getRightChild());
                    }

                    isLastElement = childQueue.isEmpty();

                    root = childQueue.poll();
                }
            } while (!isLastElement);
        }

        return indexSet;
    }

    ArrayList<Integer> traverseLeaf(BPTNode root, ArrayList<Integer> leafSet) {
        BPTNode firstLeaf = root;

        while (!firstLeaf.isLeaf) {
            Set<Integer> keySet = firstLeaf.p.keySet();
            firstLeaf = firstLeaf.p.get(keySet.iterator().next());
        }

        while (firstLeaf != null) { //Print all leaf nodes
            for (Integer i : firstLeaf.v.keySet()) {
                leafSet.add(i);
                leafSet.add(firstLeaf.v.get(i));
                System.out.println("Print Value");
            }
            firstLeaf = firstLeaf.r;
        }
        return leafSet;
    }

    void bptDelete(BPlusTree bPlusTree, BPTNode root, Integer key) {
        BPTNode target = reachToTarget(root, key);
        BPTNode duplicatedIndex = findDuplicates(target, key);
        if (target.checkElementNum() > target.getMinKeys()) { //Case 1: Current Key has enough key to be deleted.
            target.v.remove(key);
            target.updateElementNum(target);
            if (!Objects.isNull(duplicatedIndex))
                renewKey(duplicatedIndex, key, target.v.keySet().iterator().next());
        } else { //Deficiency
            System.out.println("DEFICIENCY");
            //Case 2: Borrow & Rotation
            System.out.println("Case 2");
            boolean isSuccessful = false;
            isSuccessful = rotation(target, key, target.isLeaf);
            if (!isSuccessful) { //Case 3: Merge
                System.out.println("Case 3");
                target.v.remove(key);
                target.updateElementNum(target);
                mergeNode(bPlusTree, target, key);
            }
        }
    }

    BPTNode reachToTarget(BPTNode root, Integer key) {
        boolean recursiveCall = false;

        if (root != null) {
            if (root.isLeaf) {
                System.out.println("Return Leaf");
                return root;
            } else { //Current node is index node;
                Set<Integer> keySet = root.p.keySet();
                for (Integer i : keySet) {
                    if (!recursiveCall) {
                        System.out.println("[Index] Searching for key " + key + "... Current key: " + i);
                        if (key < i) {
                            System.out.println("Goes to Left Child!");
                            recursiveCall = true;
                            root = reachToTarget(root.p.get(i), key);//Search its child if i < key
                        }
                    }
                }
                if (root.hasRightChild() && !recursiveCall) {
                    System.out.println("Goes to Right Child!");
                    root = reachToTarget(root.getRightChild(), key);//If the node has right child
                }
            }
        }
        return root;
    }

    BPTNode findDuplicates(BPTNode curNode, int key) {
        while (curNode.hasParent()) {
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

    void renewKey(BPTNode target, Integer oldKey, Integer newKey) {
        System.out.println("Change Index Required");
        BPTNode container = target.p.get(oldKey);

        target.p.remove(oldKey);
        System.out.println("Remove " + oldKey);
        target.p.put(newKey, container);
        target.updateElementNum(target);
    }

    boolean rotation(BPTNode target, Integer key, boolean isLeafNode) {
        BPTNode leftSibling = null;
        if (isLeafNode) {
            if (!Objects.isNull(target.getRightChild()) &&
                    target.getRightChild().checkElementNum() > target.getRightChild().getMinKeys()) { //If the target has right child & it has enough ley to lend
                System.out.println("Borrow From Right Child");
                target.v.remove(key);
                target.updateElementNum(target);
                rotateKey(target, target.getRightChild(), key, true);
                return true;
            } else if (!Objects.isNull(leftSibling = checkLeftSibling(target.getParent(), key)) &&
                    leftSibling.checkElementNum() > leftSibling.getMinKeys()) {
                System.out.println("Borrow From Right Child");
                target.v.remove(key);
                target.updateElementNum(target);
                rotateKey(target, leftSibling, key, false);
                return true;
            } else {
                System.out.println("Merge!");
                return false;
            }
        }
        return false;
    }

    BPTNode checkLeftSibling(BPTNode curParent, Integer targetKey) {
        BPTNode leftSibling = null;
        System.out.println("Check for left sibling");
        while (!Objects.isNull(curParent)) {
            Set<Integer> keySet = curParent.p.keySet();
            for (Integer i : keySet) {
                System.out.println("CurParentKey = " + i);
                if (targetKey < i) {
                    System.out.println("The Leftmost key of current subtree");
                    break;
                }
                if (targetKey.equals(i)) { //Target's left node
                    System.out.println("Equal : " + i);
                    leftSibling = curParent.p.get(i);
//                    if (Objects.isNull(curParent.p.get(i))) System.out.println("ERROR : NULL CHILD");
//                    if (Objects.isNull(leftSibling)) System.out.println("ERROR: LEFT NULL");
                    while (!leftSibling.isLeaf) {
                        leftSibling = leftSibling.getRightChild();
                    }
                    break;
                }
            }
            if (Objects.isNull(leftSibling)) {
                curParent = curParent.getParent();
            } else {
                System.out.println("Left Sibling Found");
                break;
            }
        }

        return leftSibling;
    }


    void rotateKey(BPTNode target, BPTNode sibling, Integer renewTarget, boolean isRightSibling) {
        Integer borrowedKey = null;
        Integer borrowedValue = null;
        BPTNode borrowedIndex = null;
        Integer newKey = null;
        BPTNode duplicated = null;

        if (isRightSibling) {
            System.out.println("RIGHT SIBLING");
            if (sibling.isLeaf) {
                borrowedKey = sibling.v.keySet().iterator().next();
                System.out.println(borrowedKey);
                borrowedValue = sibling.v.get(borrowedKey);
                sibling.v.remove(borrowedKey);
                newKey = sibling.v.keySet().iterator().next();
                target.v.put(borrowedKey, borrowedValue);
            } else {
                borrowedKey = sibling.p.keySet().iterator().next();
                System.out.println(borrowedKey);
                borrowedIndex = sibling.p.get(borrowedKey);
                sibling.p.remove(borrowedKey);
                newKey = sibling.p.keySet().iterator().next();
                target.p.put(borrowedKey, borrowedIndex);
            }
            target.updateElementNum(target);

            sibling.updateElementNum(sibling);
            duplicated = findDuplicates(sibling, borrowedKey); //Resolve duplicated index from sibling
            if (!Objects.isNull(duplicated)) renewKey(duplicated, borrowedKey, newKey);

        } else {
            if (sibling.isLeaf) {
                System.out.println("LEFT SIBLING");
                Set<Integer> keySet = sibling.v.keySet();
                for (Integer i : keySet) borrowedKey = i; //Get Last Key
                borrowedValue = sibling.v.get(borrowedKey);
                sibling.v.remove(borrowedKey);
                sibling.updateElementNum(sibling);
            }
        }

        target.v.put(borrowedKey, borrowedValue);
        target.updateElementNum(target);

        duplicated = findDuplicates(target, renewTarget); //Resolve duplicated index from the target
        if (!Objects.isNull(duplicated)) renewKey(duplicated, renewTarget, borrowedKey);
    }

    void mergeNode(BPlusTree bPlusTree, BPTNode target, Integer key) {
        System.out.println("MergeNodeCalled");
        if (target.isLeaf) { //Leaf Merge
            BPTNode curParent = target.getParent();
            Integer firstKey = curParent.p.keySet().iterator().next();
            System.out.println("FIRST KEY : " + firstKey);
            if (!(curParent.p.get(firstKey).equals(target))) {
                System.out.println("Not the first Element!");
                List<Integer> curList = new ArrayList<>(curParent.p.keySet());
                for(int i = 0; i < curList.size(); i++){
                    if(curList.get(i).equals(key)){
                        if(i < curList.size() - 1){
                            curParent.p.put(curList.get(i+1),curParent.p.get(curList.get(i)));
                        }else{
                            curParent.setRightChild(curParent.p.get(curList.get(i)));
                        }
                    }
                }
                curParent.p.remove(key);
            } else {
                curParent.p.remove(firstKey);
            }
            curParent.updateElementNum(curParent);

            if (curParent.checkElementNum() < curParent.getMinKeys()) {
                System.out.println("Index Underflow!");
                mergeNode(bPlusTree, curParent, key);//Underflow!
            }

        } else { //Index Merge
            if(underflow(target)){
                if (!target.equals(getRoot(bPlusTree))) { //If the index is not a root
                    System.out.println("Index Merge!" + target.checkElementNum() + target.getParent().checkElementNum());
                    BPTNode sibling = checkSibling(target, key);
                    if(sibling.checkElementNum() > sibling.getMinKeys()){ //Merge Case 3-1: if index family has enough key to lend
                        Integer mergeKey = sibling.p.keySet().iterator().next();

                        if (mergeKey >= key) { //IF error: check this condition
                            Integer newKey = target.getParent().p.keySet().iterator().next();
                            target.p.put(newKey, target.getRightChild());
                            target.setRightChild(sibling.p.get(mergeKey));
                        } else {
                            Set<Integer> siblingKeySet = sibling.p.keySet();
                            for (Integer i : siblingKeySet)
                                mergeKey = i;
                            target.p.put(mergeKey, sibling.getRightChild());
                            sibling.setRightChild(sibling.p.get(mergeKey));
                        }
                        sibling.p.remove(mergeKey);

                        target.updateElementNum(target);
                        sibling.updateElementNum(sibling);

                        updateKey(target);

                        mergeNode(bPlusTree,target.getParent(),key);
                    }
//                    else { // Merge case 3-1:Family hasn't enough indexes to lend; merge it's parent
//                        BPTNode curParent = target.getParent();
//                        List<Integer> parentKeyList = new ArrayList<>(curParent.p.keySet());
//                        Integer mergeKey = 0;
//                        for(int i = 0; i < parentKeyList.size(); i++){
//                            if(curParent.p.get(parentKeyList.get(i)).equals(sibling))
//                                mergeKey = parentKeyList.get(i);
//                        }
//                        if(mergeKey < key){
//                            BPTNode container = sibling.getRightChild();
//                            sibling.getRightChild().clearElement();
//                            target.p.put(mergeKey,container);
//                            List<Integer> siblingKeyList = new ArrayList<>(sibling.p.keySet());
//                            target.p.put(siblingKeyList.get(siblingKeyList.size()-1),
//                                    sibling.p.get(siblingKeyList.get(siblingKeyList.size()-1)));
//
//                            target.updateElementNum(target);
//                            sibling.updateElementNum(sibling);
//                        }else{
//                            BPTNode container = sibling.getRightChild();
//                            sibling.getRightChild().clearElement();
//                            target.p.put(mergeKey,container);
//
//                            target.updateElementNum(target);
//                            sibling.updateElementNum(sibling);
//                        }
//
//                        curParent.p.remove(mergeKey);
//                        curParent.updateElementNum(curParent);
//
//                        if(curParent.checkElementNum() < curParent.getMinKeys())
//                            mergeNode(bPlusTree,curParent,key);
//                    }
                } else { //Current Node is root node, but underflow happens

                }
            } else {
                //Merge Finished
                updateKey(target);
            }
        }
    }

    boolean underflow(BPTNode curNode){
        return curNode.checkElementNum() < curNode.getMinKeys();
    }

    BPTNode checkSibling(BPTNode target, Integer key) {
        BPTNode curParent = target.getParent();
        BPTNode checkFor = null;
        List<Integer> list = new ArrayList<>(curParent.p.keySet());

        for (int i = 0; i < list.size() - 1; i++) {
            if (curParent.p.get(list.get(i)).equals(target)) {
                System.out.println("Child of " + list.get(i) + ", Sibling : Child of " + list.get(i + 1));
                checkFor = curParent.p.get(list.get(i + 1));
            }
        }

        if (Objects.isNull(checkFor)) { //2 case: sibling - right most child OR right most child requires sibling
            if (curParent.getRightChild().equals(target)) {
                System.out.println("Right most child requires sibling");
                checkFor = curParent.p.get(list.get(list.size() - 1));
            } else {
                System.out.println("Child of " + list.get(list.size() - 1) + ", Sibling: Rightmost child");
                checkFor = curParent.getRightChild();
            }
        }
        return checkFor;
    }

    void updateKey(BPTNode target) {
        List<Integer> keyList = new ArrayList<>(target.p.keySet());
        Integer key = 0;
        Integer compareKey = 0;
        for (Integer integer : keyList) {
            key = integer;
            if(integer < keyList.size()-1){
                compareKey = getCorrectKey(target.p.get(key));
            }else{
                compareKey = getCorrectKey(target.getRightChild());
            }
            if (!key.equals(compareKey)) {
                renewKey(target, key, compareKey);
            }

        }
    }


    Integer getCorrectKey(BPTNode node){
        while(!node.isLeaf){
            node = node.p.get(node.p.keySet().iterator().next());
        }

        return node.v.keySet().iterator().next();
    }


    Integer getTargetIndex(BPTNode curParent, Integer key) {
        Integer deleteTarget = null;
        Set<Integer> keySet = curParent.p.keySet();
        for (Integer i : keySet) {
            if (i.equals(key)) {
                System.out.println("Target Index : " + i);
                deleteTarget = i;
            } else if (i > key) {
                deleteTarget = i;
                System.out.println("Target Index : " + i);
                break;
            }
        }

        return deleteTarget;
    }

    void createIndexFile(BPlusTree bPlusTree) {


        BPTNode root = getRoot(bPlusTree);
        ArrayList<Integer> indexSet = new ArrayList<>();
        ArrayList<Integer> leafSet = new ArrayList<>();
        int curElementNum = 0;
        boolean isChildNum = false;
        boolean isRootNum = true;

        try {
            FileWriter writer = new FileWriter("index.dat");
            writer.append("D:").append(String.valueOf(root.getDegree())).append('\n');

            indexSet = traverseIndex(root, indexSet);
            leafSet = traverseLeaf(root, leafSet);

            if (!indexSet.isEmpty()) {
                System.out.println(indexSet);

                for (Integer i : indexSet) {
                    // Records current element number in nodes.
                    if (i == null) {//End of current node
                        writer.append('\n');
                        curElementNum = 0;
                        isChildNum = false;
                        isRootNum = true;
                    } else {
                        if (isRootNum) {
                            writer.append('#');
                            writer.append(i.toString());
                            writer.append('/');
                            isRootNum = false;
                        } else if (!isChildNum) {
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
            if (!leafSet.isEmpty()) {
                writer.append("L:\n");
                boolean isValue = false;
                for (Integer i : leafSet) {
                    if (!isValue) {
                        writer.append(i.toString());
                        writer.append(",");
                    } else {
                        writer.append(i.toString());
                        writer.append("\n");
                    }

                    isValue = !isValue;
                }
            }

            System.out.println("Printed!");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    BPlusTree loadIndexFile(BPlusTree bPlusTree) {

        Queue<BPTNode> children = new LinkedList<>();
        BPTNode root = null;
        BPTNode container = null;
        boolean setLeaf = false;
        try {
            File file = new File("index.dat");
            FileReader reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(reader);
            String curLine = "";
            int degreeInfo = Integer.parseInt(bufReader.readLine().replace("D:", ""));//Extract degree Information
            System.out.println(degreeInfo);

            while ((curLine = bufReader.readLine()) != null) {
                System.out.println(curLine);
                if (!curLine.equals("L:")) {
                    System.out.println("Index exists");
                    if (children.peek() == null) { //Current Element is root node
                        System.out.println("Cur Node is Root Node");
                        root = new BPTNode();
                        root.setNodeInfo(degreeInfo);
                        root.determineLeaf(false);
                        int idx = 1;
                        String[] dataSet = curLine.split("/");
                        int curNodeElement = Integer.parseInt(dataSet[0].replace("#", ""));
                        while (idx <= curNodeElement) {
                            String[] elementInformation = dataSet[idx].split("-");
                            BPTNode curElementChild = new BPTNode();
                            curElementChild.setNodeInfo(degreeInfo);
                            curElementChild.setParent(root);
                            curElementChild.setElementNum(Integer.parseInt(elementInformation[1])); // Second Element contains current index number of curKey's child
                            root.p.put(Integer.parseInt(elementInformation[0]), curElementChild); //First element: key
                            children.offer(curElementChild); //Put cur child inside the queue
                            idx++;
                        }
                        if (dataSet[idx] != null) { //Current dataset contains right Child Information.
                            BPTNode curElementRightChild = new BPTNode();
                            curElementRightChild.setNodeInfo(degreeInfo);
                            curElementRightChild.setParent(root);
                            curElementRightChild.setElementNum(Integer.parseInt(dataSet[idx]));
                            root.setRightChild(curElementRightChild);
                            children.offer(curElementRightChild);
                        }

                        root.updateElementNum(root);
                        bPlusTree.setRoot(bPlusTree, root);

                        if (ROOT_NODE.equals(root)) {
                            System.out.println("Root set");
                        }

                    } else { //Index nodes in the middle.
                        BPTNode curElement = children.poll();
                        curElement.determineLeaf(false);

                        int idx = 1;
                        String[] dataSet = curLine.split("/");
                        int curNodeElement = Integer.parseInt(dataSet[0].replace("#", ""));
                        while (idx <= curNodeElement) {
                            String[] elementInformation = dataSet[idx].split("-");
                            BPTNode curElementChild = new BPTNode();
                            curElementChild.setNodeInfo(degreeInfo);
                            curElementChild.setParent(curElement);
                            curElementChild.setElementNum(Integer.parseInt(elementInformation[1])); // Second Element contains current index number of curKey's child
                            curElement.p.put(Integer.parseInt(elementInformation[0]), curElementChild); //First element: key
                            children.offer(curElementChild); //Put cur child inside the queue
                            idx++;
                        }
                        if (dataSet[idx] != null) { //Current dataset contains right Child Information.
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
            if (root == null) { //Root is not a index - split doesn't happened
                System.out.println("Cur root is not an index!");
                root = new BPTNode();
                root.setNodeInfo(degreeInfo);
                root.determineLeaf(true);
                bPlusTree.setRoot(bPlusTree, root);

                while ((curLine = bufReader.readLine()) != null) {
                    String[] leafData = curLine.split(",");
                    root.v.put(Integer.parseInt(leafData[0]), Integer.parseInt(leafData[1]));
                }

            } else {
                bufReader.reset();
                while (children.peek() != null) {
                    BPTNode leaf = children.poll();
                    leaf.determineLeaf(true);
                    int curElementNum = leaf.checkElementNum();
                    int idx = 0;
                    while (idx < curElementNum) {
                        if ((curLine = bufReader.readLine()) != null) {
                            String[] leafData = curLine.split(",");
                            leaf.v.put(Integer.parseInt(leafData[0]), Integer.parseInt(leafData[1]));
                        }
                        idx++;
                    }
                    if (container != null) container.setRightChild(leaf);

                    container = leaf;
                }
            }


            bufReader.close();
        } catch (IOException e) {
            System.out.println(e);
        }

        return bPlusTree;
    }


    void createOutputCSV(BPlusTree bPlusTree) {

        BPTNode firstLeaf = bPlusTree.getRoot(bPlusTree);

        while (!firstLeaf.isLeaf) {
            Set<Integer> keySet = firstLeaf.p.keySet();
            firstLeaf = firstLeaf.p.get(keySet.iterator().next());
        }

        try {
            FileWriter writer = new FileWriter("output.csv");

            while (firstLeaf != null) { //Print all leaf nodes
                for (Integer i : firstLeaf.v.keySet()) {
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
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    void setRoot(BPlusTree bPlusTree, BPTNode root) {
        System.out.println("ROOT SET");
        bPlusTree.ROOT_NODE = root;
    }

    BPTNode getRoot(BPlusTree bPlusTree) {
        if (bPlusTree.ROOT_NODE == null) {
            System.out.println("NULL ROOT NODE");
        }
        System.out.println("ROOT GET");
        return bPlusTree.ROOT_NODE;
    }

}

class BPTNode extends BPlusTree {
    private int maxChildren;
    private int minChildren;
    private int maxKeys;
    private int minKeys;
    private int degree;
    int m; //Number of Keys
    BPTNode r; // pointer to the rightmost child node
    BPTNode parent; // pointer to its parent
    Map<Integer, BPTNode> p; // p = Array of <>pairs
    Map<Integer, Integer> v; // v = Array of value pairs
    boolean isLeaf; //determine whether the node is index node or leaf node.


    BPTNode() {
        m = 0;
        r = null;
        parent = null;
        p = new TreeMap<>();
        v = new TreeMap<>();
    }

    void setNodeInfo(int order) {
        this.maxKeys = order - 1;
        this.minKeys = (int) Math.ceil((double) order / 2) - 1;
        this.maxChildren = order;
        this.degree = order;
        this.minChildren = (int) Math.ceil((double) order / 2);
    }

    int getMaxKeys() {
        return this.maxKeys;
    }

    int getMinKeys() {
        return this.minKeys;
    }

    int getDegree() {
        return this.degree;
    }

    void updateElementNum(BPTNode curNode) {
        if (curNode.isLeaf)
            this.m = curNode.v.size();
        else
            this.m = this.p.size();
    }

    void setElementNum(Integer num) {
        this.m = num;
    }

    int checkElementNum() {
        return m;
    }

    void determineLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    void setRightChild(BPTNode rightChild) {
        this.r = rightChild;
    }

    void setParent(BPTNode parent) {
        this.parent = parent;
    }

    boolean hasRightChild() {
        return r != null;
    }

    boolean hasParent() {
        return parent != null;
    }

    BPTNode getRightChild() {
        return this.r;
    }

    BPTNode getParent() {
        return this.parent;
    }

    void clearElement() {
        this.m = 0;
        this.p.clear();
        this.v.clear();
    }

}


public class BPTree {

    public static void main(String[] args) {

        BPlusTree bPlusTree = new BPlusTree();
//        bPlusTree = bPlusTree.create_BPlus_Tree(bPlusTree,3);
//        bPlusTree.insert_CSV(bPlusTree);
//        bPlusTree.search_single(bPlusTree,94);
//        bPlusTree.search_single(bPlusTree,95);
        bPlusTree.delete_node(bPlusTree, 555);
//        bPlusTree.search_range(bPlusTree,1,1000);

//        for(Integer i : root.p.keySet())
//            System.out.println("Key in Main :" + i);
//
        //Output
//        bPlusTree.printOutput(root);

//        String instr = null;
//        instr = args[0];
//        String line;
//        switch (instr) {
//            case "-c":
//                //line = args[1];
//                line = args[2];
//                Integer.parseInt(line);
//                System.out.println(line);
//                break;
//            case "-i":
//                break;
//            case "-d":
//                break;
//            case "-s":
//                break;
//            case "-r":
//                break;
//        }

    }
}
