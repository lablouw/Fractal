/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.cross;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Redrawable;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class CrossOrbitTrapNearestAxisColorStrategy implements OrbitTrapColorStrategy<CrossOrbitTrap>, Redrawable {

    private final CrossOrbitTrapNearestAxisColorStrategySettingsPanel settingsPanel = new CrossOrbitTrapNearestAxisColorStrategySettingsPanel();
    
    private final FractalRenderer fractalRenderer;
    private final CrossOrbitTrap orbitTrap;
    
    public CrossOrbitTrapNearestAxisColorStrategy(FractalRenderer fractalRenderer, CrossOrbitTrap orbitTrap) {
		this.fractalRenderer = fractalRenderer;
		this.orbitTrap = orbitTrap;
	}
    
    @Override
    public String getName() {
        return "Nearest axis";
    }

    @Override
    public void initForRender() {
        
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, CrossOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        int axisNo = 0;
        for (Complex c : orbit) {
            double d1 = orbitTrap.distanceFromAxis1(c);
            double d2 = orbitTrap.distanceFromAxis2(c);
            if (d1 < minDist) {
                minDist = d1;
                axisNo = 1;
            }
            if (d2 < minDist) {
                minDist = d2;
                axisNo = 2;
            }
        }
        
        if (axisNo == 1 && minDist < settingsPanel.getAxis1MaxDistance()) {
            return settingsPanel.colorPalette1.interpolateToColor((float) (minDist/settingsPanel.getAxis1MaxDistance()), false);
        } else if (axisNo == 2 && minDist < settingsPanel.getAxis2MaxDistance()) {
            return settingsPanel.colorPalette2.interpolateToColor((float) (minDist/settingsPanel.getAxis2MaxDistance()), false);
        }
        
        return Color.BLACK;
        
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, CrossOrbitTrap orbitTrap) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Color recalcColor(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void redraw() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
