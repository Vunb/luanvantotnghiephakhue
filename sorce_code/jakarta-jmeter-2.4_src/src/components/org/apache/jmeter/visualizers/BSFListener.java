/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.visualizers;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.BSFTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class BSFListener extends BSFTestElement
    implements Cloneable, SampleListener, TestBean, Visualizer {
// N.B. Needs to implement Visualizer so that TestBeanGUI can find the correct GUI class

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 234L;

    public void sampleOccurred(SampleEvent event) {
        try {
            BSFManager mgr = getManager();
            if (mgr == null) {
                log.error("Problem creating BSF manager");
                return;
            }
            mgr.declareBean("sampleEvent", event, SampleEvent.class);
            SampleResult result = event.getResult();
            mgr.declareBean("sampleResult", result, SampleResult.class);
            processFileOrScript(mgr);
            mgr.terminate();
        } catch (BSFException e) {
            log.warn("Problem in BSF script "+e);
        }
    }

    public void sampleStarted(SampleEvent e) {
    }

    public void sampleStopped(SampleEvent e) {
    }

    public void add(SampleResult sample) {
    }

    public boolean isStats() {
        return false;
    }
}
