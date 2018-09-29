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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

    Complex[][] imageNormals;

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {

        if (!fractalEngine.isBailoutReached(orbit)) {
            return Color.BLACK;
        }

        Complex dc = Complex.ONE; // derivitive of c
        Complex dz = new Complex(dc);
        Complex two = new Complex(2, 0);

        for (int i = 1; i < orbit.size(); i++) {
            dz = two.mult(dz).mult(orbit.get(i - 1)).add(dc);
        }

        Complex normal = orbit.get(orbit.size() - 1).div(dz).normalize();
        imageNormals[x][y] = normal;

        double dotRed = normal.r * lightAngleRed.r + normal.i * lightAngleRed.i + heightRed;
        dotRed = dotRed / (1 + heightRed);
        if (dotRed < 0) {
            dotRed = 0;
        }
        int rgbRed = (int) Math.floor(dotRed * 255);

        double dotGreen = normal.r * lightAngleGreen.r + normal.i * lightAngleGreen.i + heightGreen;
        dotGreen = dotGreen / (1 + heightGreen);
        if (dotGreen < 0) {
            dotGreen = 0;
        }
        int rgbGreen = (int) Math.floor(dotGreen * 255);

        double dotBlue = normal.r * lightAngleBlue.r + normal.i * lightAngleBlue.i + heightBlue;
        dotBlue = dotBlue / (1 + heightBlue);
        if (dotBlue < 0) {
            dotBlue = 0;
        }
        int rgbBlue = (int) Math.floor(dotBlue * 255);

        return new Color(rgbRed, rgbGreen, rgbBlue);

//        return new Color(Color.HSBtoRGB((float)(dot), (float)(1-dot), (float)dot));
//        return new Color(Color.HSBtoRGB((float)(t), (float)t, (float)t));
//        return new Color(Color.HSBtoRGB((float)(aveTheta/Math.PI), 1, 1));
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

                double dotRed = normal.r * lightAngleRed.r + normal.i * lightAngleRed.i + heightRed;
                dotRed = dotRed / (1 + heightRed);
                if (dotRed < 0) {
                    dotRed = 0;
                }
                int rgbRed = (int) Math.floor(dotRed * 255);

                double dotGreen = normal.r * lightAngleGreen.r + normal.i * lightAngleGreen.i + heightGreen;
                dotGreen = dotGreen / (1 + heightGreen);
                if (dotGreen < 0) {
                    dotGreen = 0;
                }
                int rgbGreen = (int) Math.floor(dotGreen * 255);

                double dotBlue = normal.r * lightAngleBlue.r + normal.i * lightAngleBlue.i + heightBlue;
                dotBlue = dotBlue / (1 + heightBlue);
                if (dotBlue < 0) {
                    dotBlue = 0;
                }
                int rgbBlue = (int) Math.floor(dotBlue * 255);

                fractalRenderer.getImage().setColor(x, y, new Color(rgbRed, rgbGreen, rgbBlue));
                
            }
        }
        fractalRenderer.updateGui();
    }

    @Override
    public String getName() {
        return "Bevel";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        imageNormals = new Complex[fractalRenderer.getImage().getBufferedImage().getWidth()][fractalRenderer.getImage().getBufferedImage().getHeight()];
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
    }

}
