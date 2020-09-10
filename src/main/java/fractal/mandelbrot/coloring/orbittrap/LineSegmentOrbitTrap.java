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

public class LineSegmentOrbitTrap extends OrbitTrap {

	private JPanel settingsPanel;
	private final JSlider compressionSlider = new JSlider(1, 100);
	private final ColorPalette colorPalette = new ColorPalette();
	private float spectrumComp = 0.05f;

	private double [][] minDists;
	private double m, c; //y=mx+c
	private double m2;
	private Complex c1;
	private Complex c2;
	// mx -y +c = 0
	// a==m, b=-1, c==c

	//Distance between line and point:
	//Distance(ax + by + c = 0, (x0, y0)) = |ax0 + by0 +c| / sqrt(a^2 + b^2)


	public LineSegmentOrbitTrap() {
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));
		initSettingsPanel();
	}

	@Override
	public Component getSettingsComponent() {
		return settingsPanel;
	}

	@Override
	public String getName() {
		return "Line segment";
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

		m = (c2.i - c1.i) / (c2.r - c1.r);
		c = c1.i - m * c1.r;
	}

	@Override
	public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
		BufferedImage im = ImageUtils.deepCopy(baseImage);
		Point p1 = fractalRenderer.getMapper().mapToImage(c1);
		Point p2 = fractalRenderer.getMapper().mapToImage(c2);
		im.getGraphics().drawLine(p1.x, p1.y, p2.x, p2.y);

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
		//project point onto line
		m2 = -1/m;
		double r = (m*c1.r-m2*c.r+c.i-c1.i) / (m-m2);
		//double i = m2*(r-c.r)+c.i;

		//check if left of, right of, or on line.
		if (r < c1.r) {
			return c1.sub(c).modulus();
		} else if (r > c2.r) {
			return c2.sub(c).modulus();
		} else {
			return Math.abs(m*c.r - c.i + this.c) / Math.sqrt(m*m + 1);
		}
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
