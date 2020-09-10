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
import fractal.mandelbrot.coloring.orbittrap.CircleOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.CrossOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.InfiniteLineOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.LineSegmentOrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;

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

	private OrbitTrap[] orbitTraps = new OrbitTrap[]{
			new CircleOrbitTrap(),
			new InfiniteLineOrbitTrap(),
			new LineSegmentOrbitTrap(),
            new CrossOrbitTrap()
	};

    private JPanel settingsPanel;
    private OrbitTrap activeOrbitTrap;
    private final FractalRenderer fractalRenderer;

    public OrbitTrapColorCalculator(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        initSettingsPanel();
    }
    
    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return activeOrbitTrap.calcColor(x, y, orbit, fractalEngine);
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
		activeOrbitTrap.init(fractalRenderer);
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
                if (activeOrbitTrap != null && settingsPanel != null) {
                    settingsPanel.remove(activeOrbitTrap.getSettingsComponent());
                }
				activeOrbitTrap = (OrbitTrap) e.getItem();
                if (settingsPanel != null) {
                    settingsPanel.add(activeOrbitTrap.getSettingsComponent());
                }
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
    }

}
