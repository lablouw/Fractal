/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.imagelookup;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;
import fractal.common.Pair;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapSettingsDialog;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;

/**
 *
 * @author Lloyd
 */
public class ImageLookupOrbitTrap extends OrbitTrap {
    
    private final List<OrbitTrapColorStrategy<ImageLookupOrbitTrap>> colorStrategies;
    
    private final JDialog settingsDialog;

    private Complex c1;
    private Complex c2;
    
    public ImageLookupOrbitTrap(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        setDefiningPoints(new Complex(-0.1, 0.5), new Complex(0.5, -0.5));
        
        colorStrategies = new ArrayList<>();
        colorStrategies.add(new ImageLookupOrbitTrapLookupColorStrategy(fractalRenderer, this));
        activeColorStrategy = colorStrategies.get(0);
        
        settingsDialog = new OrbitTrapSettingsDialog(this, colorStrategies);
    }

    @Override
    public String getName() {
        return "Image Lookup";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
        activeColorStrategy.initForRender();
    }

    @Override
    public void setDefiningPoints(Complex c1, Complex c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public JDialog getSettingsDialog() {
        return settingsDialog;
    }

    @Override
    public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
        BufferedImage im = ImageUtils.deepCopy(baseImage);
        
        BufferedImage trapImage = ((ImageLookupOrbitTrapLookupColorStrategy)activeColorStrategy).getTrapImage();
        Point p1 = fractalRenderer.getMapper().mapToImage(c1);
        Point p2 = fractalRenderer.getMapper().mapToImage(c2);
        
        im.getGraphics().drawImage(trapImage, p1.x, p1.y, p2.x-p1.x, p2.y-p1.y, null);
        
        return im;
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine) {
        return activeColorStrategy.calcColor(x, y, orbit, fractalEngine, this);
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine) {
        return activeColorStrategy.calcColor(x, y, rawGpuOrbitContainer, orbitStartIndex, orbitLength, fractalEngine, this);
    }

    @Override
    public Color reCalcColor(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Pair<Double, Double> getRelativePositionOnImage(Complex c) {
        double posR = (c.r-c1.r)/(c2.r-c1.r);
        double posI = (c.i-c1.i)/(c2.i-c1.i);
        
        return new Pair<Double, Double>(posR, posI);
    }
    
}
