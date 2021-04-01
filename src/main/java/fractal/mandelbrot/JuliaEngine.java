/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot;

import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import fractal.common.Complex;
import fractal.common.FractalEngine;
import fractal.common.ImagePlaneMapper;
import fractal.common.Pair;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author lloyd
 */
public class JuliaEngine implements FractalEngine {

    private Complex exponent = new Complex(2, 0);
    private double bailout = 2;
    private int maxIter = 500;
    private Complex c = new Complex();
    private double bailoutSquared = bailout * bailout;
    private JuliaGPUKernelFull juliaGPUKernelFull = null;
    private JuliaGPUKernelFast juliaGPUKernelFast = null;
    
    private boolean useGPUFull = false;
    private boolean useGPUFast = false;
    private int subImageWidth;
    private int subImageHeight;

    final JSpinner crSpinner = new JSpinner();
    final JSpinner ciSpinner = new JSpinner();

    @Override
    public JComponent getSettingsComponent() {
        final JSpinner iterSpinner = new JSpinner();
        iterSpinner.setModel(new SpinnerNumberModel(500, 1, null, 1));
        iterSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                maxIter = (Integer) iterSpinner.getValue();
            }
        });
        
        final JSpinner bailoutSpinner = new JSpinner();
        bailoutSpinner.setModel(new SpinnerNumberModel(2, 1, null, 1));
        bailoutSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                bailout = (Integer) bailoutSpinner.getValue();
                bailoutSquared = bailout * bailout;
            }
        });

        crSpinner.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.01d)));
        crSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                c.r = (Double) crSpinner.getValue();
            }
        });
        ciSpinner.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.01d)));
        ciSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                c.i = (Double) ciSpinner.getValue();
            }
        });
        double gpuMem = ((double) ((OpenCLDevice) Device.best()).getGlobalMemSize()) / 1024d / 1024d / 1024d;
        final JCheckBox useGpuCBFull = new JCheckBox("Use " + Device.best().getType() + " (" + gpuMem + "Gb) - Full orbit");
        final JCheckBox useGpuCBFast = new JCheckBox("Use " + Device.best().getType() + " (" + gpuMem + "Gb) - Fast");
        useGpuCBFull.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                useGPUFull = useGpuCBFull.isSelected();
                if (useGPUFull) {
                    useGpuCBFast.setSelected(false);
                }
            }
        });
        useGpuCBFast.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                useGPUFast = useGpuCBFast.isSelected();
                if (useGPUFast) {
                    useGpuCBFull.setSelected(false);
                }
            }
        });
        
        JPanel pan = new JPanel(new GridLayout(0, 1));
        
        JPanel p = new JPanel(new GridLayout(0, 2));
        p.add(new JLabel("Iterations:"));
        p.add(iterSpinner);
        pan.add(p);
        
        p = new JPanel(new GridLayout(0, 2));
        p.add(new JLabel("Bailout:"));
        p.add(bailoutSpinner);
        pan.add(p);
        
        pan.add(new JLabel("C:"));
        p = new JPanel(new GridLayout());
        p.add(crSpinner);
        p.add(new JLabel(" + i"));
        pan.add(p);
        p.add(ciSpinner);
        pan.add(p);

        pan.add(useGpuCBFull);
        pan.add(useGpuCBFast);
        
        return pan;
        
