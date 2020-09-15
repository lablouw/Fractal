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

    JComponent getSettingsComponent();

    String getName();

    void initForRender(FractalRenderer fractalRenderer);

    void complete(SynchronizedBufferedImage synchronizedBufferedImage);
    
    Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine);
    
    //#GPU_OPTIZATION:
    //Color calcColor(int x, int y, double[][], FractalEngine fractalEngine);

    Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine);
}
