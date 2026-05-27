package ai.game.demo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@SuppressWarnings({"unchecked"})
public class NodeMap<T extends NodeMap.Node<T>> extends ConcurrentSkipListMap<Integer,T> // todo: testing & cleanup
{
    private static final HashMap<Class<? extends Node<?>>, NodeMap<?>> clients = new HashMap<>();

    private static <T extends Node<T>> boolean register(Class<T> c){return clients.putIfAbsent(c, new NodeMap<T>())==null;}
    public  static <T extends Node<T>> int         size(Class<T> c){return clients.containsKey(c) && clients.get(c) != null ? clients.get(c).size() : -1;}
    public  static <T extends Node<T>> void       clear(Class<T> c){   if (clients.containsKey(c) && clients.get(c) != null)  clients.get(c).clear();}

    public  static <T extends Node<T>> T add   (T node) { T n = (T) of(node.getClass()).putIfAbsent(node.hashCode(), node);return n==null ? node : n;}
    public  static <T extends Node<T>> T delete(T node) {return (T) of(node.getClass()).remove(node.hashCode());}
    public  static <T extends Node<T>> T get   (T node) {return add(node);}
    public  static <T extends Node<T>> boolean contains(T node) {return of(node.getClass()).containsKey(node.hashCode());}

    // ! somewhat breaks for subclasses of *T* - as they will map to their own NodeMap
    public  static <T extends Node<T>> NodeMap<T> of(Class<T> c)
    {
        NodeMap<T> map = (NodeMap<T>) clients.get(c);
        if (map==null) clients.putIfAbsent(c,map = new NodeMap<>());
        return map;
    }

    public  static <T extends Node<T>> void output(Class<T> c){for (Node<?> node : clients.get(c).values()) node.output();}
    static  final String outputPath = "C:/Users/Simon/Documents/Obsidian/Games/"; // Markdown files parsable by Obsidian

    // ! Map instead of Set - to facilitate retrieving an *already present* node
    // ! to substitute *equal* nodes that are *not* the same Object in memory
    //   note: reference to "static" Map for class 'T' in static Map 'clients'
    //         should emulate "static" field per type 'T'
//    public  NodeMap(Class<T> c) {register(c); map = map(c);}
    private NodeMap(){}          // {register((Class<T>)this.getClass()); map = map((Class<T>)this.getClass());} // only to be used by subclasses

    public abstract static class Node<T extends Node<T>> implements Comparable<T>
    {
        protected final LinkedHashSet<T> parents  = new LinkedHashSet<>();
        protected final TreeSet<T> children = new TreeSet<>();

        public int depth() // calculate depth from highest parent
        {
            try {return parents.isEmpty() ? 0 : 1 + parents.getFirst().depth();}
            catch (StackOverflowError ignored)
            {
                System.out.println("\033[31;1;4m StackOverflow in Depth \033[0m");
                return 0;
            }
        }

        public int countChildren(){return children.size();}
        public int countParents (){return parents .size();}

        public T getFirstChild (){return children.getFirst();}
        public T getLastChild  (){return children.getLast ();}
        public T getFirstParent(){return parents .getFirst();}
        public T getLastParent (){return parents .getLast ();}

        protected boolean removeChild (T child) {return children.remove( child);}
        protected boolean removeParent(T parent){return parents .remove(parent);}
        public    boolean addParent   (T parent){return parents .add   (parent);}
        public    T       addChild    (T child)
        {
            child = add(child);  // substitute for potentially *equal* node already in map
            if (legacy().contains(child)) return child; // avoid infinite loops
            child.addParent((T)this);
            children.add(child);
            return child;        // return child that is *verifiably* in map
        }

        public void clear()
        {
            parents.clear();
            children.clear();
        }

        public T remove() // remove self from *all* relations - should leave self for garbage collection
        {
            T t = NodeMap.delete((T)this);
            for (Node<?> parent : parents) {parent.children.remove(this);}
            for (Node<?> child : children) { child.parents .remove(this);}
            clear();
            return t;
        }

        public  void makeRoot() {for(Node<T> node : parents) node.cull(this); parents.clear();} // culls nodes that are unreachable without backtracking
        private void cull(Node<?> newRoot) // remove self if unreachable through any parent
        {
            if (this==newRoot) return; // do not alter the node set as new root
            if (parents.isEmpty())
            {
                try
                {
                    NodeMap.delete((T) this);
                    for (Node<?> child : children) {child.parents.remove(this);}
                    for (Node<?> child : children) {child.cull(newRoot);}
                    clear();
                }
                catch (StackOverflowError ignored)
                {
                    System.out.println("\033[31;1;4m StackOverflow in Culling \033[0m");
                }
            }
        }

        public TreeSet<T> siblings() {return parents.isEmpty() ? new TreeSet<>() : parents.getFirst().children;}

        public T furthestAncestor() // gets the first child of root, that leads to this node.
        {
            try
            {
                return parents.isEmpty() || parents.getFirst().parents.isEmpty() ? (T)this : parents.getFirst().furthestAncestor();
            }
            catch (StackOverflowError e)
            {
                System.out.println("\033[31;1;4m StackOverflow in Ascending \033[0m");
                return (T)this;
            }
        }

        public LinkedHashSet<T> legacy() // returns a path of nodes, connecting this to root
        {
            try
            {
                LinkedHashSet<T> legacy = parents.isEmpty() ? new LinkedHashSet<>() : parents.getFirst().legacy();
                legacy.add((T)this); // 'this' will be 'T' or a subclass thereof, due to type bound
                return legacy;
            }
            catch (StackOverflowError e)
            {
                System.out.println("\033[31;1;4m StackOverflow in Legacy \033[0m");
                return new LinkedHashSet<>(List.of((T)this));
            }
        }

        public String toObsidian(){return toString();} // default method returning string formatted for the program obsidian
        public void output() // method writing object to file for use in obsidian todo: belongs in other class
        {
            StringBuilder s = new StringBuilder(this.toObsidian());

//            s.append("\n\nParents: ");
//            for (Node<?> n : this.parents)
//            {
//                s.append(" [[" + n.hashCode() + "]]");
//            }

            s.append("\n\nChildren: ");
            for (Node<?> n : this.children)
            {
                s.append(" [[" + n.hashCode() + "]]");
            }

            try
            {
                Files.write(Path.of(outputPath + this.hashCode() + ".md"), s.toString().getBytes());
            }
            catch (IOException e) {System.out.println("\033[31;1;4moof\033[0m");}
        }

        protected abstract int     hashIdentifier (); // require subclasses define when nodes are equal
        public       final int     hashCode       (){return hashIdentifier();}
        public             int     compareTo(T that){return this.hashCode()-that.hashCode();}
        public       final boolean equals   (Object that) // override to avoid duplicate nodes in set
        {
            return that==this
                || that!=null
                && that.getClass()==this.getClass()
                && that.hashCode()==this.hashCode();
        }
    }
}
