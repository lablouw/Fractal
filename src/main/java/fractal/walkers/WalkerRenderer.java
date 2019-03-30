/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.walkers;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.common.SynchronizedBufferedImage;
import fractal.phase.coloring.PhaseColorer;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author cp316928
 */
public class WalkerRenderer extends FractalRenderer {

    private static WalkerRenderer INSTANCE = null;
    private final WalkerEngine walkerEngine;
    private long lastGuiUpddate = System.currentTimeMillis();
    
    @Override
    public String getName() {
        return "Walkers";
    }
    
    public static WalkerRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WalkerRenderer();
        }
        return INSTANCE;
    }
    
    private WalkerRenderer() {
        this.fractalEngine = new WalkerEngine(this);
        walkerEngine = (WalkerEngine) fractalEngine;
        addColorCalculator(new PhaseColorer());
        this.fractalViewer = new FractalViewer(this);
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
        walkerEngine.start();
    }

    @Override
    public void stopRendering() {
        walkerEngine.stop();
    }

    @Override
    public void enginePerformedCalculation(List<Point> points, List<List<Complex>> orbits) {
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit) {
        if (x >= 0 && x < synchronizedBufferedImage.getBufferedImage().getWidth() &&
                y >= 0 && y < synchronizedBufferedImage.getBufferedImage().getHeight()) {
            synchronizedBufferedImage.setColor(x, y, new Color((int) orbit.get(0).r));
        }
        
        if (System.currentTimeMillis() - lastGuiUpddate > 50) {
            updateGui();
            lastGuiUpddate = System.currentTimeMillis();
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
