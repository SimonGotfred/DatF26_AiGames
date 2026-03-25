package util;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface NodeMap<T extends NodeMap.Node<T>> // todo: testing & cleanup
{
    static final   HashMap <Class<? extends Node<?>>, ConcurrentSkipListMap<Node<?>,? extends Node<?>>> clients = new HashMap<>();
    private static boolean register(Class<? extends Node<?>> c){return clients.putIfAbsent(c, new ConcurrentSkipListMap<>())==null;}

//    private T root;
//    private final ConcurrentSkipListMap<T,T> map;
//    public NodeMap(T root)     {this((Class<T>)root.getClass()); this.root = root;}
//    public NodeMap(Class<T> c) {register(c); map = (ConcurrentSkipListMap<T,T>) NodeMap.clients.get(c);}

    public default ConcurrentSkipListMap<T,T> map() {return (ConcurrentSkipListMap<T,T>) NodeMap.clients.get(this.getClass());}
    public default T add   (T node) {T n = map().putIfAbsent(node,node); return n==null ? node : n;}
    public default T remove(T node) {return map().remove(node);}

    public abstract static class Node<T extends Node<T>> implements Comparable<T> , NodeMap<T>
    {
        // public    ConcurrentSkipListMap<T,T> map() {return (ConcurrentSkipListMap<T,T>) NodeMap.clients.get(this.getClass());}
        protected ConcurrentSkipListSet<T> parents  = new ConcurrentSkipListSet<>();
        protected ConcurrentSkipListSet<T> children = new ConcurrentSkipListSet<>();

        protected Node() {}
        protected Node(T parent)
        {
            T n = map().putIfAbsent((T) this, (T) this);
            if  (n == null) n = (T)this;
            parent.addChild(n);
            n.addParent(parent);
        }

        protected boolean addChild (T  child) {return children.add( child);}
        protected boolean addParent(T parent) {return parents .add(parent);}
        public    int     depth()  {return parents.isEmpty() ? 0 : 1+parents.first().depth();}

        public ConcurrentSkipListSet<T> siblings() {return parents.isEmpty() ? new ConcurrentSkipListSet<>() : parents.first().children;}
        public T  furthestAncestor() {return parents.isEmpty() ? (T)this : parents.first().parents.first() == null ? (T)this : parents.first().furthestAncestor();}
        public List<T> legacy()
        {
            List<T> legacy = parents.isEmpty() ? new ArrayList<>() : parents.first().legacy();
            legacy.add((T)this); // 'this' will be 'T' or a subclass thereof, due to type bound
            return legacy;
        }

        protected abstract int     hashIdentifier (); // require subclasses define when nodes are equal
        public       final int     hashCode       (){return hashIdentifier();}
        public       final int     compareTo(T that){return this.hashCode() - that.hashCode();}
        public       final boolean equals   (Object that) // override to avoid duplicate nodes in set
        {
            return this==that
                    || that!=null
                    && getClass() != that.getClass()
                    && this.hashCode()==that.hashCode();
        }
    }
}
