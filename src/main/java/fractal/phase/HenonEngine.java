/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase;

import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.Pair;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author CP316928
 */
public class HenonEngine implements FractalEngine {

    private static final Random RANDOM = new Random();
    private boolean stopped = true;
    int numWalkers = 10000;

    private double theta = 1;
    private double a = RANDOM.nextDouble()*4-2;
    private double b = RANDOM.nextDouble()*4-2;
    private double c = RANDOM.nextDouble()*4-2;
    private double d = RANDOM.nextDouble()*4-2;
    
    private JCheckBox henonCB = new JCheckBox("Henon", true);
    private JCheckBox deJongeCB = new JCheckBox("de Jonge", false);
    private JLabel aLabel = new JLabel("a = 2*\u03c0 * 1");

    private final int numCores = Runtime.getRuntime().availableProcessors();

    private List<TravelerRunner> runners = new ArrayList<>();
    private final HenonRenderer henonRenderer;
    private JPanel settingsPanel;

    HenonEngine(HenonRenderer henonRenderer) {
        this.henonRenderer = henonRenderer;
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
        
        henonCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deJongeCB.setSelected(!henonCB.isSelected());
            }
        });
        
        deJongeCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                henonCB.setSelected(!deJongeCB.isSelected());
            }
        });
        
        //Hennon settings
        settingsPanel.add(henonCB);
        settingsPanel.add(aLabel);
        final JSlider aSlider = new JSlider(0, 1000);
        aSlider.setValue(0);
        aSlider.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                theta = ((double) aSlider.getValue() / 1000d) * 2 * Math.PI;
                aLabel.setText("\\u03F4 = 2*\u03c0 * " + aSlider.getValue() / 1000d);
                henonRenderer.stopRendering();
                henonRenderer.render(henonRenderer.getImage().getBufferedImage().getWidth(), henonRenderer.getImage().getBufferedImage().getHeight(), henonRenderer.getMapper().getTopLeft(), henonRenderer.getMapper().getBottomRight());
            }
        });

        settingsPanel.add(aSlider);
        
        //Hennon settings
        settingsPanel.add(deJongeCB);
        
        JButton randomizeButton = new JButton("Randomize");
        randomizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                a = RANDOM.nextDouble()*4-2;
                b = RANDOM.nextDouble()*4-2;
                c = RANDOM.nextDouble()*4-2;
                d = RANDOM.nextDouble()*4-2;
                henonRenderer.stopRendering();
                henonRenderer.render(henonRenderer.getImage().getBufferedImage().getWidth(), henonRenderer.getImage().getBufferedImage().getHeight(), henonRenderer.getMapper().getTopLeft(), henonRenderer.getMapper().getBottomRight());
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
                if (henonCB.isSelected()) {
                    HenonTraveler t = new HenonTraveler();
                    travelers.add(t);
                }
                else if (deJongeCB.isSelected()) {
                    DeJongTraveler t = new DeJongTraveler();
                    travelers.add(t);
                }
            }
        }

        @Override
        public void run() {
            while (!stopped) {
                for (Traveler t : travelers) {
                    t.move();
                    int rgb = (((t.getColor().getRed() << 8) + t.getColor().getGreen()) << 8) + t.getColor().getBlue();
                    Point p = henonRenderer.getMapper().mapToImage(t.getPosition());
                    henonRenderer.enginePerformedCalculation(p.x, p.y, Collections.singletonList(new Complex(rgb, rgb)));// quite the hack passing color back through the orbit vars...
                }
            }
        }

        public void stop() {
            stopped = true;
        }

    }
    
    private interface Traveler {
        void move();
        Complex getPosition();
        Color getColor();
    }

    private class HenonTraveler implements Traveler {

        private double r;
        private double i = 0;
        private Color color;
        private int age = 0;

        private HenonTraveler() {
            r = RANDOM.nextDouble() * 5;
            color = henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
        }

        @Override
        public void move() {
            double rn = r * Math.cos(theta) - (i - r * r) * Math.sin(theta);
            i = r * Math.sin(theta) + (i - r * r) * Math.cos(theta);
            r = rn;
            if (i == Double.NaN || r == Double.NaN || age > 100000) {
                i = 0;
                r = RANDOM.nextDouble() * 5;
                color = henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
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
    
    private class DeJongTraveler implements Traveler {

        private double r;
        private double i;
        private Color color;
        private int age = 0;

        private DeJongTraveler() {
            r = RANDOM.nextDouble();
            i = RANDOM.nextDouble()*2 - 1;
            color = Color.WHITE;//henonRenderer.getActiveColorCalculator().calcColor(0, 0, Collections.singletonList(new Complex(r, i)), null);
        }

        @Override
        public void move() {
            double rn = Math.sin(a*i) - Math.cos(b*r);
            i = Math.sin(c*r) - Math.cos(d*i);
            r = rn;
            if (i == Double.NaN || r == Double.NaN || age > 100) {
                r = RANDOM.nextDouble();
                i = RANDOM.nextDouble()*2 - 1;
                
                rn = Math.sin(a*i) - Math.cos(b*r);
                i = Math.sin(c*r) - Math.cos(d*i);
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
