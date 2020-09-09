/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.Mappers;

import fractal.common.Complex;
import org.jdesktop.swingx.JXImagePanel;

import java.awt.Point;


/**
 *
 * @author lloyd
 */
public abstract class Mapper
{
    final Complex topLeft;
    final Complex bottomRight;
    final double xStep;
    final double yStep;
    final int width;
    final int height;

    public Mapper(Complex p1, Complex p2, int width, int height) {
        this.topLeft = p1;
        this.bottomRight = p2;
        this.width = width;
        this.height = height;
        this.xStep = (p2.r-p1.r)/(double)width;
        this.yStep = (p2.i-p1.i)/(double)height;
    }

    public abstract Point mapToImage(Complex c);

    public abstract Complex mapToComplex(int x, int y);

    public Complex mapToComplex(int x, int y, JXImagePanel jXImagePanel1) {
        double scaleRatio = Math.min((double) jXImagePanel1.getWidth() / (double) jXImagePanel1.getImage().getWidth(null), (double) jXImagePanel1.getHeight() / (double) jXImagePanel1.getImage().getHeight(null));
        int scaledImageWidth = (int) (jXImagePanel1.getImage().getWidth(null) * scaleRatio);
        int scaledImageHeight = (int) (jXImagePanel1.getImage().getHeight(null) * scaleRatio);
        int xSub = (jXImagePanel1.getWidth() - scaledImageWidth) / 2;
        int ySub = (jXImagePanel1.getHeight() - scaledImageHeight) / 2;
        int relativeX = (int) ((double) ((x - xSub)) / scaleRatio);
        int relativeY = (int) ((double) ((y - ySub)) / scaleRatio);

        return mapToComplex(relativeX, relativeY);
    }
    
    public Complex getTopLeft() {
        return topLeft;
    }
    public Complex getBottomRight() {
        return bottomRight;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getXStep() {
        return xStep;
    }

    public double getYStep() {
        return yStep;
    }
    
}
