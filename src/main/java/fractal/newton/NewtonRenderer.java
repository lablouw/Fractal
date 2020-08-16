/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.newton;

import fractal.common.Antialiasable;
import fractal.common.Complex;
import fractal.common.FractalRenderer;
import fractal.common.FractalViewer;
import fractal.common.SynchronizedBufferedImage;
import fractal.common.ImageUtils;
import fractal.main;
import fractal.newton.coloring.BasinsOfAttractionColorCalulator;
import fractal.newton.coloring.NewtonIterationsColorCalculator;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXLabel;

/**
 *
 * @author cp316928
 */
public class NewtonRenderer extends FractalRenderer implements Antialiasable {

    private final int numCores = Runtime.getRuntime().availableProcessors();
    private ExecutorService es;
    private Complex[][] orbitEndPoints;
    private int[][] orbitLengths;
    private List<NewtonCalculator> currentCalculators = new ArrayList<>();
    private int aa = 1;
    private SynchronizedBufferedImage orbitImageBase;
    private boolean orbitPreview = false;
    private long lastGuiUpddate;
    private String fx = "x^3+1";
    private String dfdx = "3*x^2";
    
    private static NewtonRenderer INSTANCE = null;
    
    public static NewtonRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NewtonRenderer();
        }
        return INSTANCE;
    }
    
    private NewtonRenderer() {
        this.fractalEngine = new NewtonEngine();
        addColorCalculator(new NewtonIterationsColorCalculator(this));
        addColorCalculator(new BasinsOfAttractionColorCalulator());
        
        this.fractalViewer = new FractalViewer(this);
    }
    
    @Override
    public String getName() {
        return "Newton";
    }

    @Override
    public void mouseMoved(Complex pointOnImage) {
        if (orbitPreview)
        {
            if (orbitImageBase==null) orbitImageBase = getImage();
            List<Complex> orbit = getFractalEngine().calcOrbit(pointOnImage);
            SynchronizedBufferedImage orbitImage = new SynchronizedBufferedImage(ImageUtils.deepCopy(orbitImageBase.getBufferedImage()));
            for (Complex o : orbit)
            {
                Point p = getMapper().mapToImage(o);
                if (p.x>=0 && p.x<orbitImage.getBufferedImage().getWidth() &&
                    p.y>=0 && p.y<orbitImage.getBufferedImage().getHeight()) {
                    orbitImage.setColor(p.x, p.y, Color.WHITE);
                }
                updateGui(orbitImage);
            }
        }
        else
        {
            orbitImageBase = null;
        }
    }

    @Override
    public JComponent getCustomSettingsComponent() {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0,1));
        
        JXLabel fxLabel = new JXLabel("f(x)");
        JXLabel dfdxLabel = new JXLabel("dfdx(x)");
        final JTextField fxTextField = new JTextField(fx);
        final JTextField dfdxTextField = new JTextField(dfdx);
        
        p.add(fxLabel);
        p.add(fxTextField);
        p.add(dfdxLabel);
        p.add(dfdxTextField);
        
        fxTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {
                fx = fxTextField.getText();
            }
        });
        
        dfdxTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {
                dfdx = dfdxTextField.getText();
            }
        });
        
        final JCheckBox orbitPreviewCB = new JCheckBox("Real-time orbit preview");
        orbitPreviewCB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                orbitPreviewCB.setSelected(orbitPreviewCB.isSelected());
                orbitPreview = orbitPreviewCB.isSelected();
            }
        });
        p.add(orbitPreviewCB);
        
        return p;
    }
    
