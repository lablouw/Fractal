package fractal.mandelbrot.coloring.orbittrap.infiniteline;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Redrawable;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import java.awt.*;
import java.util.List;

public class InfiniteLineOrbitTrapDistanceColorStrategy implements OrbitTrapColorStrategy<InfiniteLineOrbitTrap>, Redrawable {

	private final InfiniteLineOrbitTrapDistanceColorStrategySettingsPanel settingsPanel = new InfiniteLineOrbitTrapDistanceColorStrategySettingsPanel(this);

	private final FractalRenderer fractalRenderer;
	private final InfiniteLineOrbitTrap orbitTrap;

	private double [][] minDists;

	public InfiniteLineOrbitTrapDistanceColorStrategy(FractalRenderer fractalRenderer, InfiniteLineOrbitTrap orbitTrap) {
		this.fractalRenderer = fractalRenderer;
		this.orbitTrap = orbitTrap;
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
		for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			double d = orbitTrap.distanceFrom(orbit.get(i));
			if (d < minDist) {
				minDist = d;
			}
		}

		minDists[x][y] = minDist;
		return minDist == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDist) * LOGARITHM_SUPPRESSION, true);
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
		return minDist == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDist) * LOGARITHM_SUPPRESSION, true);
    }

	@Override
	public Color recalcColor(int x, int y) {
		return minDists[x][y] == 0 ? Color.BLACK : settingsPanel.getColorPalette().interpolateToColor(-Math.log(minDists[x][y]) * LOGARITHM_SUPPRESSION, true);
	}

	@Override
	public Component getSettingsComponent() {
		return settingsPanel;
	}

    @Override
	public void redraw() {
		for (int x = 0; x < fractalRenderer.getImage().getBufferedImage().getWidth(); x++) {
			for (int y = 0; y < fractalRenderer.getImage().getBufferedImage().getHeight(); y++) {
				fractalRenderer.getImage().setColor(x, y, orbitTrap.reCalcColor(x, y));
			}
		}
		fractalRenderer.updateGui();
	}
}
