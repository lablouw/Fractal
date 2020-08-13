/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase.dejong;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.FractalRenderer;
import fractal.common.Pair;
import fractal.phase.Traveler;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Lloyd
 */
public class DeJongEngine implements FractalEngine {

    private static final Random RANDOM = new Random();
    private boolean stopped = true;
    int numWalkers = 10000;

    private double theta = 1;
    private double a = RANDOM.nextDouble() * 4 - 2;
    private double b = RANDOM.nextDouble() * 4 - 2;
    private double c = RANDOM.nextDouble() * 4 - 2;
    private double d = RANDOM.nextDouble() * 4 - 2;

    private JLabel aLabel = new JLabel("a = 2*\u03c0 * 1");

    private final int numCores = Runtime.getRuntime().availableProcessors();

    private List<TravelerRunner> runners = new ArrayList<>();
    private final FractalRenderer renderer;
    private JPanel settingsPanel;

    DeJongEngine(FractalRenderer renderer) {
        this.renderer = renderer;
    }

    public void start() {
        stopped = false;
        runners = new ArrayList<>();
        for (int i = 0; i < numCores; i++) {
            TravelerRunner w = new TravelerRunner();
            runners.add(w);
            new Thread(w).start();
        }
    }

    public void stop() {
        for (TravelerRunner w : runners) {
            w.stop();
        }
        stopped = true;
    }

    boolean isStopped() {
        return stopped;
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
        Complex p1 = new Complex(-2, 2);
        Complex p2 = new Complex(2, -2);
        return new Pair<>(p1, p2);
    }

    @Override
    public boolean isBailoutReached(List<Complex> orbit) {
        return false;
    }

    @Override
    public JComponent getSettingsComponent() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(0, 1));

        //Hennon settings
        JButton randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                a = RANDOM.nextDouble() * 4 - 2;
                b = RANDOM.nextDouble() * 4 - 2;
                c = RANDOM.nextDouble() * 4 - 2;
                d = RANDOM.nextDouble() * 4 - 2;
                renderer.stopRendering();
                renderer.render(renderer.getImage().getBufferedImage().getWidth(), renderer.getImage().getBufferedImage().getHeight(), renderer.getMapper().getTopLeft(), renderer.getMapper().getBottomRight());
            }
        });
        settingsPanel.add(randomizeButton);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        settingsPanel.add(stopButton);

        return settingsPanel;
    }

    @Override
    public void init() {
    }

    @Override
    public void complete() {
    }

    private class TravelerRunner implements Runnable {

        private boolean stopped = false;
        List<Traveler> travelers = new ArrayList<>();

        private TravelerRunner() {
            for (int i = 0; i < numWalkers / numCores; i++) {
                DeJongTraveler t = new DeJongTraveler();
                travelers.add(t);
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                for (Traveler t : travelers) {
                    t.move();
                    int rgb = (((t.getColor().getRed() << 8) + t.getColor().getGreen()) << 8) + t.getColor().getBlue();
                    Point p = renderer.getMapper().mapToImage(t.getPosition());
                    renderer.enginePerformedCalculation(p.x, p.y, Collections.singletonList(new Complex(rgb, rgb)));// quite the hack passing color back through the orbit vars...
                }
            }
        }

        public void stop() {
            stopped = true;
        }

    }

    private class DeJongTraveler implements Traveler {

        private double r;
        private double i;
        private Color color;
        private int age = 0;

        private DeJongTraveler() {
            r = RANDOM.nextDouble();
            i = RANDOM.nextDouble() * 2 - 1;
            color = Color.WHITE;//henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
        }

        @Override
        public void move() {
            double rn = Math.sin(a * i) - Math.cos(b * r);
            i = Math.sin(c * r) - Math.cos(d * i);
            r = rn;
            if (i == Double.NaN || r == Double.NaN || age > 100) {
                r = RANDOM.nextDouble();
                i = RANDOM.nextDouble() * 2 - 1;

                rn = Math.sin(a * i) - Math.cos(b * r);
                i = Math.sin(c * r) - Math.cos(d * i);
                r = rn;

                color = Color.WHITE;//henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
                age = 0;
            } else {
                age++;
            }
        }

        @Override
        public Complex getPosition() {
            return new Complex(r, i);
        }

        @Override
        public Color getColor() {
            return color;
        }

    }

}
