/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author lloyd
 */
public interface ColorCalculator {

	Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine);

	JComponent getSettingsComponent();

	String getName();

	void init(FractalRenderer fractalRenderer);

	void complete(SynchronizedBufferedImage synchronizedBufferedImage);

	Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine);
}
