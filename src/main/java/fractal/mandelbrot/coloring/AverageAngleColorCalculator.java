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
import fractal.mandelbrot.RawGpuOrbitContainer;

import java.awt.Color;
import java.util.ArrayList;
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
            
            for (int i = 1; i < orbit.size() - 1; i++) {
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
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        Complex lastOrbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + orbitLength - 1], rawGpuOrbitContainer.orbitsI[orbitStartIndex + orbitLength - 1]);
        if (fractalEngine.isBailoutReachedByLastOrbitPoint(lastOrbitPoint)) {
            if (orbitLength < 3) {
                return Color.WHITE;
            }

            double aveTheta = 0;

            for (int i = 1; i < orbitLength - 1; i++) {
                double dr = rawGpuOrbitContainer.orbitsR[orbitStartIndex + i + 1] - rawGpuOrbitContainer.orbitsR[orbitStartIndex + i];
                double di = rawGpuOrbitContainer.orbitsI[orbitStartIndex + i + 1] - rawGpuOrbitContainer.orbitsI[orbitStartIndex + i];
                double a = Math.sqrt(dr*dr + di*di);

                dr = rawGpuOrbitContainer.orbitsR[orbitStartIndex + i] - rawGpuOrbitContainer.orbitsR[orbitStartIndex + i - 1];
                di = rawGpuOrbitContainer.orbitsI[orbitStartIndex + i] - rawGpuOrbitContainer.orbitsI[orbitStartIndex + i - 1];
                double b = Math.sqrt(dr*dr + di*di);

                dr = rawGpuOrbitContainer.orbitsR[orbitStartIndex + i + 1] - rawGpuOrbitContainer.orbitsR[orbitStartIndex + i - 1];
                di = rawGpuOrbitContainer.orbitsI[orbitStartIndex + i + 1] - rawGpuOrbitContainer.orbitsI[orbitStartIndex + i - 1];
                double c = Math.sqrt(dr*dr + di*di);

                aveTheta += Math.acos((c*c-a*a-b*b)/(2*a*b));
            }

            aveTheta /= (double) orbitLength;

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
