package util;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodeMap<T extends NodeMap.Node<T>> // todo: testing & cleanup
{
    static final   HashMap <Class<? extends Node<?>>, ConcurrentSkipListMap<Node<?>,? extends Node<?>>> clients = new HashMap<>();
    private static boolean register(Class<? extends Node<?>> c){return clients.putIfAbsent(c, new ConcurrentSkipListMap<>())==null;}

    // ! Map instead of Set - to facilitate retrieving an *already present* node
    // ! to substitute *equal* nodes that are *not* the same Object in memory
    //   note: reference to "static" Map for class 'T' in static Map 'clients'
    //         should emulate "static" field per type 'T'
    private final ConcurrentSkipListMap<T,T> map;
    public    NodeMap(Class<T> c) {register(c); map = map(c);}
    protected NodeMap()           {map =  map((Class<T>)this.getClass());} // only to be used by subclasses

    private ConcurrentSkipListMap<T,T> map(Class<T> c) {return (ConcurrentSkipListMap<T,T>) NodeMap.clients.get(c);}
    public T add   (T node) {T n = map.putIfAbsent(node,node); return n==null ? node : n;}
    public T remove(T node) {return map.remove(node);}
    public T get   (T node) {return add(node);}

    public abstract static class Node<T extends Node<T>> extends NodeMap<T> implements Comparable<T>
    {
        protected ConcurrentSkipListSet<T> parents  = new ConcurrentSkipListSet<>();
        protected ConcurrentSkipListSet<T> children = new ConcurrentSkipListSet<>();

        protected Node() {}
        protected Node(T parent)
        {
            T n = add((T) this);
            parent.addChild(n);
            n.addParent(parent);
        }

        public int     depth (){return parents.isEmpty() ? 0 : 1+parents.first().depth();}
        public boolean delete()
        {
            T t = remove((T)this);
            for (Node<?> parent : parents) {parent.children.remove(this);}
            for (Node<?> child : children) {child.parents.remove(this);}
            return t != null;
        }

        protected boolean addParent(T parent) {return parents.add(parent);}
        protected T addChild (T  child)
        {
            child = add(child);  // substitute for potentially *equal* node that is already in map
            child.addParent((T)this);
            children.add(child); // ? check if somehow child is already in children - should be impossible
            return child;        // return child that is *verifiably* in map
        }

        public void makeRoot() {for (Node<T> node : parents) {node.cull(this);}}
        public void cull(Node<?> newRoot)
        {
            if (this==newRoot) return;
            if (parents.isEmpty())
            {
                remove((T)this);
                for (Node<?> child : children) {child.parents.remove(this);}
                for (Node<?> child : children) {child.cull(newRoot);}
            }
        }
        public void cull()
        {
            if (parents.isEmpty())
            {
                remove((T)this);
                for (Node<?> child : children) {child.parents.remove(this);}
                for (Node<?> child : children) {child.cull();}
            }
        }

        public ConcurrentSkipListSet<T> siblings() {return parents.isEmpty() ? new ConcurrentSkipListSet<>() : parents.first().children;}
        public T furthestAncestor() {return parents.isEmpty() ? (T)this : parents.first().parents.first() == null ? (T)this : parents.first().furthestAncestor();}
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
