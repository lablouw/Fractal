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
public class CircleOrbitTrapColorLookupStrategy implements OrbitTrapColorStrategy<CircleOrbitTrap>, Redrawable {

    private final CircleOrbitTrapColorLookupStrategySettingsPanel settingsPanel = new CircleOrbitTrapColorLookupStrategySettingsPanel(this);

    private final FractalRenderer fractalRenderer;
    private final CircleOrbitTrap orbitTrap;

    private double [][] minDists;

    public CircleOrbitTrapColorLookupStrategy(FractalRenderer fractalRenderer, CircleOrbitTrap orbitTrap) {
        this.fractalRenderer = fractalRenderer;
        this.orbitTrap = orbitTrap;
    }

    @Override
    public String getName() {
        return "Color Lookup";
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
        for (int i = 1; i < orbit.size(); i++) {
            double dist = orbitTrap.distanceFrom(orbit.get(i));
            if (dist < minDist) {
                minDist = dist;
            }
        }

        minDists[x][y] = minDist;

        if (minDist > orbitTrap.getRadius()) {
            return Color.BLACK;
        } else {
            return settingsPanel.getColorPalette().interpolateToColor((-minDist / orbitTrap.getRadius() / 2), false);
        }
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, CircleOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
        for (int i = 1; i < orbitLength; i++) {
            double dist = orbitTrap.distanceFrom(new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]));
            if (dist < minDist) {
                minDist = dist;
            }
        }

        minDists[x][y] = minDist;

        if (minDist > orbitTrap.getRadius()) {
            return Color.BLACK;
        } else {
            return settingsPanel.getColorPalette().interpolateToColor((-minDist / orbitTrap.getRadius() / 2), false);
        }
    }

    @Override
    public Color recalcColor(int x, int y) {
        if (minDists[x][y] > orbitTrap.getRadius()) {
            return Color.BLACK;
        } else {
            return settingsPanel.getColorPalette().interpolateToColor((-minDists[x][y] / orbitTrap.getRadius() / 2), false);
        }
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
