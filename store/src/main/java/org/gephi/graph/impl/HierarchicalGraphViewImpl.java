package org.gephi.graph.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraphView;
import org.gephi.graph.api.HierarchicalNodeGroup;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedSubgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HierarchicalGraphViewImpl extends AbstractGraphView implements GraphView, HierarchicalGraphView {
    protected final GraphViewImpl viewDelegate;

    private final GraphVersion version;

    private final HierarchicalGraphDecorator directedDecorator;

    private final HierarchicalGraphDecorator undirectedDecorator;

    private final Set<GraphObserverImpl> observers = new HashSet<GraphObserverImpl>();

    private final HierarchicalNodeGroupImpl root = new HierarchicalNodeGroupImpl(null, null);

    public HierarchicalGraphViewImpl(GraphStore store, boolean nodes, boolean edges) {
        super(store, nodes, edges);
        this.viewDelegate = new GraphViewImpl(store, nodes, edges);
        this.directedDecorator = new HierarchicalGraphDecorator(store, this, false);
        this.undirectedDecorator = new HierarchicalGraphDecorator(store, this, true);
        this.version = graphStore.version != null ? new GraphVersion(directedDecorator) : null;
    }

    public boolean containsNode(final NodeImpl node) {
        if (null == node) {
            return false;
        }

        if (!this.viewDelegate.containsNode(node)) {
            return false;
        }

        return true;
    }

    public boolean visibleNode(final NodeImpl node) {
        if (null == node) {
            return false;
        }

        final Node mapped = this.mapToVisible(node);
        if (null == mapped) {
            return false;
        }

        return mapped == node;
    }

    public boolean addNode(final Node node) {
        if (!this.viewDelegate.addNode(node)) {
            return false;
        }

        return true;
    }

    public boolean removeNode(final Node node) {
        boolean updated = this.viewDelegate.removeNode(node);
        updated |= this.root.removeNode(node, true);
        return updated;
    }

    public boolean addEdge(final Edge edge) {
        if (!this.viewDelegate.addEdge(edge)) {
            return false;
        }

        return true;
    }

    public boolean removeEdge(final Edge edge) {
        if (!this.viewDelegate.removeEdge(edge)) {
            return false;
        }

        return true;
    }

    public boolean containsEdge(final EdgeImpl edge) {
        return this.viewDelegate.containsEdge(edge);
    }

    public int getCollapsedNodeCount() {
        final Set<HierarchicalNodeGroupImpl> visibleGroups = new HashSet<HierarchicalNodeGroupImpl>();
        final Set<HierarchicalNodeGroupImpl> hiddenGroups = new HashSet<HierarchicalNodeGroupImpl>();
        for (final HierarchicalNodeGroup group : this.getGroups()) {
            final HierarchicalNodeGroupImpl impl = (HierarchicalNodeGroupImpl) group;
            if (impl.hasCollapsedParent()) {
                hiddenGroups.add(impl);
                visibleGroups.remove(impl);
            } else if (!hiddenGroups.contains(impl)) {
                visibleGroups.add(impl);
            }
        }
        return hiddenGroups.size();
    }

    public int getNodeCount() {
        return this.viewDelegate.getNodeCount() - this.getCollapsedNodeCount();
    }

    public int getEdgeCount() {
        return this.viewDelegate.getEdgeCount();
    }

    public int getEdgeCount(int type) {
        return this.viewDelegate.getEdgeCount(type);
    }

    public int getUndirectedEdgeCount() {
        return this.viewDelegate.getUndirectedEdgeCount();
    }

    public int getUndirectedEdgeCount(int type) {
        return this.viewDelegate.getUndirectedEdgeCount(type);
    }

    public void clear() {
        this.root.clear();
        this.viewDelegate.clear();
    }

    public void clearEdges() {
        this.viewDelegate.clearEdges();
    }

    public void fill() {
        this.viewDelegate.fill();
    }

    public void not() {
        this.viewDelegate.not();
    }

    @Override
    public DirectedSubgraph getDirectedGraph() {
        return this.directedDecorator;
    }

    @Override
    public UndirectedSubgraph getUndirectedGraph() {
        return this.undirectedDecorator;
    }

    @Override
    public boolean deepEquals(AbstractGraphView view) {
        return this.viewDelegate.deepEquals(view);
    }

    @Override
    public int deepHashCode() {
        return this.viewDelegate.deepHashCode();
    }

    @Override
    protected void viewDestroyed() {
        this.setStoreId(GraphViewStore.NULL_VIEW);
        for (final GraphObserverImpl observer : this.observers) {
            observer.destroyObserver();
        }
        this.observers.clear();
        this.root.clear();
        this.viewDelegate.viewDestroyed();
    }

    @Override
    protected void nodeAdded(NodeImpl node) {
        this.viewDelegate.nodeAdded(node);
    }

    @Override
    protected void nodeRemoved(NodeImpl node) {
        this.removeNode(node);
        this.viewDelegate.nodeRemoved(node);
    }

    @Override
    protected void edgeAdded(EdgeImpl edge) {
        this.viewDelegate.edgeAdded(edge);
    }

    @Override
    protected void edgeRemoved(EdgeImpl edge) {
        this.viewDelegate.edgeRemoved(edge);
    }

    @Override
    protected GraphObserverImpl createGraphObserver(final Graph graph, final boolean withDiff) {
        if (null == this.version) {
            return null;
        }

        final GraphObserverImpl observer = new GraphObserverImpl(this.graphStore, this.version, graph, withDiff);
        this.observers.add(observer);
        return observer;
    }

    @Override
    protected void destroyGraphObserver(final GraphObserver observer) {
        if (this.observers.remove(observer)) {
            ((GraphObserverImpl) observer).destroyObserver();
        }
    }

    @Override
    public Iterable<HierarchicalNodeGroup> getGroups() {
        final List<HierarchicalNodeGroup> groups = new ArrayList<HierarchicalNodeGroup>();
        groups.add(this.root);
        for (final HierarchicalNodeGroupImpl group : this.root.getGroups(true)) {
            groups.add(group);
        }
        return Collections.unmodifiableList(groups);
    }

    @Override
    public HierarchicalNodeGroup getRoot() {
        return this.root;
    }

    @Override
    public HierarchicalNodeGroup getGroup(final Node node) {
        if (null == node) {
            return null;
        }

        return this.root.find(node, true);
    }

    protected Collection<Node> mapWithHidden(final Node node) {
        if (null == node) {
            return Collections.emptyList();
        }

        final HierarchicalNodeGroupImpl group = this.root.find(node, true);
        if (null == group) {
            return Collections.singleton(node);
        }

        if (group.hasCollapsedParent()) {
            return Collections.emptyList();
        }

        if (group.isCollapsed()) {
            final Collection<Node> children = group.getNodes(true);
            final Set<Node> set = new HashSet<Node>(children.size() + 1);
            set.add(node);
            set.addAll(children);
            return Collections.unmodifiableCollection(set);
        }

        return Collections.singleton(node);
    }

    protected Node mapToVisible(final Node node) {
        final HierarchicalNodeGroupImpl group = this.root.find(node, true);
        if (null == group) {
            return node;
        } else {
            return group.mappedNode();
        }
    }

    private class HierarchicalNodeGroupImpl implements HierarchicalNodeGroup {
        private final HierarchicalNodeGroupImpl parent;

        private final Node node;

        private boolean collapsed = false;

        private final Object2ObjectMap<Node, HierarchicalNodeGroupImpl> nodeMap = new Object2ObjectOpenHashMap<Node, HierarchicalNodeGroupImpl>();

        private HierarchicalNodeGroupImpl(final HierarchicalNodeGroupImpl parent, final Node node) {
            this.parent = parent;
            this.node = node;
        }

        public void clear() {
            if (!this.nodeMap.isEmpty()) {
                return;
            }

            for (final HierarchicalNodeGroupImpl group : this.nodeMap.values()) {
                group.clear();
            }
            this.nodeMap.clear();
        }

        @Override
        public HierarchicalNodeGroup addNode(final Node childNode) {
            if (null == childNode) {
                return null;
            }

            if (this.node == childNode) {
                throw new IllegalArgumentException("Child and parent node are the same.");
            }

            graphStore.autoWriteLock();
            try {
                if (this.nodeMap.containsKey(childNode)) {
                    return null;
                }

                final HierarchicalNodeGroupImpl group = new HierarchicalNodeGroupImpl(this, childNode);
                this.nodeMap.put(childNode, group);
                return group;
            } finally {
                graphStore.autoWriteUnlock();
            }
        }

        @Override
        public boolean removeNode(final Node childNode) {
            if (null == childNode) {
                return false;
            }

            return this.removeNode(childNode, false);
        }

        public boolean removeNode(final Node childNode, final boolean recursive) {
            if (null == childNode) {
                return false;
            }

            graphStore.autoWriteLock();
            try {
                return this.removeNodeWithLock(childNode, recursive);
            } finally {
                graphStore.autoWriteUnlock();
            }
        }

        private boolean removeNodeWithLock(final Node childNode, final boolean recursive) {
            boolean updated = this.nodeMap.get(childNode) != null;
            if (recursive) {
                for (final HierarchicalNodeGroupImpl group : this.nodeMap.values()) {
                    updated |= group.removeNodeWithLock(childNode, true);
                }
            }
            return updated;
        }

        public HierarchicalNodeGroupImpl find(final Node childNode, final boolean recursive) {
            graphStore.autoReadLock();
            try {
                return this.findWithLock(childNode, recursive);
            } finally {
                graphStore.autoReadUnlock();
            }
        }

        private HierarchicalNodeGroupImpl findWithLock(final Node childNode, final boolean recursive) {
            final HierarchicalNodeGroupImpl existing = this.nodeMap.get(childNode);
            if (existing != null) {
                return existing;
            }

            if (!recursive) {
                return null;
            }

            for (final HierarchicalNodeGroupImpl child : nodeMap.values()) {
                final HierarchicalNodeGroupImpl existingChild = child.findWithLock(childNode, true);
                if (existingChild != null) {
                    return existingChild;
                }
            }

            return null;
        }

        public Node mappedNode() {
            if (this.node != null) {
                // child node
                HierarchicalNodeGroupImpl childGroup = this;
                HierarchicalNodeGroupImpl parentGroup = this.parent;
                while (parentGroup != null) {
                    if (null == parentGroup.node) {
                        return childGroup.node;
                    }
                    if (parentGroup.isExpanded()) {
                        return childGroup.node;
                    }
                    final HierarchicalNodeGroupImpl tmp = parentGroup;
                    parentGroup = parentGroup.parent;
                    childGroup = tmp;
                }
                return null;
            } else {
                // first tier node, always visible
                return null;
            }
        }

        public boolean hasCollapsedParent() {
            graphStore.autoReadLock();
            try {
                HierarchicalNodeGroupImpl group = this.parent;
                while (group != null) {
                    if (group.isCollapsed()) {
                        return true;
                    }
                    group = group.parent;
                }
                return false;
            } finally {
                graphStore.autoReadUnlock();
            }
        }

        @Override
        public boolean isCollapsed() {
            graphStore.autoReadLock();
            try {
                return this.collapsed;
            } finally {
                graphStore.autoReadUnlock();
            }
        }

        @Override
        public boolean isExpanded() {
            return !this.isCollapsed();
        }

        @Override
        public void expand() {
            this.collapsed = false;
            for (final HierarchicalNodeGroupImpl child : this.nodeMap.values()) {
                child.expand();
            }
        }

        @Override
        public void collapse() {
            this.collapsed = true;
            for (final HierarchicalNodeGroupImpl child : this.nodeMap.values()) {
                child.collapse();
            }
        }

        @Override
        public Iterable<Node> getNodes() {
            graphStore.autoReadLock();
            final List<Node> list;
            try {
                list = new ArrayList<Node>(this.nodeMap.keySet());
            } finally {
                graphStore.autoReadUnlock();
            }
            return Collections.unmodifiableCollection(list);
        }

        @Override
        public Collection<Node> getNodes(boolean recursive) {
            graphStore.autoReadLock();
            final Set<Node> set = new HashSet<Node>(this.nodeMap.size());
            try {
                if (recursive) {
                    for (final Map.Entry<Node, HierarchicalNodeGroupImpl> entry : this.nodeMap.entrySet()) {
                        set.add(entry.getKey());
                        if (recursive) {
                            set.addAll(entry.getValue().getNodes(true));
                        }
                    }
                }
            } finally {
                graphStore.autoReadUnlock();
            }
            return Collections.unmodifiableCollection(set);
        }

        public Collection<HierarchicalNodeGroupImpl> getGroups(final boolean recursive) {
            graphStore.autoReadLock();
            final List<HierarchicalNodeGroupImpl> list;
            try {
                if (recursive) {
                    list = new ArrayList<HierarchicalNodeGroupImpl>();
                    for (final HierarchicalNodeGroupImpl group : this.nodeMap.values()) {
                        list.add(group);
                        list.addAll(group.getGroups(true));
                    }
                } else {
                    list = new ArrayList<HierarchicalNodeGroupImpl>(this.nodeMap.values());
                }
            } finally {
                graphStore.autoReadUnlock();
            }
            return Collections.unmodifiableCollection(list);
        }
    }
}
