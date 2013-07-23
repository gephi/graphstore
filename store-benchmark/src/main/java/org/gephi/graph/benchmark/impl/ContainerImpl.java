/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.benchmark.impl;

/**
 *
 * @author TheSpecialisT
 */
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.gephi.graph.benchmark.ContainerLoader;
import org.gephi.graph.benchmark.EdgeDraft;
import org.gephi.graph.benchmark.ElementDraftFactory;
import org.gephi.graph.benchmark.ElementDraft;
import org.gephi.graph.benchmark.NodeDraft;
public class ContainerImpl implements ContainerLoader {
    
    private final Object2IntMap<String> nodeMap;
    protected static final int NULL_INDEX = -1;
    private final ObjectList<NodeDraftImpl> nodeList;
    private EdgeDirectionDefault edgeDefault = EdgeDirectionDefault.MIXED;
    private int undirectedEdgesCount = 0;
    private Long2ObjectMap<int[]>[] edgeTypeSets;
    private int directedEdgesCount = 0;
    
    public ContainerImpl(){
    nodeMap = new Object2IntOpenHashMap<String>();
    nodeMap.defaultReturnValue(NULL_INDEX);
    nodeList = new ObjectArrayList<NodeDraftImpl>();
    edgeTypeSets = new Long2ObjectMap[0];
    }
    @Override
    public void addEdge(EdgeDraft edgeDraft) {
        checkElementDraftImpl(edgeDraft);

        EdgeDraftImpl edgeDraftImpl = (EdgeDraftImpl) edgeDraft;
        if (edgeDraftImpl.getSource() == null) {
            //String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeSource");
            //report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }
        if (edgeDraftImpl.getTarget() == null) {
           // String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_MissingNodeTarget");
           // report.logIssue(new Issue(message, Level.SEVERE));
            return;
        }
         }

    @Override
    public void addNode(NodeDraft nodeDraft) {
         checkElementDraftImpl(nodeDraft);
        NodeDraftImpl nodeDraftImpl = (NodeDraftImpl) nodeDraft;

        if (nodeMap.containsKey(nodeDraftImpl.getId())) {
            //String message = NbBundle.getMessage(ImportContainerImpl.class, "ImportContainerException_nodeExist", nodeDraftImpl.getId());
            //report.logIssue(new Issue(message, Level.WARNING));
            return;
        }

        int index = nodeList.size();
        nodeList.add(nodeDraftImpl);
        nodeMap.put(nodeDraftImpl.getId(), index);
    }

    @Override
    public void removeEdge(EdgeDraft edgeDraft) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NodeDraft getNode(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean nodeExists(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EdgeDraft getEdge(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EdgeDraft getEdge(NodeDraft source, NodeDraft target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean edgeExists(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean edgeExists(String source, String target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean edgeExists(NodeDraft source, NodeDraft target) {
        if (source != null && target != null) {
            boolean undirected = edgeDefault.equals(EdgeDirectionDefault.UNDIRECTED) || (undirectedEdgesCount > 0 && directedEdgesCount == 0);
            long edgeId = getLongId(source, target, !undirected);
            for (Long2ObjectMap l : edgeTypeSets) {
                if (l != null) {
                    if (l.containsKey(edgeId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ElementDraftFactory factory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAllowSelfLoop(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAllowAutoNode(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAllowParallelEdge(boolean value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAutoScale(boolean autoscale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void checkElementDraftImpl(ElementDraft elmt) {
        if (elmt == null) {
            throw new NullPointerException();
        }
        if (!(elmt instanceof ElementDraftImpl)) {
            throw new ClassCastException();
        }
    }
    
    public enum EdgeDirectionDefault {

    DIRECTED, UNDIRECTED, MIXED;
}
 private long getLongId(NodeDraft source, NodeDraft target, boolean directed) {
        if (directed) {
            long edgeId = ((long) source.hashCode()) << 32;
            edgeId = edgeId | (long) (target.hashCode());
            return edgeId;
        } else {
            long edgeId = ((long) (source.hashCode() > target.hashCode() ? source.hashCode() : target.hashCode())) << 32;
            edgeId = edgeId | (long) (source.hashCode() > target.hashCode() ? target.hashCode() : source.hashCode());
            return edgeId;
        }
    
}
}
