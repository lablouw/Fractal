package fractal.common.paremetermappers;

import fractal.common.Complex;

public class InverseParameterMapper implements ParameterMapper {
	@Override
	public Complex map(Complex c) {
		return Complex.ONE.div(c);
//        return Complex.ONE.div(c).add(new Complex(0.25,0));
	}

    @Override
    public String getName() {
        return "1/\u00B5";
    }
}
