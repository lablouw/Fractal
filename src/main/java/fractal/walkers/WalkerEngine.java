/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.walkers;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.Pair;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JComponent;

/**
 *
 * @author cp316928
 */
public class WalkerEngine implements FractalEngine {
    
    private static final Random RANDOM = new Random();
    private boolean stopped = true;
    int numWalkers = 300;
    
    private WalkerRunner w;
    
    private final WalkerRenderer walkerRenderer;

    WalkerEngine(WalkerRenderer walkerRenderer) {
        this.walkerRenderer = walkerRenderer;
    }
    
    public void start() {
        stopped = false;
        w = new WalkerRunner();
        new Thread(w).start();
    }
    
    public void stop() {
        if (w != null) {
            w.stop();
        }
        stopped = true;
    }

    @Override
    public List<Complex> calcOrbit(Complex c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMaxIter(int maxIter) {
    }

    @Override
    public int getMaxIter() {
        return 0;
    }

    @Override
    public Pair<Complex, Complex> getDefaultView() {
        Complex p1 = new Complex(-1, 1);
        Complex p2 = new Complex(1, -1);
        return new Pair<>(p1, p2);
    }

    @Override
    public boolean isBailoutReached(List<Complex> orbit) {
        return false;
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public void notifyRenderComplete() {
    }
    
    private class WalkerRunner implements Runnable {

        private boolean stopped = false;
        List<Walker> walkers = new ArrayList<>();

        private WalkerRunner() {
            for (int i = 0; i < numWalkers; i++) {
                Walker w = new Walker();
                walkers.add(w);
            }
            
            for (Walker w : walkers) {
                Walker target1 = null;
                Walker target2 = null;
                
                while (target1 == null || target1.equals(w)) {
                    target1 = walkers.get(RANDOM.nextInt(numWalkers));
                }
                w.setTarget1(target1);
                
                while (target2 == null || target2.equals(w) || target2.equals(target1)) {
                    target2 = walkers.get(RANDOM.nextInt(numWalkers));
                }
                w.setTarget2(target2);
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                for (Walker w : walkers) {
                    w.move();
                }
                for (Walker w : walkers) {
                    w.updatePosition();
                    Point p = walkerRenderer.getMapper().mapToImage(w.getPosition());
                    int rgb = (((w.getColor().getRed() << 8) + w.getColor().getGreen()) << 8) + w.getColor().getBlue();
                    walkerRenderer.enginePerformedCalculation(p.x, p.y, Collections.singletonList(new Complex(rgb, rgb)));// quite the hack passing color back through the orbit vars...
                }
            }
        }

        public void stop() {
            stopped = true;
        }

    }
    
    
    private class Walker {
        private double r;
        private double i;
        private double newR;
        private double newI;
        private Color color;
        private Walker target1;
        private Walker target2;

        private Walker() {
            r = RANDOM.nextDouble()*2 - 1;
            i = RANDOM.nextDouble()*2 - 1;
//            color = Color.WHITE;//henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
            color = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
        }

        public void move() {
            Complex moveDir = new Complex(target2.r-target1.r, target2.i-target1.i).normalize();
            Complex t2ToP = new Complex(r-target2.r, i-target2.i);
            double dot = moveDir.r*t2ToP.r + moveDir.i*t2ToP.i;
            double pToPerp = new Complex(target2.r-target1.r, target2.i-target1.i).modulus()/2 + dot;
            
            newR = r - moveDir.r*pToPerp/1000;
            newI = i - moveDir.i*pToPerp/1000;
        }

        public Complex getPosition() {
            return new Complex(r, i);
        }

        public Color getColor() {
            return color;
        }

        public void setTarget1(Walker target1) {
            this.target1 = target1;
        }

        public void setTarget2(Walker target2) {
            this.target2 = target2;
        }
        
        public void updatePosition() {
            r = newR;
            i = newI;
        }

    }
    
}
