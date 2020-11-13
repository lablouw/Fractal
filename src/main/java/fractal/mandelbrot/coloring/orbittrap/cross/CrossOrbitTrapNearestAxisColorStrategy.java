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

    private final CrossOrbitTrapNearestAxisColorStrategySettingsPanel settingsPanel = new CrossOrbitTrapNearestAxisColorStrategySettingsPanel(this);
    
    private final FractalRenderer fractalRenderer;
    private final CrossOrbitTrap orbitTrap;

    private double [][] minDists;
    private int [][] minDistsAxisNo;

    
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
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
        minDists = new double[imageWidth][imageHeight];
        minDistsAxisNo = new int[imageWidth][imageHeight];
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, CrossOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        int axisNo = 0;
        for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0)
            double d1 = orbitTrap.distanceFromAxis1(orbit.get(i));
            double d2 = orbitTrap.distanceFromAxis2(orbit.get(i));
            if (d1 < minDist && d1 < settingsPanel.getAxis1MaxDistance()) {
                minDist = d1;
                axisNo = 1;
            }
            if (d2 < minDist && d2 < settingsPanel.getAxis2MaxDistance()) {
                minDist = d2;
                axisNo = 2;
            }
        }

        minDists[x][y] = minDist;
        minDistsAxisNo[x][y] = axisNo;

        return getColor(axisNo, minDist);
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, CrossOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        int axisNo = 0;
        for (int i = 1; i < orbitLength; i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0)
            Complex c = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]);
            double d1 = orbitTrap.distanceFromAxis1(c);
            double d2 = orbitTrap.distanceFromAxis2(c);
            if (d1 < minDist && d1 < settingsPanel.getAxis1MaxDistance()) {
                minDist = d1;
                axisNo = 1;
            }
            if (d2 < minDist && d2 < settingsPanel.getAxis2MaxDistance()) {
                minDist = d2;
                axisNo = 2;
            }
        }

        minDists[x][y] = minDist;
        minDistsAxisNo[x][y] = axisNo;

        return getColor(axisNo, minDist);
    }

    private Color getColor(int axisNo, double minDist) {
        if (axisNo == 1 && minDist < settingsPanel.getAxis1MaxDistance()) {
            return settingsPanel.getAxis1ColorPalette().interpolateToColor(minDist/settingsPanel.getAxis1MaxDistance(), false);
        } else if (axisNo == 2 && minDist < settingsPanel.getAxis2MaxDistance()) {
            return settingsPanel.getAxis2ColorPalette().interpolateToColor(minDist/settingsPanel.getAxis2MaxDistance(), false);
        }

        return Color.BLACK;
    }

    @Override
    public Color recalcColor(int x, int y) {
        return getColor(minDistsAxisNo[x][y], minDists[x][y]);
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, orbitTrap.reCalcColor(x, y));
            }
        }
        fractalRenderer.updateGui();
    }
    
    
    
}
