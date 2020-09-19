/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;

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

    private RawGpuOrbitContainer rawGpuOrbitContainer;

    public JuliaCalculatorGPU(JuliaRenderer mandelbrotRenderer) {
        this.juliaRenderer = mandelbrotRenderer;
    }

    public void initForRender() {
        stopped = false;

        imageWidth = juliaRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = juliaRenderer.getImage().getBufferedImage().getHeight();
        juliaEngine = juliaRenderer.getFractalEngine();

        juliaEngine.initGPUKernelForRender(imageWidth, imageHeight, juliaRenderer.getMapper());
    }

    @Override
    public void run() {
        if (juliaRenderer.getAA() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += juliaEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += juliaEngine.getSubImageHeight()) {
                    juliaEngine.doRunGPU(xOffset, yOffset, juliaRenderer.getMapper());
                    if (stopped) {
                        return;
                    }
                    doPostProcess();
                }
            }
        }

    }

    private void doPostProcess() {
        if (juliaEngine.isUseGPUFull()) {
            ExecutorService es = Executors.newFixedThreadPool(numCores);
            rawGpuOrbitContainer = juliaEngine.getRawGpuOrbitContainer();
            List<Future> futures = new ArrayList<>();
            for (int coreIndex = 0; coreIndex < numCores; coreIndex++) {
                RawGpuFullOrbitProcessor rawGpuFullOrbitProcessor = new RawGpuFullOrbitProcessor(coreIndex);
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
                    juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, juliaEngine.getLastOrbitPoint(x, y), juliaEngine.getOrbitLength(x, y));
                }
            }
        }

    }

    public void stop() {
        stopped = true;
    }

    private class RawGpuFullOrbitProcessor implements Runnable {

        private final int coreIndex;

        RawGpuFullOrbitProcessor(int coreIndex) {
            this.coreIndex = coreIndex;
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

                        juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, rawGpuOrbitContainer, orbitStartIndex, orbitLength);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }
}
