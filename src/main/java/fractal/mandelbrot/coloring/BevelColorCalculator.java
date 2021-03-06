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
 * @author Lloyd
 */
public class BevelColorCalculator implements ColorCalculator {

    private FractalRenderer fractalRenderer;

    private double angleRed = Math.PI / 4d;
    private double angleGreen = Math.PI / 4d;
    private double angleBlue = Math.PI / 4d;

    private Complex lightAngleRed = new Complex(Math.cos(angleRed), Math.sin(angleRed));
    private Complex lightAngleGreen = new Complex(Math.cos(angleGreen), Math.sin(angleGreen));
    private Complex lightAngleBlue = new Complex(Math.cos(angleBlue), Math.sin(angleBlue));

    private double heightRed = 1.5;
    private double heightGreen = 1.5;
    private double heightBlue = 1.5;

    private Complex[][] imageNormals;

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {


        if (!fractalEngine.isBailoutReached(orbit)) {
            return Color.BLACK;
        }

        Complex dc = Complex.ONE; // derivitive of c
        Complex dz = new Complex(dc);

        for (int i = 1; i < orbit.size(); i++) {
            dz = Complex.TWO.mult(dz).mult(orbit.get(i - 1)).add(dc);
        }

        Complex normal = orbit.get(orbit.size() - 1).div(dz).normalize();
        imageNormals[x][y] = normal;

        return calcLights(normal);

    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {

        int lastOrbitPointIndex = orbitStartIndex + orbitLength - 1;
        Complex lastOrbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[lastOrbitPointIndex], rawGpuOrbitContainer.orbitsI[lastOrbitPointIndex]);

        if (!fractalEngine.isBailoutReachedByLastOrbitPoint(lastOrbitPoint)) {
            return Color.BLACK;
        }

        Complex dc = Complex.ONE; // derivitive of c
        Complex dz = new Complex(dc);
        Complex two = new Complex(2, 0);

        for (int i = 1; i < orbitLength; i++) {
            Complex orbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i - 1], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i - 1]);
            dz = two.mult(dz).mult(orbitPoint).add(dc);
        }

        Complex normal = lastOrbitPoint.div(dz).normalize();
        imageNormals[x][y] = normal;


        return calcLights(normal);

    }

    private Color calcLights(Complex normal) {
        double dotRed = calcDotRed(normal);
        double dotGreen = calcDotGreen(normal);
        double dotBlue = calcDotBlue(normal);

        int rgbRed = (int) Math.floor(dotRed * 255);
        int rgbGreen = (int) Math.floor(dotGreen * 255);
        int rgbBlue = (int) Math.floor(dotBlue * 255);

        return new Color(rgbRed, rgbGreen, rgbBlue);
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not supported. Full orbit needed"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getSettingsComponent() {
        JPanel retPanel = new JPanel(new GridLayout(0, 1));

        JPanel p = new JPanel(new GridLayout(0, 2));
        JSlider angleSliderRed = new JSlider(0, 359, 45);
        angleSliderRed.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                angleRed = angleSliderRed.getValue() * Math.PI / 180d;
                lightAngleRed = new Complex(Math.cos(angleRed), Math.sin(angleRed));
                redraw();
                fractalRenderer.updateGui();
            }

        });
        p.add(new JLabel("Red \u03F4"));
        p.add(angleSliderRed);
        retPanel.add(p);
        
        p = new JPanel(new GridLayout(0, 2));
        JSlider angleSliderGreen = new JSlider(0, 359, 45);
        angleSliderGreen.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                angleGreen = angleSliderGreen.getValue() * Math.PI / 180d;
                lightAngleGreen = new Complex(Math.cos(angleGreen), Math.sin(angleGreen));
                redraw();
                fractalRenderer.updateGui();
            }

        });
        p.add(new JLabel("Green \u03F4"));
        p.add(angleSliderGreen);
        retPanel.add(p);
        
        p = new JPanel(new GridLayout(0, 2));
        JSlider angleSliderBlue = new JSlider(0, 359, 45);
        angleSliderBlue.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                angleBlue = angleSliderBlue.getValue() * Math.PI / 180d;
                lightAngleBlue = new Complex(Math.cos(angleBlue), Math.sin(angleBlue));
                redraw();
                fractalRenderer.updateGui();
            }

        });
        p.add(new JLabel("Blue \u03F4"));
        p.add(angleSliderBlue);
        retPanel.add(p);

        return retPanel;
    }

    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                
                Complex normal = imageNormals[x][y];
                if (normal == null) {
                    fractalRenderer.getImage().setColor(x, y, Color.BLACK);
                    continue;
                }

                double dotRed = calcDotRed(normal);
                double dotGreen = calcDotGreen(normal);
                double dotBlue = calcDotBlue(normal);

                int rgbRed = (int) Math.floor(dotRed * 255);
                int rgbGreen = (int) Math.floor(dotGreen * 255);
                int rgbBlue = (int) Math.floor(dotBlue * 255);

                fractalRenderer.getImage().setColor(x, y, new Color(rgbRed, rgbGreen, rgbBlue));
                
            }
        }
        fractalRenderer.updateGui();
    }

    private double calcDotRed(Complex normal) {
        double dotRed = normal.r * lightAngleRed.r + normal.i * lightAngleRed.i + heightRed;
        dotRed = dotRed / (1 + heightRed);
        if (dotRed < 0) {
            dotRed = 0;
        }
        return dotRed;
    }

    private double calcDotGreen(Complex normal) {
        double dotGreen = normal.r * lightAngleGreen.r + normal.i * lightAngleGreen.i + heightGreen;
        dotGreen = dotGreen / (1 + heightGreen);
        if (dotGreen < 0) {
            dotGreen = 0;
        }
        return dotGreen;
    }

    private double calcDotBlue(Complex normal) {
        double dotBlue = normal.r * lightAngleBlue.r + normal.i * lightAngleBlue.i + heightBlue;
        dotBlue = dotBlue / (1 + heightBlue);
        if (dotBlue < 0) {
            dotBlue = 0;
        }
        return dotBlue;
    }

    @Override
    public String getName() {
        return "Bevel";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        imageNormals = new Complex[fractalRenderer.getImage().getBufferedImage().getWidth()][fractalRenderer.getImage().getBufferedImage().getHeight()];
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
