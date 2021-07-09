/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.SynchronizedBufferedImage;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import fractal.common.Antialiasable;
import fractal.common.FractalViewer;
import fractal.common.ImageUtils;
import fractal.main;
import fractal.mandelbrot.coloring.AverageAngleColorCalculator;
import fractal.mandelbrot.coloring.BandColorCalculator;
import fractal.mandelbrot.coloring.BevelColorCalculator;
import fractal.mandelbrot.coloring.BuddahColorCalculator;
import fractal.mandelbrot.coloring.field.FieldColorCalculator;
import fractal.mandelbrot.coloring.OrbitTrapColorCalculator;
import fractal.mandelbrot.coloring.SmoothColorCalculator;
import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author lloyd
 */
public class JuliaRenderer extends FractalRenderer<JuliaEngine> implements Antialiasable {

    private final int numCores = Runtime.getRuntime().availableProcessors();
    private long lastGuiUpdate;
    private List<JuliaCalculatorCPU> currentCalculators = new ArrayList<>();
    private JuliaCalculatorGPU juliaCalculatorGPU;
    private int aa = 1;
    private SynchronizedBufferedImage orbitImageBase;
    private boolean orbitPreview = false;
    
    private static JuliaRenderer INSTANCE = null;
    
    public static JuliaRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JuliaRenderer();
        }
        return INSTANCE;
    }
    
    private JuliaRenderer() {
        this.fractalEngine = new JuliaEngine();
        addColorCalculator(new SmoothColorCalculator(this));
        addColorCalculator(new BandColorCalculator());
        addColorCalculator(new BuddahColorCalculator(this));
        addColorCalculator(new AverageAngleColorCalculator());
        addColorCalculator(new BevelColorCalculator());
        addColorCalculator(new OrbitTrapColorCalculator(this));
		addColorCalculator(new FieldColorCalculator());
        
        this.fractalViewer = new FractalViewer(this);
    }

    @Override
    protected void render() {
        busy = true;
        lastGuiUpdate = System.currentTimeMillis();
        if (!fractalEngine.isUseGPUFull() && !fractalEngine.isUseGPUFast()) {
            ExecutorService es = Executors.newFixedThreadPool(numCores);
            currentCalculators = new ArrayList<>();
            List<Future> futures = new ArrayList<>();
            for (int i = 0; i < numCores; i++) {
                JuliaCalculatorCPU c = new JuliaCalculatorCPU(i, this);
                currentCalculators.add(c);
                futures.add(es.submit(c));
            }
            for (Future f : futures) {
                try {
                    f.get();
                } catch (Exception ex) {
                    Logger.getLogger(JuliaRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            es.shutdown();
        } else {
            if (juliaCalculatorGPU == null) {
                juliaCalculatorGPU = new JuliaCalculatorGPU(this);
            }
            juliaCalculatorGPU.initForRender();
            ExecutorService es = Executors.newFixedThreadPool(1);
            Future future = es.submit(juliaCalculatorGPU);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MandelbrotRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            es.shutdown();
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
    public void enginePerformedCalculation(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength) {
        synchronizedBufferedImage.setColor(x, y, activeColorCalculator.calcColor(x, y, rawGpuOrbitContainer, orbitStartIndex, orbitLength, fractalEngine));

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
        for (JuliaCalculatorCPU c : currentCalculators) {
            c.stop();
        }
    }

    @Override
    public int getSubSamples() {
        return aa;
    }

    @Override
    public void setSubSamples(int aa) {
        this.aa = aa;
    }

    @Override
    public String getName() {
        return "Julia";
    }

    @Override
    public JComponent getCustomSettingsComponent() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1));

        final JCheckBox orbitPreviewCB = new JCheckBox("Real-time orbit preview");

        orbitPreviewCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                orbitPreview = orbitPreviewCB.isSelected();
            }
        });

        p.add(orbitPreviewCB);

        return p;
    }

    @Override
    public void mouseMoved(Complex pointOnImage) {
        if (orbitPreview) {
            if (orbitImageBase == null) {
                orbitImageBase = getImage();
            }
            List<Complex> orbit = getFractalEngine().calcParameterMappedOrbit(pointOnImage);
            SynchronizedBufferedImage orbitImage = new SynchronizedBufferedImage(ImageUtils.deepCopy(orbitImageBase.getBufferedImage()));
            for (Complex o : orbit) {
                Point p = getImagePlaneMapper().mapToImage(o);
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
    }

}
