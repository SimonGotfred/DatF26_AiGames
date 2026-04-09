package math;

import java.util.ArrayList;
import java.util.List;

public class Vector<T extends Number> extends ArrayList<T> implements Comparable<Vector>
{
    @SafeVarargs
    public Vector(T... coordinates) {super.addAll(List.of(coordinates));}
    public Vector(Vector<T> vector) {super.addAll(vector);}
    public Vector() {}

    public int    getInt   (int index) {return this.get(index).intValue();}
    public double getDouble(int index) {return this.get(index).doubleValue();}

    public T x() {return this.get(0);}
    public T y() {return this.get(1);}
    public T z() {return this.get(2);}

    public T get(int index)
    {
        try {return super.get(index);}
        catch (IndexOutOfBoundsException e)
        {return sub(getFirst(),getFirst());} // returns 0 if out of bounds
    }

    public double distanceTo(Number ... coordinates) {return this.sub(coordinates).length();}
    public double distanceTo(Vector<Number> that)    {return this.sub(that).length();}
    public double length()
    {
        double buffer = .0;
        for (Number axis : this)
        {buffer += axis.doubleValue()*axis.doubleValue();}
        return java.lang.Math.sqrt(buffer);
    }

    public Class<? extends Number> getTypeClass() {return this.getFirst().getClass();}
    protected T toType(Number n)
    {
        Number t;

        if (this.getTypeClass().isInstance(0))
        {
            t = n.intValue();
        }
        else
        {
            t = n.doubleValue();
        }

        try
        {
            return (T) t.getClass().cast(t);
        }
        catch (ClassCastException e)
        {
            return null;
        }
    }

    protected final T add(Number a, Number b) {return this.toType(a.doubleValue() + b.doubleValue());}
    protected final T sub(Number a, Number b) {return this.toType(a.doubleValue() - b.doubleValue());}

    public Vector<T> add(Number ... coordinates) {return this.add(new Vector<>(coordinates));}
    public Vector<T> sub(Number ... coordinates) {return this.sub(new Vector<>(coordinates));}

    public Vector<T> add(Vector<Number> vector)
    {
        Vector<T> _new = new Vector<>();
        for (int i = 0; i < vector.size(); i++)
        {_new.add(this.add(this.get(i), vector.get(i)));}
        return _new;
    }

    public Vector<T> sub(Vector<Number> vector)
    {
        Vector<T> _new = new Vector<>();
        for (int i = 0; i < vector.size(); i++)
        {_new.add(this.sub(this.get(i), vector.get(i)));}
        return _new;
    }

    public Vector<T> reversed()
    {
        Vector<T> _new = new Vector<>();
        for (Number n : this) {_new.add(-n.doubleValue());}
        return _new;
    }

    @SafeVarargs
    public final void setCoordinates(T... coordinates){this.setCoordinates(new Vector<>(coordinates));}
    public       void setCoordinates(Vector<T> coordinates) {this.clear(); this.addAll(coordinates);}

    @Override
    public int compareTo(Vector that)
    {
        double diff = this.size() - that.size();
        if (diff!=.0) return (int)diff;
        for (int i = 0; i < size(); i++)
        {
            diff = this.getDouble(i) - that.getDouble(i);
            if (diff<.0) return -1;
            if (diff>.0) return  1;
        }
        return 0;
    }

    public String toString()
    {
        String format = "%d";
        if (this.getTypeClass().isInstance(0.)) format = "%.2f";

        StringBuilder s = new StringBuilder("()");
        for (T axis : this)
        {
            s.insert(s.length()-1," ");
            s.insert(s.length()-1,String.format(format, axis));
            s.insert(s.length()-1," ,");
        }

        s.deleteCharAt(s.lastIndexOf(","));
        return s.toString();
    }
}
