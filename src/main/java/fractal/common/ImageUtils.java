/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.List;

/**
 *
 * @author lloyd
 */
public class ImageUtils {

    public static BufferedImage deepCopy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    public static Color interpolateColor(float v, List<Color> colors) {
        return ColorInterpolator.interpolate(v, colors);
    }
    
//    public int presedence(TreeNode treeNode) {
//        if (treeNode.getStringValue().equals("(")) return 5;
//        if (treeNode.getStringValue().equals("+") || treeNode.getStringValue().equals("-")) return 4;
//        if (treeNode.getStringValue().equals("*") || treeNode.getStringValue().equals("/")) return 3;
//        if (treeNode.getStringValue().equals("^")) return 2;
//        else return 1;
//    }
}
