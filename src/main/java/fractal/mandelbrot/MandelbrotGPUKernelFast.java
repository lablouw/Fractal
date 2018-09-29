/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import com.aparapi.Kernel;
import fractal.common.Complex;
import fractal.common.Mappers.Mapper;

/**
 *
 * @author CP316928
 */
public class MandelbrotGPUKernelFast extends Kernel {

    private int subImageWidth;
    private int subImageHeight;

    private int maxIter;

    private double z0r;
    private double z0i;
    private double bailoutSquared;

    private double[] cr;
    private double[] ci;

    private double[] finalR;
    private double[] finalI;
    private int[] orbitLengths;

    public void init(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex perterbation) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        z0r = perterbation.r;
        z0i = perterbation.i;

        cr = new double[subImageWidth * subImageHeight];
        ci = new double[subImageWidth * subImageHeight];

        finalR = new double[subImageWidth * subImageHeight];
        finalI = new double[subImageWidth * subImageHeight];

        orbitLengths = new int[subImageWidth * subImageHeight];
    }

    public void initArrays(int xOffset, int yOffset, Mapper mapper) {
        for (int x = 0; x < subImageWidth; x++) {
            for (int y = 0; y < subImageHeight; y++) {
                Complex c = mapper.mapToComplex(x + xOffset, y + yOffset);
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
            tempR = currentR * currentR - currentI * currentI + cr[x + y * getGlobalSize(0)];
            currentI = 2 * currentR * currentI + ci[x + y * getGlobalSize(0)];
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
