/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Antialiasable;
import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.common.ImageUtils;
import fractal.common.SynchronizedBufferedImage;
import fractal.main;
import fractal.mandelbrot.coloring.AverageAngleColorCalculator;
import fractal.mandelbrot.coloring.BandColorCalculator;
import fractal.mandelbrot.coloring.BevelColorCalculator;
import fractal.mandelbrot.coloring.BuddahColorCalculator;
import fractal.mandelbrot.coloring.OrbitTrapColorCalculator;
import fractal.mandelbrot.coloring.SmoothColorCalculatorMandelbrot;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lloyd
 */
public class MandelbrotRenderer extends FractalRenderer<MandelbrotEngine> implements Antialiasable {

    //Used for rightClick actions
    private static final String PERTURBATION = "Perturbation";
    private static final String JULIA = "Julia";

    private String specialAction = JULIA;

    private long lastGuiUpdate;

    private final int numCores = Runtime.getRuntime().availableProcessors();
    private List<MandelbrotCalculatorCPU> mandelbrotCalculatorsCPU = new ArrayList<>();
    private MandelbrotCalculatorGPU4 mandelbrotCalculatorGPU4 = null;

    private int aa = 1;
    private boolean realtimeJuliaPreview = false;
    private boolean realtimePerterbPreview = false;
    private boolean orbitPreview = false;
    private SynchronizedBufferedImage orbitImageBase;

    private static MandelbrotRenderer MAIN_INSTANCE = null;
    
    
    private FractalViewer juliaPreviewViewer;
    private JuliaRenderer juliaPreviewRenderer;
    
    private FractalViewer mandelbrotPreviewViewer;
    private MandelbrotRenderer mandelbrotPreviewRenderer = null;
    
    public static MandelbrotRenderer getInstance() {
        if (MAIN_INSTANCE == null) {
            MAIN_INSTANCE = new MandelbrotRenderer();
            MAIN_INSTANCE.createPreviewInstances();
        }
        return MAIN_INSTANCE;
    }
    
    private void createPreviewInstances() {
        mandelbrotPreviewRenderer = new MandelbrotRenderer();
        mandelbrotPreviewViewer = mandelbrotPreviewRenderer.getFractalViewer();
        juliaPreviewRenderer = JuliaRenderer.getInstance();
        juliaPreviewViewer = juliaPreviewRenderer.getFractalViewer();
    }
    
    private MandelbrotRenderer() {
        this.fractalEngine = new MandelbrotEngine();
        addColorCalculator(new SmoothColorCalculatorMandelbrot(this));
        addColorCalculator(new BandColorCalculator());
        addColorCalculator(new BuddahColorCalculator(this));
        addColorCalculator(new AverageAngleColorCalculator());
        addColorCalculator(new BevelColorCalculator());
        addColorCalculator(new OrbitTrapColorCalculator(this));
        
        this.fractalViewer = new FractalViewer(this);
    }

