/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.Mappers;

import fractal.common.Complex;
import java.awt.Point;


/**
 *
 * @author lloyd
 */
public abstract class Mapper
{
    protected final Complex topLeft;
    protected final Complex bottomRight;
    protected final double xStep;
    protected final double yStep;
    protected final int width;
    protected final int height;

    public Mapper(Complex p1, Complex p2, int width, int height) {
        this.topLeft = p1;
        this.bottomRight = p2;
        this.width = width;
        this.height = height;
        this.xStep = (p2.r-p1.r)/(double)width;
        this.yStep = (p2.i-p1.i)/(double)height;
    }
    
    public abstract Complex mapToComplex(int x, int y);
    
    public abstract Point mapToImage(Complex c);
    
    public Complex getTopLeft() {
        return topLeft;
    }
    public Complex getBottomRight() {
        return bottomRight;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public double getXStep() {
        return xStep;
    }

    public double getYStep() {
        return yStep;
    }
    
}
