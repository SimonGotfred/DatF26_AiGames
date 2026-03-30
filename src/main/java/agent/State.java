package agent;

import util.NodeMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public abstract class State<T extends State<T>> extends NodeMap.Node<T>
{
    public static char memSafety = 2; // safety limit (GB) to stop if usable memory subceeds

    // require subclasses define their own applicable 'Actions'
    public abstract static class Action<T extends State<T>> implements Function<T, T>, Comparable<Action<T>>
    {
        private final T state;
        public Action(T state){this.state=state;}
        public  final T apply(){return this.apply(state);}
    } // includes shortcut method

    public abstract static class Actionable<T extends State<T>>// todo: implements Comparable<Actionable<T>>
    {
        public abstract Set<Action<T>> actions();
    }

    public  final boolean minMax;
    protected     Integer fitness;
    public  final Iterator<T> iterator;
    public  final TreeSet <Action<T>> actions = new TreeSet<>();
    public  final Iterator<Action<T>> actionIterator = actions.iterator();

    protected State()              {this.minMax   = false; iterator = children.iterator();}
    protected State(boolean minMax){this.minMax   = minMax;
                                    this.iterator = this.minMax                     // children can be iterated in reverse as
                                                  ? children.iterator()             // estimation for a "countering" action
                                                  : children.descendingIterator();} //

    protected boolean alternator()          {return depth()%2 == 0;} // useful for determining whether min-/max-ing
    protected int     alternator(int cycles){return depth()%cycles;}

    // evaluating fitness must be done by subclass and may be cumbersome, but should be static
    // therefore ensure it is done only once
    public    final    int fitness(){return fitness == null ? fitness = evaluateFitness() : fitness;}
    protected abstract int evaluateFitness();

    // own fitness is ignored in preference of best/worst fitness the state *could* lead to
    public final int   min   (){return children.isEmpty() ? fitness() : children.getFirst().min();}
    public final int   max   (){return children.isEmpty() ? fitness() : children.getLast ().max();}
    public final int[] minMax(){return new int[]{min(),max()};}

    public T apply(Action<T>  action){return action.apply((T)this);}

    // note: child is NOT appended - but put sorted by fitness
    public T evaluateNextAction() {return addChild(nextFittestAction().apply());}
    public TreeSet<Action<T>> evaluateNextChild() {return nextFittestChild().actions();}
    public NavigableSet<T>    evaluate() {return evaluate(0);}
    public NavigableSet<T>    evaluate(int depth) // todo: minMax
    {
        try {if (Files.getFileStore(Path.of("C:")).getUsableSpace()>>30<1+memSafety) throw new OutOfMemoryError();} // stop when usable memory has decreased below safety limit
        catch (IOException e) {return children;}
        NavigableSet<T> set = depth>1 ? new TreeSet<>() : children;
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
    public abstract  Set<Actionable<T>> getActionables();
    public TreeSet<Action<T>> actions()
    {
        for (Actionable<T> able : getActionables())
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
}
