package fractal.mandelbrot.coloring.field;

import fractal.common.ColorCalculator;
import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Redrawable;
import fractal.common.SynchronizedBufferedImage;
import fractal.mandelbrot.JuliaEngine;
import fractal.mandelbrot.MandelbrotEngine;
import fractal.mandelbrot.RawGpuOrbitContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// See https://commons.wikimedia.org/wiki/File:Stripe_Average_Coloring_-_Mandelbrot_set_zoom_(_wake_1over3_).png
public class FieldColorCalculator implements ColorCalculator, Redrawable {

	private final ColorPalette colorPalette;
	private JPanel settingsPanel;
//	private FieldColorCalculatorSettingsDialog fieldColorCalculatorSettingsDialog;

	private int stripeDensity = 7;
	private double param1 = 2;
	private double param2 = 1;

	private int skip = 2;
	private double ln2 = Math.log(2);
	private double escapeRad;

	public FieldColorCalculator() {
		colorPalette = new ColorPalette(null, false, this);
		initSettingsPanel();
	}

	@Override
	public JComponent getSettingsComponent() {
		return settingsPanel;
	}

	@Override
	public String getName() {
		return "Field lines";
	}

	@Override
	public void initForRender(FractalRenderer fractalRenderer) {
		FractalEngine fractalEngine = fractalRenderer.getFractalEngine();
		if (fractalEngine instanceof MandelbrotEngine) {
			escapeRad = ((MandelbrotEngine)fractalEngine).getBailout();
		} else if (fractalEngine instanceof JuliaEngine) {
			escapeRad = ((JuliaEngine)fractalEngine).getBailout();
		}
	}

	@Override
	public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
	}

	@Override
	public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
		if (!fractalEngine.isBailoutReached(orbit) || orbit.size() < 2) {
			return Color.BLACK;
		}

		int orbitLength = orbit.size();
		int orbitLengthMin2 = orbitLength-2;
		double a = 0;
		double prevA = 0;
		for (int i=1; i<orbitLength; i++) {
			if (i > skip) {
				a += giveT(orbit.get(i));
			}
			if (i == orbitLengthMin2) {
				prevA = a;
			}
		}


		double finalOrbitModulus = orbit.get(orbit.size()-1).modulus();
		a /= orbit.size() - skip;
		prevA /= orbit.size() - skip - 1;

		double d = orbit.size() + 1 + Math.log(Math.log(escapeRad)/Math.log(finalOrbitModulus)) / ln2;
		d = d - (int)d;//decimal part

		a = d*a + (1-d)*prevA;

		return colorPalette.interpolateToColor(a, true);

	}

	private double giveT(Complex z) {
		return param1 + param2*Math.sin(stripeDensity*z.arg());
	}

	@Override
	public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength,
						   FractalEngine fractalEngine) {
		int lastOrbitPointIndex = orbitStartIndex + orbitLength - 1;
		Complex lastOrbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[lastOrbitPointIndex], rawGpuOrbitContainer.orbitsI[lastOrbitPointIndex]);
		if (!fractalEngine.isBailoutReachedByLastOrbitPoint(lastOrbitPoint) || orbitLength < 2) {
			return Color.BLACK;
		}

		int orbitLengthMin2 = orbitLength-2;
		double a = 0;
		double prevA = 0;
		for (int i=1; i < orbitLength; i++) {
			Complex orbitPoint = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]);
			if (i > skip) {
				a += giveT(orbitPoint);
			}
			if (i == orbitLengthMin2) {
				prevA = a;
			}
		}


		double finalOrbitModulus = lastOrbitPoint.modulus();
		a /= orbitLength - skip;
		prevA /= orbitLength - skip - 1;

		double d = orbitLength + 1 + Math.log(Math.log(escapeRad)/Math.log(finalOrbitModulus)) / ln2;
		d = d - (int)d;//decimal part

		a = d*a + (1-d)*prevA;

		return colorPalette.interpolateToColor(a, true);
	}

	@Override
	public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
		throw new UnsupportedOperationException("Not supported. Full orbit needed");
	}

	@Override
	public void redraw() {

	}

	private void initSettingsPanel() {
		settingsPanel = new JPanel(new GridLayout(0, 1));

		JButton orbitTrapSettingsButton = new JButton("Color Settings");
		orbitTrapSettingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				fieldColorCalculatorSettingsDialog.setVisible(true);
			}
		});
		settingsPanel.add(orbitTrapSettingsButton);

	}
}
