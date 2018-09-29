/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.ColorCalculator;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.MandelbrotEngine;
import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Lloyd
 */
public class ExperimentalColorCalculator implements ColorCalculator {

    private Complex trap = new Complex(-2.2, 0.2);
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        
//        if (!fractalEngine.isBailoutReached(orbit)) {
//            return Color.BLACK;
//        }
        
        double minDist = Double.MAX_VALUE;
        for (Complex p : orbit) {
            double d = p.sub(trap).modulus();
            if (d < minDist) {
                minDist = d;
            }
        }
        
        double maxPossibleDist = ((MandelbrotEngine)fractalEngine).getBailout() + trap.modulus();
        
        return new Color(Color.HSBtoRGB((float) (minDist/maxPossibleDist), 1, 1));
        
    }
    
    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not supported. Full orbit needed"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    @Override
    public String getName() {
        return "Experimental";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
