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
package org.weasis.core.ui.graphic.model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;

import org.weasis.core.ui.Messages;
import org.weasis.core.ui.graphic.AbstractDragGraphic;
import org.weasis.core.ui.graphic.Graphic;

/**
 * The Class AbstractLayer.
 * 
 * @author Nicolas Roduit
 */
public abstract class AbstractLayer implements Comparable, Serializable, Layer {

    private static final long serialVersionUID = -6113490831569841167L;
    // protected ProjectSettingsData settingsData;
    protected PropertyChangeListener pcl;
    protected transient ArrayList<LayerModel> canvas = new ArrayList<LayerModel>();
    private boolean masked;
    private int level;
    private final int drawType;
    protected ArrayList<Graphic> graphics;

    /**
     * The Class PropertyChangeHandler.
     * 
     * @author Nicolas Roduit
     */
    class PropertyChangeHandler implements PropertyChangeListener, Serializable {

        // This method gets called when a bound property is changed, inherite by PropertyChangeListener
        public void propertyChange(PropertyChangeEvent propertychangeevent) {
            Object obj = propertychangeevent.getSource();
            String s = propertychangeevent.getPropertyName();
            if ("bounds".equals(s)) { //$NON-NLS-1$
                graphicBoundsChanged((Rectangle) propertychangeevent.getOldValue(), (Rectangle) propertychangeevent
                    .getNewValue());
            } else if ("updatePane".equals(s)) { //$NON-NLS-1$
            }
            // pour toutes les autres propriétés des graphic : "selected", "shape", "intersectshape"
            else {
                // if (obj instanceof AbstractDragGraphic) {
                // repaint(((AbstractDragGraphic) obj).getRepaintBounds());
                // }
                // else {
                // repaint(((Graphic) obj).getRepaintBounds());
                // }
            }
        }

        // version 1.0 n'avait pas de UID
        // protected static final long serialVersionUID = -9094820911680205527L;
        private static final long serialVersionUID = -9094820911680205527L;

        private PropertyChangeHandler() {
        }
    }

    public AbstractLayer(LayerModel canvas1, int drawMode) {
        this.drawType = drawMode;
        this.canvas.add(canvas1);
        graphics = new ArrayList<Graphic>();
        pcl = new PropertyChangeHandler();
        // settingsData = WeasisWin.getInstance().getProjectSettingsData();
    }

    public void toFront(Graphic graphic) {
        graphics.remove(graphic);
        graphics.add(graphic);
        // repaint(graphic.getv);
    }

    public void toBack(Graphic graphic) {
        graphics.remove(graphic);
        graphics.add(0, graphic);
        // repaint(graphic.getRepaintBounds());
    }

    public void setShowDrawing(LayerModel canvas1) {
        if (canvas != null) {
            if (!canvas.contains(canvas1)) {
                this.canvas.add(canvas1);
            }
            // settingsData = WeasisWin.getInstance().getProjectSettingsData();
        }
    }

    public LayerModel getShowDrawing() {
        return canvas.get(0);
    }

    public void setVisible(boolean flag) {
        this.masked = !flag;
    }

    public boolean isVisible() {
        return !masked;
    }

    public void setLevel(int i) {
        level = i;
    }

    public int getLevel() {
        return level;
    }

    public void removeGraphicAndRepaint(Graphic graphic) {
        graphics.remove(graphic);
        graphic.removePropertyChangeListener(pcl);
        if (graphic instanceof AbstractDragGraphic) {
            repaint(((AbstractDragGraphic) graphic).getTransformedBounds());
        } else {
            repaint(graphic.getRepaintBounds());
        }
        graphic.setLayer(null);
        if (graphic.isSelected()) {
            getShowDrawing().getSelectedGraphics().remove(graphic);
        }
    }

    public void removeGraphic(Graphic graphic) {
        graphics.remove(graphic);
        graphic.removePropertyChangeListener(pcl);
        graphic.setLayer(null);
        if (graphic.isSelected()) {
            getShowDrawing().getSelectedGraphics().remove(graphic);
        }
    }

    public java.util.List getGraphics() {
        return graphics;
    }

    public abstract java.util.List getGraphicsSurfaceInArea(Rectangle rect);

    public abstract java.util.List getGraphicsBoundsInArea(Rectangle rect);

    public abstract Graphic getGraphicContainPoint(Point pos);

    public abstract void paint(Graphics2D g2, AffineTransform transform, AffineTransform inverseTransform,
        Rectangle bound);

    // public abstract void paintSVG(SVGGraphics2D g2);

    // interface comparable, permet de trier par ordre croissant les layers
    public int compareTo(Object obj) {
        int thisVal = this.getLevel();
        int anotherVal = ((AbstractLayer) obj).getLevel();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    public void repaint(Rectangle rectangle) {
        for (int i = 0; i < canvas.size(); i++) {
            canvas.get(i).repaint(rectangle);
        }
    }

    public void repaint() {
        for (int i = 0; i < canvas.size(); i++) {
            canvas.get(i).repaint();
        }
    }

    protected void graphicBoundsChanged(Rectangle rectangle, Rectangle rectangle1) {
        if (rectangle == null) {
            if (rectangle1 != null) {
                repaint(rectangle1);
            }
        } else if (rectangle1 != null) {
            repaint(rectangle.union(rectangle1));
        }
    }

    public int getDrawType() {
        return drawType;
    }

    public void deleteAllGraphic() {
        if (graphics != null) {
            for (int i = graphics.size() - 1; i >= 0; i--) {
                removeGraphic(graphics.get(i));
            }
        }
        repaint();
    }

    // public ProjectSettingsData getSettingsData() {
    // return settingsData;
    // }
}
