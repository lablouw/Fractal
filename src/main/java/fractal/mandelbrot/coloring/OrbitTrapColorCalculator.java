/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.ColorCalculator;
import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.MandelbrotEngine;
import fractal.mandelbrot.coloring.orbittrap.CircleOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.InfiniteLineOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Lloyd
 */
public class OrbitTrapColorCalculator implements ColorCalculator {

    private  JPanel settingsPanel;
    private final ColorPalette colorPalette = new ColorPalette();
    private final JSlider compressionSlider = new JSlider(1, 100);
    private float spectrumComp = 0.05f;
    private OrbitTrap orbitTrap;
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        
        double minDist = Double.MAX_VALUE;
        for (int i = 1; i<orbit.size(); i++) {//skip first mandelbrop orbit point (always == perterb or 0)
            double d = orbitTrap.distanceFrom(orbit.get(i));
            if (d < minDist) {
                minDist = d;
            }
        }
        
        return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) Math.log(minDist)*spectrumComp);
    }
    
    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not supported. Full orbit needed"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getSettingsComponent() {
        settingsPanel = new JPanel(new GridLayout(0, 1));
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
                System.out.println(spectrumComp);
            }
        });
        settingsPanel.add(compressionSlider);
        
        JComboBox<OrbitTrap> trapSelector = new JComboBox<>();
        trapSelector.setRenderer(new ListCellRenderer<OrbitTrap>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends OrbitTrap> list, OrbitTrap value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(value.getName());
            }
        });
        trapSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                orbitTrap = (OrbitTrap) e.getItem();
            }
        });
        trapSelector.addItem(new CircleOrbitTrap(new Complex(0.1, -0.2), 0.7));
        trapSelector.addItem(new InfiniteLineOrbitTrap(new Complex(-1,-1), new Complex(0, 1)));
        settingsPanel.add(trapSelector);
        
        return settingsPanel;
    }

    @Override
    public String getName() {
        return "Orbit Trap";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
