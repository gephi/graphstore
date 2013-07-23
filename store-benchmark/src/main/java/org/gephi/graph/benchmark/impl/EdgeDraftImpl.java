/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.benchmark.impl;

/**
 *
 * @author TheSpecialisT
 */
import java.awt.Color;
import org.gephi.graph.benchmark.EdgeDirection;
import org.gephi.graph.benchmark.EdgeDraft;
import org.gephi.graph.benchmark.NodeDraft;

public class EdgeDraftImpl extends ElementDraftImpl implements EdgeDraft {
    
    private NodeDraftImpl source;
    private NodeDraftImpl target;

     public EdgeDraftImpl(ContainerImpl container, String id) {
        super(container, id);
    }

    @Override
    public void setSource(NodeDraft nodeSource) {
        this.source = (NodeDraftImpl) nodeSource; 
    }

    @Override
    public void setTarget(NodeDraft nodeTarget) {
        this.target = (NodeDraftImpl) nodeTarget;
    }

    @Override
    public NodeDraft getSource() {
        return source;
        }

    @Override
    public NodeDraft getTarget() {
        return target;
           }
}
