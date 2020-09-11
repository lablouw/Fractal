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
import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Lloyd
 */
public class AverageAngleColorCalculator implements ColorCalculator {

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(orbit)) {
            if (orbit.size() < 3) {
                return Color.WHITE;
            }
            
            double aveTheta = 0;
            
            for (int i = 1; i < orbit.size() -1; i++) {
                double a = orbit.get(i+1).sub(orbit.get(i)).modulus();
                double b = orbit.get(i).sub(orbit.get(i-1)).modulus();
                double c = orbit.get(i+1).sub(orbit.get(i-1)).modulus();

                aveTheta += Math.acos((c*c-a*a-b*b)/(2*a*b));
            }
            aveTheta /= (double) orbit.size();
            
            return new Color(Color.HSBtoRGB((float)(aveTheta/Math.PI), 1, 1));
        } else {
            return Color.BLACK;
        }
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
        return "Average Angle";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
