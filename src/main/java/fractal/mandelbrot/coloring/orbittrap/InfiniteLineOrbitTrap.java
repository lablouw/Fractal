/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;

import java.awt.image.BufferedImage;

/**
 *
 * @author Lloyd
 */
public class InfiniteLineOrbitTrap extends OrbitTrap {

    private double m, c; //y=mx+c
	private Complex c1;
	private Complex c2;
	// mx -y +c = 0
    // a==m, b=-1, c==c

    //Distance between line and point:
    //Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)


	public InfiniteLineOrbitTrap() {
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));
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
        return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
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
    public String getName() {
        return "Infinite line";
    }

}
