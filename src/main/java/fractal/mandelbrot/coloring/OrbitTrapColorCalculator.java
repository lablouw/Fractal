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
import fractal.mandelbrot.coloring.orbittrap.CircleOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.InfiniteLineOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.LineSegmentOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JButton;
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

    private JPanel settingsPanel;
    private final ColorPalette colorPalette = new ColorPalette();
    private final JSlider compressionSlider = new JSlider(1, 100);
    private float spectrumComp = 0.05f;
    private OrbitTrap orbitTrap;
    private final FractalRenderer fractalRenderer;
    private double [][] minDists;//TODO: Move to orbit trap implementations
    
    public OrbitTrapColorCalculator(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        initSettingsPanel();
    }
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
//        return orbitTrap.calcColor()

        double minDist = Double.MAX_VALUE;
        for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            double d = orbitTrap.distanceFrom(orbit.get(i));
            if (d < minDist) {
                minDist = d;

            }
        }

        minDists[x][y] = minDist;
        return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDist) * spectrumComp);
    }
    
    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not supported. Full orbit needed");
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getName() {
        return "Orbit Trap";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
//        orbitTrap.init(fractalRenderer);
        minDists = new double[imageWidth][imageHeight];
    }
    
    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }
    
    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, recalcColor(minDists[x][y]));
            }
        }
        fractalRenderer.updateGui();
    }
    
    private Color recalcColor(double minDist) {
        return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDist)*spectrumComp);
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
        
        JComboBox<OrbitTrap> trapSelector = new JComboBox<>();
        trapSelector.setRenderer(new ListCellRenderer<OrbitTrap>() {
            public Component getListCellRendererComponent(JList<? extends OrbitTrap> list, OrbitTrap value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(value.getName());
            }
        });
        trapSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                orbitTrap = (OrbitTrap) e.getItem();
            }
        });
        trapSelector.addItem(new CircleOrbitTrap());
        trapSelector.addItem(new InfiniteLineOrbitTrap());
        trapSelector.addItem(new LineSegmentOrbitTrap());
        settingsPanel.add(trapSelector);
        
        JButton specifyOrbitTrapParamsButton = new JButton("Specify trap");
        specifyOrbitTrapParamsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                specifyOrbitTrapParamsButton.setEnabled(false);
                specifyOrbitTrapParamsButton.setText("Drag on image to specify");
                orbitTrap.doUserDefined(fractalRenderer, specifyOrbitTrapParamsButton);
            }
        });
        settingsPanel.add(specifyOrbitTrapParamsButton);
    }

}
