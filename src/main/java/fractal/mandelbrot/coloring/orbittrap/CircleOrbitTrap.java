/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrap extends OrbitTrap {

    private Complex center;
    private Complex pointOnCircle;
    private double radius;

    public CircleOrbitTrap() {
        setDefiningPoints(new Complex(0,0), new Complex(0.25, 0));
    }

    @Override
    public void setDefiningPoints(Complex c1, Complex c2) {
        this.center = c1;
        this.pointOnCircle = c2;
        this.radius = Math.sqrt(Math.pow(c2.r-c1.r, 2) + Math.pow(c2.i-c1.i, 2));
    }

    @Override
    public double distanceFrom(Complex c) {
        return Math.abs(c.sub(center).modulus() - radius);
    }

    @Override
    public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
        Point c = fractalRenderer.getMapper().mapToImage(center);
        Point poc = fractalRenderer.getMapper().mapToImage(pointOnCircle);
        int r = (int) Math.sqrt((poc.x-c.x) * (poc.x-c.x) + (poc.y-c.y) * (poc.y-c.y));

        BufferedImage im = ImageUtils.deepCopy(baseImage);
        im.getGraphics().drawOval(c.x-r, c.y-r, r*2, r*2);
        return im;
    }

    @Override
    public String getName() {
        return "Circle";
    }

}
