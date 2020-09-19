/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring;

import fractal.common.ColorCalculator;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fractal.mandelbrot.RawGpuOrbitContainer;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

/**
 *
 * @author cp316928
 */
public class BuddahColorCalculator implements ColorCalculator {

    private long[][] hitMapRed;
    private long[][] hitMapGreen;
    private long[][] hitMapBlue;
    private float maxHitsRed;
    private float maxHitsGreen;
    private float maxHitsBlue;
    private double cutoffRed = 20;
    private double cutoffGreen = 20;
    private double cutoffBlue = 20;
    private int maxIterRed = 100;
    private int maxIterGreen = 100;
    private int maxIterBlue = 100;
    
    private final FractalRenderer fractalRenderer;
    private Object lock = new Object();
    
    public BuddahColorCalculator(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        synchronized(lock) {
            initHitMaps();

            try {
                if (orbit.size() < fractalEngine.getMaxIter()) {
                    for (int i=1; i<orbit.size(); i++) {//start at 1 because we don't want to add z0 to hitmap
                        addToHitMaps(orbit.get(i), i);
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        int col = 255 - (int)((double)orbit.size()/(double)fractalEngine.getMaxIter()*255);
        return new Color(col,col,col);
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        synchronized(lock) {
            initHitMaps();

            try {
                if (orbitLength < fractalEngine.getMaxIter()) {
                    for (int i=1; i<orbitLength; i++) {//start at 1 because we don't want to add z0 to hitmap
                        Complex c = new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]);
                        addToHitMaps(c, i);
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        int col = 255 - (int)((double)orbitLength/(double)fractalEngine.getMaxIter()*255);
        return new Color(col,col,col);
    }

    private void initHitMaps() {
        if (hitMapRed == null) {
            hitMapRed = new long[fractalRenderer.getMapper().getWidth()][fractalRenderer.getMapper().getHeight()];
        }
        if (hitMapGreen == null) {
            hitMapGreen = new long[fractalRenderer.getMapper().getWidth()][fractalRenderer.getMapper().getHeight()];
        }
        if (hitMapBlue == null) {
            hitMapBlue = new long[fractalRenderer.getMapper().getWidth()][fractalRenderer.getMapper().getHeight()];
        }
    }

    private void addToHitMaps(Complex c, int i) {
        Point p = fractalRenderer.getMapper().mapToImage(c);
        if (p.x<0 || p.y<0 || p.x>=fractalRenderer.getMapper().getWidth() || p.y>=fractalRenderer.getMapper().getHeight()) return;
        if (i <= maxIterRed){
            hitMapRed[p.x][p.y]++;
            if (hitMapRed[p.x][p.y] > maxHitsRed) maxHitsRed = hitMapRed[p.x][p.y];
        }
        if (i <= maxIterGreen){
            hitMapGreen[p.x][p.y]++;
            if (hitMapGreen[p.x][p.y] > maxHitsGreen) maxHitsGreen = hitMapGreen[p.x][p.y];
        }
        if (i <= maxIterBlue) {
            hitMapBlue[p.x][p.y]++;
            if (hitMapBlue[p.x][p.y] > maxHitsBlue) maxHitsBlue = hitMapBlue[p.x][p.y];
        }
    }

    @Override
    public Color calcColor(int x, int y, Complex lastOrbitPoint, int orbitLength, FractalEngine fractalEngine) {
        throw new UnsupportedOperationException("Not supported. Full orbit needed"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getSettingsComponent() {
        JXPanel p = new JXPanel();
        p.setLayout(new GridLayout(0, 1));
        
        //cutoff spinners
        final JSpinner cutoffSpinnerRed = new JSpinner(new SpinnerNumberModel(cutoffRed, 0, 100, 1));
        final JSpinner cutoffSpinnerGreen = new JSpinner(new SpinnerNumberModel(cutoffGreen, 0, 100, 1));
        final JSpinner cutoffSpinnerBlue = new JSpinner(new SpinnerNumberModel(cutoffBlue, 0, 100, 1));
        cutoffSpinnerRed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cutoffRed = (Double) cutoffSpinnerRed.getValue();
                complete(fractalRenderer.getImage());
                fractalRenderer.updateGui();
            }
        });
        cutoffSpinnerGreen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cutoffGreen = (Double) cutoffSpinnerGreen.getValue();
                complete(fractalRenderer.getImage());
                fractalRenderer.updateGui();
            }
        });
        cutoffSpinnerBlue.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                cutoffBlue = (Double) cutoffSpinnerBlue.getValue();
                complete(fractalRenderer.getImage());
                fractalRenderer.updateGui();
            }
        });
        
