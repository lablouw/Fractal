/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.newton.coloring;

import fractal.common.ColorCalculator;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.newton.NewtonRenderer;
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
 * @author lloyd
 */
public class NewtonIterationsColorCalculator implements ColorCalculator {

    private final FractalRenderer fractalRenderer;
    private JPanel settingsPanel;

    private float spectrum = 1;
    private float spectrumComp = 1;

    public NewtonIterationsColorCalculator(final FractalRenderer renderer) {
        this.fractalRenderer = renderer;
        initSettingsPanel();
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return new Color(Color.HSBtoRGB((((float) orbit.size()) / ((float) fractalEngine.getMaxIter()) - spectrum) * spectrumComp, 1, 1));
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        return new Color(Color.HSBtoRGB((((float) orbitLength) / ((float) fractalEngine.getMaxIter()) - spectrum) * spectrumComp, 1, 1));
    }

    private Color recalcColor(int orbitSize, FractalEngine fractalEngine) {
        return new Color(Color.HSBtoRGB((((float) orbitSize) / ((float) fractalEngine.getMaxIter()) - spectrum) * spectrumComp, 1, 1));
    }

    private void initSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(0, 1));

        settingsPanel.add(new JLabel("Spectrum"));
        final JSlider spectrumSlider = new JSlider(0, 255);
        spectrumSlider.setValue(0);
        spectrumSlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                spectrum = (float) spectrumSlider.getValue() / 255f;
                redraw();
            }
        });

        settingsPanel.add(spectrumSlider);

        settingsPanel.add(new JLabel("Spectrum Compression"));
        final JSlider widthSlider = new JSlider(1, 2000);
        widthSlider.setValue(1);
        widthSlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                spectrumComp = (float) ((float) (double) widthSlider.getValue() / 500d);
                redraw();
            }
        });
        settingsPanel.add(widthSlider);
    }

    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, recalcColor(((NewtonRenderer) fractalRenderer).getOrbitSize(x, y), fractalRenderer.getFractalEngine()));
            }
        }
        fractalRenderer.updateGui();
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getName() {
        return "Iterations";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
