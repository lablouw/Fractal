/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fractal.common;

/**
 *
 * @author Lloyd
 * @param <T1>
 * @param <T2>
 */
public class Pair<T1,T2>
{
    private T1 o1;
    private T2 o2;

    public Pair(T1 o1, T2 o2)
    {
        this.o1 = o1;
        this.o2 = o2;
    }

    public T1 getFirst()
    {
        return o1;
    }

    public T2 getSecond()
    {
        return o2;
    }

    public void setFirst(T1 first)
    {
        this.o1 = first;
    }

    public void setSecond(T2 second)
    {
        this.o2 = second;
    }



    public boolean equals(Pair<T1,T2> other)
    {
        return o1.equals(other.getFirst()) && o2.equals(other.getSecond());
    }
}
