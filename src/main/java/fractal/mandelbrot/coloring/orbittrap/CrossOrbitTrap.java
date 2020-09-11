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

public class CrossOrbitTrap extends OrbitTrap {

	private static final double EPSILON = 1E-15;
	private JPanel settingsPanel;
	private final JSlider compressionSlider = new JSlider(1, 100);
	private final ColorPalette colorPalette = new ColorPalette();
	private float spectrumComp = 0.05f;

	private double [][] minDists;
	private double m1, c1; //y=mx+c
	private double m2, c2; //y=mx+c
	private Complex d1;
	private Complex d2;
	private boolean vertical = false;

	public CrossOrbitTrap() {
		setDefiningPoints(new Complex(0, 0), new Complex(1, 0));
		initSettingsPanel();
	}

	@Override
	public String getName() {
		return "Cross";
	}

	@Override
	public void init(FractalRenderer fractalRenderer) {
		int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
		int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
		minDists = new double[imageWidth][imageHeight];
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
	public Component getSettingsComponent() {
		return settingsPanel;
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

	private void redraw() {
		for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
			for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
				fractalRenderer.getImage().setColor(x, y, reCalcColor(x, y));
			}
		}
		fractalRenderer.updateGui();
	}

	private double distanceFrom(Complex c) {
		if (vertical) {
			return Math.min(Math.abs(c.r-d1.r), Math.abs(c.i - d1.i));
		}

		return Math.min(
				Math.abs(m1*c.r - c.i + c1) / Math.sqrt(m1*m1 + 1),
				Math.abs(m2*c.r - c.i + c2) / Math.sqrt(m2*m2 + 1)
		);
	}
}
