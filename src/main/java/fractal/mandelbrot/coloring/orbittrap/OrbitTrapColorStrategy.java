/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.mandelbrot.RawGpuOrbitContainer;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public interface OrbitTrapColorStrategy<T extends OrbitTrap> {
    
    public static final float LOGARITHM_SUPPRESSION = 0.05f;
    
    String getName();
    
    void initForRender();
    
    Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, T orbitTrap);
    
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, T orbitTrap);
    Color recalcColor(int x, int y);

    Component getSettingsComponent();

    
}
