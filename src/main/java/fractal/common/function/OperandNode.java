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
public class OperandNode extends TreeNode {
    private final Complex value;

    OperandNode(String stringValue) {
        this.stringValue = stringValue;
        value = new Complex();
        
        if ("x".equals(stringValue)) {
            return;
        }
        
        if ("PI".equals(stringValue)) {
            value.r = Math.PI;
        }
        else if ("E".equals(stringValue)) {
            value.r = Math.E;
        }
        else if (stringValue.contains("i")) {
            String val = stringValue.replace("i", "");
            if (val.isEmpty()) {
                val="1";
            }
            value.i = Double.parseDouble(val);
        }
        else {
            value.r = Double.parseDouble(stringValue);
        }
    }

    @Override
    public Complex evaluate(Complex x) throws Exception {
        if ("x".equals(stringValue)) {
            return x;
        }
        return value;
    }

}
