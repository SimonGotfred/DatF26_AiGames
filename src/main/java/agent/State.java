package agent;

import util.NodeMap;

import java.util.*;

public abstract class State<T extends State<T>> extends NodeMap.Node<T>
{
    // require subclasses define their own applicable 'Actions'
    public abstract static class Action<T extends State<T>> implements Comparable<Action<T>>
    {public final T apply(T state){return state.apply(this);}} // includes shortcut method

    private Integer fitness;
    public  final Iterator<T> iterator;
    public  final TreeSet <Action<T>> actions = new TreeSet<>();
    public  final Iterator<Action<T>> actionIterator = actions.iterator();

    protected State()        {iterator = children.iterator();}
    protected State(T parent){super(parent);
                                 iterator = alternator()                    // children can be iterated reversed as
                                          ? children.iterator()             // estimation for a "countering" action
                                          : children.descendingIterator();} //

    boolean    alternator()          {return depth()%2 == 0;} // useful for determining min/max likelihood
    int        alternator(int cycles){return depth()%cycles;}

    protected abstract int evaluateFitness();
    public    final    int fitness(){return fitness == null ? fitness = evaluateFitness() : fitness;}

    public final int[] minMax(){return new int[]{min(),max()};}
    public final int   min   (){return children.isEmpty() ? fitness() : children.getFirst().min();}
    public final int   max   (){return children.isEmpty() ? fitness() : children.getLast().max();}

    public abstract T apply(Action<T>  action);
    public abstract TreeSet<Action<T>> actions();
    public T evaluateNextAction()
    {
        T child = apply(nextFittestAction());
        children.add(child); // note: child is NOT appended - but put sorted by fitness
        return child;
    }
    public TreeSet<Action<T>> evaluateNextChild() {return nextFittestChild().actions();}

    /*
    public TreeSet<T> evaluate()
    {
        actions().forEach(action -> children.add(apply(action)));
        children.remove(parent);
        return children;
    }
     */ // ? deprecated

    /* todo: find out exactly how iterators interact with 'ConcurrentSkipListSet'
       ? sorts high to low or opposite
       ? where iterator ends up when adding to set
       ? would iterator miss better fit entries if ahead of new additions
     */
    public boolean   hasMoreActions   (){return actionIterator.hasNext();}
    public Action<T> nextFittestAction(){return actionIterator.next();}
    public boolean   hasMoreChildren  (){return iterator.hasNext();}
    public        T  nextFittestChild (){return iterator.next();}

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
