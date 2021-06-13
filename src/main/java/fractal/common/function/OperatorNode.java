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
public class OperatorNode extends TreeNode {
    
    public OperatorNode(String operator) {
        this.stringValue = operator;
    }
    
    public String getOperator() {
        return getStringValue();
    }
    
    //See https://wiki.openoffice.org/wiki/Documentation/How_Tos/Calc:_Complex_Number_functions for possible accuracy improvements for trig functions
    @Override
    public Complex evaluate(Complex x) throws Exception {
        if (isOpeningParen() || isClosingParen()) {
            throw new Exception("Cannot evaluate \""+stringValue+"\"");
        }
        
        if ("+".equals(stringValue)) {
            return leftChild.evaluate(x).add(rightChild.evaluate(x));
        }
        else if ("-".equals(stringValue)) {
            return leftChild.evaluate(x).sub(rightChild.evaluate(x));
        }
        else if ("*".equals(stringValue)) {
            return leftChild.evaluate(x).mult(rightChild.evaluate(x));
        }
        else if ("/".equals(stringValue)) {
            return leftChild.evaluate(x).div(rightChild.evaluate(x));
        }
        else if ("^".equals(stringValue)) {
            //a^b = E^(b*log(a))
            return leftChild.evaluate(x).log().mult(rightChild.evaluate(x)).exp();
        }
        else if ("sinh".equals(stringValue)) {
            return leftChild.evaluate(x).sinh();
        }
        else if ("sin".equals(stringValue)) {
            return leftChild.evaluate(x).sin();
        }
        else if ("abs".equals(stringValue)) {
            return new Complex(leftChild.evaluate(x).modulus(), 0);
        }
        else if ("cos".equals(stringValue)) {
            return leftChild.evaluate(x).cos();
        }
        else if ("tan".equals(stringValue)) {
            return leftChild.evaluate(x).tan();
        }
        else if ("cosh".equals(stringValue)) {
            return leftChild.evaluate(x).cosh();
        }
        else if ("tanh".equals(stringValue)) {
            return leftChild.evaluate(x).tanh();
        }
        else if ("cosec".equals(stringValue)) {
            return leftChild.evaluate(x).cosec();
        }
        else if ("sec".equals(stringValue)) {
            return leftChild.evaluate(x).sec();
        }
        else if ("cot".equals(stringValue)) {
            return leftChild.evaluate(x).cot();
        }
        else if ("cosech".equals(stringValue)) {
            return leftChild.evaluate(x).cosech();
        }
        else if ("sech".equals(stringValue)) {
            return leftChild.evaluate(x).sech();
        }
        else if ("coth".equals(stringValue)) {
            return leftChild.evaluate(x).coth();
        }
        else if ("log".equals(stringValue)) {
            return leftChild.evaluate(x).log();
        }
        else if ("exp".equals(stringValue)) {
            return leftChild.evaluate(x).exp();
        }
        else if ("sqrt".equals(stringValue)) {
            return leftChild.evaluate(x).sqrt();
        }
        throw new Exception("Unknown function: \""+stringValue+"\"");
    }
    
    @Override
    public boolean isOperatorNode() {
        return true;
    }
    
    public int getNumOperands() {
        if ("+".equals(stringValue) ||
                 "-".equals(stringValue) ||
                 "*".equals(stringValue) ||
                 "/".equals(stringValue) ||
                 "^".equals(stringValue)) {
            return 2;
        }
        else {
            return 1;
        }
    }
    
    public int getPrecedence() {
        if ("(".equals(stringValue)) {
            return 6;
        }
        else if ("+".equals(stringValue) || "-".equals(stringValue)) {
            return 5;
        }
        else if ("*".equals(stringValue) || "/".equals(stringValue)) {
            return 4;
        }
        if ("^".equals(stringValue)) {
            return 3;
        }
        else return 2;
    }

    public boolean isOpeningParen() {
        return "(".equals(stringValue) || "[".equals(stringValue) || "{".equals(stringValue);
    }
    
    public boolean isClosingParen() {
        return ")".equals(stringValue) || "]".equals(stringValue) || "}".equals(stringValue);
    }
    
    public boolean isMatchingParen(OperatorNode closingParenNode) throws Exception {
        if (")".equals(closingParenNode.toString()) && stringValue.equals("(")) return true;
        if ("]".equals(closingParenNode.toString()) && stringValue.equals("[")) return true;
        if ("}".equals(closingParenNode.toString()) && stringValue.equals("{")) return true;
        
        return false;
    }

}
