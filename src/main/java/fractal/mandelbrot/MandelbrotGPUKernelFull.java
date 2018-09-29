/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import com.aparapi.Kernel;
import fractal.common.Complex;
import fractal.common.Mappers.Mapper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author CP316928
 */
public class MandelbrotGPUKernelFull extends Kernel {

    private int subImageWidth;
    private int subImageHeight;

    private int maxIter;

    private double z0r;
    private double z0i;
    private double bailoutSquared;

    private double[] cr;
    private double[] ci;

    private double[] orbitsR;
    private double[] orbitsI;

    public void init(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex perterbation) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        z0r = perterbation.r;
        z0i = perterbation.i;

        cr = new double[subImageWidth * subImageHeight];
        ci = new double[subImageWidth * subImageHeight];

        orbitsR = new double[subImageWidth * subImageHeight * maxIter];
        orbitsI = new double[subImageWidth * subImageHeight * maxIter];
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
        int orbitStartIndex = x * maxIter + y * maxIter * getGlobalSize(0);
        orbitsR[orbitStartIndex] = z0r;
        orbitsI[orbitStartIndex] = z0i;
        int iter = 0;
        while (iter < maxIter - 1 && orbitsR[orbitStartIndex + iter] * orbitsR[orbitStartIndex + iter] + orbitsI[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] < bailoutSquared) {
            orbitsR[orbitStartIndex + iter + 1] = orbitsR[orbitStartIndex + iter] * orbitsR[orbitStartIndex + iter] - orbitsI[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] + cr[x + y * getGlobalSize(0)];
            orbitsI[orbitStartIndex + iter + 1] = 2 * orbitsR[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] + ci[x + y * getGlobalSize(0)];
            iter++;
        }
        if (iter < maxIter - 1) {
            orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
            orbitsI[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
        }
    }
    //Only this runs on the GPU

    public List<Complex> getOrbit(int x, int y) {
        List<Complex> orbit = new ArrayList<>();
        int orbitStartIndex = x * maxIter + y * maxIter * subImageWidth;
        int iter = 0;
        while (iter < maxIter && orbitsR[orbitStartIndex + iter] != Double.MAX_VALUE) {
            orbit.add(new Complex(orbitsR[orbitStartIndex + iter], orbitsI[orbitStartIndex + iter]));
            iter++;
        }

        return orbit;
    }

    public int getSubImageWidth() {
        return subImageWidth;
    }

    public int getSubImageHeight() {
        return subImageHeight;
    }
}
