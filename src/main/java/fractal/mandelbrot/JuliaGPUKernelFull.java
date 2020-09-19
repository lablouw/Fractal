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

    private RawGpuOrbitContainer rawGpuOrbitContainer;

    public void init(int subImageWidth, int subImageHeight, int maxIter, double bailoutSquared, Complex c) {
        this.subImageWidth = subImageWidth;
        this.subImageHeight = subImageHeight;

        this.maxIter = maxIter;
        this.bailoutSquared = bailoutSquared;

        cr = c.r;
        ci = c.i;

        z0r = new double[subImageWidth * subImageHeight];
        z0i = new double[subImageWidth * subImageHeight];

        rawGpuOrbitContainer = new RawGpuOrbitContainer();
        rawGpuOrbitContainer.orbitsR = new double[subImageWidth * subImageHeight * maxIter];
        rawGpuOrbitContainer.orbitsI = new double[subImageWidth * subImageHeight * maxIter];
        rawGpuOrbitContainer.orbitLengths = new int[subImageWidth][subImageHeight];
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
        rawGpuOrbitContainer.orbitsR[orbitStartIndex] = z0r[x + y * getGlobalSize(0)];
        rawGpuOrbitContainer.orbitsI[orbitStartIndex] = z0i[x + y * getGlobalSize(0)];
        int iter = 0;
        while (iter < maxIter - 1 && rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] + rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] < bailoutSquared) {
            rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter + 1] = rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] - rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] + cr;
            rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter + 1] = 2 * rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter] * rawGpuOrbitContainer.orbitsI[orbitStartIndex + iter] + ci;
            iter++;
        }
        if (iter < maxIter - 1) {
            rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
            rawGpuOrbitContainer.orbitsR[orbitStartIndex + iter + 1] = Double.MAX_VALUE;
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
