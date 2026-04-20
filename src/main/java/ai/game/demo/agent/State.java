package ai.game.demo.agent;

import ai.game.demo.util.NodeMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

public abstract class State<T extends State<T>> extends NodeMap.Node<T>
{
    private static LocalDateTime timeStart = LocalDateTime.MAX;
    private static int timeLimit=10000;
    public  static final char  memSafety = 2; // safety limit (GB) to stop if usable memory subceeds
    private static final State MIN_STATE = sus(Integer.MIN_VALUE);
    private static final State MAX_STATE = sus(Integer.MAX_VALUE);

    private static void setTimeLimit(int sec) {if (sec<0) return; timeLimit=sec*1000;}
    private static State sus(int fitness)
    {
        return new State(fitness)
        {
            @Override protected int evaluateFitness() {return 0;}
            @Override public Set<Actionable> getActionables(boolean minMax) {return Set.of();}
            @Override protected int hashIdentifier() {return 0;}
            @Override public int compareTo(Object o) {return 0;}
        };
    }

    private static boolean atLimit()
    {
        try {return Files.getFileStore(Path.of("C:")).getUsableSpace()>>30<1+memSafety // stop when usable memory has decreased below safety limit
                 || timeStart.until(LocalDateTime.now(), ChronoUnit.MILLIS)>timeLimit;}
        catch (IOException e) {return true;}
    }

    // require subclasses define their own applicable 'Actions'
    public abstract static class Action<T extends State<T>> implements Function<T, T>, Comparable<Action<T>>
    {
        private final T state;
        public Action(T state){this.state=state;}
        public  final T apply(){return state.addChild(this.apply(state));}
    } // includes shortcut method

    public abstract static class Actionable<T extends State<T>>// todo: implements Comparable<Actionable<T>>
    {
        public abstract Set<Action<T>> actions();
    }

    protected     Integer fitness;
    public  final Iterator<T> iterator;
    public  final Set <Action<T>> actions = new HashSet<>();
    public  final Iterator<Action<T>> actionIterator = actions.iterator();

    private   State(int fitness)   {this(); this.fitness = fitness;}
    protected State(){this.iterator = children.iterator();} // children.descendingIterator()

    protected boolean alternator()          {return depth()%2 == 0;} // useful for determining whether min-/max-ing
    protected int     alternator(int cycles){return depth()%cycles;}

    // evaluating fitness must be done by subclass and may be cumbersome, but should be static
    // therefore ensure it is done only once
    protected abstract int evaluateFitness();
    public    final    int fitness(){return fitness == null ? fitness = evaluateFitness() : fitness;}
    public    final    T   fittestChild()
    {
        return children.stream().max(Comparator.comparingInt(State::fitness)).orElse((T)this);
    }

    // own fitness is ignored in preference of best/worst fitness the state *could* lead to
    public final int min   (){return children.isEmpty() ? fitness() : children.getFirst().min();}
    public final int max   (){return children.isEmpty() ? fitness() : children.getLast ().max();}
//    public final int minMax(){return children.isEmpty() ? fitness() : alternator()
//                                   ? children.getFirst().minMax()
//                                   : children.getLast() .minMax()   ;}

    public    final T minMax(boolean minMax){timeStart=LocalDateTime.now(); return minMax(minMax, 3);}
    public    final T minMax(int depth){return minMax(false, depth);}
    protected final T minMax(boolean minMax, int depth){return minMax((T[])new State[]{MIN_STATE, MAX_STATE}, minMax, depth);}
    protected final T minMax(T[] ab, boolean minMax, int depth)
    {
        depth--;
        if (!children.isEmpty()) return (minMax ? children.getFirst() : children.getLast()).minMax(ab,minMax,depth); // skip calculations if already done // todo: ensure children sorted & account for minMax state
        return depth < 0 || atLimit() ? (T)this : minMax ? min(ab, depth) : max(ab, depth);
    }

    private T min(T[]ab,int depth)
    {
        T eval = (T)MAX_STATE;
        for (Actionable<T> actionable : getActionables(true))
        {
            for (Action<T> action : actionable.actions())
            {
                eval = eval.min(action.apply().minMax(ab,false,depth)); // 'apply' returns *already existing* State, if
                // present
                ab[0] = ab[0].min(eval);
                if (ab[0].fitness()>=ab[1].fitness()) return eval;
            }
        }
        return eval;
    }

    private T max(T[]ab,int depth)
    {
        T eval = (T)MIN_STATE;
        for (Actionable<T> actionable : getActionables(false))
        {
            for (Action<T> action : actionable.actions())
            {
                eval = eval.max(action.apply().minMax(ab,true,depth));
                ab[1] = ab[1].max(eval);
                if (ab[0].fitness()>=ab[1].fitness()) return eval;
            }
        }
        return eval;
    }

    public T apply(Action<T>  action){return action.apply((T)this);}

    // note: child is NOT appended - but put sorted by fitness
    public T evaluateNextAction() {return addChild(nextFittestAction().apply());}
    public Set<Action<T>> evaluateNextChild() {return nextFittestChild().actions();}
    public Set<T>    evaluate() {return evaluate(0);}
    public Set<T>    evaluate(int depth) // todo: minMax
    {
        try {if (Files.getFileStore(Path.of("C:")).getUsableSpace()>>30<1+memSafety) throw new OutOfMemoryError();} // stop when usable memory has decreased below safety limit
        catch (IOException e) {return children;}
        Set<T> set = depth>1 ? new HashSet<>() : children;
        for (Action<T> action : actions()) {addChild(action.apply());}
        if (depth>1) children.forEach(child->set.addAll(child.evaluate(depth-1)));
        return set;
    }

    public int compareTo(T other) {return this.fitness()-other.fitness();}

    /* todo: find out exactly how iterators interact with 'ConcurrentSkipListSet'
       ? sorts high to low or opposite
       ? where iterator ends up when adding to set
       ? would iterator miss better fit entries if ahead of new additions
     */
    public boolean   hasMoreActions   (){return actionIterator.hasNext();}
    public Action<T> nextFittestAction(){return actionIterator.next();}
    public boolean   hasMoreChildren  (){return iterator.hasNext();}
    public        T  nextFittestChild (){return iterator.next();}
    public abstract  Set<Actionable<T>> getActionables(boolean minMax); // todo: int
    public Set<Action<T>> actions()
    {
        for (Actionable<T> able : getActionables(true))
            actions.addAll(able.actions());
        return actions;
    }

    /*
    public TreeSet<T> evaluate()
    {
        actions().forEach(action -> children.add(apply(action)));
        children.remove(parent);
        return children;
    }
     */ // ? deprecated

    /*
    public T       nextFittestChild(boolean alternator)
    {
        return alternator ? children.removeFirst() : children.removeLast();
    }
    public T       nextFittestChild(Comparator<T> comparator)
    {
        T next = children.stream().max(comparator).orElseThrow();
        children.remove(next);
        return next;
    }
     */ // ? irrelevant

    public T max(T t) {return fitness()>t.fitness() ? (T)this : t;}
    public T min(T t) {return fitness()<t.fitness() ? (T)this : t;}
}
