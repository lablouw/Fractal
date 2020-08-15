/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase.coloring;

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
 * @author Lloyd
 */
public class WhiteColorer implements ColorCalculator {

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return Color.WHITE;
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    @Override
    public String getName() {
        return "White";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        return Color.WHITE;
    }
    
}
