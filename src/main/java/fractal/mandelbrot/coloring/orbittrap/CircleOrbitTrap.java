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
public class CircleOrbitTrap implements OrbitTrap {

    private final Complex center;
    private final double radius;

    public CircleOrbitTrap(Complex center, double radius) {
        this.center = center;
        this.radius = radius;
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
