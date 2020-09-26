/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase.coloring;

import fractal.common.ColorCalculator;
import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.RawGpuOrbitContainer;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author CP316928
 */
public class PhaseColorer implements ColorCalculator {
    
    private ColorPalette colorPalette = new ColorPalette(null, false, null);
    private JPanel settingsPanel;
    
    public PhaseColorer() {
        initSettingsPanel();
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return colorPalette.interpolateToColor((float) orbit.get(0).r);//TODO: temporary for Henon only
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        return null;
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getName() {
        return "Traveler Age";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        float a = (float)orbitLength/(float)fractalEngine.getMaxIter();
        return colorPalette.interpolateToColor(a);
    }

    private void initSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(0, 1));
        
        settingsPanel.add(colorPalette.getRepresentitivePanel());
    }
    
}
