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

package org.apache.jmeter.protocol.http.config;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;

public class MultipartUrlConfigTest extends TestCase {

    public MultipartUrlConfigTest(String name) {
        super(name);
    }

    @SuppressWarnings("deprecation")
    public void testConstructors() {
        MultipartUrlConfig muc = new MultipartUrlConfig();
        assertEquals(0, muc.getArguments().getArgumentCount());
        assertEquals(0, muc.getHTTPFileArgs().getHTTPFileArgCount());
        muc = new MultipartUrlConfig("boundary");
        assertEquals(0, muc.getArguments().getArgumentCount());
        assertEquals(0, muc.getHTTPFileArgs().getHTTPFileArgCount());
        assertEquals("boundary", muc.getBoundary());
    }

    public void testParseArguments() {
        String queryString
            = "Content-Disposition: form-data; name=\"aa\"\n"
            + "Content-Type: text/plain; charset=ISO-8859-1\n"
            + "Content-Transfer-Encoding: 8bit\n"
            + "\n"
            + "aa\n"
            + "--7d159c1302d0y0\n"
            + "Content-Disposition: form-data; name=\"xx\"\n"
            + "Content-Type: text/plain; charset=ISO-8859-1\n"
            + "Content-Transfer-Encoding: 8bit\n"
            + "\n"
            + "xx\n"
            + "--7d159c1302d0y0\n"
            + "Content-Disposition: form-data; name=\"param1\"; filename=\"file1\"\n"
            + "Content-Type: text/plain\n"
            + "Content-Transfer-Encoding: binary\n"
            + "\n"
            + "file content\n"
            + "\n";
        MultipartUrlConfig muc = new MultipartUrlConfig("7d159c1302d0y0");
        muc.parseArguments(queryString);
        HTTPFileArgs files = muc.getHTTPFileArgs();
        assertEquals(1, files.getHTTPFileArgCount());
        HTTPFileArg file = (HTTPFileArg) files.iterator().next().getObjectValue();
        assertEquals("file1", file.getPath());
        assertEquals("param1", file.getParamName());
        assertEquals("text/plain", file.getMimeType());
        Arguments args = muc.getArguments();
        assertEquals(2, args.getArgumentCount());
        Argument arg = args.getArgument(0);
        assertEquals("aa", arg.getName());
        assertEquals("aa", arg.getValue());
        arg = args.getArgument(1);
        assertEquals("xx", arg.getName());
        assertEquals("xx", arg.getValue());
    }
}
