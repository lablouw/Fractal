/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import fractal.common.Mappers.LinearMapper;
import fractal.common.Mappers.Mapper;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import fractal.mandelbrot.RawGpuOrbitContainer;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;

/**
 *
 * @author lloyd
 */
public abstract class FractalRenderer<T extends FractalEngine> {

    private Mapper mapper;
    private List<ColorCalculator> colorCalculators = new ArrayList<>();
    protected ColorCalculator activeColorCalculator;
    protected T fractalEngine;
    protected FractalViewer fractalViewer;
    protected long lastRenderTime;
    protected long t0;
    protected int imageWidth = 1920;
    protected int imageHeight = 1080;
    protected SynchronizedBufferedImage synchronizedBufferedImage = new SynchronizedBufferedImage(imageWidth, imageHeight);
    protected boolean busy = false;

    protected void addColorCalculator(ColorCalculator colorCalculator) {
        if (colorCalculators.isEmpty()) {
            this.activeColorCalculator = colorCalculator;
        }
        colorCalculators.add(colorCalculator);
    }

    public Mapper getMapper() {
        if (mapper == null && fractalEngine != null) {
        	mapper = new LinearMapper(fractalEngine.getDefaultView().getFirst(), fractalEngine.getDefaultView().getSecond(), imageWidth, imageHeight);
        }
        return mapper;
    }
    
    public boolean isBusy() {
        return busy;
    }

    public ColorCalculator getActiveColorCalculator() {
        return activeColorCalculator;
    }

    public T getFractalEngine() {
        return fractalEngine;
    }

    public FractalViewer getFractalViewer() {
        return fractalViewer;
    }

    public SynchronizedBufferedImage getImage() {
        return synchronizedBufferedImage;
    }

    public void render(final int width, final int height, Complex p1, Complex p2) {
        stopRendering();
        this.imageWidth = width;
        this.imageHeight = height;
        synchronizedBufferedImage = new SynchronizedBufferedImage(width, height);
        activeColorCalculator.initForRender(this);
        t0 = System.currentTimeMillis();

        //fix aspect ratio if required
        double imageAspect = (double) width / (double) height;
        double complexAspect = (p2.r - p1.r) / (p2.i - p1.i);
        if (complexAspect > imageAspect) {
            p1.i = p2.i - Math.abs(p2.r - p1.r) / imageAspect;
        } else if (imageAspect > complexAspect) {
            p2.r = p1.r + Math.abs(p2.i - p1.i) * imageAspect;
        }

        mapper = new LinearMapper(p1, p2, width, height);

        new Thread(new Runnable() {
            @Override
            public void run() {
                render();
                notifyRenderComplete();
                updateGui();
            }
        }).start();
    }

    private void notifyRenderComplete() {
        activeColorCalculator.complete(synchronizedBufferedImage);
        fractalEngine.notifyRenderComplete();
    }
    
    public void clearImage() {
        synchronizedBufferedImage = new SynchronizedBufferedImage(imageWidth, imageHeight);
    }

    public synchronized void updateGui() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fractalViewer != null) {
                    fractalViewer.setImage(synchronizedBufferedImage.getBufferedImage());
                    fractalViewer.setRenderTime(lastRenderTime);
                }
            }
        }).start();
    }

    public synchronized void updateGui(final SynchronizedBufferedImage image) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fractalViewer != null) {
                    fractalViewer.setImage(image.getBufferedImage());
                    fractalViewer.setRenderTime(lastRenderTime);
                }
            }
        }).start();
    }

    public JComponent getSettingsComponent() {
        JComboBox<ColorCalculator> colComboBox = new JComboBox<>();
        for (ColorCalculator cc : colorCalculators) {
            colComboBox.addItem(cc);
        }
        colComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    activeColorCalculator = (ColorCalculator) e.getItem();
                    fractalViewer.updateColorCalculator(activeColorCalculator);
                }
            }
        });

        colComboBox.setRenderer(new ListCellRenderer<ColorCalculator>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends ColorCalculator> list, ColorCalculator value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JXLabel(value.getName());
            }
        });

        JXPanel p = new JXPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(new JXLabel("Color Calculator"));
        p.add(colComboBox);

        JXPanel p2 = new JXPanel();
        p2.setLayout(new VerticalLayout());
        p2.add(p);
        JComponent customSettingsComponent = getCustomSettingsComponent();
        if (customSettingsComponent != null) {
            p2.add(getCustomSettingsComponent());
        }
        return p2;
    }

    public abstract String getName();

    public abstract void mouseMoved(Complex pointOnImage);

    protected abstract JComponent getCustomSettingsComponent();
    
    protected abstract void render();

    public abstract void stopRendering();

    public abstract void enginePerformedCalculation(List<Point> points, List<List<Complex>> orbits);

    public abstract void enginePerformedCalculation(int x, int y, List<Complex> orbit);

    public abstract void enginePerformedCalculation(int x, int y, RawGpuOrbitContainer rawGpuOrbitContainer, int orbitStartIndex, int orbitLength);

    //Used for antialiasing which is earmarked for refactor
    public abstract void enginePerformedCalculation(int x, int y, List<Complex> orbit, Color color);

    public abstract void performSpecialClickAction(Complex clickLocation);
}
