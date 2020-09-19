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

    private RawGpuOrbitContainer rawGpuOrbitContainer;

    public void init(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex perterbation) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        z0r = perterbation.r;
        z0i = perterbation.i;

        cr = new double[subImageWidth * subImageHeight];
        ci = new double[subImageWidth * subImageHeight];

        rawGpuOrbitContainer = new RawGpuOrbitContainer();
        rawGpuOrbitContainer.orbitsR = new double[subImageWidth * subImageHeight * maxIter];
        rawGpuOrbitContainer.orbitsI = new double[subImageWidth * subImageHeight * maxIter];
        rawGpuOrbitContainer.orbitLengths = new int[subImageWidth][subImageHeight];
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
        rawGpuOrbitContainer.orbitsR[orbitStartIndex] = z0r;
        rawGpuOrbitContainer.orbitsI[orbitStartIndex] = z0i;
        int iter = 0;
        while (iter < maxIter - 1 && rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] + rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] < bailoutSquared) {
            rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter + 1] = rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] - rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] + cr[x + y * getGlobalSize(0)];
            rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter + 1] = 2 * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] + ci[x + y * getGlobalSize(0)];
            iter++;
        }
        if (iter < maxIter - 1) {
            rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
            rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
        }
        rawGpuOrbitContainer.orbitLengths[x][y] = iter+1;
    }
    //Only this runs on the GPU

    public RawGpuOrbitContainer getRawGpuOrbitContainer() {
        return rawGpuOrbitContainer;
    }

    public int getSubImageWidth() {
        return subImageWidth;
    }

    public int getSubImageHeight() {
        return subImageHeight;
    }
}
