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
package org.gephi.graph.benchmark.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.testng.Reporter;

/**
 * Bridge between Java Logging API and TestNG's Reporter
 *
 * @author mbastian
 */
public class ReporterHandler extends Handler {

    @Override
    public void publish(LogRecord record) {
        String prefix = "";
        if (record.getLevel().equals(Level.INFO)) {
            prefix = "[INFO] ";
        } else if (record.getLevel().equals(Level.WARNING)) {
            prefix = "[WARN] ";
        } else if (record.getLevel().equals(Level.SEVERE)) {
            prefix = "[SEVERE] ";
        }
        Reporter.log(prefix + record.getMessage() + "<br>", true);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
