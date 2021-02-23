package fractal.common.mappers;

import fractal.common.Complex;

import java.awt.*;


public class InverseMapper extends Mapper {

	private final Complex center;

	public InverseMapper(Complex topLeft, Complex bottomRight, int width, int height, Complex center) {
		super(topLeft, bottomRight, width, height);
		this.center = center;
	}

	@Override
	public Point mapToImage(Complex c) {
		return null;
	}

	@Override
	public Complex mapToComplex(int x, int y) {
		return null;
	}
}
