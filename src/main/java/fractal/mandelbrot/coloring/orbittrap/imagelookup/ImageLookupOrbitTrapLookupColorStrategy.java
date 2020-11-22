/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap.imagelookup;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Pair;
import fractal.common.Redrawable;
import fractal.mandelbrot.RawGpuOrbitContainer;
import fractal.mandelbrot.coloring.orbittrap.OrbitTrapColorStrategy;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class ImageLookupOrbitTrapLookupColorStrategy implements OrbitTrapColorStrategy<ImageLookupOrbitTrap>, Redrawable {

    private final ImageLookupOrbitTrapLookupStrategySettingsPanel settingsPanel = new ImageLookupOrbitTrapLookupStrategySettingsPanel(this);
    
    private final FractalRenderer fractalRenderer;
    private final ImageLookupOrbitTrap orbitTrap;

    public ImageLookupOrbitTrapLookupColorStrategy(FractalRenderer fractalRenderer, ImageLookupOrbitTrap orbitTrap) {
        this.fractalRenderer = fractalRenderer;
		this.orbitTrap = orbitTrap;
    }

    @Override
    public String getName() {
        return "Image Lookup";
    }

    @Override
    public void initForRender() {
    }

    @Override
    public Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine, ImageLookupOrbitTrap orbitTrap) {
        for (int i = 1; i < orbit.size(); i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            Pair<Double, Double> posOnImage = orbitTrap.getRelativePositionOnImage(orbit.get(i));
            if (posOnImage.getFirst()>=0 && posOnImage.getFirst()<1 && posOnImage.getSecond()>=0 && posOnImage.getSecond()<1) {
                int imgX = (int) (posOnImage.getFirst() * settingsPanel.getTrapImage().getWidth());
                int imgY = (int) (posOnImage.getSecond() * settingsPanel.getTrapImage().getHeight());
                return new Color(settingsPanel.getTrapImage().getRGB(imgX, imgY));
            }
        }
        
        return Color.BLACK;
    }

    @Override
    public Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine, ImageLookupOrbitTrap orbitTrap) {
        for (int i = 1; i < orbitLength; i++) {//skip first mandelbrot orbit point (always == perterb i.e. 0) TODO: what about other fractals
            Pair<Double, Double> posOnImage = orbitTrap.getRelativePositionOnImage(new Complex(rawGpuOrbitContainer.orbitsR[orbitStartIndex + i], rawGpuOrbitContainer.orbitsI[orbitStartIndex + i]));
            if (posOnImage.getFirst()>=0 && posOnImage.getFirst()<1 && posOnImage.getSecond()>=0 && posOnImage.getSecond()<1) {
                int imgX = (int) (posOnImage.getFirst() * settingsPanel.getTrapImage().getWidth());
                int imgY = (int) (posOnImage.getSecond() * settingsPanel.getTrapImage().getHeight());
                
                Color c = Color.WHITE;
                try {
                    c = new Color(settingsPanel.getTrapImage().getRGB(imgX, imgY));
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    System.out.println(imgX+"/");
                }
                return c;
            }
        }
        
        return Color.BLACK;
    }

    @Override
    public Color recalcColor(int x, int y) {
        return null;
    }

    @Override
    public Component getSettingsComponent() {
        return settingsPanel;
    }

    @Override
    public void redraw() {
    }

    public BufferedImage getTrapImage() {
        return settingsPanel.getTrapImage();
    }
    
}
