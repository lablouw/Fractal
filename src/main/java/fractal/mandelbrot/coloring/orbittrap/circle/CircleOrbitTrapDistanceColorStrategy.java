/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.circle;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Redrawable;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import java.awt.*;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrapDistanceColorStrategy implements OrbitTrapColorStrategy<CircleOrbitTrap>, Redrawable {

    private final CircleOrbitTrapDistanceColorStrategySettingsPanel settingsPanel = new CircleOrbitTrapDistanceColorStrategySettingsPanel(this);
    
    private double [][] minDists;
    private final FractalRenderer fractalRenderer;
    private final CircleOrbitTrap orbitTrap;

    public CircleOrbitTrapDistanceColorStrategy(FractalRenderer fractalRenderer, CircleOrbitTrap orbitTrap) {
        this.fractalRenderer = fractalRenderer;
        this.orbitTrap = orbitTrap;
    }
    
    @Override
    public String getName() {
        return "Distance to Circle";
    }
    
    @Override
    public void initForRender() {
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
        minDists = new double[imageWidth][imageHeight];
    }
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, CircleOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            double d = Math.abs(orbitTrap.distanceFrom(orbit.get(i)));
            if (d < minDist) {
                minDist = d;
            }
        }

        minDists[x][y] = minDist;
        return minDist == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDist) * LOGARITHM_SUPPRESSION, true);
    }
    
    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, CircleOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        for (int i = 1; i < orbitLength; i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            double d = Math.abs(orbitTrap.distanceFrom(new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i])));
            if (d < minDist) {
                minDist = d;
            }
        }

        minDists[x][y] = minDist;
        return minDist == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDist) * LOGARITHM_SUPPRESSION, true);
    }

    @Override
    public Color recalcColor(int x, int y) {
        return minDists[x][y] == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDists[x][y]) * LOGARITHM_SUPPRESSION, true);
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
