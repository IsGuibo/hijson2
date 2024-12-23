package com.hgb.tools.element;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

/**
 * <p>Title: OpenSwing</p> 
 * <p>Description: </p> 
 * <p>Copyright: Copyright (c) 2005</p> 
 * <p>Company: </p> 
 * @author <a href="mailto:sunkingxie@hotmail.com">SunKing</a> 
 * @version 1.0
 */
public class JCloseableTabbedPane extends JTabbedPane implements Serializable {
    public static final String ON_TAB_CLOSE = "ON_TAB_CLOSE";
    public static final String ON_TAB_DOUBLECLICK = "ON_TAB_DOUBLECLICK";
    private JPopupMenu popup = null;

    public JCloseableTabbedPane() {
        super();
        init();
    }

    public JCloseableTabbedPane(int tabPlacement) {
        super(tabPlacement);
        init();
    }

    public JCloseableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        init();
    }

    protected void init() {
        addMouseListener(new DefaultMouseAdapter());
    }

    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }

    public void setIconDrawCenter(int index, boolean drawCenter) {
        ((CloseIcon) getIconAt(index)).setDrawCenter(drawCenter);
        repaint();
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    public boolean isDrawCenter(int index) {
        return ((CloseIcon) getIconAt(index)).isDrawCenter();
    }

    protected EventListenerList closeListenerList = new EventListenerList();

    public void addCloseListener(ActionListener l) {
        closeListenerList.add(ActionListener.class, l);
    }

    public void removeCloseListener(ActionListener l) {
        closeListenerList.remove(ActionListener.class, l);
    }

    protected void fireClosed(ActionEvent e) {
        Object[] listeners = closeListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    class DefaultMouseAdapter extends MouseAdapter {
        CloseIcon icon;

        public void mousePressed(MouseEvent e) {
            int index = indexAtLocation(e.getX(), e.getY());
            if (index != -1) {
                icon = (CloseIcon) getIconAt(index);
                if (icon.getBounds().contains(e.getPoint())) {
                    icon.setPressed(true);
                    fireClosed(new ActionEvent(
                            e.getComponent(),
                            ActionEvent.ACTION_PERFORMED,
                            ON_TAB_CLOSE));
                } else if (e.getClickCount() == 2) {
                    fireClosed(new ActionEvent(
                            e.getComponent(),
                            ActionEvent.ACTION_PERFORMED,
                            ON_TAB_DOUBLECLICK));
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            int index = indexAtLocation(e.getX(), e.getY());
            if (index != -1) {
                icon = (CloseIcon) getIconAt(index);
                if (icon.getBounds().contains(e.getPoint())) {
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (icon != null) {
                icon.setPressed(false);
                icon = null;
                repaint();
            }
            if (popup != null) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if (indexAtLocation(e.getX(), e.getY()) != -1) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    public Icon getIconAt(int index) {
        Icon icon = super.getIconAt(index);
        if (!(icon instanceof CloseIcon)) {
            super.setIconAt(index, new CloseIcon());
        }
        return super.getIconAt(index);
    }

    static class CloseIcon implements Icon {
        Rectangle rec = new Rectangle(0, 0, 15, 16);
        private boolean pressed = false;
        private boolean drawCenter = true;

        public synchronized void paintIcon(
                Component c, Graphics g, int x1, int y1) {
            int x = x1, y = y1 + 1;
            if (pressed) {
                x++;
                y++;
            }
            rec.x = x;
            rec.y = y;
            Color oldColor = Color.WHITE;
            //去掉按钮立体形状  
/*            g.setColor(UIManager.getColor("TabbedPane.highlight")); 
            g.drawLine(x, y, x, y + rec.height); 
            g.drawLine(x, y, x + rec.width, y); 
            g.setColor(UIManager.getColor("TabbedPane.shadow")); 
            g.drawLine(x, y + rec.height, x + rec.width, y + rec.height); 
            g.drawLine(x + rec.width, y, x + rec.width, y + rec.height); 
            g.setColor(UIManager.getColor("TabbedPane.foreground"));*/
            //draw X  
            //left top  
            g.drawRect(x + 4, y + 4, 1, 1);
            g.drawRect(x + 5, y + 5, 1, 1);
            g.drawRect(x + 5, y + 9, 1, 1);
            g.drawRect(x + 4, y + 10, 1, 1);
            //center  
            if (drawCenter) {
                g.drawRect(x + 6, y + 6, 1, 1);
                g.drawRect(x + 8, y + 6, 1, 1);
                g.drawRect(x + 6, y + 8, 1, 1);
                g.drawRect(x + 8, y + 8, 1, 1);
            }
            //right top  
            g.drawRect(x + 10, y + 4, 1, 1);
            g.drawRect(x + 9, y + 5, 1, 1);
            //right bottom  
            g.drawRect(x + 9, y + 9, 1, 1);
            g.drawRect(x + 10, y + 10, 1, 1);
            g.setColor(oldColor);
        }

        private void drawRec(Graphics g, int x, int y) {
            g.drawRect(x, y, 1, 1);
        }

        public Rectangle getBounds() {
            return rec;
        }

        public void setBounds(Rectangle rec) {
            this.rec = rec;
        }

        public int getIconWidth() {
            return rec.width;
        }

        public int getIconHeight() {
            return rec.height;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        public void setDrawCenter(boolean drawCenter) {
            this.drawCenter = drawCenter;
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isDrawCenter() {
            return drawCenter;
        }
    }

}  