/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase.dejong;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.common.SynchronizedBufferedImage;
import fractal.main;
import fractal.phase.coloring.PhaseColorer;
import fractal.phase.coloring.WhiteColorer;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Lloyd
 */
public class DeJongRenderer extends FractalRenderer<DeJongEngine> {

    private static DeJongRenderer INSTANCE = null;
    private long lastGuiUpdate = System.currentTimeMillis();

    public static DeJongRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeJongRenderer();
        }
        return INSTANCE;
    }

    private DeJongRenderer() {
        fractalEngine = new DeJongEngine(this);
        addColorCalculator(new WhiteColorer());
        addColorCalculator(new PhaseColorer());
        this.fractalViewer = new FractalViewer(this);
    }

    @Override
    public String getName() {
        return "DeJong renderer";
    }

    @Override
    public void mouseMoved(Complex pointOnImage) {
    }

    @Override
    protected JComponent getCustomSettingsComponent() {
        return null;
    }

    @Override
    protected void renderFractal() {
        fractalEngine.start();
    }

    @Override
    public void stopRendering() {
        fractalEngine.stop();
    }

    @Override
    public void enginePerformedCalculation(List<Point> points, List<List<Complex>> orbits) {
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit) {

        if (x >= 0 && x < synchronizedBufferedImage.getBufferedImage().getWidth()
                && y >= 0 && y < synchronizedBufferedImage.getBufferedImage().getHeight()) {
            synchronizedBufferedImage.setColor(x, y, new Color((int) orbit.get(0).r));
        }

        if (System.currentTimeMillis() - lastGuiUpdate > main.getGuiUpdateInterval()) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit, Color color) {
    }

    @Override
    public void engineCompleted(SynchronizedBufferedImage image) {
    }

    @Override
    public void performSpecialClickAction(Complex clickLocation) {
    }

}
