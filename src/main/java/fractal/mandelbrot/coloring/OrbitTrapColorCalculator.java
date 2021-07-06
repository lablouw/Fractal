/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.ColorCalculator;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.circle.CircleOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.cross.CrossOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.infiniteline.InfiniteLineOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.linesegment.LineSegmentOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.imagelookup.ImageLookupOrbitTrap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class OrbitTrapColorCalculator implements ColorCalculator {

    private OrbitTrap[] orbitTraps;

    private JPanel settingsPanel;
    private OrbitTrap activeOrbitTrap;
    private final FractalRenderer<?> fractalRenderer;

    public OrbitTrapColorCalculator(FractalRenderer<?> fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        
        orbitTraps = new OrbitTrap[]{
            new CircleOrbitTrap(fractalRenderer),
            new InfiniteLineOrbitTrap(fractalRenderer),
            new LineSegmentOrbitTrap(fractalRenderer),
            new CrossOrbitTrap(fractalRenderer),
            new ImageLookupOrbitTrap(fractalRenderer)
        };
        
        initSettingsPanel();
    }
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return activeOrbitTrap.calcColor(x, y, orbit, fractalEngine);
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        return activeOrbitTrap.calcColor(x, y, rawGpuOrbitContainer, orbitStartIndex, orbitLength, fractalEngine);
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
    public void initForRender(FractalRenderer fractalRenderer) {
	activeOrbitTrap.initForRender(fractalRenderer);
    }
    
    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }
    
    private void initSettingsPanel() {
        settingsPanel = new JPanel(new GridLayout(0, 1));

        JComboBox<OrbitTrap> trapSelector = new JComboBox<>();
        trapSelector.setRenderer(new ListCellRenderer<OrbitTrap>() {
            public Component getListCellRendererComponent(JList<? extends OrbitTrap> list, OrbitTrap value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(value.getName());
            }
        });
        trapSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
		activeOrbitTrap = (OrbitTrap) e.getItem();
            }
        });
        for (OrbitTrap orbitTrap : orbitTraps) {
        	trapSelector.addItem(orbitTrap);
		}
        settingsPanel.add(trapSelector);
        
        JButton specifyOrbitTrapParamsButton = new JButton("Specify trap");
        specifyOrbitTrapParamsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                specifyOrbitTrapParamsButton.setEnabled(false);
                specifyOrbitTrapParamsButton.setText("Drag on image to specify");
                activeOrbitTrap.doUserDefined(fractalRenderer, specifyOrbitTrapParamsButton);
            }
        });
        settingsPanel.add(specifyOrbitTrapParamsButton);

        JButton orbitTrapSettingsButton = new JButton("Orbit trap settings");
        orbitTrapSettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeOrbitTrap.getSettingsDialog().setVisible(true);
            }
        });
        settingsPanel.add(orbitTrapSettingsButton);
    }

}
