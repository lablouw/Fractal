/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.*;
import fractal.mandelbrot.RawGpuOrbitContainer;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author lloyd
 */
public class SmoothColorCalculator implements ColorCalculator, Redrawable {

    private final FractalRenderer fractalRenderer;
    private JPanel settingsPanel;
    private Complex[][] orbitEndPoints;
    private int[][] orbitLengths;
    
    private final ColorPalette colorPalette;

    public SmoothColorCalculator(final FractalRenderer renderer) {
        this.fractalRenderer = renderer;
        colorPalette = new ColorPalette(null, false, this);
        initSettingsPanel();
    }

    @Override
    public void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, recalcColor(orbitEndPoints[x][y], orbitLengths[x][y], fractalRenderer.getFractalEngine()));
            }
        }
        fractalRenderer.updateGui();
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        orbitEndPoints[x][y] = orbit.get(orbit.size() - 1);
        orbitLengths[x][y] = orbit.size();
        if (fractalEngine.isBailoutReached(orbit)) {
            float nSmooth = (float) (orbit.size() + 1 - Math.log(Math.log(orbit.get(orbit.size() - 1).modulus())) / Math.log(2));
            float a = nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter();
            return colorPalette.interpolateToColor(a, true);
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        int lastOrbitPointIndex = orbitStartIndex + orbitLength - 1;
        Complex lastOrbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[lastOrbitPointIndex], rawGpuOrbitContainer.orbitsI[lastOrbitPointIndex]);
        return calcColor(x, y, lastOrbitPoint, orbitLength, fractalEngine);
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        orbitEndPoints[x][y] = lastOrbitPoint;
        orbitLengths[x][y] = orbitLength;
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            float nSmooth = (float) (orbitLength + 1 - Math.log(Math.log(lastOrbitPoint.modulus())) / Math.log(2));
            float a = nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter();
            return colorPalette.interpolateToColor(a, true);
        } else {
            return Color.BLACK;
        }
    }

    private Color recalcColor(Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            float nSmooth = (float) (orbitLength + 1 - Math.log(Math.log(lastOrbitPoint.modulus())) / Math.log(2));
            float a = nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter();
            return colorPalette.interpolateToColor(a, true);
        } else {
            return Color.BLACK;
        }
    }

    @Override
    public String getName() {
        return "Smooth";
    }

    private void initSettingsPanel() {
        settingsPanel = new JPanel(new GridLayout(0, 1));

        settingsPanel.add(colorPalette.getRepresentitivePanel());
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
        orbitEndPoints = new Complex[imageWidth][imageHeight];
        orbitLengths = new int[imageWidth][imageHeight];
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
