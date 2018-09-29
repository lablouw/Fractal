/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

/**
 *
 * @author lloyd
 */
public interface Antialiasable
{
    public static final int NONE = 1;
    public static final int AA2 = 2;
    public static final int AA3 = 3;
    public static final int AA4 = 4;
    
    public int getAA();
    public void setAA(int aa);
}
