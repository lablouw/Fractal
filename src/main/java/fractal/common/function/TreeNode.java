/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.function;

import fractal.common.Complex;

/**
 *
 * @author cp316928
 */
public abstract class TreeNode {
    TreeNode leftChild = null;
    TreeNode rightChild = null;
    String stringValue;

    void setLeftChild(TreeNode leftChild) {
        this.leftChild = leftChild;
    }

    void setRightChild(TreeNode rightChild) {
        this.rightChild = rightChild;
    }

    String getStringValue() {
        return stringValue;
    }
    
    @Override
    public String toString() {
        return stringValue;
    }

    public abstract Complex evaluate(Complex x) throws Exception ;
    
}
