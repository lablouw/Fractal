/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.paremetermappers;

import fractal.common.Complex;
import fractal.common.function.FunctionParser;
import fractal.common.function.TreeNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lloyd
 */
public class UserDefinedFunctionParameterMapper implements ParameterMapper {

    private String function = "tanx/expx";
    private FunctionParser functionParser = new FunctionParser();
    private TreeNode treeNode = null;
    
    @Override
    public Complex map(Complex c) {
        Complex eval = c;
        try {
            if (treeNode == null) {
            synchronized(function) {
                treeNode = functionParser.buildTree(function);
            }}
            eval = treeNode.evaluate(c);
        } catch (Exception ex) {
            System.out.println(c);
            Logger.getLogger(UserDefinedFunctionParameterMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return eval;
    }

    @Override
    public String getName() {
        return "User defined";
    }
    
}
