package fractal.mandelbrot.coloring.orbittrap.linesegment;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Redrawable;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import java.awt.*;
import java.util.List;

public class LineSegmentOrbitTrapOrthogonalProjectionColorStrategy implements OrbitTrapColorStrategy<LineSegmentOrbitTrap>, Redrawable {

	private final LineSegmentOrbitTrapOrthogonalProjectionColorStrategySettingsPanel settingsPanel = new LineSegmentOrbitTrapOrthogonalProjectionColorStrategySettingsPanel(this);

	private final FractalRenderer fractalRenderer;
	private final LineSegmentOrbitTrap orbitTrap;

	private double [][] projectedPositionsOfNearest;

	public LineSegmentOrbitTrapOrthogonalProjectionColorStrategy(FractalRenderer fractalRenderer, LineSegmentOrbitTrap orbitTrap) {
		this.fractalRenderer = fractalRenderer;
		this.orbitTrap = orbitTrap;
	}

	@Override
	public String getName() {
		return "Orthogonal Projection";
	}

	@Override
	public void initForRender() {
		int imageWidth = fractalRenderer.getImage().getBufferedImage().getWidth();
		int imageHeight = fractalRenderer.getImage().getBufferedImage().getHeight();
		projectedPositionsOfNearest = new double[imageWidth][imageHeight];
	}

	@Override
	public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, LineSegmentOrbitTrap orbitTrap) {
		double projectedPositionOfNearest = Double.MAX_VALUE;
		double minDist = Double.MAX_VALUE;
		for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			double d = orbitTrap.distanceFrom(orbit.get(i));
			if (d < minDist) {
				minDist = d;
				projectedPositionOfNearest = orbitTrap.getProjectedPosition(orbit.get(i));
			}
		}

		projectedPositionsOfNearest[x][y] = projectedPositionOfNearest;
		if (projectedPositionOfNearest == Double.MAX_VALUE) {
			return Color.BLACK;
		}
		return settingsPanel.getColorPalette().interpolateToColor(projectedPositionOfNearest, false);
	}

	@Override
	public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, LineSegmentOrbitTrap orbitTrap) {
		double projectedPositionOfNearest = Double.MAX_VALUE;
		double minDist = Double.MAX_VALUE;
		for (int i = 1; i < orbitLength; i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
			Complex c = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]);
			double d = orbitTrap.distanceFrom(c);
			if (d < minDist) {
				minDist = d;
				projectedPositionOfNearest = orbitTrap.getProjectedPosition(c);
			}
		}
		projectedPositionsOfNearest[x][y] = projectedPositionOfNearest;

		if (projectedPositionOfNearest == Double.MAX_VALUE) {
			return Color.BLACK;
		}
		return settingsPanel.getColorPalette().interpolateToColor(projectedPositionOfNearest, false);
	}

	@Override
	public Color recalcColor(int x, int y) {
		if (projectedPositionsOfNearest[x][y] == Double.MAX_VALUE) {
			return Color.BLACK;
		}
		return settingsPanel.getColorPalette().interpolateToColor(projectedPositionsOfNearest[x][y], false);
	}

	@Override
	public Component getSettingsComponent() {
		return null;
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
