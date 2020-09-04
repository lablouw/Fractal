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
public class InfiniteLineOrbitTrap implements OrbitTrap {

    private double m, c; //y=mx+c
    //-mx +y -c = 0
    // a==-m, b==1 c==-c
    
    // mx -y +c = 0
    // a==m, b=-1, c==c
    
    //Distance between line and point:
    //Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)

    public InfiniteLineOrbitTrap(Complex p1, Complex p2) {
        if (p1.r > p2.r) {
            Complex t = p1;
            p1 = p2;
            p2 = t;
        }

        m = (p2.i - p1.i) / (p2.r - p1.r);
        c = p1.i - m * p1.r;
    }

    @Override
    public double distanceFrom(Complex p) {
        return Math.abs(m*p.r - p.i + c) / Math.sqrt(m*m + 1);
    }

    @Override
    public String getName() {
        return "Infinite line";
    }

}
