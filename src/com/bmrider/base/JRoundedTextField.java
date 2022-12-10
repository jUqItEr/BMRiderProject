package com.bmrider.base;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JRoundedTextField extends JTextField {
    private final int _arc;
    private Shape shape;

    public JRoundedTextField(int size) {
        super(size);
        setOpaque(false);

        _arc = size;
    }
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, _arc, _arc);
        super.paintComponent(g);
    }

    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, _arc, _arc);
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, _arc, _arc);
        }
        return shape.contains(x, y);
    }
}