    @Override
    protected void render() {
        busy = true;
        lastGuiUpdate = System.currentTimeMillis();
        if (!fractalEngine.isUseGPUFull() && !fractalEngine.isUseGPUFast()) {
            ExecutorService es = Executors.newFixedThreadPool(numCores);
            mandelbrotCalculatorsCPU = new ArrayList<>();
            List<Future> futures = new ArrayList<>();
            for (int coreIndex = 0; coreIndex < numCores; coreIndex++) {
                MandelbrotCalculatorCPU c = new MandelbrotCalculatorCPU(coreIndex, this);
                mandelbrotCalculatorsCPU.add(c);
                futures.add(es.submit(c));
            }
            for (Future f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            es.shutdown();
        } else {
//            MandelbrotCalculatorGPU mandelbrotCalculatorGPU = new MandelbrotCalculatorGPU(this);
//            
//            ExecutorService es = Executors.newFixedThreadPool(1);
//            for (int roundNumber = 0; roundNumber < mandelbrotCalculatorGPU.getTotalRounds(); roundNumber++) {
//                mandelbrotCalculatorGPU.setRoundNumber(roundNumber);
//                Future future = es.submit(mandelbrotCalculatorGPU);
//                try {
//                    future.get();
//                } catch (InterruptedException | ExecutionException ex) {
//                    Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            es.shutdown();
//            System.gc();

//            if (mandelbrotCalculatorGPU2 == null) {
//                mandelbrotCalculatorGPU2 = new MandelbrotCalculatorGPU2(this);
//            }
//            ExecutorService es = Executors.newFixedThreadPool(1);
//            Future future = es.submit(mandelbrotCalculatorGPU2);
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException ex) {
//                Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            es.shutdown();
//            System.gc();
            
//            mandelbrotCalculatorGPU3 = new MandelbrotCalculatorGPU3(this);
//            ExecutorService es = Executors.newFixedThreadPool(1);
//            Future future = es.submit(mandelbrotCalculatorGPU3);
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException ex) {
//                Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            es.shutdown();
//            System.gc();
            
            if (mandelbrotCalculatorGPU4 == null) {
                mandelbrotCalculatorGPU4 = new MandelbrotCalculatorGPU4(this);
            }
            mandelbrotCalculatorGPU4.init();
            ExecutorService es = Executors.newFixedThreadPool(1);
            Future future = es.submit(mandelbrotCalculatorGPU4);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            es.shutdown();
//            System.gc();
            
        }
        lastRenderTime = System.currentTimeMillis() - t0;
        busy = false;
    }

    @Override
    public void enginePerformedCalculation(List<Point> points, List<List<Complex>> orbits) {
        for (int i = 0; i < points.size(); i++) {
            synchronizedBufferedImage.setColor(points.get(i).x, points.get(i).y, activeColorCalculator.calcColor(points.get(i).x, points.get(i).y, orbits.get(i), fractalEngine));
        }

        if (System.currentTimeMillis() - lastGuiUpdate > main.getGuiUpdateInterval()) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit) {
        synchronizedBufferedImage.setColor(x, y, activeColorCalculator.calcColor(x, y, orbit, fractalEngine));

        if (System.currentTimeMillis() - lastGuiUpdate > main.getGuiUpdateInterval()) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit, Color color) {
        synchronizedBufferedImage.setColor(x, y, color);

        if (System.currentTimeMillis() - lastGuiUpdate > main.getGuiUpdateInterval()) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }
    
    void enginePerformedCalculation(int x, int y, Complex lastOrbitPoint, int orbitLength) {
        synchronizedBufferedImage.setColor(x, y, activeColorCalculator.calcColor(x, y, lastOrbitPoint, orbitLength, fractalEngine));

        if (System.currentTimeMillis() - lastGuiUpdate > main.getGuiUpdateInterval()) {
            updateGui();
            lastGuiUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void stopRendering() {
        for (MandelbrotCalculatorCPU c : mandelbrotCalculatorsCPU) {
            c.stop();
        }
        if (mandelbrotCalculatorGPU4 != null) {
            mandelbrotCalculatorGPU4.stop();
        }
    }

    @Override
    public int getAA() {
        return aa;
    }

    @Override
    public void setAA(int aa) {
        this.aa = aa;
    }

    @Override
    public String getName() {
        return "Mandelbrot";
    }

    @Override
    public JComponent getCustomSettingsComponent() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1));

        JXPanel p2 = new JXPanel();
        p2.setLayout(new GridLayout(1, 2));
        p2.add(new JXLabel("Middle click:"));
        JComboBox<String> middleClickCombobox = new JComboBox<>();
        middleClickCombobox.addItem(JULIA);
        middleClickCombobox.addItem(PERTURBATION);
        middleClickCombobox.addItemListener(new MiddleClickComboboxItemListener());
        p2.add(middleClickCombobox);

        p.add(p2);

        final JCheckBox juliaPreviewCB = new JCheckBox("Real-time Julia preview");
        final JCheckBox perterbationPreviewCB = new JCheckBox("Real-time perterbation preview");
        final JCheckBox orbitPreviewCB = new JCheckBox("Real-time orbit preview");
        juliaPreviewCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                realtimeJuliaPreview = juliaPreviewCB.isSelected();
                if (realtimeJuliaPreview) {
                    perterbationPreviewCB.setSelected(false);
                    orbitPreviewCB.setSelected(false);
                }
            }
        });
        perterbationPreviewCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                realtimePerterbPreview = perterbationPreviewCB.isSelected();
                if (realtimePerterbPreview) {
                    juliaPreviewCB.setSelected(false);
                    orbitPreviewCB.setSelected(false);
                }
            }
        });
        orbitPreviewCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                orbitPreview = orbitPreviewCB.isSelected();
                if (orbitPreview) {
                    juliaPreviewCB.setSelected(false);
                    perterbationPreviewCB.setSelected(false);
                }
            }
        });
        p.add(juliaPreviewCB);
        p.add(perterbationPreviewCB);
        p.add(orbitPreviewCB);

        return p;
    }

    @Override
    public void mouseMoved(Complex pointOnImage) {
        if (realtimeJuliaPreview) {
            if (!juliaPreviewViewer.isVisible()) {
                juliaPreviewViewer.setVisible(true);
            }

            if (!juliaPreviewRenderer.isBusy()) {
                juliaPreviewRenderer.getFractalEngine().setC(pointOnImage);
                juliaPreviewViewer.drawNow();
            }
        }

        if (realtimePerterbPreview) {
            if (!mandelbrotPreviewViewer.isVisible()) {
                mandelbrotPreviewViewer.setVisible(true);
            }
            
            if (!mandelbrotPreviewRenderer.isBusy()) {
                mandelbrotPreviewRenderer.getFractalEngine().setPerterbation(pointOnImage);
                mandelbrotPreviewViewer.drawNow();
            }
        }

        if (orbitPreview) {
            if (orbitImageBase == null) {
                orbitImageBase = getImage();
            }
            List<Complex> orbit = getFractalEngine().calcOrbit(pointOnImage);
            SynchronizedBufferedImage orbitImage = new SynchronizedBufferedImage(ImageUtils.deepCopy(orbitImageBase.getBufferedImage()));
            for (Complex o : orbit) {
                Point p = getMapper().mapToImage(o);
                if (p.x >= 0 && p.x < orbitImage.getBufferedImage().getWidth()
                        && p.y >= 0 && p.y < orbitImage.getBufferedImage().getHeight()) {
                    orbitImage.setColor(p.x, p.y, Color.WHITE);
                }
                updateGui(orbitImage);
            }
        } else {
            orbitImageBase = null;
        }
    }

    @Override
    public void performSpecialClickAction(Complex clickLocation) {
        if (JULIA.equals(specialAction)) {
            juliaPreviewRenderer.getFractalEngine().setC(clickLocation);
            juliaPreviewRenderer.getFractalViewer().setVisible(true);
            juliaPreviewRenderer.render(imageWidth, imageHeight, juliaPreviewRenderer.getFractalEngine().getDefaultView().getFirst(), juliaPreviewRenderer.getFractalEngine().getDefaultView().getSecond());
        } else if (PERTURBATION.equals(specialAction)) {
            fractalEngine.setPerterbation(clickLocation);
            stopRendering();
            render();
        }
    }


    private class MiddleClickComboboxItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            specialAction = (String) e.getItem();
        }

    }

}
