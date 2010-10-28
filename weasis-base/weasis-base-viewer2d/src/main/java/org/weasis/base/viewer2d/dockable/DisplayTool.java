/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.base.viewer2d.dockable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.weasis.base.viewer2d.EventManager;
import org.weasis.base.viewer2d.View2dContainer;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.ui.docking.PluginTool;
import org.weasis.core.ui.editor.SeriesViewerEvent;
import org.weasis.core.ui.editor.SeriesViewerListener;
import org.weasis.core.ui.editor.SeriesViewerEvent.EVENT;
import org.weasis.core.ui.editor.image.AnnotationsLayer;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.graphic.model.AbstractLayer;
import org.weasis.core.ui.graphic.model.Tools;
import org.weasis.core.ui.util.CheckNode;
import org.weasis.core.ui.util.TreeLayer;

public class DisplayTool extends PluginTool implements SeriesViewerListener {

    public final static String IMAGE = "Image";
    public final static String ANNOTATIONS = "Annotations";

    public final static String BUTTON_NAME = "Display";

    private final TreeLayer tree = new TreeLayer();
    private CheckNode image;
    private CheckNode info;
    private CheckNode drawings;

    public DisplayTool(String pluginName, Icon icon) {
        super(BUTTON_NAME, pluginName, ToolWindowAnchor.RIGHT);
        setDockableWidth(210);
        jbInit();

    }

    private void jbInit() {
        setLayout(new BorderLayout(0, 0));
        iniTree();
    }

    public void iniTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        root.add(image = new CheckNode(IMAGE, true));
        info = new CheckNode(ANNOTATIONS, true);
        info.add(new CheckNode(AnnotationsLayer.ANNOTATIONS, true));
        info.add(new CheckNode(AnnotationsLayer.SCALE, true));
        info.add(new CheckNode(AnnotationsLayer.LUT, true));
        info.add(new CheckNode(AnnotationsLayer.IMAGE_ORIENTATION, true));
        info.add(new CheckNode(AnnotationsLayer.WINDOW_LEVEL, true));
        info.add(new CheckNode(AnnotationsLayer.ZOOM, true));
        info.add(new CheckNode(AnnotationsLayer.ROTATION, true));
        info.add(new CheckNode(AnnotationsLayer.FRAME, true));
        info.add(new CheckNode(AnnotationsLayer.PIXEL, true));
        root.add(info);
        drawings = new CheckNode(ActionW.DRAW, true);
        drawings.add(new CheckNode(Tools.MEASURE, true));
        root.add(drawings);

        DefaultTreeModel model = new DefaultTreeModel(root, false);
        tree.constructTree(model);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TreeLayer layer = (TreeLayer) e.getSource();
                TreePath path = layer.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    Object node = path.getLastPathComponent();
                    if (node instanceof CheckNode) {
                        CheckNode checkNode = (CheckNode) node;
                        checkNode.setSelected(!checkNode.isSelected());
                        if (checkNode.isUpdateChildren() && checkNode.getChildCount() > 0) {
                            TreeLayer.fireToChildren(checkNode.children(), checkNode.isSelected());
                        } else if (checkNode.isUpdateParent()) {
                            TreeLayer.fireParentChecked(checkNode);
                        }
                        tree.upadateNode(checkNode);
                        changeLayerSelection(checkNode);
                    }
                }
            }
        });
        expandTree(tree, root);
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void changeLayerSelection(CheckNode userObject) {
        String selection = userObject.toString();
        boolean selected = userObject.isSelected();
        EventManager eventManager = EventManager.getInstance();
        if (IMAGE.equals(selection)) {
            DefaultView2d view = eventManager.getSelectedViewPane();
            if (view != null && selected != view.getImageLayer().isVisible()) {
                view.getImageLayer().setVisible(selected);
                view.repaint();
            }
        } else if (ANNOTATIONS.equals(selection)) {
            DefaultView2d view = eventManager.getSelectedViewPane();
            if (view != null && selected != view.getInfoLayer().isVisible()) {
                view.getInfoLayer().setVisible(selected);
                view.repaint();
            }

        } else if (info.equals(userObject.getParent())) {
            DefaultView2d view = eventManager.getSelectedViewPane();
            if (view != null) {
                AnnotationsLayer layer = view.getInfoLayer();
                if (layer != null) {
                    if (layer.setDisplayPreferencesValue(selection, selected)) {
                        view.repaint();
                    }
                }
            }
        } else if (ActionW.DRAW.toString().equals(selection)) {
            DefaultView2d view = eventManager.getSelectedViewPane();
            if (view != null) {
                view.setDrawingsVisibility(selected);
            }
        } else if (drawings.equals(userObject.getParent())) {
            DefaultView2d view = eventManager.getSelectedViewPane();
            if (view != null && userObject.getUserObject() instanceof Tools) {
                Tools tool = (Tools) userObject.getUserObject();
                AbstractLayer layer = view.getLayerModel().getLayer(tool);
                if (layer != null) {
                    layer.setVisible(selected);
                    view.repaint();
                }
            }
        }
    }

    public void iniTreeValues(DefaultView2d view) {
        if (view != null) {
            image.setSelected(view.getImageLayer().isVisible());
            tree.upadateNode(image);
            AnnotationsLayer layer = view.getInfoLayer();
            if (layer != null) {
                info.setSelected(layer.isVisible());
                Enumeration en = info.children();
                while (en.hasMoreElements()) {
                    Object node = en.nextElement();
                    if (node instanceof CheckNode) {
                        CheckNode checkNode = (CheckNode) node;
                        checkNode.setSelected(layer.getDisplayPreferences(node.toString()));
                    }
                }
                tree.upadateNode(info);
            }
        }
    }

    @Override
    public Component getToolComponent() {
        return this;
    }

    public void expandAllTree() {
        tree.expandRow(4);
    }

    @Override
    protected void changeToolWindowAnchor(ToolWindowAnchor anchor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void changingViewContentEvent(SeriesViewerEvent event) {
        if (event.getEventType().equals(EVENT.SELECT) && event.getSeriesViewer() instanceof View2dContainer) {
            iniTreeValues(((View2dContainer) event.getSeriesViewer()).getSelectedImagePane());
        }
    }

    private static void expandTree(JTree tree, DefaultMutableTreeNode start) {
        for (Enumeration children = start.children(); children.hasMoreElements();) {
            DefaultMutableTreeNode dtm = (DefaultMutableTreeNode) children.nextElement();
            if (!dtm.isLeaf()) {
                //
                TreePath tp = new TreePath(dtm.getPath());
                tree.expandPath(tp);
                //
                expandTree(tree, dtm);
            }
        }
        return;
    }

}
