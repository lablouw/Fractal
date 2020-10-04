package fractal.mandelbrot.coloring.orbittrap.cross;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapSettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CrossOrbitTrap extends OrbitTrap {

	private final List<OrbitTrapColorStrategy<CrossOrbitTrap>> colorStrategies;

	private static final double EPSILON = 1E-15;
	private final OrbitTrapSettingsDialog<CrossOrbitTrap, OrbitTrapColorStrategy<CrossOrbitTrap>> settingsDialog;

	private double m1, c1; //y=mx+c
	private double m2, c2; //y=mx+c
	private Complex d1;
	private Complex d2;
	private boolean vertical = false;

	public CrossOrbitTrap(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));

		colorStrategies = new ArrayList<>();
		colorStrategies.add(new CrossOrbitTrapDistanceColorStrategy(fractalRenderer, this));
		activeColorStrategy = colorStrategies.get(0);

		settingsDialog = new OrbitTrapSettingsDialog<>(this, colorStrategies);
	}

	@Override
	public String getName() {
		return "Cross";
	}

	@Override
	public void initForRender(FractalRenderer fractalRenderer) {
		activeColorStrategy.initForRender();
	}

	@Override
	public void setDefiningPoints(Complex d1, Complex d2) {
		this.d1 = d1;
		this.d2 = d2;

		if (Math.abs(d2.r - d1.r) < EPSILON || Math.abs(d2.i - d1.i) < EPSILON) {
			vertical = true;
		} else {
			m1 = (d2.i - d1.i) / (d2.r - d1.r);
			c1 = d1.i - m1 * d1.r;

			m2 = -1/m1;
			c2 = d1.i - m2 * d1.r;

			vertical = false;
		}
	}

	@Override
	public JDialog getSettingsDialog() {
		return settingsDialog;
	}

	@Override
	public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
		BufferedImage im = ImageUtils.deepCopy(baseImage);

		int leftX = 0;
		int rightX = fractalRenderer.getImage().getBufferedImage().getWidth();

		double leftR = fractalRenderer.getMapper().mapToComplex(leftX, 0).r;
		double rightR = fractalRenderer.getMapper().mapToComplex(rightX, 0).r;

		double leftI = m1 * leftR + c1;
		double rightI = m1 * rightR +c1;
		int leftY = fractalRenderer.getMapper().mapToImage(new Complex(0, leftI)).y;
		int rightY = fractalRenderer.getMapper().mapToImage(new Complex(0, rightI)).y;

		im.getGraphics().drawLine(leftX, leftY, rightX, rightY);

		leftI = m2 * leftR + c2;
		rightI = m2 * rightR + c2;
		leftY = fractalRenderer.getMapper().mapToImage(new Complex(0, leftI)).y;
		rightY = fractalRenderer.getMapper().mapToImage(new Complex(0, rightI)).y;

		im.getGraphics().drawLine(leftX, leftY, rightX, rightY);

		return im;

	}

	@Override
	public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
		return activeColorStrategy.calcColor(x, y, orbit, fractalEngine, this);
	}
        
    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        return activeColorStrategy.calcColor(x, y, rawGpuOrbitContainer, orbitStartIndex, orbitLength, fractalEngine, this);
    }

	@Override
	public Color reCalcColor(int x, int y) {
		return activeColorStrategy.recalcColor(x, y);
	}

	public double distanceFrom(Complex c) {
		if (vertical) {
			return Math.min(Math.abs(c.r-d1.r), Math.abs(c.i - d1.i));
		}

		return Math.min(
				Math.abs(m1*c.r - c.i + c1) / Math.sqrt(m1*m1 + 1),
				Math.abs(m2*c.r - c.i + c2) / Math.sqrt(m2*m2 + 1)
		);
	}
}
