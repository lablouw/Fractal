/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.function;

import fractal.common.Complex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cp316928
 */
public class FunctionParser {
    
    private final Stack<OperatorNode> operatorStack = new Stack<OperatorNode>();
    private final List<TreeNode> postfixNodes = new ArrayList<TreeNode>();
    
    public static void main(String[] args) throws Exception {
        FunctionParser fp = new FunctionParser();
        TreeNode treeNode = fp.buildTree("sin(pi/2)");
        System.out.println(treeNode.evaluate(new Complex(0,0)));
    }
    
    public TreeNode buildTree(String function) throws Exception {
        int index = 0;
        
        //construct postFix list
        while (index < function.length()) {
            TreeNode treeNode = parseToken(function, index);
//            System.out.println("\n\n\n\nParsed \""+treeNode+"\"");
            if (treeNode instanceof OperandNode) {
                operandNodeParsed((OperandNode) treeNode);
            }
            else {
                operatorNodeParsed((OperatorNode) treeNode);
            }
//            System.out.println("operatorStack: \n"+printList(operatorStack, false)); System.out.println("postfixStack: \n"+printList(postfixNodes, true));
            index += treeNode.getStringValue().length();
        }
        while (!operatorStack.isEmpty()) {
            postfixNodes.add(operatorStack.pop());
        }
//        System.out.println("operatorStack: \n"+printList(operatorStack, false)); System.out.println("postfixStack: \n"+printList(postfixNodes, true));
        
        //Convert postfix list to tree
        Stack<TreeNode> treeStack = new Stack<TreeNode>();
        for (int i=0; i<postfixNodes.size(); i++) {
            TreeNode n = postfixNodes.get(i);
            if (n instanceof OperandNode) {
                treeStack.push(n);
            }
            else if (n instanceof OperatorNode) {
                if (((OperatorNode)n).getNumOperands() == 1) {
                    n.setLeftChild(treeStack.pop());
                    treeStack.push(n);
                }
                else {
                    n.setRightChild(treeStack.pop());
                    n.setLeftChild(treeStack.pop());
                    treeStack.push(n);
                }
            }
        }
        return treeStack.pop();
    }
    
    private void operatorNodeParsed(OperatorNode operatorNode) throws Exception {
        if (operatorNode.isOpeningParen()) {
            operatorStack.push(operatorNode);
        }
        else if (operatorNode.isClosingParen()) {
            while (!operatorStack.peek().isMatchingParen(operatorNode)) {
                postfixNodes.add(operatorStack.pop());
            }
            if (!operatorStack.pop().isOpeningParen()) throw new Exception("Expected to pop opening parenthisis.");
        }
        else {//we have operator
            if ("-".equals(operatorNode.getOperator()) && (postfixNodes.isEmpty() || "(".equals(operatorStack.peek().getOperator()))) {
                postfixNodes.add(new OperandNode("0"));
            }
            while (!operatorStack.isEmpty() && operatorStack.peek().getPrecedence() <= operatorNode.getPrecedence()) {
                postfixNodes.add(operatorStack.pop());
            }
            operatorStack.push(operatorNode);
        }
    }
    
    private void operandNodeParsed(OperandNode operandNode) {
        postfixNodes.add(operandNode);
    }
    
    private TreeNode parseToken(String function, int index) {
        if (isOperand(function.substring(index, index+1))) {
            return parseOperand(function, index);
        }
        else {
            return parseOperator(function, index);
        }
    }

    private TreeNode parseOperator(String function, int index) {
        int startIndex = index;
        String sub;
        do {
            index++;
            sub = function.substring(startIndex, index);
        }
        while (    !"(".equals(sub)
                && !")".equals(sub)
                && !"+".equals(sub)
                && !"-".equals(sub)
                && !"*".equals(sub)
                && !"/".equals(sub)
                && !"^".equals(sub)
                && !"abs".equals(sub)
                && !"cos".equals(sub)
                && !"tan".equals(sub)
                && !"sin".equals(sub)
                && !"sinh".equals(sub)
                && !"cosh".equals(sub)
                && !"tanh".equals(sub)
                && !"cosec".equals(sub)
                && !"sec".equals(sub)
                && !"cot".equals(sub)
                && !"cosech".equals(sub)
                && !"sech".equals(sub)
                && !"coth".equals(sub)
                && !"log".equals(sub)
                && !"exp".equals(sub)
                && !"pi".equals(sub)
                && !"E".equals(sub)
                && !"sqrt".equals(sub)
                && !"re".equals(sub)
                && !"im".equals(sub));
        
        if ("E".equals(sub) || "pi".equals(sub)) {
            return new OperandNode(sub);
        }
        
        return new OperatorNode(sub);
        
    }
    
    private TreeNode parseOperand(String function, int index) {
        int startIndex = index;
        do {
            index++;
        } while (index < function.length() && isOperand(function.substring(startIndex, index)));
        if (index < function.length() || function.substring(startIndex, index).endsWith(")")) index--;
        
        return new OperandNode(function.substring(startIndex, index));
    }

    private boolean isOperand(String c) {
        try {
            if ("x".equals(c)) {
                return true;
            }
            if (c.endsWith("i") && c.indexOf("i")==c.lastIndexOf("i")) {
                return true;
            }
            Double.parseDouble(c.replace("i", ""));
            return true;
        }
        catch(NumberFormatException ex) {
            return false;
        }
    }
    
    
    private String printList(List l, boolean horizontal) {
        String ss = "";
        if (horizontal) {
            for (Object o : l) {
                ss += o.toString()+" ";
            }
        }
        else {
            for (int i=l.size()-1; i>=0; i--) {
                ss += l.get(i).toString()+"\n";
            }
        }
        return ss;
    }
    
}
