/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * Created on Dec 9, 2003
 *
 */
package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.IntegerProperty;

/**
 * @version $Revision: 905027 $
 */
public abstract class OnErrorTestElement extends AbstractTestElement {
    private static final long serialVersionUID = 240L;

    /* Action to be taken when a Sampler error occurs */
    public final static int ON_ERROR_CONTINUE = 0;

    public final static int ON_ERROR_STOPTHREAD = 1;

    public final static int ON_ERROR_STOPTEST = 2;

    public final static int ON_ERROR_STOPTEST_NOW = 3;

    /* Property name */
    public final static String ON_ERROR_ACTION = "OnError.action";

    protected OnErrorTestElement() {
        super();
    }

    public void setErrorAction(int value) {
        setProperty(new IntegerProperty(ON_ERROR_ACTION, value));
    }

    public int getErrorAction() {
        int value = getPropertyAsInt(ON_ERROR_ACTION);
        return value;
    }

    public boolean isContinue() {
        return getErrorAction() == ON_ERROR_CONTINUE;
    }

    public boolean isStopThread() {
        return getErrorAction() == ON_ERROR_STOPTHREAD;
    }

    public boolean isStopTest() {
        return getErrorAction() == ON_ERROR_STOPTEST;
    }

    public boolean isStopTestNow() {
        return getErrorAction() == ON_ERROR_STOPTEST_NOW;
    }
}
