/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import fractal.common.paremetermappers.ParameterMapper;
import fractal.common.paremetermappers.StraightParameterMapper;

import javax.swing.*;
import java.util.List;

/**
 * @author lloyd
 */
public abstract class FractalEngine {

	protected ParameterMapper parameterMapper = new StraightParameterMapper();

	public List<Complex> calcParameterMappedOrbit(Complex c) {
		return calcStraightOrbit(parameterMapper.map(c));
	}

	protected abstract List<Complex> calcStraightOrbit(Complex c);

	public abstract void setMaxIter(int maxIter);

	public abstract int getMaxIter();

	public abstract Pair<Complex, Complex> getDefaultView();

	public abstract boolean isBailoutReached(List<Complex> orbit);

	public abstract boolean isBailoutReachedByLastOrbitPoint(Complex lastOrbitPoint);

	public abstract JComponent getSettingsComponent();

}
