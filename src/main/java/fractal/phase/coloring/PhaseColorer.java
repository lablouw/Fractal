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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 *
 * @author CP316928
 */
public class PhaseColorer implements ColorCalculator {
    
    private ColorPalette colorPalette = new ColorPalette();
    private JPanel settingsPanel;
    
    private final JSlider compressionSlider = new JSlider(1, 2000);
    private float spectrumComp = 1;

    public PhaseColorer() {
        initSettingsPanel();
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return colorPalette.getColor((float) orbit.get(0).r);//TODO: temporary for Henon only
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
    public void init(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        float a = ((float)orbitLength/(float)fractalEngine.getMaxIter() * spectrumComp) % 1;
        return colorPalette.getColor(a);
    }

    private void initSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(0, 1));
        
        settingsPanel.add(colorPalette);
        
        settingsPanel.add(new JLabel("Spectrum compression"));
        compressionSlider.setValue(1);
        compressionSlider.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {
                spectrumComp = (float) ((float) (double) compressionSlider.getValue() / 500d);
            }
        });
        settingsPanel.add(compressionSlider);

    }
    
}
