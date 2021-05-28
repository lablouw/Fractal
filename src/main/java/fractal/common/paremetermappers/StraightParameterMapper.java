package fractal.common.paremetermappers;

import fractal.common.Complex;

public class StraightParameterMapper implements ParameterMapper {

	@Override
	public Complex map(Complex c) {
		return c;
	}

    @Override
    public String getName() {
        return "None";
    }

}
