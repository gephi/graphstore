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
package org.gephi.graph.api;

/**
 * Undirected graph
 * <p>
 * A directed edge and its reverse edge of the same type are seen as a single undirected edge. Edges are paired one to
 * one, as many as possible, so the whole graph's edge count only depends on the edges it holds. With several edges of
 * the same type between two nodes, which edges are paired depends on the order of insertion and removal. A subgraph
 * sees a directed edge and its reverse edge as a single edge only if they are paired and it contains both.
 */
public interface UndirectedGraph extends Graph {
}
