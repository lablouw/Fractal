/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author lloyd
 */
public interface FractalEngine
{
    public List<Complex> calcOrbit(Complex c);
    public void setMaxIter(int maxIter);
    public int getMaxIter();
    public Pair<Complex, Complex> getDefaultView();
    public boolean isBailoutReached(List<Complex> orbit);
    public JComponent getSettingsComponent();
    public void init();

    public void complete();
}
