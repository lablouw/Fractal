/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.circle;

import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrapColorLookupStrategy implements OrbitTrapColorStrategy<CircleOrbitTrap>{

    @Override
    public String getName() {
        return "Color gradient lookup";
    }

    @Override
    public void initForRender() {
        
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, CircleOrbitTrap orbitTrap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Color recalcColor(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getSettingsComponent() {
        return null;
    }

}
