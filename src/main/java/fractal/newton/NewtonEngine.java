/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.newton;

import fractal.common.function.TreeNode;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.Pair;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Lloyd
 */
public class NewtonEngine extends FractalEngine {

    private int maxIter = 100;
    private double bailout = 0.000001;
    private Complex a = new Complex(Complex.ONE);
    private TreeNode fxRootNode;
    private TreeNode dfdxRootNode;

    public TreeNode getFxRootNode() {
        return fxRootNode;
    }

    public void setFxRootNode(TreeNode fxRootNode) {
        this.fxRootNode = fxRootNode;
    }

    public TreeNode getDfdxRootNode() {
        return dfdxRootNode;
    }

    public void setDfdxRootNode(TreeNode dfdxRootNode) {
        this.dfdxRootNode = dfdxRootNode;
    }
    
    @Override
    public List<Complex> calcStraightOrbit(Complex c) {
        int iter = 0;
        Complex z0 = new Complex(c);
        Complex z1 = null;
        double diff;
        List<Complex> orbit = new ArrayList<>();
        
        try {
            do {
                orbit.add(z0);
                Complex dfdx = dfdxRootNode.evaluate(z0);
                if (dfdx.isEpsilonZero()) {
                    break;
                }
                Complex fx = fxRootNode.evaluate(z0);
                if (a.equals(Complex.ONE)) {
                    z1 = z0.sub(fx.div(dfdx));
                } else {
                    z1 = z0.sub(a.mult(fx.div(dfdx)));
                }
                diff = z1.sub(z0).modulus();
                z0 = z1;
                iter++;
            } while (diff >= bailout && iter < maxIter);
            if (z1 != null) {
                orbit.add(z1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return orbit;
    }

    @Override
    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }

    @Override
    public int getMaxIter() {
        return maxIter;
    }

    @Override
    public Pair<Complex, Complex> getDefaultView() {
        Complex p1 = new Complex(-3, 3);
        Complex p2 = new Complex(3, -3);
        return new Pair<>(p1, p2);
    }

    @Override
    public boolean isBailoutReached(List<Complex> orbit) {
        double diff = orbit.get(orbit.size()-1).sub(orbit.get(orbit.size()-2)).modulus();
        return diff < bailout;
    }

    @Override
    public boolean isBailoutReachedByLastOrbitPoint(Complex lastOrbitPoint) {
        throw new UnsupportedOperationException("Not applicable for Newton fractal");
    }

    public double getBailout() {
        return bailout;
    }
    
    public void setBailout(double bailout) {
        this.bailout = bailout;
    }

    @Override
    public JComponent getSettingsComponent() {
        final JSpinner iterSpinner = new JSpinner();
        iterSpinner.setModel(new SpinnerNumberModel(maxIter, 1, null, 1));
        iterSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxIter = (Integer) iterSpinner.getValue();
            }
        });
        
        final JTextField bailoutField = new JTextField(String.valueOf(bailout));
        bailoutField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {
                bailout = Double.parseDouble(bailoutField.getText());
            }
        });
        
        final JSpinner aR = new JSpinner();
        aR.setModel(new SpinnerNumberModel(1.0d, null, null, 0.01d));
        aR.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                a.r = (Double) aR.getValue();
            }
        });
        final JSpinner aI = new JSpinner();
        aI.setModel(new SpinnerNumberModel(0.0d, null, null, 0.01d));
        aI.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                a.i = (Double) aI.getValue();
            }
        });
        
        JPanel cp = new JPanel(new GridLayout());
        cp.add(aR);
        cp.add(new JLabel(" + i"));
        cp.add(aI);
        
        JPanel p = new JPanel(new GridLayout(0,1));
        p.add(new JLabel("Iterations:"));
        p.add(iterSpinner);
        p.add(new JLabel("a:"));
        p.add(cp);
        
        return p;
    }
}
