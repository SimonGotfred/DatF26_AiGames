package ai.game.demo.agent;

import ai.game.demo.util.NodeMap;
import ai.game.demo.util.PausableThread;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

public class Agent<T extends State<T>> extends PausableThread
{
    public static char memSafety  = 2; // safety limit (GB) to pause if usable memory subceeds
    private final FileStore store = Files.getFileStore(Path.of("C:"));

    @Getter private T currentState;
    private final NodeMap<T> memory;
    private final ConcurrentSkipListSet<T> backlog = new ConcurrentSkipListSet<>(Comparator.comparingInt(ai.game.demo.agent.State::fitness));

    public Agent(Class<T> c) throws IOException {memory = NodeMap.of(c);}
    public Agent(T initialState) throws IOException
    {
        this((Class<T>)initialState.getClass());
        currentState=initialState;
        backlog.add(currentState);
    }

    public T determine()
    {
        return updateState(currentState.fittestChild());
    }

    public T updateState(T state) // ? weave states, that have not been "thought" of, together with states in memory
    {                            //  ? currently, if such occurs, entire memory is purged, apart from the new state
        currentState = memory.get(state); // get potentially equal state from memory since
                                         //  it could probably already have been evaluated
                                        //   then cull unreachable states from memory
        new Thread(()->currentState.makeRoot()); // set a Thread to cull now unreachable States
        return currentState;
    }

    public T evaluate()
    {
        T t = determine();
        unpause();
        return t;
    }

    @SneakyThrows public boolean noMemory(){return store.getUsableSpace()>>30<1+memSafety;}
    protected void loop()
    {
        while(noMemory())onSpinWait();
        if (stopping()) return;
        T state = backlog.removeFirst();
        backlog.addAll(state.evaluate().stream().filter(NodeMap.Node::noChildren).toList());
        if (backlog.isEmpty())
        {
            backlog.add(state);
            pause();
        }
    }
}
