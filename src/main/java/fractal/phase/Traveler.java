/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.phase;

import fractal.common.Complex;
import java.awt.Color;

/**
 *
 * @author Lloyd
 */
public interface Traveler {

    void move();

    Complex getPosition();

    Color getColor();

    public int getAge();
}