//    @Override
//    public void init() {
//        synchronizedBufferedImage = new SynchronizedBufferedImage(imageWidth, imageHeight);
//    }

    @Override
    protected void renderFractal() {
        busy = true;
        FunctionParser functionParser = new FunctionParser();
        TreeNode fxRoot, dfdxRoot;
        try {
            fxRoot = functionParser.buildTree(fx);
            dfdxRoot = functionParser.buildTree(dfdx);
            ((NewtonEngine)fractalEngine).setFxRootNode(fxRoot);
            ((NewtonEngine)fractalEngine).setDfdxRootNode(dfdxRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        SynchronizedBufferedImage synchronizedBI = new SynchronizedBufferedImage(imageWidth, imageHeight);
        orbitEndPoints = new Complex[imageWidth][imageHeight];
        orbitLengths = new int[imageWidth][imageHeight];
        currentCalculators = new ArrayList<>();
        es = Executors.newFixedThreadPool(numCores);
        List<Future> futures = new ArrayList<>();
        for (int coreIndex=0; coreIndex<numCores; coreIndex++)
        {
            NewtonCalculator c = new NewtonCalculator(coreIndex, synchronizedBI);
            currentCalculators.add(c);
            futures.add(es.submit(c));
        }
        for (Future f : futures)
        {
            try
            {
                f.get();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        es.shutdown();
        lastRenderTime = System.currentTimeMillis()-t0;
        busy = false;
    }

    @Override
    public void stopRendering() {
        for (NewtonCalculator c : currentCalculators) c.stop();
    }

    @Override
    public void enginePerformedCalculation(List<Point> points, List<List<Complex>> orbits) {
        for (int i=0; i<points.size(); i++)
        {
            List<Complex> orbit = orbits.get(i);
            synchronizedBufferedImage.setColor(points.get(i).x, points.get(i).y, activeColorCalculator.calcColor(points.get(i).x, points.get(i).y, orbits.get(i), fractalEngine));
            orbitEndPoints[points.get(i).x][points.get(i).y] = orbit.get(orbit.size()-1);
            orbitLengths[points.get(i).x][points.get(i).y] = orbit.size();
        }
        
        if (System.currentTimeMillis()-lastGuiUpddate > main.getGuiUpdateInterval())
        {
            updateGui();
            lastGuiUpddate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit) {
        synchronizedBufferedImage.setColor(x, y, activeColorCalculator.calcColor(x, y, orbit, fractalEngine));
        orbitEndPoints[x][y] = orbit.get(orbit.size()-1);
        orbitLengths[x][y] = orbit.size();
        
        if (System.currentTimeMillis()-lastGuiUpddate > main.getGuiUpdateInterval())
        {
            updateGui();
            lastGuiUpddate = System.currentTimeMillis();
        }
    }

    @Override
    public void enginePerformedCalculation(int x, int y, List<Complex> orbit, Color color) {
        synchronizedBufferedImage.setColor(x, y, color);
        orbitEndPoints[x][y] = orbit.get(orbit.size()-1);
        orbitLengths[x][y] = orbit.size();
        
        if (System.currentTimeMillis()-lastGuiUpddate > main.getGuiUpdateInterval())
        {
            updateGui();
            lastGuiUpddate = System.currentTimeMillis();
        }
    }
    
    @Override
    public void engineCompleted(SynchronizedBufferedImage image) {
        
    }

    @Override
    public int getAA() {
        return aa;
    }

    @Override
    public void setAA(int aa) {
        this.aa = aa;
    }
    
    public Complex getLastOrbitPoint(int x, int y)
    {
        return orbitEndPoints[x][y];
    }

    public int getOrbitSize(int x, int y)
    {
        return orbitLengths[x][y];
    }

    @Override
    public void performSpecialClickAction(Complex clickLocation) {
    }
    
    
    private class NewtonCalculator implements Runnable
    {
        private final int coreIndex;
        private boolean stopped = false;
        private final SynchronizedBufferedImage image;

        public NewtonCalculator(int i, SynchronizedBufferedImage image)
        {
            this.coreIndex = i;
            this.image = image;
        }

        @Override
        public void run() {
            if (aa == Antialiasable.NONE)
            {
                for (int x=coreIndex; x<imageWidth; x+=numCores)
                {
                    List<List<Complex>> orbits = new ArrayList<>();
                    List<Point> points = new ArrayList<>(imageWidth);
                    for (int y=0; y<imageHeight; y++)
                    {
                        Complex c = getMapper().mapToComplex(x, y);
                        List<Complex> orbit = fractalEngine.calcOrbit(c);
                        points.add(new Point(x,y));
                        orbits.add(orbit);
                    }
                    if (stopped) return;
                    enginePerformedCalculation(points, orbits);
                }
            }
            else
            {
                double xStep = Math.abs(getMapper().getXStep());
                double yStep = Math.abs(getMapper().getYStep());
                double aaXStep = xStep/(double)aa;
                double aaYStep = yStep/(double)aa;
                int xSteps, ySteps;
                for (int x=coreIndex; x<imageWidth; x+=numCores)
                {
                    for (int y=0; y<imageHeight; y++)
                    {
                        Complex c = getMapper().mapToComplex(x, y);
                        int colorR = 0;
                        int colorG = 0;
                        int colorB = 0;
                        List<Complex> repOrbit = null;
                        xSteps=0;
                        for (double r=c.r-aaXStep*(aa-1)/2; xSteps<aa; r+=aaXStep)
                        {
                            ySteps=0;
                            for (double i=c.i-aaYStep*(aa-1)/2; ySteps<aa; i+=aaYStep)
                            {
                                Complex aaStep = new Complex(r,i);
                                List<Complex> orbit = fractalEngine.calcOrbit(aaStep);
                                if (aaStep.equals(c)) repOrbit = orbit;
                                Color color = activeColorCalculator.calcColor(x, y, orbit, fractalEngine);
                                colorR += color.getRed();
                                colorG += color.getGreen();
                                colorB += color.getBlue();
                                ySteps++;
                            }
                            xSteps++;
                        }
                        if (stopped) return;
                        if (repOrbit == null) repOrbit = fractalEngine.calcOrbit(c);
                        colorR = colorR/(aa*aa);
                        colorG = colorG/(aa*aa);
                        colorB = colorB/(aa*aa);
                        enginePerformedCalculation(x, y, repOrbit, new Color(colorR, colorG, colorB));
                    }
                }
            }
        }

        private void stop() {
            stopped = true;
        }
    }
    
}
