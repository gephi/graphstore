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

import java.io.IOException;

/**
 * Thrown when reading a serialized graph model written by a newer, incompatible version of graphstore than the one
 * doing the reading.
 * <p>
 * Extends {@link IOException} so it is caught by existing code that only handles I/O errors, while callers that want to
 * report a specific, localized message can catch this type directly and use {@link #getFileVersion()} and
 * {@link #getMaxSupportedVersion()} instead of parsing the message.
 */
public class UnsupportedFormatVersionException extends IOException {

    private final float fileVersion;
    private final float maxSupportedVersion;

    public UnsupportedFormatVersionException(float fileVersion, float maxSupportedVersion) {
        super("Unsupported serialization format version: " + fileVersion + ". This file was written by a newer version of graphstore than this library supports (up to " + maxSupportedVersion + "). Please upgrade graphstore to read this file.");
        this.fileVersion = fileVersion;
        this.maxSupportedVersion = maxSupportedVersion;
    }

    /**
     * Returns the format version the file was written with.
     *
     * @return file format version
     */
    public float getFileVersion() {
        return fileVersion;
    }

    /**
     * Returns the highest format version this version of graphstore can read.
     *
     * @return max supported format version
     */
    public float getMaxSupportedVersion() {
        return maxSupportedVersion;
    }
}
