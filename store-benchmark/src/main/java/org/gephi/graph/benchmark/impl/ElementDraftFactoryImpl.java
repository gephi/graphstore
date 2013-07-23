/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.benchmark.impl;

import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.benchmark.EdgeDraft;
import org.gephi.graph.benchmark.ElementDraftFactory;
import org.gephi.graph.benchmark.NodeDraft;
/**
 *
 * @author TheSpecialisT
 */
public class ElementDraftFactoryImpl implements ElementDraftFactory {
    
    protected final ContainerImpl container;
    protected final static AtomicInteger NODE_IDS = new AtomicInteger();
    protected final static AtomicInteger EDGE_IDS = new AtomicInteger();
    
        
    public ElementDraftFactoryImpl(ContainerImpl container) {
        this.container = container;
    }

    @Override
    public NodeDraft newNodeDraft() {
        NodeDraft node = new NodeDraftImpl(container, "n" + NODE_IDS.getAndIncrement());
        return node;
    }

    @Override
    public NodeDraft newNodeDraft(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EdgeDraft newEdgeDraft() {
       EdgeDraftImpl edge = new EdgeDraftImpl(container, "e" + EDGE_IDS.getAndIncrement());
        return edge;
    }

    @Override
    public EdgeDraft newEdgeDraft(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
