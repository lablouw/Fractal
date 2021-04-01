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

/**
 *
 * @author CP316928
 */
public class MandelbrotCalculatorGPU implements Runnable {

    private final MandelbrotRenderer mandelbrotRenderer;
    private final MandelbrotEngine mandelbrotEngine;
    private int subImageWidth;
    private final int imageWidth;
    private final int imageHeight;
    private int roundNumber;
    private int totalRounds;
    private int xOffset;
    
    private final MandelbrotKernel kernel;

    public MandelbrotCalculatorGPU(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = (MandelbrotEngine)mandelbrotRenderer.getFractalEngine();
        
        // Calculate total rounds required
        long maxMemImage = (long) Double.BYTES * 2L * (long) imageHeight * (long) imageWidth * (long) mandelbrotEngine.getMaxIter();
        long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();
        totalRounds = (int) (Math.floor((double) maxMemImage / (double) gpuMemAvailable) + 1);
        subImageWidth = (int) Math.ceil((float)imageWidth / (float)totalRounds); //max number of cols in a subImage, some will have less **
        //Check that the array size is not bigger that Integer.MAX_VALUE (H*W*maxIter < Integer.MAX_VALUE)
        long arrSize = subImageWidth * imageHeight * mandelbrotEngine.getMaxIter();
        if (arrSize > Integer.MAX_VALUE) {
            System.out.println("WARN: MAX ArraySize exceeded by "+(arrSize-Integer.MAX_VALUE));
            double newSubWidth = (double)Integer.MAX_VALUE / (double)(imageHeight * mandelbrotEngine.getMaxIter());
            subImageWidth = (int) Math.floor(newSubWidth);
            totalRounds = imageWidth/subImageWidth;
        }
        
        System.out.println(subImageWidth+" * "+totalRounds +"="+ subImageWidth*totalRounds);
        kernel = new MandelbrotKernel();
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getSubSamples() == Antialiasable.NONE) {
            xOffset = subImageWidth * roundNumber;
            if (xOffset > imageWidth) return;// we might overshoot because of **
            Range range = Range.create(subImageWidth * imageHeight);
//            long t0 = System.currentTimeMillis();
//            System.out.println(roundNumber+"/"+totalRounds+": "+xOffset+"-"+(xOffset+subImageWidth));
            kernel.updateRangeValues();
            kernel.execute(range);
//            System.out.println("Aparapi: " + (System.currentTimeMillis() - t0)+"\n");

            for (int x = 0; x < subImageWidth; x++) {
                if (x+xOffset < imageWidth) {
                    for (int y = 0; y < imageHeight; y++) {
                        mandelbrotRenderer.enginePerformedCalculation(x + xOffset, y, Collections.singletonList(kernel.getLastOrbitPoint(x, y)));
                    }
                }
            }
        }
    }

    void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getTotalRounds() {
        return totalRounds;
    }
    
    private class MandelbrotKernel extends Kernel {

        private final double z0r = mandelbrotEngine.getPerterbation().r;
        private final double z0i = mandelbrotEngine.getPerterbation().i;
        private final int maxIter = mandelbrotEngine.getMaxIter();
        private final double bailoutSquared = mandelbrotEngine.getBailout() * mandelbrotEngine.getBailout();
        
        private final double[] cr = new double[subImageWidth * imageHeight];
        private final double[] ci = new double[subImageWidth * imageHeight];
        private final double[] finalR = new double[subImageWidth * imageHeight];
        private final double[] finalI = new double[subImageWidth * imageHeight];

        public MandelbrotKernel() {
        }
        
        public void updateRangeValues() {
            for (int x = 0; x < subImageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    Complex c = mandelbrotRenderer.getImagePlaneMapper().mapToComplex(x + xOffset, y);
                    cr[x + y * subImageWidth] = c.r;
                    ci[x + y * subImageWidth] = c.i;
                }
            }
        }

        //Only this runs on the GPU
        @Override
        public void run() {
            int i = getGlobalId();
            double currentR = z0r;
            double currentI = z0i;
            double tempR;
            int iter = 0;
            while (currentR * currentR + currentI * currentI < bailoutSquared && iter < maxIter) {
                tempR = currentR * currentR - currentI * currentI + cr[i];
                currentI = 2 * currentR * currentI + ci[i];
                currentR = tempR;
                iter++;
            }
            finalR[i] = currentR;
            finalI[i] = currentI;
        }
        //Only this runs on the GPU

        public Complex getLastOrbitPoint(int x, int y) {
            return new Complex(finalR[x + y * subImageWidth], finalI[x + y * subImageWidth]);
        }

    }
}
