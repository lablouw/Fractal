package fractal.mandelbrot;

/**
 * A container class for the arrays returned by the gpu.
 * This is because primitives are pass-by-value and we don't want to do that with an array with hundreds of millions of entries spanning gigabytes of memory.
 * so no getters either, access the arrays from where they lie.
 */
public class RawGpuOrbitContainer {

	public double[] orbitsR;
	public double[] orbitsI;
	public int[][]orbitLengths;

}
