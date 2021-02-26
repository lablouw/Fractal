/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.mappers;

import fractal.common.Complex;
import java.awt.Point;

/**
 *
 * @author cp316928
 */
public class LinearMapper extends Mapper {

    private double rStep; //horizontal distance per pixel
    private double iStep; //vertical distance per pixel

    public LinearMapper(Complex topLeft, Complex bottomRight, int width, int height) {
        super(topLeft, bottomRight, width, height);
        rStep = (bottomRight.r - topLeft.r) / (double) width;
        iStep = (bottomRight.i - topLeft.i) / (double) height;
    }

    @Override
    public Complex mapToComplex(double x, double y) {
        return new Complex(topLeft.r + rStep * x, topLeft.i + iStep * y);
    }

    @Override
    public Point mapToImage(Complex c) {
        return new Point((int) Math.round((c.r - topLeft.r) / rStep), (int) Math.round((c.i - topLeft.i) / iStep));
    }

}
