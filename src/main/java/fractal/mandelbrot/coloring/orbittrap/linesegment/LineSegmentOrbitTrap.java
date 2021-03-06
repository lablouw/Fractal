package fractal.mandelbrot.coloring.orbittrap.linesegment;

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

public class LineSegmentOrbitTrap extends OrbitTrap {

	private final List<OrbitTrapColorStrategy<LineSegmentOrbitTrap>> colorStrategies;

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


	public LineSegmentOrbitTrap(FractalRenderer fractalRenderer) {
                this.fractalRenderer = fractalRenderer;
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));

		colorStrategies = new ArrayList<>();
		colorStrategies.add(new LineSegmentOrbitTrapDistanceColorStrategy(fractalRenderer, this));
		colorStrategies.add(new LineSegmentOrbitTrapOrthogonalProjectionColorStrategy(fractalRenderer, this));
		activeColorStrategy = colorStrategies.get(0);

		settingsDialog = new OrbitTrapSettingsDialog(this, colorStrategies);
	}

	@Override
	public JDialog getSettingsDialog() {
		return settingsDialog;
	}

	@Override
	public String getName() {
		return "Line segment";
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
		BufferedImage im = ImageUtils.deepCopy(baseImage);
		Point p1 = fractalRenderer.getImagePlaneMapper().mapToImage(c1);
		Point p2 = fractalRenderer.getImagePlaneMapper().mapToImage(c2);
		im.getGraphics().drawLine(p1.x, p1.y, p2.x, p2.y);

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
			if (c1.i > c2.i) {
				if (c.i > c2.i && c.i < c1.i) {
					return (Math.abs(c.r - c1.r));
				}
			} else {
				if (c.i > c1.i && c2.i < c.i) {
					return (Math.abs(c.r - c1.r));
				}
			}
			return Math.min(c1.sub(c).modulus(), c2.sub(c).modulus());
		}

		//project point onto line
		double m2 = -1 / m;
		double r = (m*c1.r - m2*c.r + c.i - c1.i) / (m - m2);
		//double i = m2*(r-c.r)+c.i;

		//check if left of, right of, or on line.
		if (r < c1.r) {
			return c1.sub(c).modulus();//left of line
		} else if (r > c2.r) {
			return c2.sub(c).modulus();//right of line
		} else {//on line
			return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
		}
	}

	public double getProjectedPosition(Complex c) {
		if (vertical) {//Does not project onto line
			if ((c1.i > c2.i && c.i > c1.i) || (c1.i < c2.i && c.i < c1.i)) {
				return Double.MAX_VALUE;
			}

			if (c1.i > c2.i) {//projects onto line, c1->0, c2->1
				return (c1.i-c.i) / (c1.i-c2.i);
			} else {
				return (c.i-c1.i) / (c2.i-c1.i);
			}
		}

		//project point onto line
		double m2 = -1 / m;
		double r = (m*c1.r - m2*c.r + c.i - c1.i) / (m - m2);
		if (r < c1.r || r > c2.r) { //Does not project onto line
			return Double.MAX_VALUE;
		}

		return (r-c1.r) / (c2.r-c1.r);

	}
}
