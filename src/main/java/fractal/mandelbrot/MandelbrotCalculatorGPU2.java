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
import java.util.List;

/**
 *
 * @author CP316928
 */
public class MandelbrotCalculatorGPU2 implements Runnable {

    private final MandelbrotRenderer mandelbrotRenderer;
    private final MandelbrotEngine mandelbrotEngine;
    private final int imageWidth;
    private final int imageHeight;
    
    private int subImageWidth;
    private int subImageHeight;
    private int xOffset = 0;
    private int yOffset = 0;
    
    private final MandelbrotKernel kernel;

    public MandelbrotCalculatorGPU2(MandelbrotRenderer mandelbrotRenderer) {
        this.mandelbrotRenderer = mandelbrotRenderer;
        imageWidth = mandelbrotRenderer.getImage().getBufferedImage().getWidth();
        imageHeight = mandelbrotRenderer.getImage().getBufferedImage().getHeight();
        mandelbrotEngine = (MandelbrotEngine)mandelbrotRenderer.getFractalEngine();
        
        long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();
        
        // Calculate optimal subImageSize
        subImageWidth = imageWidth+1;
        subImageHeight = imageHeight+1;
        long maxMemImage = Long.MAX_VALUE;
        long arrayLengthRequired = Long.MAX_VALUE;
        while (maxMemImage > gpuMemAvailable || arrayLengthRequired > Integer.MAX_VALUE) {
            subImageWidth /= 2;
            subImageHeight /= 2;
            maxMemImage = (long) Double.BYTES * 2L * (long) subImageHeight * (long) subImageWidth * (long) mandelbrotEngine.getMaxIter();
            arrayLengthRequired = subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter();
        }
        System.out.println("subImage: "+subImageWidth+"x"+subImageHeight);
        kernel = new MandelbrotKernel();
    }

    @Override
    public void run() {
        if (mandelbrotRenderer.getSubSamples() == Antialiasable.NONE) {
            
            for (xOffset = 0; xOffset < imageWidth; xOffset += subImageWidth) {
                for (yOffset = 0; yOffset < imageHeight; yOffset += subImageHeight){
                    Range range = Range.create(subImageWidth * subImageHeight * mandelbrotEngine.getMaxIter());//Inside or outside loop?
                    long t0=System.currentTimeMillis();
                    kernel.initArrays();
                    System.out.println("\ninitArrays: "+(System.currentTimeMillis()-t0));
                    t0=System.currentTimeMillis();
                    kernel.execute(range);
                    System.out.println("execute: "+(System.currentTimeMillis()-t0));
                    t0=System.currentTimeMillis();
                    kernel.postProcess();
                    System.out.println("postProcess: "+(System.currentTimeMillis()-t0));
                }
            }
            
        }
    }

    private class MandelbrotKernel extends Kernel {

        private final double z0r = mandelbrotEngine.getPerterbation().r;
        private final double z0i = mandelbrotEngine.getPerterbation().i;
        private final int maxIter = mandelbrotEngine.getMaxIter();
        private final double bailoutSquared = mandelbrotEngine.getBailout() * mandelbrotEngine.getBailout();
        
        private final double[] orbitR = new double[subImageWidth * subImageHeight * maxIter];
        private final double[] orbitI = new double[subImageWidth * subImageHeight * maxIter];

        public MandelbrotKernel() {
        }
        
        public void initArrays() {
//            z0r
//            z0i
//            maxIter
//            bailoutSquared
//            orbitR
//            orbitI
            
            for (int x = 0; x < subImageWidth; x++) {
                if (x + xOffset >= imageWidth) continue;
                for (int y = 0; y < subImageHeight; y++) {
                    if (y + yOffset >= imageHeight) continue;
                    Complex c = mandelbrotRenderer.getMapper().mapToComplex(x + xOffset, y + yOffset);
                    orbitR[x*subImageHeight*maxIter + y*maxIter] = c.r;
                    orbitI[x*subImageHeight*maxIter + y*maxIter] = c.i;
//                    System.out.println("("+x+","+y+")("+(x*subImageWidth*maxIter + y*maxIter)+")="+c.r);
                }
            }
        }

        //Only this runs on the GPU
        @Override
        public void run() {
            int i = getGlobalId();
            if (i % maxIter != 0) return;
            double Cr = orbitR[i];
            double Ci = orbitI[i];
            orbitR[i] = z0r;
            orbitI[i] = z0i;
            int iter = 0;
            int j = i;
            while (orbitR[j]*orbitR[j] + orbitI[j]*orbitI[j] < bailoutSquared && iter < maxIter) {
                orbitR[j+1] = orbitR[j]*orbitR[j] - orbitI[j]*orbitI[j] + Cr;
                orbitI[j+1] = 2*orbitR[j]*orbitI[j] + Ci;
                iter++;
                j++;
            }
            if (iter < maxIter-1) {
                orbitR[j+1] = Double.MAX_VALUE;
                orbitI[j+1] = Double.MAX_VALUE;
            }
        }
        //Only this runs on the GPU
        
        public void postProcess() {
            for (int x = 0; x < subImageWidth; x++) {
                if (x+xOffset >= imageWidth) continue;
                for (int y = 0; y < subImageHeight; y++) {
                    if (y+yOffset >= imageHeight) continue;
                    mandelbrotRenderer.enginePerformedCalculation(x+xOffset, y+yOffset, kernel.getOrbit(x, y));
                }
            }
        }

        public List<Complex> getOrbit(int x, int y) {
            List<Complex> orbit = new ArrayList<>();
            int orbitStartIndex = x*subImageHeight*maxIter + y*maxIter;
//            System.out.println();
//            System.out.print("Orbit ("+x+","+y+")("+(x*subImageWidth*maxIter + y*maxIter)+")=");
            for (int i = orbitStartIndex; i<orbitStartIndex+maxIter && orbitR[i] != Double.MAX_VALUE; i++) {
//                System.out.print("["+orbitR[i]+","+orbitI[i]+"]");
                orbit.add(new Complex(orbitR[i], orbitI[i]));
            }
            
            return orbit;
        }

    }
}
