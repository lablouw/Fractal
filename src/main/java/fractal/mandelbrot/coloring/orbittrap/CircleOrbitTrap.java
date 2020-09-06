/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrap extends OrbitTrap {

    private Complex center;
    private double radius;
    
    @Override
    public void setDefiningPoints(Complex p1, Complex p2) {
        this.center = p1;
        this.radius = Math.sqrt(Math.pow(p2.r-p1.r, 2) + Math.pow(p2.i-p1.i, 2));
    }

    @Override
    public double distanceFrom(Complex p) {
        return Math.abs(p.sub(center).modulus() - radius)*2;
    }

    @Override
    public String getName() {
        return "Circle";
    }

}
