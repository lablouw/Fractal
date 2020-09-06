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

/**
 *
 * @author lloyd
 */
public class SmoothColorCalculatorMandelbrot implements ColorCalculator {

    private final FractalRenderer fractalRenderer;
    private JPanel settingsPanel;
    private float spectrumPhase = 1;
    private float spectrumComp = 1;
    private float gamma = 1;
    private Complex[][] orbitEndPoints;
    private int[][] orbitLengths;
    
    private final JSlider compressionSlider = new JSlider(1, 2000);
    private final JSlider phaseSlider = new JSlider(0, 255);
    private final JSlider gammaSlider = new JSlider(-100, 100);
    private final JLabel gammaLabel = new JLabel("Spectrum Gamma = 1");
    private final ColorPalette colorPalette = new ColorPalette();

    public SmoothColorCalculatorMandelbrot(final FractalRenderer renderer) {
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
            float a = ((nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter()) * spectrumComp + spectrumPhase) % 1;
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
            float a = ((nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter()) * spectrumComp + spectrumPhase) % 1;
            return colorPalette.getColor(a);
        } else {
            return Color.BLACK;
        }
    }

    private Color recalcColor(Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        if (fractalEngine.isBailoutReached(Collections.singletonList(lastOrbitPoint))) {
            float nSmooth = (float) (orbitLength + 1 - Math.log(Math.log(lastOrbitPoint.modulus())) / Math.log(2));
            float a = ((nSmooth / (float) fractalRenderer.getFractalEngine().getMaxIter()) * spectrumComp + spectrumPhase) % 1;
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
        settingsPanel = new JPanel(new GridLayout(0, 1));

        settingsPanel.add(colorPalette);

        settingsPanel.add(new JLabel("Spectrum phase"));
        phaseSlider.setValue(0);
        phaseSlider.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            
            @Override
            public void mouseReleased(MouseEvent e) {
                spectrumPhase = (float) phaseSlider.getValue() / 255f;
                redraw();
            }
        });

        settingsPanel.add(phaseSlider);

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
                redraw();
            }
        });
        settingsPanel.add(compressionSlider);
        
        settingsPanel.add(gammaLabel);
        gammaSlider.setValue(1);
        gammaSlider.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                if (gammaSlider.getValue() >= 1) {
                    gamma = gammaSlider.getValue()/10f;
                } else if (gammaSlider.getValue() < 1) {
                    gamma = 1f/((-gammaSlider.getValue()+2)/10f);
                }
                gammaLabel.setText("Spectrum Gamma = "+gamma);
                
                redraw();
            }
        });
        settingsPanel.add(gammaSlider);
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
