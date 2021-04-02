package fractal.common.paremetermappers;

import fractal.common.Complex;

public class InverseParameterMapper implements ParameterMapper {
	@Override
	public Complex map(Complex c) {
		return Complex.ONE.div(c);
	}
}
