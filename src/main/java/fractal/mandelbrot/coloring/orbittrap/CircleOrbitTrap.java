/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrap extends OrbitTrap {

    private JPanel settingsPanel;
    private final JSlider compressionSlider = new JSlider(1, 100);
    private final ColorPalette colorPalette = new ColorPalette();
    private float spectrumComp = 0.05f;

    private double [][] minDists;
    private Complex center;
    private Complex pointOnCircle;
    private double radius;

    public CircleOrbitTrap() {
        setDefiningPoints(new Complex(0,0), new Complex(0.25, 0));
        initSettingsPanel();
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public String getName() {
        return "Circle";
    }

    @Override
    public void init(FractalRenderer fractalRenderer) {
        int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
        int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
        minDists = new double[imageWidth][imageHeight];
    }

    @Override
    public void setDefiningPoints(Complex c1, Complex c2) {
        this.center = c1;
        this.pointOnCircle = c2;
        this.radius = Math.sqrt(Math.pow(c2.r-c1.r, 2) + Math.pow(c2.i-c1.i, 2));
    }

    @Override
    public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
        Point c = fractalRenderer.getMapper().mapToImage(center);
        Point poc = fractalRenderer.getMapper().mapToImage(pointOnCircle);
        int r = (int) Math.sqrt((poc.x-c.x) * (poc.x-c.x) + (poc.y-c.y) * (poc.y-c.y));

        BufferedImage im = ImageUtils.deepCopy(baseImage);
        im.getGraphics().drawOval(c.x-r, c.y-r, r*2, r*2);
        return im;
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        double minDist = Double.MAX_VALUE;
        for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            double d = distanceFrom(orbit.get(i));
            if (d < minDist) {
                minDist = d;
            }
        }

        minDists[x][y] = minDist;
        return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDist) * spectrumComp);
    }

    @Override
    public Color reCalcColor(int x, int y) {
        return minDists[x][y] == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDists[x][y])*spectrumComp);
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

    private double distanceFrom(Complex c) {
        return Math.abs(c.sub(center).modulus() - radius);
    }

    private void redraw() {
        for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
            for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
                fractalRenderer.getImage().setColor(x, y, reCalcColor(x, y));
            }
        }
        fractalRenderer.updateGui();
    }

}
