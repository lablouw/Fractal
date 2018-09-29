/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import fractal.newton.TreeNode;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 *
 * @author lloyd
 */
public class Utils {

    public static BufferedImage deepCopy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    public int presedence(TreeNode treeNode) {
        if (treeNode.getStringValue().equals("(")) return 5;
        if (treeNode.getStringValue().equals("+") || treeNode.getStringValue().equals("-")) return 4;
        if (treeNode.getStringValue().equals("*") || treeNode.getStringValue().equals("/")) return 3;
        if (treeNode.getStringValue().equals("^")) return 2;
        else return 1;
    }
}
