package agent;

import java.util.TreeSet;

public class Agent<T extends State<T>>
{
    private final TreeSet<T> memory = new TreeSet<>();

    public T act(T state) {return evaluate(state).apply(state);}
    public State.Action<T> evaluate(T state) // todo: algorithm
    {
        if (!memory.add(state)) state = memory.r()
        return null;
    }
}
