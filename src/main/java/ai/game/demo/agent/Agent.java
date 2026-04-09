package agent;

import util.NodeMap;

public class Agent<T extends State<T>>
{
    private T currentState;
    private final NodeMap<T> memory;
    public  Agent(Class<T> c){memory = NodeMap.of(c);}

    public T act(T state) {return evaluate(state).apply(state);}
    public State.Action<T> evaluate(T state) // todo: algorithm
    {
        updateState(state);
        return null;
    }

    public void updateState(T state) // ? weave states, that have not been "though" of, together with states in memory
    {                               //  ? currently, if such occurs, entire memory is purged, apart from the new state
        state = memory.get(state); // get potentially equal state from memory since
        currentState.cull(state); //  it could probably already have been evaluated
        currentState = state;    //   then cull unreachable states from memory
    }
}
