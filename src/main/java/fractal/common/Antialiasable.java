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
    int NONE = 1;

    int getSubSamples();
    void setSubSamples(int subSamples);
}
