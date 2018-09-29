/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;
import fractal.common.Complex;
import java.awt.Color;
import java.awt.Point;
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
    private final int aa;
    private final int imageWidth;
    private final int imageHeight;

    public JuliaCalculatorCPU(int coreIndex, JuliaRenderer juliaRenderer) {
        this.coreIndex = coreIndex;
        this.juliaRenderer = juliaRenderer;
        this.aa = juliaRenderer.getAA();
        this.imageWidth = juliaRenderer.getImage().getBufferedImage().getWidth();
        this.imageHeight = juliaRenderer.getImage().getBufferedImage().getHeight();
    }

    @Override
    public void run() {
        if (aa == Antialiasable.NONE) {
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                List<List<Complex>> orbits = new ArrayList<>();
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
            double xStep = Math.abs(juliaRenderer.getMapper().getXStep());
            double yStep = Math.abs(juliaRenderer.getMapper().getYStep());
            double aaXStep = xStep / (double) aa;
            double aaYStep = yStep / (double) aa;
            int xSteps, ySteps;
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                for (int y = 0; y < imageHeight; y++) {
                    Complex c = juliaRenderer.getMapper().mapToComplex(x, y);
                    int colorR = 0;
                    int colorG = 0;
                    int colorB = 0;
                    List<Complex> repOrbit = null;
                    xSteps = 0;
                    for (double r = c.r - aaXStep * (aa - 1) / 2; xSteps < aa; r += aaXStep) {
                        ySteps = 0;
                        for (double i = c.i - aaYStep * (aa - 1) / 2; ySteps < aa; i += aaYStep) {
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
                    colorR = colorR / (aa * aa);
                    colorG = colorG / (aa * aa);
                    colorB = colorB / (aa * aa);
                    juliaRenderer.enginePerformedCalculation(x, y, repOrbit, new Color(colorR, colorG, colorB));
                }
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
