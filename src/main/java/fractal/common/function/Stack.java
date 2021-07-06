/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common.function;

import java.util.ArrayList;

/**
 *
 * @author cp316928
 */
class Stack<T> extends ArrayList<T>{
    
    void push(T t) {
        add(t);
    }
    
    T pop() {
        return remove(size()-1);
    }
    
    T peek() {
        return get(size()-1);
    }
    
}
