/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author lloyd
 */
public interface ColorCalculator {

    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine);

    public JComponent getSettingsComponent();

    public String getName();

    public void init(FractalRenderer fractalRenderer);

    public void complete(SynchronizedBufferedImage synchronizedBufferedImage);

    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine);
}