        JXPanel cutoffPanel = new JXPanel();
        cutoffPanel.setLayout(new GridLayout(0, 2));
        cutoffPanel.add(new JXLabel("R Cutoff"));
        cutoffPanel.add(cutoffSpinnerRed);
        cutoffPanel.add(new JXLabel("G Cutoff"));
        cutoffPanel.add(cutoffSpinnerGreen);
        cutoffPanel.add(new JXLabel("B Cutoff"));
        cutoffPanel.add(cutoffSpinnerBlue);
        p.add(cutoffPanel);
        
        //Maxiter Spinenrs
        final JSpinner maxIterSpinnerRed = new JSpinner(new SpinnerNumberModel(maxIterRed, 0, Integer.MAX_VALUE, 1));
        final JSpinner maxIterSpinnerGreen = new JSpinner(new SpinnerNumberModel(maxIterGreen, 0, Integer.MAX_VALUE, 1));
        final JSpinner maxIterSpinnerBlue = new JSpinner(new SpinnerNumberModel(maxIterBlue, 0, Integer.MAX_VALUE, 1));
        maxIterSpinnerRed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxIterRed = (Integer) maxIterSpinnerRed.getValue();
//                complete(fractalRenderer.getImage());
//                fractalRenderer.updateGui();
            }
        });
        maxIterSpinnerGreen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxIterGreen = (Integer) maxIterSpinnerGreen.getValue();
//                complete(fractalRenderer.getImage());
//                fractalRenderer.updateGui();
            }
        });
        maxIterSpinnerBlue.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxIterBlue = (Integer) maxIterSpinnerBlue.getValue();
//                complete(fractalRenderer.getImage());
//                fractalRenderer.updateGui();
            }
        });
        
        JXPanel maxIterPanel = new JXPanel();
        maxIterPanel.setLayout(new GridLayout(0, 2));
        maxIterPanel.add(new JXLabel("R maxIter"));
        maxIterPanel.add(maxIterSpinnerRed);
        maxIterPanel.add(new JXLabel("G maxIter"));
        maxIterPanel.add(maxIterSpinnerGreen);
        maxIterPanel.add(new JXLabel("B maxIter"));
        maxIterPanel.add(maxIterSpinnerBlue);
        p.add(maxIterPanel);
        
        return p;
    }
    
    @Override
    public String getName() {
        return "Buddah";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
        maxHitsRed = 0;
        maxHitsGreen = 0;
        maxHitsBlue = 0;
        hitMapRed = null;
        hitMapGreen = null;
        hitMapBlue = null;
    }

    @Override
    public void complete(SynchronizedBufferedImage synchronizedBufferedImage) {
        double maxHitLimitRed = maxHitsRed/100d*cutoffRed;
        double maxHitLimitGreen = maxHitsGreen/100d*cutoffGreen;
        double maxHitLimitBlue = maxHitsBlue/100d*cutoffBlue;
        for (int x=0; x<synchronizedBufferedImage.getBufferedImage().getWidth(); x++) {
            for (int y=0; y<synchronizedBufferedImage.getBufferedImage().getHeight(); y++) {
                int red, green, blue;
                if (hitMapRed[x][y] > maxHitLimitRed) red = 255;
                else red = (int) (hitMapRed[x][y]/maxHitLimitRed*255);
                if (hitMapGreen[x][y] > maxHitLimitGreen) green = 255;
                else green = (int) (hitMapGreen[x][y]/maxHitLimitGreen*255);
                if (hitMapBlue[x][y] > maxHitLimitBlue) blue = 255;
                else blue = (int) (hitMapBlue[x][y]/maxHitLimitBlue*255);
                
                synchronizedBufferedImage.setColor(x, y, new Color(red, green, blue));
            }
        }
    }

}
