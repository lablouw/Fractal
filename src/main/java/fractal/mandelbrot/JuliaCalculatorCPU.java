/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;
import fractal.common.Complex;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CP316928
 */
public class JuliaCalculatorCPU implements Runnable {

    private final int numCores = Runtime.getRuntime().availableProcessors();
    private final int coreIndex;
    private boolean stopped = false;
    private final JuliaRenderer juliaRenderer;
    private final int subSamples;
    private final int imageWidth;
    private final int imageHeight;

    public JuliaCalculatorCPU(int coreIndex, JuliaRenderer juliaRenderer) {
        this.coreIndex = coreIndex;
        this.juliaRenderer = juliaRenderer;
        this.subSamples = juliaRenderer.getSubSamples();
        this.imageWidth = juliaRenderer.getImage().getBufferedImage().getWidth();
        this.imageHeight = juliaRenderer.getImage().getBufferedImage().getHeight();
    }

    @Override
    public void run() {
        if (subSamples == Antialiasable.NONE) {
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                List<List<Complex>> orbits = new ArrayList<>(imageHeight);
                List<Point> points = new ArrayList<>(imageWidth);
                for (int y = 0; y < imageHeight; y++) {
                    Complex z0 = juliaRenderer.getMapper().mapToComplex(x, y);
                    List<Complex> orbit = juliaRenderer.getFractalEngine().calcOrbit(z0);
                    orbits.add(orbit);
                    points.add(new Point(x, y));
                }
                if (stopped) {
                    return;
                }
                juliaRenderer.enginePerformedCalculation(points, orbits);
            }
        } else {
            double aaXStep = xStep / (double) subSamples;
            double aaYStep = yStep / (double) subSamples;
            int xSteps, ySteps;
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                for (int y = 0; y < imageHeight; y++) {
                    Complex c = juliaRenderer.getMapper().mapToComplex(x, y);
                    int colorR = 0;
                    int colorG = 0;
                    int colorB = 0;
                    List<Complex> repOrbit = null;
                    xSteps = 0;
                    for (double r = c.r - aaXStep * (subSamples - 1) / 2; xSteps < subSamples; r += aaXStep) {
                        ySteps = 0;
                        for (double i = c.i - aaYStep * (subSamples - 1) / 2; ySteps < subSamples; i += aaYStep) {
                            Complex aaStep = new Complex(r, i);
                            List<Complex> orbit = juliaRenderer.getFractalEngine().calcOrbit(aaStep);
                            if (aaStep.equals(c)) {
                                repOrbit = orbit;
                            }
                            Color color = juliaRenderer.getActiveColorCalculator().calcColor(x, y, orbit, juliaRenderer.getFractalEngine());
                            colorR += color.getRed();
                            colorG += color.getGreen();
                            colorB += color.getBlue();
                            ySteps++;
                        }
                        xSteps++;
                    }
                    if (stopped) {
                        return;
                    }
                    if (repOrbit == null) {
                        repOrbit = juliaRenderer.getFractalEngine().calcOrbit(c);
                    }
                    colorR = colorR / (subSamples * subSamples);
                    colorG = colorG / (subSamples * subSamples);
                    colorB = colorB / (subSamples * subSamples);
                    juliaRenderer.enginePerformedCalculation(x, y, repOrbit, new Color(colorR, colorG, colorB));
                }
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
