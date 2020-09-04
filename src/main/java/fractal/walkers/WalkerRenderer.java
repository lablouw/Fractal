/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.walkers;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.phase.coloring.PhaseColorer;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author cp316928
 */
public class WalkerRenderer extends FractalRenderer<WalkerEngine> {

    private static WalkerRenderer INSTANCE = null;
    private long lastGuiUpdate = System.currentTimeMillis();
    
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
        fractalEngine = new WalkerEngine(this);
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
    protected void render() {
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
        if (x >= 0 && x < synchronizedBufferedImage.getBufferedImage().getWidth() &&
                y >= 0 && y < synchronizedBufferedImage.getBufferedImage().getHeight()) {
            synchronizedBufferedImage.setColor(x, y, new Color((int) orbit.get(0).r));
        }
        
        if (System.currentTimeMillis() - lastGuiUpdate > 50) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit, Color color) {
    }

    @Override
    public void performSpecialClickAction(Complex clickLocation) {
    }
    
}