//        JPanel cp = new JPanel(new GridLayout());
//        cp.add(crSpinner);
//        cp.add(new JLabel(" + i"));
//        cp.add(ciSpinner);
//
//        JPanel p = new JPanel(new GridLayout(0, 1));
//        p.add(new JLabel("Iterations:"));
//        p.add(iterSpinner);
//        p.add(new JLabel("Bailout:"));
//        p.add(bailoutSpinner);
//        p.add(new JLabel("C:"));
//        p.add(cp);
//        p.add(useGpuCBFull);
//        p.add(useGpuCBFast);
//
//        return p;
    }

    @Override
    public List<Complex> calcOrbit(Complex z) {
        List<Complex> orbit = new ArrayList<>(maxIter);
        orbit.add(z);
        int iter = 1;
        if (exponent.r == 2 && exponent.i == 0) {
            while (z.r * z.r + z.i * z.i < bailoutSquared && iter < maxIter) {
                z = z.square();
                z = z.add(c);
                orbit.add(z);
                iter++;
            }
        } else if (exponent.i == 0 && Math.round(exponent.r) == exponent.r) {
            while (z.r * z.r + z.i * z.i < bailoutSquared && iter < maxIter) {
                for (int i = 1; i < exponent.r; i++) {
                    z = z.mult(z);
                }
                z = z.add(c);
                orbit.add(z);
                iter++;
            }
        }

        return orbit;
    }
    
    public void initGPUKernelForRender(int imageWidth, int imageHeight) {
        if (useGPUFull) {
            if (juliaGPUKernelFull == null) {
                juliaGPUKernelFull = new JuliaGPUKernelFull();
            }

            // Calculate optimal subImageSize
            long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();
            subImageWidth = imageWidth * 2;
            subImageHeight = imageHeight * 2;
            long maxMemImage = Long.MAX_VALUE;
            long arrayLengthRequired = Long.MAX_VALUE;
            while (maxMemImage > gpuMemAvailable || arrayLengthRequired > Integer.MAX_VALUE) {
                if (subImageWidth > 1) subImageWidth /= 2;
                if (subImageHeight > 1) subImageHeight /= 2;
                maxMemImage = (long) subImageHeight * (long) subImageWidth * (long) maxIter * (long) Double.BYTES * 2L;
                arrayLengthRequired = subImageWidth * subImageHeight * maxIter;
            }
            System.out.println("subImage size: " + subImageWidth + "x" + subImageHeight);

            juliaGPUKernelFull.initForRender(subImageWidth, subImageHeight, maxIter, bailoutSquared, c);
        } else if (useGPUFast) {
            if (juliaGPUKernelFast == null) {
                juliaGPUKernelFast = new JuliaGPUKernelFast();
            }
            
            // Calculate optimal subImageSize
            long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();
            subImageWidth = 640;
            subImageHeight = 480;
            long maxMemImage = Long.MAX_VALUE;
            long arrayLengthRequired = Long.MAX_VALUE;
            while (maxMemImage > gpuMemAvailable || arrayLengthRequired > Integer.MAX_VALUE) {
                if (subImageWidth > 1) subImageWidth /= 2;
                if (subImageHeight > 1) subImageHeight /= 2;
                maxMemImage = (long) subImageHeight * (long) subImageWidth * 2 * (long) Double.BYTES * 2L;
                arrayLengthRequired = subImageWidth * subImageHeight;
            }
            System.out.println("subImage size: " + subImageWidth + "x" + subImageHeight);

            juliaGPUKernelFast.initForRender(subImageWidth, subImageHeight, maxIter, bailoutSquared, c);
        }
    }
    
    public void doRunGPU(int xOffset, int yOffset, ImagePlaneMapper imagePlaneMapper, double xSubSamplePos, double ySubSamplePos, int subSamples) {
        if (useGPUFull) {
            juliaGPUKernelFull.initArrays(xOffset, yOffset, imagePlaneMapper, xSubSamplePos, ySubSamplePos, subSamples);
            Range range = Range.create2D(juliaGPUKernelFull.getSubImageWidth(), juliaGPUKernelFull.getSubImageHeight());
            juliaGPUKernelFull.execute(range);
        } else if (useGPUFast) {
            juliaGPUKernelFast.initArrays(xOffset, yOffset, imagePlaneMapper, xSubSamplePos, ySubSamplePos, subSamples);
            Range range = Range.create2D(juliaGPUKernelFast.getSubImageWidth(), juliaGPUKernelFast.getSubImageHeight());
            juliaGPUKernelFast.execute(range);
        }
    }

    public RawGpuOrbitContainer getRawGpuOrbitContainer() {
        return juliaGPUKernelFull.getRawGpuOrbitContainer();
    }
    
    public Complex getLastOrbitPoint(int x, int y) {
        return juliaGPUKernelFast.getLastOrbitPoint(x, y);
    }
    
    public int getOrbitLength(int x, int y) {
        return juliaGPUKernelFast.getOrbitLength(x, y);
    }

    public double getBailout() {
        return bailout;
    }

    public Complex getExponent() {
        return exponent;
    }

    public Complex getC() {
        return c;
    }

    @Override
    public int getMaxIter() {
        return maxIter;
    }

    @Override
    public boolean isBailoutReached(List<Complex> orbit) {
        Complex last = orbit.get(orbit.size() - 1);
        return last.r * last.r + last.i * last.i >= bailoutSquared;
    }

    @Override
    public boolean isBailoutReachedByLastOrbitPoint(Complex lastOrbitPoint) {
        return lastOrbitPoint.r * lastOrbitPoint.r + lastOrbitPoint.i * lastOrbitPoint.i >= bailoutSquared;
    }

    @Override
    public void setMaxIter(int maxIter) {
        this.maxIter = maxIter;
    }

    @Override
    public Pair<Complex, Complex> getDefaultView() {
        Complex p1 = new Complex(-2, 1.5);
        Complex p2 = new Complex(2, -1.5);
        return new Pair<>(p1, p2);
    }

    public void setBailout(double bailout) {
        this.bailout = bailout;
    }

    public void setExponent(Complex exponent) {
        this.exponent = exponent;
    }

    public void setC(Complex c) {
        this.c = c;
        crSpinner.setValue(c.r);
        ciSpinner.setValue(c.i);
    }

    boolean isUseGPUFull() {
        return useGPUFull;
    }
    
    boolean isUseGPUFast() {
        return useGPUFast;
    }

    public int getSubImageWidth() {
        return subImageWidth;
    }

    public int getSubImageHeight() {
        return subImageHeight;
    }

}
