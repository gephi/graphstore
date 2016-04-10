package org.gephi.graph.api;

/**
 * A hierarchical graph view which allows for collapsible sub-groups. Each node,
 * whether it is collapsed or extended, should be in core in view. If a node is
 * node is hidden by collapsed parent, it will omitted within
 * directed/undirected graph queries.
 */
public interface HierarchicalGraphView extends GraphView {
    /**
     * @return Return iterator for each hierarchical node group.
     */
    Iterable<HierarchicalNodeGroup> getGroups();

    /**
     * @return Return root node which first-tier nodes should be attached.
     */
    HierarchicalNodeGroup getRoot();

    /**
     * @param node node within graph graph
     * @return Return existing hierarchical group if available; else null.
     */
    HierarchicalNodeGroup getGroup(Node node);
}
