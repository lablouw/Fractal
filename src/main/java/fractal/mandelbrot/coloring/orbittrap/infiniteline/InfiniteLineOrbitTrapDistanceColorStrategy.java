package fractal.mandelbrot.coloring.orbittrap.infiniteline;

import fractal.common.ColorPalette;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class InfiniteLineOrbitTrapDistanceColorStrategy implements OrbitTrapColorStrategy<InfiniteLineOrbitTrap> {

	private JPanel settingsPanel;
	private final ColorPalette colorPalette;

	private final FractalRenderer fractalRenderer;
	private final InfiniteLineOrbitTrap orbitTrap;

	private double [][] minDists;

	public InfiniteLineOrbitTrapDistanceColorStrategy(FractalRenderer fractalRenderer, InfiniteLineOrbitTrap orbitTrap) {
		this.fractalRenderer = fractalRenderer;
		this.orbitTrap = orbitTrap;
        colorPalette = new ColorPalette(null, false);
		initSettingsPanel();
	}

	@Override
	public String getName() {
		return "Distance to line";
	}

	@Override
	public void initForRender() {
		int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
		int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
		minDists = new double[imageWidth][imageHeight];
	}

	@Override
	public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, InfiniteLineOrbitTrap orbitTrap) {
		double minDist = Double.MAX_VALUE;
		for (Complex c : orbit) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			double d = orbitTrap.distanceFrom(c);
			if (d < minDist) {
				minDist = d;
			}
		}

		minDists[x][y] = minDist;
		return minDist == 0 ? Color.BLACK : colorPalette.interpolateToColor((float) -Math.log(minDist) * LOGARITHM_SUPPRESSION);
	}
    
    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, InfiniteLineOrbitTrap orbitTrap) {
        double minDist = Double.MAX_VALUE;
		for (int i = 1; i < orbitLength; i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			double d = orbitTrap.distanceFrom(new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]));
			if (d < minDist) {
				minDist = d;
			}
		}

		minDists[x][y] = minDist;
		return minDist == 0 ? Color.BLACK : colorPalette.interpolateToColor((float) -Math.log(minDist) * LOGARITHM_SUPPRESSION);
    }

	@Override
	public Color recalcColor(int x, int y) {
		return minDists[x][y] == 0 ? Color.BLACK : colorPalette.interpolateToColor((float) -Math.log(minDists[x][y])*LOGARITHM_SUPPRESSION);
	}

	@Override
	public Component getSettingsComponent() {
		return settingsPanel;
	}

	private void initSettingsPanel() {
		settingsPanel = new JPanel(new GridLayout(0, 1));
		settingsPanel.add(colorPalette.getRepresentitivePanel());

//		settingsPanel.add(new JLabel("Spectrum compression"));
//		compressionSlider.setValue(1);
//		compressionSlider.addMouseListener(new MouseListener() {
//			public void mouseClicked(MouseEvent e) {}
//			public void mousePressed(MouseEvent e) {}
//			public void mouseEntered(MouseEvent e) {}
//			public void mouseExited(MouseEvent e) {}
//			public void mouseReleased(MouseEvent e) {
//				spectrumComp = (float) ((float) (double) compressionSlider.getValue() / 500d);
//				redraw();
//			}
//		});
//		settingsPanel.add(compressionSlider);
	}

	private void redraw() {
		for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
			for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
				fractalRenderer.getImage().setColor(x, y, orbitTrap.reCalcColor(x, y));
			}
		}
		fractalRenderer.updateGui();
	}
}
