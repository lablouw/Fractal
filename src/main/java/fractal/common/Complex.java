/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

/**
 *
 * @author lloyd
 */
public class Complex
{
    public double r=0, i=0;
    private static final double EPSILON = 1E-15;
    public static final Complex ONE = new Complex(1, 0);

    public Complex(double r, double i) {
        this.r = r;
        this.i = i;
    }
    
    public Complex(Complex other)
    {
        this.r = other.r;
        this.i = other.i;
    }

    public Complex() {
    }
    
    public Complex sub(Complex other)
    {
        return new Complex(r-other.r, i-other.i);
    }
    
    public Complex add(Complex other)
    {
        return new Complex(r+other.r, i+other.i);
    }
    
    public Complex mult(Complex other)
    {
        return new Complex(r*other.r - i*other.i, r*other.i + i*other.r);
    }
    
    public Complex div(Complex w) {
        double d = w.r*w.r+w.i*w.i;
        double newR = (r*w.r+i*w.i)/d;
        double newI = (i*w.r-r*w.i)/d;
        return new Complex(newR, newI);
    }
    
    public Complex exp() {
        return new Complex(Math.exp(r)*Math.cos(i),Math.exp(r)*Math.sin(i));
    }
    
    public Complex log() {
        return new Complex(Math.log(this.modulus()),this.arg());
    }
    
    public Complex sqrt() {
        double r=Math.sqrt(this.modulus());
        double theta=this.arg()/2;
        return new Complex(r*Math.cos(theta),r*Math.sin(theta));
    }
    
    private double cosh(double theta) {
        return (Math.exp(theta)+Math.exp(-theta))/2;
    }
    
    private double sinh(double theta) {
        return (Math.exp(theta)-Math.exp(-theta))/2;
    }
    
    public Complex sin() {
        return new Complex(cosh(i)*Math.sin(r),sinh(i)*Math.cos(r));
    }
    
    public Complex cos() {
        return new Complex(cosh(i)*Math.cos(r),-sinh(i)*Math.sin(r));
    }
    
    public Complex sinh() {
        return new Complex(sinh(r)*Math.cos(i),cosh(r)*Math.sin(i));
    }
    
    public Complex cosh() {
        return new Complex(cosh(r)*Math.cos(i),sinh(r)*Math.sin(i));
    }
    
    public Complex tanh() {
        return new Complex(sinh().div(cosh()));
    }
    
    public Complex tan() {
        return (sin()).div(cos());
    }
    
    public Complex cosec() {
        return ONE.div(sin());
    }

    public Complex sec() {
        return ONE.div(cos());
    }

    public Complex cot() {
        return ONE.div(tan()); 
    }

    public Complex cosech() {
        return ONE.div(sinh());
    }

    public Complex sech() {
        return ONE.div(cosh());
    }

    public Complex coth() {
        return ONE.div(tanh());
    }
    
    public Complex negate() {
        return new Complex(-r,-i);
    }
    
    public double modulus()
    {
        if (r!=0 || i!=0) {
            return Math.sqrt(r*r+i*i);
        } else {
            return 0;
        }
    }
    
    public double real() {
        return r;
    }
    
    public double imag() {
        return i;
    }
    
    public double arg() {
        return Math.atan2(i,r);
    }
    
    public Complex conj() {
        return new Complex(r,-i);
    }
    
    public boolean equals(Complex other)
    {
        return r==other.r && i==other.i;
    }
    
    @Override
    public String toString()
    {
        if (r==-0.0) {
            r=0;
        }
        if (i==-0.0) {
            i=0;
        }
        return r+((i<0) ? ("-i"+(-i)) : ("+i"+i));
    }

    public String toStringZeroFill() {
        String R = String.format("%.16f",r);
        String I = String.format("%.16f", Math.abs(i));
        return R+" "+((i<0) ? "-i"+(I) : "+i"+I);
    }

    public boolean isEpsilonZero() {
        return modulus() < EPSILON;
    }

    public Complex normalize() {
        double n = Math.sqrt(r*r+i*i);
        return new Complex(r/n, i/n);
    }

    public Complex square() {
        return new Complex(r*r - i*i, 2*r*i);
    }

}
