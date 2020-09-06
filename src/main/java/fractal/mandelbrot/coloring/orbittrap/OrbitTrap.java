/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;

import fractal.common.Complex;
import fractal.common.FractalRenderer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;

/**
 *
 * @author Lloyd
 */
public abstract class OrbitTrap {

    private MouseListener[] tempListeners;
    private FractalRenderer fractalRenderer;
    private OrbitTrap orbitTrap;
    
    public abstract String getName();
    public abstract void setDefiningPoints(Complex p1, Complex p2);
    public abstract double distanceFrom(Complex p);
    
    public void doUserDefined(FractalRenderer fractalRenderer, OrbitTrap orbitTrap, JButton buttonToEnable) {
        this.fractalRenderer = fractalRenderer;
        this.orbitTrap = orbitTrap;
        tempListeners = fractalRenderer.getFractalViewer().getImagePanel().getMouseListeners();
        for (MouseListener ml : tempListeners) {
            fractalRenderer.getFractalViewer().getImagePanel().removeMouseListener(ml);
        }
        
         fractalRenderer.getFractalViewer().getImagePanel().addMouseListener(new OrbitTrapDefeiningMouseListener(buttonToEnable));
    }

    private class OrbitTrapDefeiningMouseListener implements MouseListener {
        Complex p1, p2;
        private final JButton buttonToEnable;

        private OrbitTrapDefeiningMouseListener(JButton buttonToEnable) {
            this.buttonToEnable = buttonToEnable;
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        
        @Override
        public void mousePressed(MouseEvent e) {
            p1 = fractalRenderer.getMapper().mapToComplex(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            p2 = fractalRenderer.getMapper().mapToComplex(e.getX(), e.getY());
            fractalRenderer.getFractalViewer().getImagePanel().removeMouseListener(this);
            for (MouseListener ml : tempListeners) {
                fractalRenderer.getFractalViewer().getImagePanel().addMouseListener(ml);
            }

            orbitTrap.setDefiningPoints(p1, p2);
            buttonToEnable.setEnabled(true);
            buttonToEnable.setText("Specify trap");
        }

    }
    
    
    
}
