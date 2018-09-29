/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.newton.coloring;

import fractal.common.ColorCalculator;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author cp316928
 */
public class BasinsOfAttractionColorCalulator implements ColorCalculator {

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        Complex root = orbit.get(orbit.size()-1);
        float h = (float)(root.arg()/(2*Math.PI));
        float s = 1;
        float v = 1;
        return new Color(Color.HSBtoRGB(h,s,v));
    }
    
    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        Complex root = lastOrbitPoint;
        float h = (float)(root.arg()/(2*Math.PI));
        float s = 1;
        float v = 1;
        return new Color(Color.HSBtoRGB(h,s,v));
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    @Override
    public String getName() {
        return "Basins of attraction";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
