/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.circle;

import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrapDistanceColorStrategy implements OrbitTrapColorStrategy<CircleOrbitTrap>{

    private JPanel settingsPanel;
    private final ColorPalette colorPalette = new ColorPalette();
    private float spectrumComp = 0.05f;
    private final JSlider compressionSlider = new JSlider(1, 100);
    
    private double [][] minDists;
    private final FractalRenderer fractalRenderer;
    private final CircleOrbitTrap orbitTrap;
    
    public CircleOrbitTrapDistanceColorStrategy(FractalRenderer fractalRenderer, CircleOrbitTrap orbitTrap) {
        this.fractalRenderer = fractalRenderer;
        this.orbitTrap = orbitTrap;
        initSettingsPanel();
    }
    
    @Override
    public String getName() {
        return "Distance to circle";
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
        return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDist) * spectrumComp);
    }

    @Override
    public Color recalcColor(int x, int y) {
        return minDists[x][y] == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDists[x][y])*spectrumComp);
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }
    
    private void initSettingsPanel() {
        settingsPanel = new JPanel(new GridLayout(0, 1));
        settingsPanel.add(colorPalette);

        settingsPanel.add(new JLabel("Spectrum compression"));
        compressionSlider.setValue(1);
        compressionSlider.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {
                spectrumComp = (float) ((float) (double) compressionSlider.getValue() / 500d);
                redraw();
            }
        });
        settingsPanel.add(compressionSlider);
    }
    
    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, orbitTrap.reCalcColor(x, y));
            }
        }
        fractalRenderer.updateGui();
    }
    
}
