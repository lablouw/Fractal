/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.mappers;

import fractal.common.Complex;
import org.jdesktop.swingx.JXImagePanel;

import java.awt.Point;


/**
 *
 * @author lloyd
 */
public abstract class Mapper {
    final Complex topLeft;
    final Complex bottomRight;
    final double rStep; //horizontal distance per pixel
    final double iStep; //vertical distance per pixel
    private final int width;
    private final int height;

    protected Mapper(Complex topLeft, Complex bottomRight, int width, int height) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.width = width;
        this.height = height;
        this.rStep = (bottomRight.r - topLeft.r) / (double) width;
        this.iStep = (bottomRight.i - topLeft.i) / (double) height;
    }

    public abstract Point mapToImage(Complex c);

    public abstract Complex mapToComplex(int x, int y);

    /**
     * Use if the x and y coordinates are from a scaled image (i.e. mouse position) rather than absolute/exact.
     */
    public Complex mapToComplex(int x, int y, JXImagePanel imagePanel) {
        double scaleRatio = Math.min((double) imagePanel.getWidth() / (double) imagePanel.getImage().getWidth(null), (double) imagePanel.getHeight() / (double) imagePanel.getImage().getHeight(null));
        int scaledImageWidth = (int) (imagePanel.getImage().getWidth(null) * scaleRatio);
        int scaledImageHeight = (int) (imagePanel.getImage().getHeight(null) * scaleRatio);
        int xSub = (imagePanel.getWidth() - scaledImageWidth) / 2;
        int ySub = (imagePanel.getHeight() - scaledImageHeight) / 2;
        int relativeX = (int) ((double) (x - xSub) / scaleRatio);
        int relativeY = (int) ((double) (y - ySub) / scaleRatio);

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

    public double getRStep() {
        return rStep;
    }

    public double getIStep() {
        return iStep;
    }
    
}
