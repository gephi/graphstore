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
package org.gephi.attribute.api;

import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.NodeIterable;

/**
 *
 * @author mbastian
 */
public interface TimestampIndex {

    public double getMinTimestamp();

    public double getMaxTimestamp();

    public NodeIterable getNodes(double timestamp);

    public NodeIterable getNodes(double from, double to);

    public EdgeIterable getEdges(double timestamp);

    public EdgeIterable getEdges(double from, double to);
}
