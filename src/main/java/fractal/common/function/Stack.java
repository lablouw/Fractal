/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.function;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cp316928
 */
public class Stack<T> extends ArrayList<T>{
    
    public void push(T t) {
        add(t);
    }
    
    public T pop() {
        return remove(size()-1);
    }
    
    public T peek() {
        return get(size()-1);
    }
    
}
