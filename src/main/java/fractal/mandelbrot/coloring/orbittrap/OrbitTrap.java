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
public interface OrbitTrap {
    
    String getName();
    
    double distanceFrom(Complex p);
    
}
