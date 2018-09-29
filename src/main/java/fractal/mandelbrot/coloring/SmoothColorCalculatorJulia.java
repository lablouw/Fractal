/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.*;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author lloyd
 */
public class SmoothColorCalculatorJulia implements ColorCalculator {

    private final FractalRenderer fractalRenderer;
    private JPanel settingsPanel;
    private float spectrum = 1;
    private float spectrumComp = 1;
    private Complex[][] orbitEndPoints;
    private int[][] orbitLengths;
    private final JSlider compressionSlider = new JSlider(1, 2000);
    private final ColorPalette colorPalette = new ColorPalette();

    public SmoothColorCalculatorJulia(final FractalRenderer renderer) {
        this.fractalRenderer = renderer;
        initSettingsPanel();
    }

    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, recalcColor(orbitEndPoints[x][y], orbitLengths[x][y], fractalRenderer.getFractalEngine()));
            }
        }
        fractalRenderer.updateGui();
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        orbitEndPoints[x][y] = orbit.get(orbit.size() - 1);
        orbitLengths[x][y] = orbit.size();
        if (fractalEngine.isBailoutReached(orbit)) {
            float nSmooth = (float) (orbit.size() + 1 - Math.log(Math.log(orbit.get(orbit.size() - 1).modulus())) / Math.log(2));
            float a =((nSmooth / ((float) fractalRenderer.getFractalEngine().getMaxIter()) - spectrum) * spectrumComp) % 1;
            return colorPalette.getColor(a);
        } else {
            return Color.BLACK;
        }
    }
    
    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        orbitEndPoints[x][y] = lastOrbitPoint;
        orbitLengths[x][y] = orbitLength;
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            float nSmooth = (float) (orbitLength + 1 - Math.log(Math.log(lastOrbitPoint.modulus())) / Math.log(2));
            float a =((nSmooth / ((float) fractalRenderer.getFractalEngine().getMaxIter()) - spectrum) * spectrumComp) % 1;
            return colorPalette.getColor(a);
        } else {
            return Color.BLACK;
        }
    }

    private Color recalcColor(Complex lastOrbitPoint, int orbitSize, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            float nSmooth = (float) (orbitSize + 1 - Math.log(Math.log(lastOrbitPoint.modulus())) / Math.log(2));
            float a =((nSmooth / ((float) fractalRenderer.getFractalEngine().getMaxIter()) - spectrum) * spectrumComp) % 1;
            return colorPalette.getColor(a);
        } else {
            return Color.BLACK;
        }
    }
    
    @Override
    public String getName() {
        return "Smooth";
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
        compressionSlider.setValue(1);
        compressionSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                spectrumComp = (float) ((float) (double) compressionSlider.getValue() / 500d);
                redraw();
            }
        });
        settingsPanel.add(compressionSlider);
    }

    @Override
    public JComponent getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
        orbitEndPoints = new Complex[imageWidth][imageHeight];
        orbitLengths = new int[imageWidth][imageHeight];
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
        compressionSlider.setMaximum(fractalRenderer.getFractalEngine().getMaxIter());
    }

}
