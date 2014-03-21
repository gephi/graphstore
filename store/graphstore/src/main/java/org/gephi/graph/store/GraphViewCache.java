/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.store;

/**
 *
 * @author mbastian
 */
public final class GraphViewCache {

    /*private final GraphViewImpl view;
     private final ColtBitVectorRank nodeRanks;
     private CachedNode[] nodes;
     private Edge[] edges;

     public GraphViewCache(final GraphViewImpl view) {
     this.view = view;
     this.nodeRanks = new ColtBitVectorRank(view.nodeBitVector);
     this.nodes = new CachedNode[view.nodeCount];
     this.edges = new Edge[view.edgeCount];
     }

     private void ensureNodeCapacity(int capacity) {
     if (capacity >= nodes.length) {
     final int newLength = (int) Math.min(Math.max((106039l * nodes.length) >>> 16, capacity), Integer.MAX_VALUE);
     final CachedNode[] t = new CachedNode[newLength];
     System.arraycopy(nodes, 0, t, 0, nodes.length);
     nodes = t;
     }
     }

     private void ensureEdgeCapacity(int capacity) {
     if (capacity >= edges.length) {
     final int newLength = (int) Math.min(Math.max((106039l * edges.length) >>> 16, capacity), Integer.MAX_VALUE);
     final Edge[] t = new Edge[newLength];
     System.arraycopy(edges, 0, t, 0, edges.length);
     edges = t;
     }
     }

     public void init() {
     //Nodes
     int nodeIndex = 0;
     int typeCount = view.graphStore.edgeTypeStore.length;
     NodeStore.NodeStoreIterator nodeIterator = view.graphStore.nodeStore.iterator();
     while (nodeIterator.hasNext()) {
     NodeImpl node = nodeIterator.next();
     if (view.nodeBitVector.get(node.storeId)) {
     CachedNode cachedNode = new CachedNode(node, typeCount);
     nodes[nodeIndex++] = cachedNode;
     }
     }

     //Edges
     int edgeIndex = 0;
     EdgeStore.EdgeStoreIterator edgeIterator = view.graphStore.edgeStore.iterator();
     while (edgeIterator.hasNext()) {
     EdgeImpl edge = edgeIterator.next();
     if (view.edgeBitVector.get(edge.storeId)) {
     edges[edgeIndex++] = edge;

     CachedNode source = getCachedNode(edge.source);
     CachedNode target = edge.isSelfLoop() ? source : getCachedNode(edge.target);

     source.addOutEdge(edge);
     target.addInEdge(edge);

     if (edge.isMutual() && edge.source.storeId > edge.target.storeId) {
     EdgeImpl mutual = (EdgeImpl) view.graphStore.getEdge(edge.target, edge.source, edge.type);
     if (view.edgeBitVector.get(mutual.storeId)) {
     source.mutualDegree++;
     target.mutualDegree++;
     }
     }
     }
     }
     }

     private CachedNode getCachedNode(NodeImpl node) {
     int rank = (int) nodeRanks.rank(node.storeId);
     return nodes[rank];
     }

     private static final class CachedNode {

     private final NodeImpl node;
     private ObjectSet<EdgeImpl>[] inEdges;
     private ObjectSet<EdgeImpl>[] outEdges;
     private int inDegree;
     private int outDegree;
     private int mutualDegree;

     protected CachedNode(final NodeImpl node, int typeCount) {
     this.node = node;
     this.inEdges = new ObjectSet[typeCount];
     this.outEdges = new ObjectSet[typeCount];
     }

     protected void addInEdge(EdgeImpl edge) {
     inEdges[edge.type].add(edge);
     inDegree++;
     }

     protected void addOutEdge(EdgeImpl edge) {
     outEdges[edge.type].add(edge);
     }

     protected void removeInEdge(EdgeImpl edge) {
     inEdges[edge.type].remove(edge);
     inDegree--;
     }

     protected void removeOutEdge(EdgeImpl edge) {
     inEdges[edge.type].remove(edge);
     inDegree--;
     }

     protected NodeImpl getNode() {
     return node;
     }
     }

     private final class NodeCacheIterator implements NodeIterator {

     private final int nodeCount = nodes.length;
     private int index;

     @Override
     public boolean hasNext() {
     return index < nodeCount;
     }

     @Override
     public Node next() {
     return nodes[index++].node;
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private final class EdgeCacheIterator implements EdgeIterator {

     private final int edgeCount = edges.length;
     private int index;

     @Override
     public boolean hasNext() {
     return index < edgeCount;
     }

     @Override
     public Edge next() {
     return edges[index++];
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeInCacheIterator implements EdgeIterator {

     private final int edgeCount;
     private final Edge[] edges;
     private int index;

     public EdgeInCacheIterator(CachedNode node) {
     this.edges = node.inEdges;
     this.edgeCount = edges.length;
     }

     @Override
     public boolean hasNext() {
     return index < edgeCount;
     }

     @Override
     public Edge next() {
     return edges[index++];
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeOutCacheIterator implements EdgeIterator {

     private final int edgeCount;
     private final Edge[] edges;
     private int index;

     public EdgeOutCacheIterator(CachedNode node) {
     this.edges = node.outEdges;
     this.edgeCount = edges.length;
     }

     @Override
     public boolean hasNext() {
     return index < edgeCount;
     }

     @Override
     public Edge next() {
     return edges[index++];
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeInOutCacheIterator implements EdgeIterator {

     private final int inEdgeCount;
     private final int outEdgeCount;
     private final Edge[] inEdges;
     private final Edge[] outEdges;
     private int index;
     private boolean out;

     public EdgeInOutCacheIterator(CachedNode node) {
     this.outEdges = node.outEdges;
     this.outEdgeCount = outEdges.length;
     this.inEdges = node.inEdges;
     this.inEdgeCount = inEdges.length;
     this.out = true;
     }

     @Override
     public boolean hasNext() {
     if (out) {
     return index < outEdgeCount;
     } else {
     return index < inEdgeCount;
     }
     }

     @Override
     public Edge next() {
     if (out) {
     if (++index == outEdgeCount) {
     out = false;
     index = 0;
     return outEdges[outEdgeCount - 1];
     }
     return outEdges[index++];
     } else {
     return inEdges[index++];
     }
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeInOutTypeCacheIterator implements EdgeIterator {

     private final CachedNode node;
     private final int type;
     private final int inEdgeCount;
     private final int outEdgeCount;
     private final Edge[] inEdges;
     private final Edge[] outEdges;
     private int index;
     private boolean out;
     private Edge pointer;

     public EdgeInOutTypeCacheIterator(CachedNode node, int type) {
     this.outEdges = node.outEdges;
     this.outEdgeCount = outEdges.length;
     this.inEdges = node.inEdges;
     this.inEdgeCount = inEdges.length;
     this.out = true;
     this.type = type;
     this.node = node;
     this.index = node.outTypeOffset[type];
     }

     @Override
     public boolean hasNext() {
     if (out) {
     while (index < outEdgeCount) {
     if ((pointer = outEdges[index++]).getType() != type) {
     out = false;
     index = node.inTypeOffset[type];
     } else {
     return true;
     }

     }
     }
     if (!out) {
     while (index < inEdgeCount) {
     if ((pointer = inEdges[index++]).getType() != type) {
     return false;
     }
     return true;
     }
     }
     return false;
     }

     @Override
     public Edge next() {
     return pointer;
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeInTypeCacheIterator implements EdgeIterator {

     private final int edgeCount;
     private final int type;
     private final Edge[] edges;
     private int index;
     private Edge pointer;

     public EdgeInTypeCacheIterator(CachedNode node, int type) {
     this.edges = node.inEdges;
     this.index = node.inTypeOffset[type];
     this.edgeCount = edges.length;
     this.type = type;
     }

     @Override
     public boolean hasNext() {
     while (index < edgeCount) {
     if ((pointer = edges[index++]).getType() != type) {
     return false;
     }
     return true;
     }
     return false;
     }

     @Override
     public Edge next() {
     return pointer;
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class EdgeOutTypeCacheIterator implements EdgeIterator {

     private final int edgeCount;
     private final int type;
     private final Edge[] edges;
     private int index;
     private Edge pointer;

     public EdgeOutTypeCacheIterator(CachedNode node, int type) {
     this.edges = node.outEdges;
     this.index = node.outTypeOffset[type];
     this.edgeCount = edges.length;
     this.type = type;
     }

     @Override
     public boolean hasNext() {
     while (index < edgeCount) {
     if ((pointer = edges[index++]).getType() != type) {
     return false;
     }
     return true;
     }
     return false;
     }

     @Override
     public Edge next() {
     return pointer;
     }

     @Override
     public void remove() {
     throw new UnsupportedOperationException("Not supported yet.");
     }
     }

     private static final class ColtBitVectorRank {

     protected transient long[] bits;
     final protected BitVector bitVector;
     final protected long[] count;
     final protected int numWords;
     final protected long numOnes;
     final protected long lastOne;

     public ColtBitVectorRank(final BitVector bitVector) {
     this.bitVector = bitVector;
     this.bits = bitVector.elements();
     final long length = bitVector.size();

     numWords = (int) ((length + Long.SIZE - 1) / Long.SIZE);

     final int numCounts = (int) ((length + 8 * Long.SIZE - 1) / (8 * Long.SIZE)) * 2;
     // Init rank/select structure
     count = new long[numCounts + 1];

     long c = 0, l = -1;
     int pos = 0;
     for (int i = 0; i < numWords; i += 8, pos += 2) {
     count[ pos] = c;
     c += Long.bitCount(bits[ i]);
     if (bits[ i] != 0) {
     l = i * 64L + mostSignificantBit(bits[ i]);
     }
     for (int j = 1; j < 8; j++) {
     count[ pos + 1] |= (i + j <= numWords ? c - count[ pos] : 0x1FFL) << 9 * (j - 1);
     if (i + j < numWords) {
     c += Long.bitCount(bits[ i + j]);
     if (bits[ i + j] != 0) {
     l = (i + j) * 64L + mostSignificantBit(bits[ i + j]);
     }
     }
     }
     }

     numOnes = c;
     lastOne = l;
     count[ numCounts] = c;
     }

     public long rank(long pos) {
     // This test can be eliminated if there is always an additional word at the end of the bit array.
     if (pos > lastOne) {
     return numOnes;
     }

     final int word = (int) (pos / 64);
     final int block = word / 4 & ~1;
     final int offset = word % 8 - 1;

     return count[ block] + (count[ block + 1] >>> (offset + (offset >>> 32 - 4 & 0x8)) * 9 & 0x1FF) + Long.bitCount(bits[ word] & ((1L << pos % 64) - 1));
     }

     public long numBits() {
     return count.length * (long) Long.SIZE;
     }

     public long count() {
     return numOnes;
     }

     public long rank(long from, long to) {
     return rank(to) - rank(from);
     }

     public long lastOne() {
     return lastOne;
     }

     public BitVector bitVector() {
     return bitVector;
     }

     private int mostSignificantBit(long x) {
     if (x == 0) {
     return -1;
     }

     int msb = 0;

     if ((x & 0xFFFFFFFF00000000L) != 0) {
     x >>>= (1 << 5);
     msb += (1 << 5);
     }

     if ((x & 0xFFFF0000) != 0) {
     x >>>= (1 << 4);
     msb += (1 << 4);
     }

     // We have now reduced the problem to finding the msb in a 16-bit word.

     x |= x << 16;
     x |= x << 32;

     final long y = x & 0xFF00F0F0CCCCAAAAL;

     long t = 0x8000800080008000L & (y | ((y | 0x8000800080008000L) - (x ^ y)));

     t |= t << 15;
     t |= t << 30;
     t |= t << 60;

     return (int) (msb + (t >>> 60));
     }
     }*/
}
