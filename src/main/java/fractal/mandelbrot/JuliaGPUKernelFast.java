/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import com.aparapi.Kernel;
import fractal.common.Complex;
import fractal.common.ImagePlaneMapper;

/**
 *
 * @author CP316928
 */
public class JuliaGPUKernelFast extends Kernel {
    private int subImageWidth;
    private int subImageHeight;

    private int maxIter;

    private double cr;
    private double ci;
    private double bailoutSquared;

    private double[] z0r;
    private double[] z0i;

    private double[] finalR;
    private double[] finalI;
    private int[] orbitLengths;
    
    public void initForRender(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex c) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        cr = c.r;
        ci = c.i;

        z0r = new double[subImageWidth * subImageHeight];
        z0i = new double[subImageWidth * subImageHeight];

        finalR = new double[subImageWidth * subImageHeight];
        finalI = new double[subImageWidth * subImageHeight];

        orbitLengths = new int[subImageWidth * subImageHeight];
    }
    
   public void initArrays(int xOffset, int yOffset, ImagePlaneMapper imagePlaneMapper, double xSubSamplePos, double ySubSamplePos, int subSamples) {
        for (int x = 0; x < subImageWidth; x++) {
            for (int y = 0; y < subImageHeight; y++) {
                Complex z0 = imagePlaneMapper.mapToComplex(xOffset + x + xSubSamplePos/subSamples, yOffset + y + ySubSamplePos/subSamples);
                z0r[x + y * subImageWidth] = z0.r;
                z0i[x + y * subImageWidth] = z0.i;
            }
        }
    }
   
    //Only this runs on the GPU
    @Override
    public void run() {
        int x = getGlobalId(0);
        int y = getGlobalId(1);
        double currentR = z0r[x + y * getGlobalSize(0)];
        double currentI = z0i[x + y * getGlobalSize(0)];
        double tempR;
        int iter = 0;
        while (currentR * currentR + currentI * currentI < bailoutSquared && iter < maxIter) {
            tempR = currentR * currentR - currentI * currentI + cr;
            currentI = 2 * currentR * currentI + ci;
            currentR = tempR;
            iter++;
        }
        finalR[x + y * getGlobalSize(0)] = currentR;
        finalI[x + y * getGlobalSize(0)] = currentI;
        orbitLengths[x + y * getGlobalSize(0)] = iter;
    }
    //Only this runs on the GPU
    public Complex getLastOrbitPoint(int x, int y) {
        return new Complex(finalR[x + y * subImageWidth], finalI[x + y * subImageWidth]);
    }

    public int getOrbitLength(int x, int y) {
        return orbitLengths[x + y * subImageWidth];
    }

    public int getSubImageWidth() {
        return subImageWidth;
    }

    public int getSubImageHeight() {
        return subImageHeight;
    }
}
