/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.infiniteline;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class InfiniteLineOrbitTrap extends OrbitTrap {

	private final List<OrbitTrapColorStrategy<InfiniteLineOrbitTrap>> colorStrategies;

	private static final double EPSILON = 1E-15;
	private final JDialog settingsDialog;

	private double m, c; //y=mx+c
	private Complex c1;
	private Complex c2;
	private boolean vertical = false;
	// mx -y +c = 0
    // a==m, b=-1, c==c

    //Distance between line and point:
    //Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)


	public InfiniteLineOrbitTrap(FractalRenderer fractalRenderer) {
		this.fractalRenderer = fractalRenderer;
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));

		colorStrategies = new ArrayList<>();
		colorStrategies.add(new InfiniteLineOrbitTrapDistanceColorStrategy(fractalRenderer, this));
		activeColorStrategy = colorStrategies.get(0);

		settingsDialog = null;//new CircleOrbitTrapSettingsDialog(this, fractalRenderer, colorStrategies);
	}

	@Override
	public JDialog getSettingsDialog() {
		return settingsDialog;
	}

	@Override
	public String getName() {
		return "Infinite line";
	}

	@Override
	public void initForRender(FractalRenderer fractalRenderer) {
		activeColorStrategy.initForRender();
	}

	@Override
    public void setDefiningPoints(Complex c1, Complex c2) {
    	if (c1.r > c2.r) {
			Complex t = c1;
			c1 = c2;
			c2 = t;
		}
		this.c1 = c1;
		this.c2 = c2;

		if (c2.r - c1.r < EPSILON) {
			vertical = true;
		} else {
			m = (c2.i - c1.i) / (c2.r - c1.r);
			c = c1.i - m * c1.r;
			vertical = false;
		}
    }

	@Override
	public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
		int leftX = 0;
		int rightX = fractalRenderer.getImage().getBufferedImage().getWidth();

		double leftR = fractalRenderer.getMapper().mapToComplex(leftX, 0).r;
		double rightR = fractalRenderer.getMapper().mapToComplex(rightX, 0).r;

		double leftI = m * leftR + c;
		double rightI = m * rightR +c;

		int leftY = fractalRenderer.getMapper().mapToImage(new Complex(0, leftI)).y;
		int rightY = fractalRenderer.getMapper().mapToImage(new Complex(0, rightI)).y;

		BufferedImage im = ImageUtils.deepCopy(baseImage);
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
			return Math.abs(c.r - c1.r);
		}
		return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
	}

}
