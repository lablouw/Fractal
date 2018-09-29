/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.newton;

import fractal.common.Complex;

/**
 *
 * @author cp316928
 */
public abstract class TreeNode {
    protected TreeNode leftChild = null;
    protected TreeNode rightChild = null;
    protected String stringValue;

    public TreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(TreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public TreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(TreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public String getStringValue() {
        return stringValue;
    }
    
    @Override
    public String toString() {
        return stringValue;
    }

    public abstract boolean isOperatorNode();
    
    public abstract Complex evaluate(Complex x) throws Exception ;
    
}
