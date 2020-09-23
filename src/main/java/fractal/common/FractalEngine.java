/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.util.List;
import javax.swing.JComponent;

/**
 * @author lloyd
 */
public interface FractalEngine {
	List<Complex> calcOrbit(Complex c);

	void setMaxIter(int maxIter);

	int getMaxIter();

	Pair<Complex, Complex> getDefaultView();

	boolean isBailoutReached(List<Complex> orbit);

	boolean isBailoutReachedByLastOrbitPoint(Complex lastOrbitPoint);

	JComponent getSettingsComponent();

}
