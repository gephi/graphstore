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

/**
 *
 * @author mbastian
 */
public interface ColumnObserver {

    /**
     * Returns true if the column has changed.
     *
     * @return true if changed, false otherwise
     */
    public boolean hasColumnChanged();

    /**
     * Gets the column this observer belongs to.
     *
     * @return the table
     */
    public Column getColumn();

    /**
     * Destroys this observer.
     */
    public void destroy();

    /**
     * Returns true if this observer has been destroyed.
     *
     * @return true if destroyed, false otherwise
     */
    public boolean isDestroyed();
}
