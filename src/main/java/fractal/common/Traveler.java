/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.awt.Color;

/**
 * @author Lloyd
 */
public abstract class Traveler {

	public abstract void move();

	protected int age = 0;
	protected Color color = Color.BLACK;
	protected Complex position = new Complex();

	public int getAge() {
		return age;
	}

	public Color getColor() {
		return color;
	}

	public Complex getPosition() {
		return position;
	}

}
