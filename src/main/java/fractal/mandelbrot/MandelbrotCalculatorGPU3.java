/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import fractal.common.Antialiasable;
import fractal.common.Complex;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author CP316928
 */
public class MandelbrotCalculatorGPU3 implements Runnable {

    private final MandelbrotRenderer mandelbrotRenderer;
    private final MandelbrotEngine mandelbrotEngine;
    private final int imageWidth;
    private final int imageHeight;

    private int subImageWidth;
    private int subImageHeight;
    private int xOffset = 0;
    private int yOffset = 0;

    private final MandelbrotKernel kernel;

    public MandelbrotCalculatorGPU3(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = (MandelbrotEngine) mandelbrotRenderer.getFractalEngine();

        long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();

        // Calculate optimal subImageSize
        subImageWidth = imageWidth * 2;
        subImageHeight = imageHeight * 2;
        long maxMemImage = Long.MAX_VALUE;
        long arrayLengthRequired = Long.MAX_VALUE;
        while (maxMemImage > gpuMemAvailable || arrayLengthRequired > Integer.MAX_VALUE) {
            subImageWidth /= 2;
            subImageHeight /= 2;
            maxMemImage = (long) Double.BYTES * 2L * (long) subImageHeight * (long) subImageWidth * (long) mandelbrotEngine.getMaxIter();
            arrayLengthRequired = subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter();
        }
        System.out.println("subImage size: " + subImageWidth + "x" + subImageHeight);
        kernel = new MandelbrotKernel();
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getSubSamples() == Antialiasable.NONE) {

                    Range range = Range.create2D(subImageWidth, subImageHeight);//Inside or outside loop?
            for (xOffset = 0; xOffset < imageWidth; xOffset += subImageWidth) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += subImageHeight) {
                    kernel.initArrays();
                    kernel.execute(range);
                    kernel.postProcess();
                }
            }

        }
    }

    private class MandelbrotKernel extends Kernel {

        private final double z0r = mandelbrotEngine.getPerterbation().r;
        private final double z0i = mandelbrotEngine.getPerterbation().i;
        private final int maxIter = mandelbrotEngine.getMaxIter();
        private final double bailoutSquared = mandelbrotEngine.getBailout() * mandelbrotEngine.getBailout();

        private final double[] cr = new double[subImageWidth * subImageHeight];
        private final double[] ci = new double[subImageWidth * subImageHeight];

        private final double[] finalR = new double[subImageWidth * subImageHeight];
        private final double[] finalI = new double[subImageWidth * subImageHeight];
        
        public MandelbrotKernel() {
        }

        public void initArrays() {
            for (int x = 0; x < subImageWidth; x++) {
                for (int y = 0; y < subImageHeight; y++) {
                    Complex c = mandelbrotRenderer.getMapper().mapToComplex(x + xOffset, y + yOffset);
                    cr[x + y * subImageWidth] = c.r;
                    ci[x + y * subImageWidth] = c.i;
                }
            }
        }

        //Only this runs on the GPU
        @Override
        public void run() {
            int x = getGlobalId(0);
            int y = getGlobalId(1);
            double currentR = z0r;
            double currentI = z0i;
            double tempR;
            int iter = 0;
            while (currentR * currentR + currentI * currentI < bailoutSquared && iter < maxIter) {
                tempR = currentR * currentR - currentI * currentI + cr[x + y*getGlobalSize(0)];
                currentI = 2 * currentR * currentI + ci[x + y*getGlobalSize(0)];
                currentR = tempR;
                iter++;
            }
            finalR[x + y*getGlobalSize(0)] = currentR;
            finalI[x + y*getGlobalSize(0)] = currentI;
        }
        //Only this runs on the GPU

        private void postProcess() {
            for (int x = 0; x < subImageWidth; x++) {
                if (x + xOffset < imageWidth) {
                    for (int y = 0; y < subImageHeight; y++) {
                        if (y + yOffset < imageHeight) {
                            mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y + yOffset, getOrbit(x, y));
                        }
                    }
                }
            }
        }

        private List<Complex> getOrbit(int x, int y) {
            return Collections.singletonList(new Complex(finalR[x + y * subImageWidth], finalI[x + y * subImageWidth]));
        }

    }
}
