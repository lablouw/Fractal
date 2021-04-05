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
public class MandelbrotEngine extends FractalEngine {

    private Complex exponent = new Complex(2, 0);
    private double bailout = 2;
    private int maxIter = 500;
    private Complex perterbation = new Complex();
    private double bailoutSquared = bailout * bailout;
    private MandelbrotGPUKernelFull mandelbrotGPUKernelFull = null;
    private MandelbrotGPUKernelFast mandelbrotGPUKernelFast = null;
    
    private boolean useGPUFull = false;
    private boolean useGPUFast = false;
    private int subImageWidth;
    private int subImageHeight;

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

        final JSpinner perterbR = new JSpinner();
        perterbR.setModel(new SpinnerNumberModel(0.0d, null, null, 0.01d));
        perterbR.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                perterbation.r = (Double) perterbR.getValue();
            }
        });
        final JSpinner perterbI = new JSpinner();
        perterbI.setModel(new SpinnerNumberModel(0.0d, null, null, 0.01d));
        perterbI.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                perterbation.i = (Double) perterbI.getValue();
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
        
        pan.add(new JLabel("Perterbation:"));
        p = new JPanel(new GridLayout());
        p.add(perterbR);
        p.add(new JLabel(" + i"));
        pan.add(p);
        p.add(perterbI);
        pan.add(p);

        pan.add(useGpuCBFull);
        pan.add(useGpuCBFast);
        
        return pan;
    }

    @Override
    public List<Complex> calcStraightOrbit(Complex c) {
        Complex z = new Complex(perterbation);
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
            if (mandelbrotGPUKernelFull == null) {
                mandelbrotGPUKernelFull = new MandelbrotGPUKernelFull();
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

            mandelbrotGPUKernelFull.initForRender(subImageWidth, subImageHeight, maxIter, bailoutSquared, perterbation);
        } else if (useGPUFast) {
            if (mandelbrotGPUKernelFast == null) {
                mandelbrotGPUKernelFast = new MandelbrotGPUKernelFast();
            }
            
            // Calculate optimal subImageSize
            long gpuMemAvailable = ((OpenCLDevice) Device.best()).getMaxMemAllocSize();
            subImageWidth = 640;
            subImageHeight = 480;
            long maxMemImage = Long.MAX_VALUE;
            long arrayLengthRequired = Long.MAX_VALUE;
            while (maxMemImage > gpuMemAvailable || arrayLengthRequired > Integer.MAX_VALUE && subImageWidth > imageWidth && subImageHeight > imageHeight) {
                if (subImageWidth > 1) subImageWidth /= 2;
                if (subImageHeight > 1) subImageHeight /= 2;
                maxMemImage = (long) subImageHeight * (long) subImageWidth * 2 * (long) Double.BYTES * 2L;
                arrayLengthRequired = subImageWidth * subImageHeight;
            }
            System.out.println("subImage size: " + subImageWidth + "x" + subImageHeight);

            mandelbrotGPUKernelFast.initForRender(subImageWidth, subImageHeight, maxIter, bailoutSquared, perterbation);
        }
    }

    public void doRunGPU(int xOffset, int yOffset, ImagePlaneMapper imagePlaneMapper, double xSubSamplePos, double ySubSamplePos, int subSamples) {
        if (useGPUFull) {
            mandelbrotGPUKernelFull.initArrays(xOffset, yOffset, imagePlaneMapper, xSubSamplePos, ySubSamplePos, subSamples, activeParameterMapper);
            Range range = Range.create2D(mandelbrotGPUKernelFull.getSubImageWidth(), mandelbrotGPUKernelFull.getSubImageHeight());
            mandelbrotGPUKernelFull.execute(range);
        } else if (useGPUFast) {
            mandelbrotGPUKernelFast.initArrays(xOffset, yOffset, imagePlaneMapper, xSubSamplePos, ySubSamplePos, subSamples, activeParameterMapper);
            Range range = Range.create2D(mandelbrotGPUKernelFast.getSubImageWidth(), mandelbrotGPUKernelFast.getSubImageHeight());
            mandelbrotGPUKernelFast.execute(range);
        }
    }

    public RawGpuOrbitContainer getRawGpuOrbitContainer() {
        return mandelbrotGPUKernelFull.getRawGpuOrbitContainer();
    }
    
    public Complex getLastOrbitPoint(int x, int y) {
        return mandelbrotGPUKernelFast.getLastOrbitPoint(x, y);
    }
    
    public int getOrbitLength(int x, int y) {
        return mandelbrotGPUKernelFast.getOrbitLength(x, y);
    }

    public double getBailout() {
        return bailout;
    }

    public void setBailout(double bailout) {
        this.bailout = bailout;
    }

    public Complex getExponent() {
        return exponent;
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
        Complex p1 = new Complex(-2.5, 1.5);
        Complex p2 = new Complex(1.5, -1.5);
        return new Pair<>(p1, p2);
    }

    public void setExponent(Complex exponent) {
        this.exponent = exponent;
    }

    public void setPerterbation(Complex perterbation) {
        this.perterbation = perterbation;
    }

    public Complex getPerterbation() {
        return perterbation;
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
