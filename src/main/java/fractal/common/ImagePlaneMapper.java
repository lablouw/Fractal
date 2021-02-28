/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import fractal.common.Complex;
import org.jdesktop.swingx.JXImagePanel;

import java.awt.Point;


/**
 *
 * @author lloyd
 */
public class ImagePlaneMapper {
    private final Complex topLeft;
    private final Complex bottomRight;
    private final int width;
    private final int height;

    private double rStep; //horizontal distance per pixel
    private double iStep; //vertical distance per pixel

    public ImagePlaneMapper(Complex topLeft, Complex bottomRight, int width, int height) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.width = width;
        this.height = height;
        rStep = (bottomRight.r - topLeft.r) / (double) width;
        iStep = (bottomRight.i - topLeft.i) / (double) height;
    }

    public Complex mapToComplex(double x, double y) {
        return new Complex(topLeft.r + rStep * x, topLeft.i + iStep * y);
    }

    public Point mapToImage(Complex c) {
        return new Point((int) Math.round((c.r - topLeft.r) / rStep), (int) Math.round((c.i - topLeft.i) / iStep));
    }

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

}
