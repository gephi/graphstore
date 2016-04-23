package org.gephi.graph.api;

/**
 * A group of nodes within a hierarchical graph view.
 */
public interface HierarchicalNodeGroup {
    /**
     * @return Returns true if the group is collapsed, which means the children
     *         are hidden from visible graph.
     */
    boolean isCollapsed();

    /**
     * @return Returns true if the group is expanded, which means the children
     *         are visible within visible graph.
     */
    boolean isExpanded();

    /**
     * Expand node, displaying children.
     */
    void expand();

    /**
     * Collapse node, hiding children.
     */
    void collapse();

    /**
     * @return Returns true if this group contains on or more children nodes.
     */
    boolean hasChildren();

    /**
     * @return Returns true if this group is the root node.
     */
    boolean isRoot();

    /**
     * @return Return iterator containing children nodes (not recursive).
     */
    Iterable<Node> getNodes();

    /**
     * @return Return iterator containing children nodes.
     */
    Iterable<Node> getNodes(boolean recursive);

    /**
     * Add node to this group.
     *
     * @param node child node
     * @return Returns child hierarchical group, if created; else null.
     */
    HierarchicalNodeGroup addNode(Node node);

    /**
     * Remove node from group.
     *
     * @param node child node
     * @return Returns true if child hierarchical group was remoce; else false.
     */
    boolean removeNode(Node node);
}
