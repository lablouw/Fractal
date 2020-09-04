/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;

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

    public void init() {
        stopped = false;

        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = mandelbrotRenderer.getFractalEngine();

        mandelbrotEngine.initGPUKernel(imageWidth, imageHeight);
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getAA() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += mandelbrotEngine.getSubImageWidth()) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += mandelbrotEngine.getSubImageHeight()) {
                    mandelbrotEngine.doRunGPU(xOffset, yOffset, mandelbrotRenderer.getMapper());
                    if (stopped) {
                        return;
                    }
                    doPostProcess();
                }
            }
        }

    }

    private void doPostProcess() {
        for (int x = 0; x < mandelbrotEngine.getSubImageWidth(); x++) {
            if (x + xOffset < imageWidth) {
                for (int y = 0; y < mandelbrotEngine.getSubImageHeight(); y++) {
                    if (y + yOffset < imageHeight) {
                        if (mandelbrotEngine.isUseGPUFull()) {
                            //TODO: (test) if (stopped) {return;}
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, mandelbrotEngine.getGPUOrbit(x, y));
                        } else if (mandelbrotEngine.isUseGPUFast()) {
                            //TODO: (test) if (stopped) {return;}
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, mandelbrotEngine.getLastOrbitPoint(x, y), mandelbrotEngine.getOrbitLength(x, y));
                        }
                    }
                }
            }
        }
    }
    
    public void stop() {
        stopped = true;
    }

}
