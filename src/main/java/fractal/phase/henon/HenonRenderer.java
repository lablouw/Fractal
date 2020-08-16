/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase.henon;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.common.SynchronizedBufferedImage;
import fractal.main;
import fractal.phase.coloring.PhaseColorer;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author CP316928
 */
public class HenonRenderer extends FractalRenderer {

    private static HenonRenderer INSTANCE = null;
    private final HenonEngine henonEngine;
    private long lastGuiUpddate = System.currentTimeMillis();
    
    
    public static HenonRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HenonRenderer();
        }
        return INSTANCE;
    }
    
    private HenonRenderer() {
        this.fractalEngine = new HenonEngine(this);
        henonEngine = (HenonEngine) fractalEngine;
        addColorCalculator(new PhaseColorer());
        this.fractalViewer = new FractalViewer(this);
    }

    @Override
    public String getName() {
        return "Phase map renderer";
    }

    @Override
    public void mouseMoved(Complex pointOnImage) {
        if (henonEngine.isStopped()) {
            
        }
    }

    @Override
    protected JComponent getCustomSettingsComponent() {
        return null;
    }

    @Override
    protected void renderFractal() {
        henonEngine.start();
    }

    @Override
    public void stopRendering() {
        henonEngine.stop();
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
        
        if (System.currentTimeMillis() - lastGuiUpddate > main.getGuiUpdateInterval()) {
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
