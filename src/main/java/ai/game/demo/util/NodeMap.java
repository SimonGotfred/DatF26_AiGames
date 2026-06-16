package ai.game.demo.util;


import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@SuppressWarnings({"unchecked"})
public class NodeMap<T extends NodeMap.Node<T>> extends ConcurrentSkipListMap<Integer, T> // todo: testing & cleanup
{
    private static final HashMap<Class<? extends Node<?>>, NodeMap<?>> clients = new HashMap<>();

    public static <T extends Node<T>> int size(Class<T> c) {
        return clients.containsKey(c) && clients.get(c) != null ? clients.get(c).size() : -1;
    }

    public static <T extends Node<T>> T add(T node) {
        T n = (T) of(node.getClass()).putIfAbsent(node.hashCode(), node);
        return n == null ? node : n;
    }

    public static <T extends Node<T>> void delete(T node) {
        of(node.getClass()).remove(node.hashCode());
    }

    public static <T extends Node<T>> T get(T node) {
        return add(node);
    }

    public static <T extends Node<T>> boolean contains(T node) {
        return of(node.getClass()).containsKey(node.hashCode());
    }

    // ! somewhat breaks for subclasses of *T* - as they will map to their own NodeMap
    public static <T extends Node<T>> NodeMap<T> of(Class<T> c) {
        NodeMap<T> map = (NodeMap<T>) clients.get(c);
        if (map == null) clients.putIfAbsent(c, map = new NodeMap<>());
        return map;
    }


    // ! Map instead of Set - to facilitate retrieving an *already present* node
    // ! to substitute *equal* nodes that are *not* the same Object in memory
    //   note: reference to "static" Map for class 'T' in static Map 'clients'
    //         should emulate "static" field per type 'T'

    private NodeMap() {
    }  // only to be used by subclasses

    public abstract static class Node<T extends Node<T>> implements Comparable<T> {
        protected final LinkedHashSet<T> parents = new LinkedHashSet<>();
        protected final TreeSet<T> children = new TreeSet<>();

        public int depth() {
            try {
                return parents.isEmpty() ? 0 : 1 + parents.getFirst().depth();
            } catch (StackOverflowError ignored) {
                System.out.println("\033[31;1;4m StackOverflow in Depth \033[0m");
                return 0;
            }
        }



        public void addParent(T parent) {
            parents.add(parent);
        }

        public T addChild(T child) {
            child = add(child);  // substitute for potentially *equal* node already in map
            if (legacy().contains(child)) return child; // avoid infinite loops
            child.addParent((T) this);
            children.add(child);
            return child;        // return child that is *verifiably* in map
        }

        public void makeRoot() {
            for (Node<T> node : parents) node.cull(this);
            parents.clear();
        }

        private void cull(Node<?> newRoot) {
            if (this == newRoot) return;
            if (parents.isEmpty()) {
                try {
                    NodeMap.delete((T) this);
                    for (Node<?> child : children) {
                        child.parents.remove(this);
                    }
                    for (Node<?> child : children) {
                        if (NodeMap.contains((T) child)) child.cull(newRoot);
                    }
                } catch (StackOverflowError ignored) {
                    System.out.println("\033[31;1;4m StackOverflow in Culling \033[0m");
                }
            }
        }

        public T furthestAncestor() {
            try {
                return parents.isEmpty() || parents.getFirst().parents.isEmpty() ? (T) this : parents.getFirst().furthestAncestor();
            } catch (StackOverflowError e) {
                System.out.println("\033[31;1;4m StackOverflow in Ascending \033[0m");
                return (T) this;
            }
        }

        public LinkedHashSet<T> legacy() {
            try {
                LinkedHashSet<T> legacy = parents.isEmpty() ? new LinkedHashSet<>() : parents.getFirst().legacy();
                legacy.add((T) this); // 'this' will be 'T' or a subclass thereof, due to type bound
                return legacy;
            } catch (StackOverflowError e) {
                System.out.println("\033[31;1;4m StackOverflow in Legacy \033[0m");
                return new LinkedHashSet<>(List.of((T) this));
            }
        }


        protected abstract int hashIdentifier(); // require subclasses define when nodes are equal

        public final int hashCode() {
            return hashIdentifier();
        }

        public int compareTo(T that) {
            return this.hashCode() - that.hashCode();
        }

        public final boolean equals(Object that) // override to avoid duplicate nodes in set
        {
            return this == that
                    || that != null
                    && getClass() != that.getClass()
                    && this.hashCode() == that.hashCode();
        }
    }
}
