/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.mandelbrot.RawGpuOrbitContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public abstract class OrbitTrap {

    private MouseListener[] tempListeners;
    protected FractalRenderer fractalRenderer;

    public abstract String getName();
    public abstract void initForRender(FractalRenderer fractalRenderer);
    public abstract void setDefiningPoints(Complex c1, Complex c2);
    public abstract Component getSettingsComponent();
    public abstract BufferedImage drawOrbitTrap(BufferedImage baseImage, FractalRenderer fractalRenderer);
    public abstract Color calcColor(int x, int y, List<Complex> orbit, FractalEngine fractalEngine);
    public abstract Color calcColor(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength, FractalEngine fractalEngine);
    public abstract Color reCalcColor(int x, int y);

    public void doUserDefined(FractalRenderer fractalRenderer, JButton buttonToEnable) {
        this.fractalRenderer = fractalRenderer;
        tempListeners = fractalRenderer.getFractalViewer().getImagePanel().getMouseListeners();
        for (MouseListener ml : tempListeners) {
            fractalRenderer.getFractalViewer().getImagePanel().removeMouseListener(ml);
        }

        OrbitTrapDefiningMouseListener orbitTrapDefiningMouseListener = new OrbitTrapDefiningMouseListener(buttonToEnable);
        fractalRenderer.getFractalViewer().getImagePanel().addMouseListener(orbitTrapDefiningMouseListener);
        fractalRenderer.getFractalViewer().getImagePanel().addMouseMotionListener(orbitTrapDefiningMouseListener);

    }


    private class OrbitTrapDefiningMouseListener extends MouseMotionAdapter implements MouseListener {
        private Complex p1;
        private Complex p2;
        private final JButton buttonToEnable;
        private BufferedImage baseImage;

        private OrbitTrapDefiningMouseListener(JButton buttonToEnable) {
            this.buttonToEnable = buttonToEnable;
            this.baseImage = fractalRenderer.getFractalViewer().getImage();
        }

        public void mousePressed(MouseEvent e) {
            p1 = fractalRenderer.getMapper().mapToComplex(e.getX(), e.getY(), fractalRenderer.getFractalViewer().getImagePanel());
        }

        public void mouseReleased(MouseEvent e) {
            p2 = fractalRenderer.getMapper().mapToComplex(e.getX(), e.getY(), fractalRenderer.getFractalViewer().getImagePanel());
            fractalRenderer.getFractalViewer().getImagePanel().removeMouseListener(this);
            fractalRenderer.getFractalViewer().getImagePanel().removeMouseMotionListener(this);
            for (MouseListener ml : tempListeners) {
                fractalRenderer.getFractalViewer().getImagePanel().addMouseListener(ml);
            }

            setDefiningPoints(p1, p2);
            buttonToEnable.setEnabled(true);
            buttonToEnable.setText("Specify trap");
            fractalRenderer.render(
                    fractalRenderer.getImage().getBufferedImage().getWidth(),
                    fractalRenderer.getImage().getBufferedImage().getHeight(),
                    fractalRenderer.getMapper().getTopLeft(),
                    fractalRenderer.getMapper().getBottomRight());
        }

        public void mouseDragged(MouseEvent e) {
            p2 = fractalRenderer.getMapper().mapToComplex(e.getX(), e.getY(), fractalRenderer.getFractalViewer().getImagePanel());
            setDefiningPoints(p1, p2);
            fractalRenderer.getFractalViewer().setImage(drawOrbitTrap(baseImage, fractalRenderer));
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {}
    }


}
