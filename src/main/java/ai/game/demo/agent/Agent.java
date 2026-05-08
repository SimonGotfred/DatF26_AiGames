package ai.game.demo.agent;

import ai.game.demo.util.NodeMap;
import ai.game.demo.util.PausableThread;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Agent<T extends State<T>> extends PausableThread
{
    private static final int maxDepth = 7;
    public static char memSafety  = 2; // safety limit (GB) to pause if usable memory subceeds
    private static final FileStore store;

    static
    {
        try {store = Files.getFileStore(Path.of("C:"));}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @Getter private T currentState;
    private final NodeMap<T> map;
    private final ArrayList<Iterator<T>> backlog = new ArrayList<>();
    private final ArrayList<T> memory = new ArrayList<>();
    private T[] alphaBeta;

    private Agent(Class<T> c) {map = NodeMap.of(c);}
    public Agent(T state)
    {
        this((Class<T>)state.getClass());
        currentState = NodeMap.get(state);
        backlog.add(Set.of(currentState).iterator());
        alphaBeta = ai.game.demo.agent.State.getAlphaBeta(currentState);
    }

    public T determine()
    {
        pause();
        return updateState(currentState.fittestChild());
    }

    public T updateState(T state){return updateState(state,false);}
    public T updateState(T state, boolean pause)
    {
        currentState = NodeMap.get(state); // get potentially equal state from memory since
                                          //  it could probably already have been evaluated
                                         //   then cull unreachable states from memory
        pause(); while(!paused()){}
        backlog.clear(); backlog.add(Set.of(currentState).iterator());
        alphaBeta = ai.game.demo.agent.State.getAlphaBeta(currentState);
        new Thread(()->{currentState.makeRoot();if(!pause)unpause();}) // set a Thread to cull unreachable States
                  .start();
        return currentState;
    }

    public T act(){return act(false);}
    public T act(boolean pause)
    {
        while(paused()){} pause(); while(!paused()){}
        ai.game.demo.agent.State.debugFlag = true;
        updateState(currentState.minMax().furthestAncestor(),pause);
        ai.game.demo.agent.State.debugFlag = false;
        return currentState;
    }

    private boolean memFlag = false;
    @SneakyThrows public boolean noMemory(){return store.getUsableSpace()>>30<memSafety;}
    protected void loop()
    {
        if (noMemory()) {System.out.println("\033[33;3m No Memory - spinning \033[0m");memFlag=true;}
        while(noMemory()) if (pausing()) return; onSpinWait();
        if (memFlag) {System.out.println("\033[33;3m Memory Released - running \033[0m");memFlag=false;}
        if (stopping()) return;
        if (backlog.isEmpty()) {System.out.println("\033[31;1;4m Backlog Exhausted \033[0m");Stop();return;}

//        printBacklog();

        if (backlog.size() < maxDepth && backlog.getLast().hasNext())
        {
            T t = backlog.getLast().next().minMax();
            t.minMax(alphaBeta);
            backlog.add(t.children.iterator());

            if (alphaBeta[0].fitness()>=alphaBeta[1].fitness()) backlog.removeLast();
        }
        else backlog.removeLast();
    }

    private void printBacklog()
    {
        System.out.println(Arrays.toString(backlog.toArray())
                                 .replace(" java.util.LinkedHashMap$LinkedKeyIterator","")
                                 .replace("java.util.ImmutableCollections$Set12$1","")
                                 .replace(",","-")
                                 .replace("[","")
                                 .replace("]",""));
    }
}
