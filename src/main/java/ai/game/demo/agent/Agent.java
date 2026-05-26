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
    public static char memSafety  = 2; // safety limit (GB) to pause if usable memory subceeds
    private static final FileStore store;

    static
    {
        try {store = Files.getFileStore(Path.of("C:"));}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    private final NodeMap<T> map; // for debugging

    @Getter private T currentState;
    private final ArrayList<Iterator<T>> backlog = new ArrayList<>();
    private final ArrayList<T> memory = new ArrayList<>();
    private T[] alphaBeta;
    public  int maxDepth = 7;

    private Agent(Class<T> c)
    {
        map = NodeMap.of(c);
    }
    public Agent(T state)
    {
        this((Class<T>)state.getClass());
        currentState = NodeMap.get(state);
        backlog.add(Set.of(currentState).iterator());
        alphaBeta = ai.game.demo.agent.State.newAlphaBeta(currentState);
        try {System.out.println((store.getUsableSpace()>>30));}
        catch (IOException e) {}
    }

    public T updateState(T state){return updateState(state,false);}
    public T updateState(T state, boolean pause)
    {
        try
        {
            currentState = currentState.addChild(state); // get potentially equal state from memory since
            //  it could probably already have been evaluated
            //   then cull unreachable states from memory
            pause();
            awaitPause();
            backlog.clear();
            backlog.add(Set.of(currentState).iterator()); // todo: more fluid handling of purging backlog
            alphaBeta = ai.game.demo.agent.State.newAlphaBeta(currentState);
            new Thread(() -> {
                try
                {
                    currentState.makeRoot();
                    if (!pause) unpause();
                }
                catch (OutOfMemoryError ignored)
                {
                    System.out.println("\033[31;1;4m OutOfMemory in Cull \033[0m");
                }
            }).start(); // set a Thread to cull unreachable States
            return currentState;
        }catch (OutOfMemoryError ignored){System.out.println("\033[31;1;4m OutOfMemory in Update \033[0m");}
        return currentState;
    }

    public void start(boolean start){if(start)start();else Stop();}

    public T act(){return act(false);}
    public T act(boolean pause)
    {
        if (stopping()) return currentState;
        while(paused()){} pause(); awaitPause();
        ai.game.demo.agent.State.debugFlag = true;
        updateState(currentState.minMax().furthestAncestor(),pause);
        ai.game.demo.agent.State.debugFlag = false;
        return currentState;
    }

    private boolean memFlag = false; // used for noting in console when stalling because of memory
    @SneakyThrows public boolean noMemory(){return store.getUsableSpace()>>30<memSafety;}
    protected void loop()
    {
        if (noMemory()) {System.out.println("\033[33;3m No Memory - stalling \033[0m");memFlag=true;}
        while(noMemory()) if (pausing()) return; onSpinWait();
        if (memFlag) {System.out.println("\033[33;3m Memory Released - running \033[0m");memFlag=false;}
        if (stopping()) return;
        if (backlog.isEmpty()) {System.out.println("\033[31;1;4m Backlog Exhausted \033[0m");Stop();return;}

        iterativeDeepening();
    }

    private int w = 0, depth = 0;
    private void iterativeDeepening()
    {
        if (backlog.getFirst().hasNext()) // if there are unrealized children of State being processed
        {
            try
            {
                T state = backlog.getFirst().next(); // get next State in layer.
                if (state.depth() != depth)
                {
                    depth = state.depth();
                    System.out.println("depth: " + depth);
                }
                state.minMax(alphaBeta);            // realize with children *limited by Alpha/Beta*. note: a given child may already exist and even be realized through another parent State.
                backlog.add(state.iterator());     // que list of children for processing. note: may be empty
                if(w>25)
                {
                    backlog.removeFirst();
                    w = 0;
                }
                else w++;
            }
            catch (OutOfMemoryError ignored)
            {
                System.out.println("\033[31;1;4m OutOfMemory in Loop \033[0m");

                try {System.out.println((store.getUsableSpace()>>30));}
                catch (IOException e) {}
            }
        }                                     // note: all iterators of States at a given depth follow immediately after each other and considers priority with regard to minMax
        else backlog.removeFirst(); // when all immediate children of State being processed has been realized, pop State from que.
    }

    private void depthFirst()
    {
        if (backlog.size() < maxDepth && backlog.getLast().hasNext())
        {
            T state = backlog.getLast().next().minMax(); // find most suitable child for State being processed
            state.minMax(alphaBeta);                    // realize child with its own children
            backlog.add(state.iterator());             // set child to be processed
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
