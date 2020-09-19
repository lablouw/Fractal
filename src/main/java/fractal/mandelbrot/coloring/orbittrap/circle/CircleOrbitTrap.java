/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.circle;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.ImageUtils;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrap;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class CircleOrbitTrap extends OrbitTrap {
    
    private final List<OrbitTrapColorStrategy<CircleOrbitTrap>> colorStrategies;
    private OrbitTrapColorStrategy<CircleOrbitTrap> activeColorStrategy;

    private final JDialog settingsDialog;

    private Complex center;
    private Complex pointOnCircle;
    private double radius;

    public CircleOrbitTrap(FractalRenderer fractalRenderer) {
        this.fractalRenderer = fractalRenderer;
        setDefiningPoints(new Complex(0,0), new Complex(0.25, 0));

        colorStrategies = new ArrayList<>();
        colorStrategies.add(new CircleOrbitTrapDistanceColorStrategy(fractalRenderer, this));
        colorStrategies.add(new CircleOrbitTrapColorLookupStrategy(fractalRenderer, this));
        activeColorStrategy = colorStrategies.get(0);
        
        settingsDialog = new CircleOrbitTrapSettingsDialog(this, fractalRenderer, colorStrategies);
    }

    @Override
    public Component getSettingsComponent() {
        return settingsDialog;
    }

    @Override
    public String getName() {
        return "Circle";
    }

    @Override
    public void initForRender(FractalRenderer fractalRenderer) {
        activeColorStrategy.initForRender();
    }

    @Override
    public void setDefiningPoints(Complex c1, Complex c2) {
        this.center = c1;
        this.pointOnCircle = c2;
        this.radius = Math.sqrt(Math.pow(c2.r-c1.r, 2) + Math.pow(c2.i-c1.i, 2));
    }

    @Override
    public BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer) {
        Point c = fractalRenderer.getMapper().mapToImage(center);
        Point poc = fractalRenderer.getMapper().mapToImage(pointOnCircle);
        int r = (int) Math.sqrt((poc.x-c.x) * (poc.x-c.x) + (poc.y-c.y) * (poc.y-c.y));

        BufferedImage im = ImageUtils.deepCopy(baseImage);
        im.getGraphics().drawOval(c.x-r, c.y-r, r*2, r*2);
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
        return activeColorStrategy.recalcColor(x, y);
    }

    public double distanceFrom(Complex c) {
        return c.sub(center).modulus() - radius;
    }

    public double getRadius() {
        return radius;
    }

    public OrbitTrapColorStrategy<CircleOrbitTrap> getActiveColorStrategy() {
        return activeColorStrategy;
    }

    public void setActiveColorStrategy(OrbitTrapColorStrategy<CircleOrbitTrap> colorStrategy) {
        this.activeColorStrategy = colorStrategy;
    }
}
