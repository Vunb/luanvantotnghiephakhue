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

package org.apache.jmeter.protocol.tcp.control.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.tcp.config.gui.TCPConfigGui;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class TCPSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 240L;

    private LoginConfigGui loginPanel;

    private TCPConfigGui TcpDefaultPanel;

    public TCPSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        loginPanel.configure(element);
        TcpDefaultPanel.configure(element);
    }

    public TestElement createTestElement() {
        TCPSampler sampler = new TCPSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        sampler.addTestElement(TcpDefaultPanel.createTestElement());
        sampler.addTestElement(loginPanel.createTestElement());
        this.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        TcpDefaultPanel.clearGui();
        loginPanel.clearGui();
    }

    public String getLabelResource() {
        return "tcp_sample_title"; // $NON-NLS-1$
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        TcpDefaultPanel = new TCPConfigGui(false);
        mainPanel.add(TcpDefaultPanel);

        loginPanel = new LoginConfigGui(false);
        loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config"))); // $NON-NLS-1$
        mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
    }
}
