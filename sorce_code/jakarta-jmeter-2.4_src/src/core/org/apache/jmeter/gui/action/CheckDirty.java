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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Check if the TestPlan has been changed since it was last saved
 *
 */
public class CheckDirty extends AbstractAction implements HashTreeTraverser, ActionListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Map<JMeterTreeNode, TestElement> previousGuiItems;

    private boolean checkMode = false;

    private boolean removeMode = false;

    private boolean dirty = false;

    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.CHECK_DIRTY);
        commands.add(ActionNames.SUB_TREE_SAVED);
        commands.add(ActionNames.SUB_TREE_MERGED);
        commands.add(ActionNames.SUB_TREE_LOADED);
        commands.add(ActionNames.ADD_ALL);
        commands.add(ActionNames.CHECK_REMOVE);
    }

    public CheckDirty() {
        previousGuiItems = new HashMap<JMeterTreeNode, TestElement>();
        ActionRouter.getInstance().addPreActionListener(ExitCommand.class, this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ActionNames.EXIT)) {
            doAction(e);
        }
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ActionNames.SUB_TREE_SAVED)) {
            HashTree subTree = (HashTree) e.getSource();
            subTree.traverse(this);
        } else if (action.equals(ActionNames.SUB_TREE_LOADED)) {
            ListedHashTree addTree = (ListedHashTree) e.getSource();
            addTree.traverse(this);
        } else if (action.equals(ActionNames.ADD_ALL)) {
            previousGuiItems.clear();
            GuiPackage.getInstance().getTreeModel().getTestPlan().traverse(this);
        } else if (action.equals(ActionNames.CHECK_REMOVE)) {
            GuiPackage guiPackage = GuiPackage.getInstance();
            JMeterTreeNode[] nodes = guiPackage.getTreeListener().getSelectedNodes();
            removeMode = true;
            for (int i = nodes.length - 1; i >= 0; i--) {
                guiPackage.getTreeModel().getCurrentSubTree(nodes[i]).traverse(this);
            }
            removeMode = false;
        }
        // If we are merging in another test plan, we know the test plan is dirty now
        if(action.equals(ActionNames.SUB_TREE_MERGED)) {
            dirty = true;
        }
        else {
            dirty = false;
            checkMode = true;
            HashTree wholeTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
            wholeTree.traverse(this);
            checkMode = false;
        }
        GuiPackage.getInstance().setDirty(dirty);
    }

    /**
     * The tree traverses itself depth-first, calling processNode for each
     * object it encounters as it goes.
     */
    public void addNode(Object node, HashTree subTree) {
        log.debug("Node is class:" + node.getClass());
        JMeterTreeNode treeNode = (JMeterTreeNode) node;
        if (checkMode) {
            // Only check if we have not found any differences so far
            if(!dirty) {
                if (previousGuiItems.containsKey(treeNode)) {
                    if (!previousGuiItems.get(treeNode).equals(treeNode.getTestElement())) {
                        dirty = true;
                    }
                } else {
                    dirty = true;
                }
            }
        } else if (removeMode) {
            previousGuiItems.remove(treeNode);
        } else {
            previousGuiItems.put(treeNode, (TestElement) treeNode.getTestElement().clone());
        }
    }

    /**
     * Indicates traversal has moved up a step, and the visitor should remove
     * the top node from it's stack structure.
     */
    public void subtractNode() {
    }

    /**
     * Process path is called when a leaf is reached. If a visitor wishes to
     * generate Lists of path elements to each leaf, it should keep a Stack data
     * structure of nodes passed to it with addNode, and removing top items for
     * every subtractNode() call.
     */
    public void processPath() {
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
