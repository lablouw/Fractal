/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CP316928
 */
public class JuliaCalculatorGPU implements Runnable {

    private final int numCores = Runtime.getRuntime().availableProcessors();

    private final JuliaRenderer juliaRenderer;
    private JuliaEngine juliaEngine;
    private int imageWidth;
    private int imageHeight;

    private int xOffset = 0;
    private int yOffset = 0;

    private boolean stopped = false;
    private List<Color>[][] preAAColors;

    private RawGpuOrbitContainer rawGpuOrbitContainer;

    public JuliaCalculatorGPU(JuliaRenderer mandelbrotRenderer) {
        this.juliaRenderer = mandelbrotRenderer;
    }

    public void initForRender() {
        stopped = false;

        imageWidth = juliaRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = juliaRenderer.getImage().getBufferedImage().getHeight();
        juliaEngine = juliaRenderer.getFractalEngine();

        juliaEngine.initGPUKernelForRender(imageWidth, imageHeight);
    }

    @Override
    public void run() {
        if (juliaRenderer.getSubSamples() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += juliaEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += juliaEngine.getSubImageHeight()) {
                    juliaEngine.doRunGPU(xOffset, yOffset, juliaRenderer.getMapper(), 0, 0, Antialiasable.NONE);
                    if (stopped) {
                        return;
                    }
                    doPostProcess(xOffset, yOffset, false);
                }
            }
        } else {
            int subSamples = juliaRenderer.getSubSamples();
            for (xOffset = 0; xOffset < imageWidth; xOffset += juliaEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += juliaEngine.getSubImageHeight()) {
                    preAAColors = new List[juliaEngine.getSubImageWidth()][juliaEngine.getSubImageHeight()];
                    for (double xSubSamplePos = 0; xSubSamplePos < subSamples; xSubSamplePos++) {
                        for (double ySubSamplePos = 0; ySubSamplePos < subSamples; ySubSamplePos++) {
                            juliaEngine.doRunGPU(xOffset, yOffset, juliaRenderer.getMapper(), xSubSamplePos, ySubSamplePos, subSamples);
                            if (stopped) {
                                return;
                            }
                            doPostProcess(xOffset, yOffset, true);

                        }
                    }
                }
            }
        }

    }

    private void doPostProcess(int xOffset, int yOffset, boolean antialias) {
        if (juliaEngine.isUseGPUFull()) {
            ExecutorService es = Executors.newFixedThreadPool(numCores);
            rawGpuOrbitContainer = juliaEngine.getRawGpuOrbitContainer();
            List<Future> futures = new ArrayList<>();
            for (int coreIndex = 0; coreIndex < numCores; coreIndex++) {
                RawGpuFullOrbitProcessor rawGpuFullOrbitProcessor = new RawGpuFullOrbitProcessor(coreIndex, antialias);
                futures.add(es.submit(rawGpuFullOrbitProcessor));
            }
            for (Future f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            es.shutdown();

        } else if (juliaEngine.isUseGPUFast()) {
            int subImageWidth = juliaEngine.getSubImageWidth();
            int subImageHeight = juliaEngine.getSubImageHeight();

            for (int x = 0; x < subImageWidth; x++) {
                if (x + xOffset >= imageWidth) break;
                for (int y = 0; y < subImageHeight; y++) {
                    if (y + yOffset >= imageHeight) break;
                    if (!antialias) {//No AA
                        juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, juliaEngine.getLastOrbitPoint(x, y),
                                juliaEngine.getOrbitLength(x, y));
                    } else {
                        Color c = juliaRenderer.getActiveColorCalculator().calcColor(x + xOffset, y + yOffset, juliaEngine.getLastOrbitPoint(x, y), juliaEngine.getOrbitLength(x, y), juliaEngine);
                        if (preAAColors[x][y] == null) {
                            preAAColors[x][y] = new ArrayList<>(juliaRenderer.getSubSamples()*juliaRenderer.getSubSamples());
                        }
                        preAAColors[x][y].add(c);
                    }
                }
            }
        }

        if (antialias) {//we have AA
            for (int x = xOffset; x < Math.min(xOffset + juliaEngine.getSubImageWidth(), imageWidth); x++) {
                for (int y = yOffset; y < Math.min(yOffset + juliaEngine.getSubImageHeight(),imageHeight); y++) {
                    Color c = averageColor(preAAColors[x - xOffset][y - yOffset]);
                    juliaRenderer.enginePerformedCalculation(x, y, null, c);
                }
            }
        }
    }

    private Color averageColor(List<Color> colors) {
        int r=0,g=0,b =0;
        for (Color c : colors) {
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        return new Color(r/colors.size(), g/colors.size(), b/colors.size());
    }

    public void stop() {
        stopped = true;
    }

    private class RawGpuFullOrbitProcessor implements Runnable {

        private final int coreIndex;
        private final boolean antialias;

        RawGpuFullOrbitProcessor(int coreIndex, boolean antialias) {
            this.coreIndex = coreIndex;
            this.antialias = antialias;
        }

        @Override
        public void run() {
            try {
                int subImageWidth = juliaEngine.getSubImageWidth();
                int subImageHeight = juliaEngine.getSubImageHeight();
                int maxIter = juliaEngine.getMaxIter();

                for (int x = coreIndex; x < subImageWidth; x += numCores) {
                    if (x + xOffset >= imageWidth) break;
                    for (int y = 0; y < subImageHeight; y++) {
                        if (y + yOffset >= imageHeight) break;

                        int orbitStartIndex = x * maxIter + y * maxIter * subImageWidth;
                        int orbitLength = rawGpuOrbitContainer.orbitLengths[x][y];

                        if (!antialias) {//No AA
                            juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, rawGpuOrbitContainer, orbitStartIndex, orbitLength);
                        } else {//Save color for later averaging
                            Color c = juliaRenderer.getActiveColorCalculator().calcColor(x + xOffset, y + yOffset, rawGpuOrbitContainer, orbitStartIndex, orbitLength, juliaEngine);
                            if (preAAColors[x][y] == null) {
                                preAAColors[x][y] = new ArrayList<>(juliaRenderer.getSubSamples()*juliaRenderer.getSubSamples());
                            }
                            preAAColors[x][y].add(c);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }
}
