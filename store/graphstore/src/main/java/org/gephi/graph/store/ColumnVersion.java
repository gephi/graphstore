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
public class ColumnVersion {

    protected final ColumnImpl column;
    protected int version = Integer.MIN_VALUE + 1;

    public ColumnVersion(ColumnImpl column) {
        this.column = column;
    }

    public synchronized int incrementAndGetVersion() {
        version++;
        if (version == Integer.MAX_VALUE) {
            version = Integer.MIN_VALUE + 1;
            handleReset();
        }
        return version;
    }

    private void handleReset() {

    }
}
