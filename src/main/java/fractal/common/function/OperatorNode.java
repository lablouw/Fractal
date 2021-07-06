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
    
	OperatorNode(String operator) {
        this.stringValue = operator;
    }
    
    String getOperator() {
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
        else if ("abs".equals(stringValue)) {
            return new Complex(leftChild.evaluate(x).modulus(), 0);
        }

        else if ("sin".equals(stringValue)) {
            return leftChild.evaluate(x).sin();
        }
        else if ("cos".equals(stringValue)) {
            return leftChild.evaluate(x).cos();
        }
        else if ("tan".equals(stringValue)) {
            return leftChild.evaluate(x).tan();
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

        else if ("hsin".equals(stringValue)) {
            return leftChild.evaluate(x).hsin();
        }
        else if ("hcos".equals(stringValue)) {
            return leftChild.evaluate(x).hcos();
        }
        else if ("htan".equals(stringValue)) {
            return leftChild.evaluate(x).htan();
        }
        else if ("hcosec".equals(stringValue)) {
            return leftChild.evaluate(x).hcosec();
        }
        else if ("hsec".equals(stringValue)) {
            return leftChild.evaluate(x).hsec();
        }
        else if ("hcot".equals(stringValue)) {
            return leftChild.evaluate(x).hcot();
        }

        else if ("asin".equals(stringValue)) {
            return leftChild.evaluate(x).asin();
        }
        else if ("acos".equals(stringValue)) {
            return leftChild.evaluate(x).acos();
        }
        else if ("atan".equals(stringValue)) {
            return leftChild.evaluate(x).atan();
        }
        else if ("acsc".equals(stringValue)) {
            return leftChild.evaluate(x).acosec();
        }
        else if ("asec".equals(stringValue)) {
            return leftChild.evaluate(x).asec();
        }
        else if ("acot".equals(stringValue)) {
            return leftChild.evaluate(x).acot();
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
        else if ("RE".equals(stringValue)) {
            return new Complex(leftChild.evaluate(x).r, 0);
        }
        else if ("IM".equals(stringValue)) {
            return new Complex(0, leftChild.evaluate(x).i);
        }
        else if ("conj".equals(stringValue)) {
            return leftChild.evaluate(x).conj();
        }
        throw new Exception("Unknown function: \""+stringValue+"\"");
    }
    
    int getNumOperands() {
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
    
    int getPrecedence() {
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

    boolean isOpeningParen() {
        return "(".equals(stringValue) || "[".equals(stringValue) || "{".equals(stringValue);
    }
    
    boolean isClosingParen() {
        return ")".equals(stringValue) || "]".equals(stringValue) || "}".equals(stringValue);
    }
    
    boolean isMatchingParen(OperatorNode closingParenNode) {
        return ")".equals(closingParenNode.toString()) && stringValue.equals("(") ||
            "]".equals(closingParenNode.toString()) && stringValue.equals("[") ||
            "}".equals(closingParenNode.toString()) && stringValue.equals("{");
    }

}
