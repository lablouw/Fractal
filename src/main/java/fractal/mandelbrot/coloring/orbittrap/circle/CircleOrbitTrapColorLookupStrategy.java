/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.circle;

import fractal.common.ColorPalette;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.mandelbrot.RawGpuOrbitContainer;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrapColorLookupStrategy implements OrbitTrapColorStrategy<CircleOrbitTrap> {

    private JPanel settingsPanel;//TODO: JPanel for every strategy to be displayed in CircleOrbitTrapSettingsDialog
    private final ColorPalette colorPalette = new ColorPalette(null, false, null);
    

    public CircleOrbitTrapColorLookupStrategy() {
        initSettingsPanel();
    }

    @Override
    public String getName() {
        return "Color Lookup";
    }

    @Override
    public void initForRender() {
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

        if (minDist > orbitTrap.getRadius()) {
            return Color.BLACK;
        } else {
            return colorPalette.interpolateToColor((float) (-minDist / orbitTrap.getRadius() / 2), false);
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

        if (minDist > orbitTrap.getRadius()) {
            return Color.BLACK;
        } else {
            return colorPalette.interpolateToColor((float) (-minDist / orbitTrap.getRadius() / 2), false);
        }
    }

    @Override
    public Color recalcColor(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }
    
    private void initSettingsPanel() {
        settingsPanel = new JPanel(new GridLayout(0, 1));
        settingsPanel.add(colorPalette.getRepresentativePanel());
    }

}
