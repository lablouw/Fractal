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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author CP316928
 */
public class MandelbrotCalculatorGPU5 implements Runnable {

    private final MandelbrotRenderer mandelbrotRenderer;
    private MandelbrotEngine mandelbrotEngine;
    private int imageWidth;
    private int imageHeight;

    private int subImageWidth;
    private int subImageHeight;
    private int xOffset = 0;
    private int yOffset = 0;

    boolean stopped = false;

    private final MandelbrotKernel kernel;

    public MandelbrotCalculatorGPU5(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
        kernel = new MandelbrotKernel();
    }

    public void init() {
        stopped = false;

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
            maxMemImage = (long) subImageHeight * (long) subImageWidth * (long) mandelbrotEngine.getMaxIter() * (long) Double.BYTES * 2L;
            arrayLengthRequired = subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter();
        }
        System.out.println("subImage size: " + subImageWidth + "x" + subImageHeight);
        
        kernel.init();
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getAA() == Antialiasable.NONE) {
            for (xOffset = 0; xOffset < imageWidth; xOffset += subImageWidth) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += subImageHeight) {
                    if (stopped) {
                        return;
                    }
                    Range range = Range.create2D(subImageWidth, subImageHeight);//Inside or outside loop?
                    kernel.initArrays();
                    kernel.execute(range);
                    kernel.postProcess();
                }
            }
        }

    }

    public void stop() {
        stopped = true;
    }

    private class MandelbrotKernel extends Kernel {

        private double z0r;
        private double z0i;
        private int maxIter;
        private double bailoutSquared;

        private double[] cr;
        private double[] ci;

        private double[] orbitsR;
        private double[] orbitsI;

        public void init() {
            z0r = mandelbrotEngine.getPerterbation().r;
            z0i = mandelbrotEngine.getPerterbation().i;
            maxIter = mandelbrotEngine.getMaxIter();
            bailoutSquared = mandelbrotEngine.getBailout() * mandelbrotEngine.getBailout();

            cr = new double[subImageWidth * subImageHeight];
            ci = new double[subImageWidth * subImageHeight];

            orbitsR = new double[subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter()];
            orbitsI = new double[subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter()];
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
            int orbitStartIndex = x * maxIter + y * maxIter * getGlobalSize(0);
            orbitsR[orbitStartIndex] = z0r;
            orbitsI[orbitStartIndex] = z0i;
            int iter = 0;
            while (orbitsR[orbitStartIndex+iter] * orbitsR[orbitStartIndex+iter] + orbitsI[orbitStartIndex+iter] * orbitsI[orbitStartIndex+iter] < bailoutSquared && iter < maxIter - 1) {
                orbitsR[orbitStartIndex + iter+1] = orbitsR[orbitStartIndex+iter] * orbitsR[orbitStartIndex+iter] - orbitsI[orbitStartIndex+iter] * orbitsI[orbitStartIndex+iter] + cr[x + y * getGlobalSize(0)];;
                orbitsI[orbitStartIndex + iter+1] = 2 * orbitsR[orbitStartIndex+iter] * orbitsI[orbitStartIndex+iter] + ci[x + y * getGlobalSize(0)];
                iter++;
            }
            if (iter < maxIter - 1) {
                orbitsR[orbitStartIndex + iter+1] = Double.MAX_VALUE;
                orbitsR[orbitStartIndex + iter+1] = Double.MAX_VALUE;
            }
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
//            return Collections.singletonList(new Complex(finalR[x + y * subImageWidth], finalI[x + y * subImageWidth]));
            List<Complex> orbit = new ArrayList<>();
            int orbitStartIndex = x * maxIter + y * maxIter * subImageWidth;
            int iter = 0;
            while (orbitsR[orbitStartIndex + iter] != Double.MAX_VALUE && iter < maxIter - 1) {
                orbit.add(new Complex(orbitsR[orbitStartIndex + iter], orbitsI[orbitStartIndex + iter]));
                iter++;
            }

            return orbit;
        }

    }
}
