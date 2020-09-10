/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class InfiniteLineOrbitTrap extends OrbitTrap {

	private static final double EPSILON = 1E-15;
	private JPanel settingsPanel;
	private final JSlider compressionSlider = new JSlider(1, 100);
	private final ColorPalette colorPalette = new ColorPalette();
	private float spectrumComp = 0.05f;

	private double [][] minDists;
	private double m, c; //y=mx+c
	private Complex c1;
	private Complex c2;
	private boolean vertical = false;
	// mx -y +c = 0
    // a==m, b=-1, c==c

    //Distance between line and point:
    //Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)


	public InfiniteLineOrbitTrap() {
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));
		initSettingsPanel();
	}

	@Override
	public Component getSettingsComponent() {
		return settingsPanel;
	}

	@Override
	public String getName() {
		return "Infinite line";
	}

	@Override
	public void init(FractalRenderer fractalRenderer) {
		int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
		int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
		minDists = new double[imageWidth][imageHeight];
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
		double minDist = Double.MAX_VALUE;
		for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			double d = distanceFrom(orbit.get(i));
			if (d < minDist) {
				minDist = d;
			}
		}

		minDists[x][y] = minDist;
		return minDist == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDist) * spectrumComp);
	}

	@Override
	public Color reCalcColor(int x, int y) {
		return minDists[x][y] == 0 ? Color.BLACK : colorPalette.getColor((float) -Math.log(minDists[x][y])*spectrumComp);
	}

	private void initSettingsPanel() {
		settingsPanel = new JPanel(new GridLayout(0, 1));
		settingsPanel.add(colorPalette);

		settingsPanel.add(new JLabel("Spectrum compression"));
		compressionSlider.setValue(1);
		compressionSlider.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {
				spectrumComp = (float) ((float) (double) compressionSlider.getValue() / 500d);
				redraw();
			}
		});
		settingsPanel.add(compressionSlider);
	}

	private double distanceFrom(Complex c) {
		if (vertical) {
			return Math.abs(c.r - c1.r);
		}
		return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
	}

	private void redraw() {
		for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
			for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
				fractalRenderer.getImage().setColor(x, y, reCalcColor(x, y));
			}
		}
		fractalRenderer.updateGui();
	}

}
