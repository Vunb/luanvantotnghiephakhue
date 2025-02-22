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

package org.apache.jorphan.gui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders numbers in a JTable with a specified format
 */
public class NumberRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 240L;

    protected final NumberFormat formatter;

    public NumberRenderer() {
        super();
        formatter = NumberFormat.getInstance();
        setHorizontalAlignment(JLabel.RIGHT);
    }

    public NumberRenderer(String format) {
        super();
        formatter = new DecimalFormat(format);
        setHorizontalAlignment(JLabel.RIGHT);
    }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}