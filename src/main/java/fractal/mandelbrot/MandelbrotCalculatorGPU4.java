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
public class MandelbrotCalculatorGPU4 implements Runnable {

    private final int numCores = Runtime.getRuntime().availableProcessors();

    private final MandelbrotRenderer mandelbrotRenderer;
    private MandelbrotEngine mandelbrotEngine;
    private int imageWidth;
    private int imageHeight;

    private int xOffset = 0;
    private int yOffset = 0;

    private boolean stopped = false;
    List<Color>[][] preAAColors;

    private RawGpuOrbitContainer rawGpuOrbitContainer;

    public MandelbrotCalculatorGPU4(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
    }

    public void initForRender() {
        stopped = false;

        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = mandelbrotRenderer.getFractalEngine();

        mandelbrotEngine.initGPUKernelForRender(imageWidth, imageHeight);
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getSubSamples() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += mandelbrotEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += mandelbrotEngine.getSubImageHeight()) {
                    mandelbrotEngine.doRunGPU(xOffset, yOffset, mandelbrotRenderer.getMapper(), 0, 0);
                    if (stopped) {
                        return;
                    }
                    doPostProcess(xOffset, yOffset, false);
                }
            }
        } else {
            int subSamples = mandelbrotRenderer.getSubSamples();
            double rStep = Math.abs(mandelbrotRenderer.getMapper().getRStep());
            double iStep = Math.abs(mandelbrotRenderer.getMapper().getIStep());
            double aaRStep = rStep / (double) subSamples;
            double aaIStep = iStep / (double) subSamples;

            double[] aaROffsets = new double[subSamples];
            double[] aaIOffsets = new double[subSamples];
            for (int i = 0; i < subSamples; i++) {
                aaROffsets[i] = i*aaRStep - rStep/2;
                aaIOffsets[i] = i*aaIStep - iStep/2;
            }

            for (xOffset = 0; xOffset < imageWidth; xOffset += mandelbrotEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += mandelbrotEngine.getSubImageHeight()) {
                    int a = 1;
                    preAAColors = new List[mandelbrotEngine.getSubImageWidth()][mandelbrotEngine.getSubImageHeight()];
                    for (double aaROffset : aaROffsets) {
                        for (double aaIOffset : aaIOffsets) {
                            System.out.println("Round "+a+" of "+aaROffsets.length*aaIOffsets.length);
                            a++;

                            mandelbrotEngine.doRunGPU(xOffset, yOffset, mandelbrotRenderer.getMapper(), aaROffset, aaIOffset);
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
        if (mandelbrotEngine.isUseGPUFull()) {
            ExecutorService es = Executors.newFixedThreadPool(numCores);
            rawGpuOrbitContainer = mandelbrotEngine.getRawGpuOrbitContainer();
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

        } else if (mandelbrotEngine.isUseGPUFast()) {
            //This seems to have too much overhead and is a bit slower
//            ExecutorService es = Executors.newFixedThreadPool(numCores);
//            List<Future> futures = new ArrayList<>();
//            for (int coreIndex = 0; coreIndex < numCores; coreIndex++) {
//                RawGpuFastOrbitProcessor rawGpuFastOrbitProcessor = new RawGpuFastOrbitProcessor(coreIndex);
//                futures.add(es.submit(rawGpuFastOrbitProcessor));
//            }
//            for (Future f : futures) {
//                try {
//                    f.get();
//                } catch (InterruptedException | ExecutionException ex) {
//                    Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            es.shutdown();
            int subImageWidth = mandelbrotEngine.getSubImageWidth();
            int subImageHeight = mandelbrotEngine.getSubImageHeight();

            for (int x = 0; x < subImageWidth; x++) {
                if (x + xOffset >= imageWidth) break;
                for (int y = 0; y < subImageHeight; y++) {
                    if (y + yOffset >= imageHeight) break;
                    if (!antialias) {//No AA
                        mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, mandelbrotEngine.getLastOrbitPoint(x, y),
                                mandelbrotEngine.getOrbitLength(x, y));
                    } else {
                        Color c = mandelbrotRenderer.getActiveColorCalculator().calcColor(x + xOffset, y + yOffset, mandelbrotEngine.getLastOrbitPoint(x, y), mandelbrotEngine.getOrbitLength(x, y), mandelbrotEngine);
                        if (preAAColors[x][y] == null) {
                            preAAColors[x][y] = new ArrayList<>(mandelbrotRenderer.getSubSamples()*mandelbrotRenderer.getSubSamples());
                        }
                        preAAColors[x][y].add(c);
                    }
                }
            }
        }

        if (antialias) {//we have AA
            for (int x = xOffset; x < Math.min(xOffset + mandelbrotEngine.getSubImageWidth(), imageWidth); x++) {
                for (int y = yOffset; y < Math.min(yOffset + mandelbrotEngine.getSubImageHeight(),imageHeight); y++) {
                    Color c = averageColor(preAAColors[x - xOffset][y - yOffset]);
                    mandelbrotRenderer.enginePerformedCalculation(x, y, null, c);
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
                int subImageWidth = mandelbrotEngine.getSubImageWidth();
                int subImageHeight = mandelbrotEngine.getSubImageHeight();
                int maxIter = mandelbrotEngine.getMaxIter();

                for (int x = coreIndex; x < subImageWidth; x += numCores) {
                    if (x + xOffset >= imageWidth) break;
                    for (int y = 0; y < subImageHeight; y++) {
                        if (y + yOffset >= imageHeight) break;

                        int orbitStartIndex = x * maxIter + y * maxIter * subImageWidth;
                        int orbitLength = rawGpuOrbitContainer.orbitLengths[x][y];

                        if (!antialias) {//No AA
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, rawGpuOrbitContainer, orbitStartIndex, orbitLength);
                        } else {//Save color for later averaging
                            Color c = mandelbrotRenderer.getActiveColorCalculator().calcColor(x + xOffset, y + yOffset, rawGpuOrbitContainer, orbitStartIndex, orbitLength, mandelbrotEngine);
                            if (preAAColors[x][y] == null) {
                                preAAColors[x][y] = new ArrayList<>(mandelbrotRenderer.getSubSamples()*mandelbrotRenderer.getSubSamples());
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

    //This seems to have too much overhead and is a bit slower
//    private class RawGpuFastOrbitProcessor implements Runnable {
//
//        private final int coreIndex;
//
//        public RawGpuFastOrbitProcessor(int coreIndex) {
//            this.coreIndex = coreIndex;
//        }
//
//        @Override
//        public void run() {
//            try {
//                int subImageWidth = mandelbrotEngine.getSubImageWidth();
//                int subImageHeight = mandelbrotEngine.getSubImageHeight();
//
//                for (int x = coreIndex; x < subImageWidth; x += numCores) {
//                    if (x + xOffset >= imageWidth) break;
//                    for (int y = 0; y < subImageHeight; y++) {
//                        if (y + yOffset >= imageHeight) break;
//                        mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, mandelbrotEngine.getLastOrbitPoint(x, y), mandelbrotEngine.getOrbitLength(x, y));
//                    }
//                }
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                throw ex;
//            }
//        }
//    }

}
