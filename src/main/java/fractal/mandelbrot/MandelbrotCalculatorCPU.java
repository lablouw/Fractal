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
public class MandelbrotCalculatorCPU implements Runnable {

    private final int numCores = Runtime.getRuntime().availableProcessors();
    private final int coreIndex;
    private boolean stopped = false;
    private final MandelbrotRenderer mandelbrotRenderer;
    private final int subSamples;
    private final int imageWidth;
    private final int imageHeight;

    public MandelbrotCalculatorCPU(int coreIndex, MandelbrotRenderer mandelbrotRenderer) {
        this.coreIndex = coreIndex;
        this.mandelbrotRenderer = mandelbrotRenderer;
        this.subSamples = mandelbrotRenderer.getSubSamples();
        this.imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        this.imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
    }
    
    @Override
    public void run() {
        if (subSamples == Antialiasable.NONE) {
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                List<List<Complex>> orbits = new ArrayList<>(imageHeight);
                List<Point> points = new ArrayList<>(imageWidth);
                for (int y = 0; y < imageHeight; y++) {
                    Complex c = mandelbrotRenderer.getImagePlaneMapper().mapToComplex(x, y);
                    List<Complex> orbit = mandelbrotRenderer.getFractalEngine().calcOrbit(c);
                    points.add(new Point(x, y));
                    orbits.add(orbit);
                }
                if (stopped) {
                    return;
                }
                mandelbrotRenderer.enginePerformedCalculation(points, orbits);
            }
        } else {
            for (int x = coreIndex; x < imageWidth; x += numCores) {
                for (int y = 0; y < imageHeight; y++) {
                    int colorR = 0;
                    int colorG = 0;
                    int colorB = 0;
                    List<Complex> repOrbit = null;
                    for (double xSubSamplePos = 0; xSubSamplePos < subSamples; xSubSamplePos++) {
                        for (double ySubSamplePos = 0; ySubSamplePos < subSamples; ySubSamplePos++) {
                            Complex c = mandelbrotRenderer.getImagePlaneMapper().mapToComplex(x+xSubSamplePos/subSamples, y+ySubSamplePos/subSamples);
                            List<Complex> orbit = mandelbrotRenderer.getFractalEngine().calcOrbit(c);
                            repOrbit = orbit;
                            Color color = mandelbrotRenderer.getActiveColorCalculator().calcColor(x, y, orbit, mandelbrotRenderer.getFractalEngine());
                            colorR += color.getRed();
                            colorG += color.getGreen();
                            colorB += color.getBlue();
                        }
                    }
                    if (stopped) {
                        return;
                    }
                    colorR = colorR / (subSamples * subSamples);
                    colorG = colorG / (subSamples * subSamples);
                    colorB = colorB / (subSamples * subSamples);
                    mandelbrotRenderer.enginePerformedCalculation(x, y, repOrbit, new Color(colorR, colorG, colorB));
                }
            }
        }
    }

    public void stop() {
        stopped = true;
    }

}
