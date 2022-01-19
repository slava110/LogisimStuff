package com.slava_110.logisimstuff.util;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;

public final class ComponentUtils {

    public static Icon loadIcon(String path) {
        return new ImageIcon(ComponentUtils.class.getClassLoader().getResource(path));
    }

    public static Image loadImage(String path) {
        return Toolkit.getDefaultToolkit().createImage(ComponentUtils.class.getClassLoader().getResource(path));
    }

    public static void drawImage(InstancePainter painter, Image image) {
        drawImage(painter, image, painter.getBounds().getX(), painter.getBounds().getY(), painter.getBounds().getWidth(), painter.getBounds().getHeight());
    }

    public static void drawImage(InstancePainter painter, Image image, int x, int y, int width, int height) {
        painter.getGraphics().drawImage(image, x, y, width, height, (img, infoflags, x1, y1, width1, height1) -> {
            return true;
        });
    }

    public static void drawVarTable(
            Graphics g,
            Bounds bounds,
            int fontSize,
            String[] variables,
            Value[][] values
    ) {
        int rows = (int) (Math.pow(2, variables.length) + 1);
        int columns = variables.length + values.length;

        ComponentUtils.drawTable(g, bounds, rows, columns, 15, (x, y) -> {
            if(y == 0) {
                if(x < variables.length) {
                    return variables[x];
                } else {
                    return "F" + (x - variables.length + 1);
                }
            } else if(x >= variables.length && y > 0) {
                return getDisplayForValue(values[x - variables.length][rows - (y + 1)]);
            } else {
                return String.valueOf(ComponentUtils.getVarForTable(x, y, variables.length));
            }
        });

        Graphics2D g1 = (Graphics2D) g.create();
        g1.setStroke(new BasicStroke(2));

        g1.drawLine(
                bounds.getX(),
                ComponentUtils.getTableY(bounds, 1, rows),
                bounds.getX() + bounds.getWidth(),
                ComponentUtils.getTableY(bounds, 1, rows)
        );

        g1.drawLine(
                ComponentUtils.getTableX(bounds, variables.length, columns),
                bounds.getY(),
                ComponentUtils.getTableX(bounds, variables.length, columns),
                bounds.getY() + bounds.getHeight()
        );

        for (int i = 1; i < variables.length; i++) {
            int y = ComponentUtils.getTableY(bounds, i * variables.length + 1, rows);
            g1.drawLine(
                    bounds.getX(),
                    y,
                    bounds.getX() + bounds.getWidth(),
                    y
            );
        }
        g1.dispose();
    }

    public static String getDisplayForValue(Value val) {
        if(val == Value.TRUE) {
            return "1";
        } else if (val == Value.FALSE) {
            return "0";
        } else if (val == Value.UNKNOWN) {
            return "*";
        } else if(val == Value.ERROR) {
            return "!";
        } else {
            return "?";
        }
    }

    public static void drawTable(Graphics g, Bounds bounds, int rows, int columns, int fontSize, BiFunction<Integer, Integer, String> cellFunc) {
        for (int i = 1; i < rows; i++) {
            g.drawLine(bounds.getX(), getTableY(bounds, i, rows), bounds.getX() + bounds.getWidth(), getTableY(bounds, i, rows));
        }
        for (int i = 1; i < columns; i++) {
            g.drawLine(getTableX(bounds, i, columns), bounds.getY(), getTableX(bounds, i, columns), bounds.getY() + bounds.getHeight());
        }

        useFont(g, getDefaultFont(fontSize), fm -> {
            for (int x = 0; x < columns; x++) {
                for (int y = 0; y < rows; y++) {
                    String cell = cellFunc.apply(x, y);
                    g.drawString(
                            cell,
                            getTableX(bounds, x, columns) + getTableCellWidth(bounds, columns) / 2 - fm.stringWidth(cell) / 2,
                            getTableY(bounds, y, rows) + getTableCellHeight(bounds, rows) / 2 + (fm.getAscent() - fm.getDescent()) / 2
                    );
                }
            }
        });
    }

    public static int getTableX(Bounds bounds, int x, int columns) {
        return bounds.getX() + getTableCellWidth(bounds, columns) * x;
    }

    public static int getTableCellWidth(Bounds bounds, int columns) {
        return bounds.getWidth() / columns;
    }

    public static int getTableY(Bounds bounds, int y, int rows) {
        return bounds.getY() + getTableCellHeight(bounds, rows) * y;
    }

    public static int getTableCellHeight(Bounds bounds, int rows) {
        return bounds.getHeight() / rows;
    }

    public static Font getDefaultFont(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }

    public static void useFont(Graphics g, Font font, Consumer<FontMetrics> func) {
        Font oldFont = g.getFont();
        g.setFont(font);
        func.accept(g.getFontMetrics());
        g.setFont(oldFont);
    }

    public static int getVarForTable(int x, int y, int amountOfVars) {
        return Character.getNumericValue(String.format("%" + amountOfVars + "s", Integer.toBinaryString(y - 1)).replace(' ', '0').charAt(x));
    }

    @Nullable
    public static <T extends InstanceData> T getData(InstanceState state) {
        return (T) state.getData();
    }

    @NotNull
    public static <T extends InstanceData> T getOrCreateData(InstanceState state, Supplier<T> provider) {
        T data = getData(state);
        if(data == null) {
            data = provider.get();
            state.setData(data);
        }
        return data;
    }
}
