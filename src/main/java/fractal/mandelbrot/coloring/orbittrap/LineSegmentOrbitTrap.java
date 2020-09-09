package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LineSegmentOrbitTrap extends OrbitTrap {

	private double m, c; //y=mx+c
	private Complex c1;
	private Complex c2;
	// mx -y +c = 0
	// a==m, b=-1, c==c

	//Distance between line and point:
	//Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)


	public LineSegmentOrbitTrap() {
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));
	}

	@Override
	public String getName() {
		return "Line segment";
	}

	@Override
	public void setDefiningPoints(Complex c1, Complex c2) {
		this.c1 = c1;
		this.c2 = c2;
		if (c1.r > c2.r) {
			Complex t = c1;
			c1 = c2;
			c2 = t;
		}

		m = (c2.i - c1.i) / (c2.r - c1.r);
		c = c1.i - m * c1.r;
	}

	@Override
	public double distanceFrom(Complex c) {
		//project point onto line
		double m2 = -1/m;
		double r = (m*c1.r-m2*c.r+c.i-c1.i) / (m-m2);
		double i = m2*(r-c.r)+c.i;

		//check if left of, right of, or on line.
		if (r < c1.r) {
			return c1.sub(c).modulus();
		} else if (r > c2.r) {
			return c2.sub(c).modulus();
		} else {
			return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
		}
	}

	@Override
	public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
		BufferedImage im = ImageUtils.deepCopy(baseImage);
		Point p1 = fractalRenderer.getMapper().mapToImage(c1);
		Point p2 = fractalRenderer.getMapper().mapToImage(c2);
		im.getGraphics().drawLine(p1.x, p1.y, p2.x, p2.y);

		return im;
	}
}
