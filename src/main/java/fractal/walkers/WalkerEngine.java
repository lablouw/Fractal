/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.walkers;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.Pair;
import fractal.common.Traveler;

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
    private int numWalkers = 50;
    
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
    public boolean isBailoutReachedByLastOrbitPoint(Complex lastOrbitPoint) {
        return false;
    }

    @Override
    public JComponent getSettingsComponent() {
        return null;
    }

    private class WalkerRunner implements Runnable {

        private boolean stopped = false;
        List<TwoTargetSeekingTraveler> twoTargetSeekingTravelers = new ArrayList<>(numWalkers);

        private WalkerRunner() {
            for (int i = 0; i < numWalkers; i++) {
                TwoTargetSeekingTraveler t = new TwoTargetSeekingTraveler();
                twoTargetSeekingTravelers.add(t);
            }
            
            for (TwoTargetSeekingTraveler t : twoTargetSeekingTravelers) {
                TwoTargetSeekingTraveler target1 = null;
                TwoTargetSeekingTraveler target2 = null;
                
                while (target1 == null || target1.equals(t)) {
                    target1 = twoTargetSeekingTravelers.get(RANDOM.nextInt(numWalkers));
                }
                t.setTarget1(target1);
                
                while (target2 == null || target2.equals(t) || target2.equals(target1)) {
                    target2 = twoTargetSeekingTravelers.get(RANDOM.nextInt(numWalkers));
                }
                t.setTarget2(target2);
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                for (TwoTargetSeekingTraveler w : twoTargetSeekingTravelers) {
                    w.move();
                }
                for (TwoTargetSeekingTraveler w : twoTargetSeekingTravelers) {
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
    
    
    private class TwoTargetSeekingTraveler extends Traveler {
        private Complex newPosition;
        private TwoTargetSeekingTraveler target1;
        private TwoTargetSeekingTraveler target2;

        private TwoTargetSeekingTraveler() {
            position.r = RANDOM.nextDouble()*2 - 1;
            position.i = RANDOM.nextDouble()*2 - 1;
            color = new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
        }

        public void move() {
            double t1r = target1.position.r;
            double t1i = target1.position.i;
            double t2r = target2.position.r;
            double t2i = target2.position.i;

            Complex moveDir = new Complex(t2r-t1r, t2i-t1i).normalize();
            Complex t2ToP = new Complex(position.r-t2r, position.i-t2i);
            double dot = moveDir.r*t2ToP.r + moveDir.i*t2ToP.i;
            double pToPerp = new Complex(t2r-t1r, t2i-t1i).modulus()/2 + dot;

            newPosition = new Complex(position.r - moveDir.r*pToPerp/1000, position.i - moveDir.i*pToPerp/1000);
        }

        void setTarget1(TwoTargetSeekingTraveler target1) {
            this.target1 = target1;
        }

        void setTarget2(TwoTargetSeekingTraveler target2) {
            this.target2 = target2;
        }
        
        void updatePosition() {
            position = newPosition;
        }

    }
    
}
