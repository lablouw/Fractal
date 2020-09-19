/*
 * To change this template, choose Tools | Templates
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
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author lloyd
 */
public class BandColorCalculator implements ColorCalculator {

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(orbit)) {
            return new Color(Color.HSBtoRGB(((float) orbit.size()) / ((float) fractalEngine.getMaxIter()), 1, 1));
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        Complex lastOrbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex+orbitLength-1], rawGpuOrbitContainer.orbitsI[orbitStartIndex+orbitLength-1]);
        if (fractalEngine.isBailoutReachedByLastOrbitPoint(lastOrbitPoint)) {
            return new Color(Color.HSBtoRGB(((float) orbitLength) / ((float) fractalEngine.getMaxIter()), 1, 1));
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    @Override
    public String getName() {
        return "Banded";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            return new Color(Color.HSBtoRGB(((float) orbitLength) / ((float) fractalEngine.getMaxIter()), 1, 1));
        } else {
            return Color.BLACK;
        }
    }

}
