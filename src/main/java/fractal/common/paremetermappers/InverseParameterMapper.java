package fractal.common.paremetermappers;

import fractal.common.Complex;

public class InverseParameterMapper implements ParameterMapper {
    
    private Complex center = Complex.ZERO;
    
	@Override
	public Complex map(Complex c) {
        return Complex.ONE.div(c).add(center);
	}

    @Override
    public String getName() {
        return "1/\u00B5";
    }

    public Complex getCenter() {
        return center;
    }

    public void setCenter(Complex center) {
        this.center = center;
    }
    
    
}
