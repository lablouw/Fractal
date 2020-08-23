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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Lloyd
 */
public class DeJongEngine implements FractalEngine {

    private static final Random RANDOM = new Random();
    private FractalEngine INSTANCE;
    private boolean stopped = true;
    private int numTravelers = 10000;
    private int maxTravelerAge = 200;
    private int skipInitIters = 0;

    private double a;
    private double b;
    private double c;
    private double d;

    private final int numCores = Runtime.getRuntime().availableProcessors();

    private List<TravelerRunner> runners;
    private final FractalRenderer renderer;
    
    JButton randomizeButton;
    private JPanel settingsPanel;

    DeJongEngine(FractalRenderer renderer) {
        this.runners = new ArrayList<>();
        this.renderer = renderer;
        settingsPanel = (JPanel) getSettingsComponent();
        INSTANCE = this;
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
        return maxTravelerAge;
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
        settingsPanel = new JPanel(new GridLayout(0, 1));

        //DeJong settings
        JPanel panelA = new JPanel(new GridLayout(2,0));
        JPanel panelB = new JPanel(new GridLayout(2,0));
        JPanel panelC = new JPanel(new GridLayout(2,0));
        JPanel panelD = new JPanel(new GridLayout(2,0));
        JLabel labelA = new JLabel();
        JLabel labelB = new JLabel();
        JLabel labelC = new JLabel();
        JLabel labelD = new JLabel();
        final JSlider sliderA = new JSlider(-2000, 2000);
        final JSlider sliderB = new JSlider(-2000, 2000);
        final JSlider sliderC = new JSlider(-2000, 2000);
        final JSlider sliderD = new JSlider(-2000, 2000);
        panelA.add(labelA);
        panelB.add(labelB);
        panelC.add(labelC);
        panelD.add(labelD);
        panelA.add(sliderA);
        panelB.add(sliderB);
        panelC.add(sliderC);
        panelD.add(sliderD);
        sliderA.setValue((int)RANDOM.nextDouble() * 4000 - 2000);
        sliderB.setValue((int)RANDOM.nextDouble() * 4000 - 2000);
        sliderC.setValue((int)RANDOM.nextDouble() * 4000 - 2000);
        sliderD.setValue((int)RANDOM.nextDouble() * 4000 - 2000);
        sliderA.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                a = (double)sliderA.getValue()/1000d;
                labelA.setText(a+"");
                stop();
                renderer.clearImage();
                start();
            }
        });
        sliderB.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                b = (double)sliderB.getValue()/1000d;
                labelB.setText(b+"");
                stop();
                renderer.clearImage();
                start();
            }
        });
        sliderC.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                c = (double)sliderC.getValue()/1000d;
                labelC.setText(c+"");
                stop();
                renderer.clearImage();
                start();
            }
        });
        sliderD.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                d = (double)sliderD.getValue()/1000d;
                labelD.setText(d+"");
                stop();
                renderer.clearImage();
                start();
            }
        });
        settingsPanel.add(panelA);
        settingsPanel.add(panelB);
        settingsPanel.add(panelC);
        settingsPanel.add(panelD);
        
        //Max traveler age
        JSpinner maxAgeSpinner = new JSpinner();
        maxAgeSpinner.setValue(200);
        maxAgeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxTravelerAge = (int) maxAgeSpinner.getValue();
            }
        });
        JLabel ageLabel = new JLabel("Max traveler iter:");
        JPanel agePanel = new JPanel(new GridLayout(0,2));
        agePanel.add(ageLabel);
        agePanel.add(maxAgeSpinner);
        settingsPanel.add(agePanel);
        
        //Skip initail iters
        JSpinner skipIterSpinner = new JSpinner();
        skipIterSpinner.setValue(0);
        skipIterSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                skipInitIters = (int) skipIterSpinner.getValue();
            }
        });
        JLabel skipIterLabel = new JLabel("Skip initial iters:");
        JPanel skipIterPanel = new JPanel(new GridLayout(0,2));
        agePanel.add(skipIterLabel);
        agePanel.add(skipIterSpinner);
        settingsPanel.add(skipIterPanel);
        
        
        
        randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                a = RANDOM.nextDouble() * 4 - 2;
                labelA.setText(a+"");
                sliderA.setValue((int) (a * 1000));
                
                b = RANDOM.nextDouble() * 4 - 2;
                labelB.setText(b+"");
                sliderB.setValue((int) (b * 1000));
                
                c = RANDOM.nextDouble() * 4 - 2;
                labelC.setText(c+"");
                sliderC.setValue((int) (c * 1000));
                
                d = RANDOM.nextDouble() * 4 - 2;
                labelD.setText(d+"");
                sliderD.setValue((int) (d * 1000));
                
                if (e != null) {//not the inititalizing call from the constructor
                    renderer.stopRendering();
                    renderer.render(renderer.getImage().getBufferedImage().getWidth(), renderer.getImage().getBufferedImage().getHeight(), renderer.getMapper().getTopLeft(), renderer.getMapper().getBottomRight());
                }
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
    public void notifyRenderComplete() {
    }

    private class TravelerRunner implements Runnable {

        private boolean stopped = false;
        List<Traveler> travelers = new ArrayList<>();

        private TravelerRunner() {
            for (int i = 0; i < numTravelers / numCores; i++) {
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
                    if (!stopped && t.getAge() > skipInitIters) {
                        renderer.enginePerformedCalculation(p.x, p.y, Collections.singletonList(new Complex(rgb, rgb)));// quite the hack passing color back through the orbit vars...
                    }
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
        }

        @Override
        public void move() {
            double rn = Math.sin(a * i) - Math.cos(b * r);
            i = Math.sin(c * r) - Math.cos(d * i);
            r = rn;
            if (i == Double.NaN || r == Double.NaN || age > maxTravelerAge) {
                r = RANDOM.nextDouble();
                i = RANDOM.nextDouble() * 2 - 1;

                rn = Math.sin(a * i) - Math.cos(b * r);
                i = Math.sin(c * r) - Math.cos(d * i);
                r = rn;

                age = 1;
                color = renderer.getActiveColorCalculator().calcColor(0, 0, new Complex(r, i), age, INSTANCE);
            } else {
                color = renderer.getActiveColorCalculator().calcColor(0, 0, new Complex(r, i), age, INSTANCE);
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
        
        @Override
        public int getAge() {
            return age;
        }
    }

}
