package ai.game.demo.agent;

import ai.game.demo.util.NodeMap;

import java.util.*;
import java.util.function.Function;

public abstract class State<T extends State<T>> extends NodeMap.Node<T>
{
    public static boolean debugFlag = false;

    public  static final State<?> MIN_STATE = artificialState(Integer.MIN_VALUE);
    public  static final State<?> MAX_STATE = artificialState(Integer.MAX_VALUE);
    public  static <T extends State<T>> T[] newAlphaBeta(T t){return (T[])new State[]{MIN_STATE, MAX_STATE};}
    private static State<?> artificialState(int fitness)
    {
        return new State(fitness)
        {
            @Override protected int hashIdentifier()  {return fitness;}
            @Override protected int evaluateFitness() {return fitness;}
            @Override public LinkedHashSet<Actionable<?>> getActionables(boolean minMax) {return new LinkedHashSet<>();}
        };
    }

    public static int         compare(State a,State b) {return a.compareTo(b);}
    public static < T extends State<T>> T max(T a,T b) {return a.fitness()>b.fitness()?a:b;}
    public static < T extends State<T>> T min(T a,T b) {return a.fitness()<b.fitness()?a:b;}

    // require subclasses define their own applicable 'Actions'
    public abstract static class Action<T extends State<T>> implements Function<T, T>, Comparable<Action<T>>
    {
        private   final  T  state;
        private   Integer fitness;
        public    Action(T state) {this.state=state;}
        public    final  T apply(){return state.addChild(this.apply(state));}
        protected abstract int evaluateFitness();
        public    final    int fitness(){return fitness == null ? fitness = evaluateFitness() : fitness;}
        public    final    int compareTo(Action<T> other){return this.fitness()==other.fitness()?1:other.fitness()-this.fitness();}
    }

    public abstract static class Actionable<T extends State<T>> implements Comparable<Actionable<T>>
    {
        public abstract TreeSet<Action<T>> actions();
    }

    protected     Integer fitness;
//    public  final Iterator<T> iterator;
//    public  final LinkedHashSet <Action<T>> actions = new LinkedHashSet<>();
//    public  final Iterator<Action<T>> actionIterator = actions.iterator();

    public  State(){}
    private State(int fitness){this.fitness = fitness;}
//    protected State(){this.iterator = children.iterator();} // children.descendingIterator()

//    protected boolean alternator()          {return depth()%2 == 0;} // useful for determining whether min-/max-ing
//    protected int     alternator(int cycles){return depth()%cycles;}

    public Iterator<T> iterator() {return maximize() ? children.iterator() : children.descendingIterator();}

    // evaluating fitness must be done by subclass and may be cumbersome, but should be a consistent value
    // therefore ensure it is done only once
    protected abstract int evaluateFitness();
    public    final    int fitness(){return fitness == null ? fitness = evaluateFitness() : fitness;}

    // own fitness is ignored in preference of best/worst fitness the state *could* lead to
    public final int min   (){return children.isEmpty() ? fitness() : children.getFirst().min();}
    public final int max   (){return children.isEmpty() ? fitness() : children.getLast ().max();}
//    public final int minMax(){return children.isEmpty() ? fitness() : alternator()
//                                   ? children.getFirst().minMax()
//                                   : children.getLast() .minMax()   ;}

    public boolean maximize()               {return true;}
    public boolean minimize()               {return !maximize();}
    public final T minMax(int depth)        {return minMax(newAlphaBeta((T)this), depth);}
    public final T minMax()                 {return minMax(null,0);} // 'ab=null' works out here as 'ab' is not used if depth < 1
    public final T minMax(T[] ab)           {return minMax(ab,1);}
    public final T minMax(T[] ab, int depth){return minMax(ab,depth,minimize());}
    public final T minMax(T[] ab, int depth, boolean minMax)
    {
        try
        {
            depth--;
            if (!children.isEmpty()) return (minMax ? children.getFirst() : children.getLast()).minMax(ab, depth, minMax); // skip calculations if already processed
            return depth < 0 ? (T)this : minMax ? min(ab, depth) : max(ab, depth);
        }
        catch (StackOverflowError e) {System.out.println("\033[31;1;4m StackOverflow in Diving \033[0m");/**/ return (T)this;}
    }

    private T min(T[]ab){return min(ab,0);}
    private T min(T[]ab,int depth)
    {
        T eval = (T)MAX_STATE;
        for (Actionable<T> actionable : getActionables(true))
        {
            for (Action<T> action : actionable.actions().reversed())
            {
                eval = eval.min(action.apply().minMax(ab,depth)); // 'apply' fetches *already existing* State, if duplicate
                ab[0] = ab[0].min(eval);
                if (ab[0].fitness()>=ab[1].fitness()) return eval;
            }
        }
        return eval;
    }

    private T max(T[]ab){return max(ab,0);}
    private T max(T[]ab,int depth)
    {
        T eval = (T)MIN_STATE;
        for (Actionable<T> actionable : getActionables(false))
        {
            for (Action<T> action : actionable.actions())
            {
                eval = eval.max(action.apply().minMax(ab,depth)); // 'apply' fetches *already existing* State, if duplicate
                ab[1] = ab[1].max(eval);
                if (ab[0].fitness()>=ab[1].fitness()) return eval;
            }
        }
        return eval;
    }

    public T apply(Action<T>  action){return action.apply((T)this);}
    public abstract LinkedHashSet<Actionable<T>> getActionables(boolean minMax);
    public final    LinkedHashSet<Actionable<T>> getActionables(){return getActionables(minimize());}

    public final T         max(T other) {return fitness()>other.fitness() ? (T)this : other;}
    public final T         min(T other) {return fitness()<other.fitness() ? (T)this : other;}
    public final int compareTo(T other) {return this.fitness()==other.fitness()?super.compareTo(other):other.fitness()-this.fitness();}
    // returns '1' if equal fitness, because TreeSet otherwise will consider the States 'equal'
}
