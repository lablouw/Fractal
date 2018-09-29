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
public class JuliaGPUKernelFull extends Kernel {

    private int subImageWidth;
    private int subImageHeight;

    private int maxIter;

    private double cr;
    private double ci;
    private double bailoutSquared;

    private double[] z0r;
    private double[] z0i;

    private double[] orbitsR;
    private double[] orbitsI;

    public void init(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex c) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        cr = c.r;
        ci = c.i;

        z0r = new double[subImageWidth * subImageHeight];
        z0i = new double[subImageWidth * subImageHeight];

        orbitsR = new double[subImageWidth * subImageHeight * maxIter];
        orbitsI = new double[subImageWidth * subImageHeight * maxIter];
    }

    public void initArrays(int xOffset, int yOffset, Mapper mapper) {
        for (int x = 0; x < subImageWidth; x++) {
            for (int y = 0; y < subImageHeight; y++) {
                Complex z0 = mapper.mapToComplex(x + xOffset, y + yOffset);
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
        int orbitStartIndex = x * maxIter + y * maxIter * getGlobalSize(0);
        orbitsR[orbitStartIndex] = z0r[x + y * getGlobalSize(0)];
        orbitsI[orbitStartIndex] = z0i[x + y * getGlobalSize(0)];
        int iter = 0;
        while (iter < maxIter - 1 && orbitsR[orbitStartIndex + iter] * orbitsR[orbitStartIndex + iter] + orbitsI[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] < bailoutSquared) {
            orbitsR[orbitStartIndex + iter + 1] = orbitsR[orbitStartIndex + iter] * orbitsR[orbitStartIndex + iter] - orbitsI[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] + cr;
            orbitsI[orbitStartIndex + iter + 1] = 2 * orbitsR[orbitStartIndex + iter] * orbitsI[orbitStartIndex + iter] + ci;
            iter++;
        }
        if (iter < maxIter - 1) {
            orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
            orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
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
