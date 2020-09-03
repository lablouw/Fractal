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
public class JuliaCalculatorGPU implements Runnable {

    private final JuliaRenderer juliaRenderer;
    private JuliaEngine juliaEngine;
    private int imageWidth;
    private int imageHeight;

    private int xOffset = 0;
    private int yOffset = 0;

    boolean stopped = false;

    public JuliaCalculatorGPU(JuliaRenderer mandelbrotRenderer) {
        this.juliaRenderer = mandelbrotRenderer;
    }

    public void init() {
        stopped = false;

        imageWidth = juliaRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = juliaRenderer.getImage().getBufferedImage().getHeight();
        juliaEngine = juliaRenderer.getFractalEngine();

        juliaEngine.initGPUKernel(imageWidth, imageHeight, juliaRenderer.getMapper());
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
        for (int x = 0; x < juliaEngine.getSubImageWidth(); x++) {
            if (x + xOffset < imageWidth) {
                for (int y = 0; y < juliaEngine.getSubImageHeight(); y++) {
                    if (y + yOffset < imageHeight) {
                        if (juliaEngine.isUseGPUFull()) {
                            juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, juliaEngine.getGPUOrbit(x, y));
                        } else if (juliaEngine.isUseGPUFast()) {
                            juliaRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, juliaEngine.getLastOrbitPoint(x, y), juliaEngine.getOrbitLength(x, y));
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
