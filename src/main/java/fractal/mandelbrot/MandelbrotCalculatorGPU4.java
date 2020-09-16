/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;
import fractal.common.Complex;

import java.util.List;

/**
 *
 * @author CP316928
 */
public class MandelbrotCalculatorGPU4 implements Runnable {

    private final MandelbrotRenderer mandelbrotRenderer;
    private MandelbrotEngine mandelbrotEngine;
    private int imageWidth;
    private int imageHeight;

    private int xOffset = 0;
    private int yOffset = 0;

    private boolean stopped = false;

    public MandelbrotCalculatorGPU4(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
    }

    public void initForRender() {
        stopped = false;
        postProcessTime = 0;
        gpuTime = 0;
        getOrbitTime = 0;

        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = mandelbrotRenderer.getFractalEngine();

        mandelbrotEngine.initGPUKernelForRender(imageWidth, imageHeight);
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getAA() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += mandelbrotEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += mandelbrotEngine.getSubImageHeight()) {
                    long t = System.currentTimeMillis();
                    mandelbrotEngine.doRunGPU(xOffset, yOffset, mandelbrotRenderer.getMapper());
                    gpuTime += System.currentTimeMillis() - t;
                    if (stopped) {
                        return;
                    }
                    doPostProcess();
                }
            }
        }
        System.out.println("GPU TIME "+gpuTime);
        System.out.println("POST PROCESSING TIME "+postProcessTime);
        System.out.println("GET ORBIT TIME "+getOrbitTime);
    }

    long postProcessTime = 0;
    long gpuTime = 0;
    long getOrbitTime = 0;
    private void doPostProcess() {
        //TODO: #GPU_OPTIZATION: let getGPUOrbit return 2x double[] and fire this methods code off in it's own thread
        //We may need to throttle the GPU runs if the CPU can't keep up and RAM starts to run low
        long t = System.currentTimeMillis();
        for (int x = 0; x < mandelbrotEngine.getSubImageWidth(); x++) {
            if (x + xOffset < imageWidth) {
                for (int y = 0; y < mandelbrotEngine.getSubImageHeight(); y++) {
                    if (y + yOffset < imageHeight) {
                        if (mandelbrotEngine.isUseGPUFull()) {
                            long t2 = System.currentTimeMillis();
                            List<Complex> a = mandelbrotEngine.getGPUOrbit(x, y);//This is expensive
                            getOrbitTime += System.currentTimeMillis() - t2;
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, a);
                        } else if (mandelbrotEngine.isUseGPUFast()) {
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, mandelbrotEngine.getLastOrbitPoint(x, y), mandelbrotEngine.getOrbitLength(x, y));
                        }
                    }
                }
            }
        }
        postProcessTime += System.currentTimeMillis() - t;
    }
    
    public void stop() {
        stopped = true;
    }

}
