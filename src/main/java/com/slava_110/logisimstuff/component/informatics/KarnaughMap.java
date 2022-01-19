package com.slava_110.logisimstuff.component.informatics;

import static com.slava_110.logisimstuff.util.ComponentUtils.getTableCellHeight;
import static com.slava_110.logisimstuff.util.ComponentUtils.getTableCellWidth;
import static com.slava_110.logisimstuff.util.ComponentUtils.getTableX;
import static com.slava_110.logisimstuff.util.ComponentUtils.getTableY;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeEvent;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.component.informatics.KMapSolver.KMapCell;
import com.slava_110.logisimstuff.util.ComponentUtils;
import com.slava_110.logisimstuff.util.TerminalWriter;

public class KarnaughMap extends InstanceFactory {
    private static final Attribute<String> ATTR_VAR_1 = Attributes.forString(
            "variable1",
            () -> "Variable 1"
    );
    private static final Attribute<String> ATTR_VAR_2 = Attributes.forString(
            "variable2",
            () -> "Variable 2"
    );
    private static final Attribute<String> ATTR_VAR_3 = Attributes.forString(
            "variable3",
            () -> "Variable 3"
    );
    private static final Attribute<String> ATTR_VAR_4 = Attributes.forString(
            "variable4",
            () -> "Variable 4"
    );

    public KarnaughMap() {
        super("karnaugh_map", () -> "Karnaugh Map");
        setOffsetBounds(Bounds.create(-180, -180, 180, 180));

        setAttributes(
                new Attribute[]{
                        ATTR_VAR_1, ATTR_VAR_2, ATTR_VAR_3, ATTR_VAR_4
                }, new Object[]{
                        "a", "b", "c", "d"
                }
        );

        Port[] ports = new Port[6];
        ports[0] = new Port(-180, -90, Port.INPUT, 16);
        ports[0].setToolTip(() -> "Function Data");
        ports[1] = new Port(-180, -60, Port.INPUT, 1);
        ports[1].setToolTip(() -> "MKNF?");
        ports[2] = new Port(0, -120, Port.OUTPUT, 7);
        ports[2].setToolTip(() -> "Interval Writer Output");
        ports[3] = new Port(0, -90, Port.INPUT, 1);
        ports[3].setToolTip(() -> "Interval Writer Next Char");
        ports[4] = new Port(0, -60, Port.OUTPUT, 1);
        ports[4].setToolTip(() -> "Interval Writer Status");
        ports[5] = new Port(0, -30, Port.INPUT, 1);
        ports[5].setToolTip(() -> "Interval Writer Reset");


        setPorts(ports);
    }

    @Override
    public InstanceState createInstanceState(CircuitState state, Instance instance) {
        InstanceState instanceState = super.createInstanceState(state, instance);

        instance.getAttributeSet().addAttributeListener(new AttributeListener() {
            @Override
            public void attributeListChanged(AttributeEvent e) {}

            @Override
            public void attributeValueChanged(AttributeEvent e) {
                KarnaughMapData data = (KarnaughMapData) instanceState.getData();
                if(data != null)
                    data.tryCalculateIntervals(instanceState);
            }
        });

        return instanceState;
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawBounds();
        painter.drawPorts();

        Graphics g = painter.getGraphics();
        Bounds bounds = painter.getBounds();
        KarnaughMapData data = ComponentUtils.getData(painter);

        if(data != null) {
            ComponentUtils.drawTable(g, bounds, 5, 5, 20, (x, y) -> {
                if(y == 0 && x > 0) {
                    return KMapSolver.KMapGrayCodes.get(4).rows.get(x - 1);
                } else if(x == 0 && y > 0) {
                    return KMapSolver.KMapGrayCodes.get(4).cols.get(y - 1);
                } else if(x > 0 && y > 0) {
                    return getDisplayForValue(data, x - 1, y - 1);
                } else {
                    return "";
                }
            });

            drawIntervals(g, bounds, data);
        }
    }

    private static final Color[] INTERVAL_COLORS = new Color[]{
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.ORANGE,
            Color.PINK,
            Color.RED,
            Color.YELLOW
    };

    private void drawIntervals(Graphics g, Bounds bounds, KarnaughMapData data) {
        if(data.solveRes != null && !data.solveRes.groups.isEmpty()) {
            Graphics2D g1 = (Graphics2D) g.create();

            g1.setStroke(new BasicStroke(2));
            g1.clipRect(
                    bounds.getX() + getTableCellWidth(bounds, 5),
                    bounds.getY() + getTableCellHeight(bounds, 5),
                    bounds.getWidth() - getTableCellWidth(bounds, 5),
                    bounds.getHeight() - getTableCellHeight(bounds, 5)
            );

            int lastColor = 0;
            for (List<KMapCell> interval : data.solveRes.groups) {
                g1.setColor(INTERVAL_COLORS[lastColor++]);

                KMapCell begin = interval.get(0);
                KMapCell end = interval.get(interval.size() - 1);

                boolean yCont = end.row - begin.row == 3 && !interval.stream().anyMatch(c -> c.row == 1);
                boolean xCont = end.col - begin.col == 3 && !interval.stream().anyMatch(c -> c.col == 1);

                if(xCont && yCont) {
                    drawInterval(g1, bounds, begin.col + 1, begin.row + 1, 0, 0);
                    drawInterval(g1, bounds, begin.col + 1, end.row + 1, 0, 5);
                    drawInterval(g1, bounds, end.col + 1, begin.row + 1, 5, 0);
                    drawInterval(g1, bounds, end.col + 1, end.row + 1, 5, 5);
                } else if(yCont) {
                    drawInterval(g1, bounds, begin.col + 1, begin.row + 1, end.col + 1, 0);
                    drawInterval(g1, bounds, end.col + 1, end.row + 1, begin.col + 1, 5);
                } else if(xCont) {
                    drawInterval(g1, bounds, begin.col + 1, begin.row + 1, 0, end.row + 1);
                    drawInterval(g1, bounds, end.col + 1, end.row + 1, 5, begin.row + 1);
                } else {
                    drawInterval(g1, bounds, begin.col + 1, begin.row + 1, end.col + 1, end.row + 1);
                }
            }
            g1.dispose();
        }
    }

    private void drawInterval(Graphics g, Bounds bounds, int x1, int y1, int x2, int y2) {
        g.drawRoundRect(
                getTableX(bounds, Math.min(x1, x2), 5) + 3,
                getTableY(bounds, Math.min(y1, y2), 5) + 3,
                getTableCellWidth(bounds, 5) * (Math.abs(x2 - x1) + 1) - 6,
                getTableCellHeight(bounds, 5) * (Math.abs(y2 - y1) + 1) - 6,
                10,
                10
        );
    }

    private String getDisplayForValue(KarnaughMapData data, int x, int y) {
        if(x > 1)
            x = x == 2 ? 3 : 2;
        if(y > 1)
            y = y == 2 ? 3 : 2;

        int index = y * 4 + x;
        if(data.minterms.contains(index))
            return "1";
        else if(data.dontcares.contains(index))
            return "X";
        else if(data.errored.contains(index))
            return "!";
        else
            return "0";
    }

    @Override
    public void propagate(InstanceState state) {
        KarnaughMapData data = ComponentUtils.getOrCreateData(state, () -> {
            state.setPort(4, Value.FALSE, 1);
            return new KarnaughMapData();
        });
        data.propogate(state);
    }

    private static class KarnaughMapData implements InstanceData, Cloneable {
        public final List<Integer> minterms = new ArrayList<>();
        public final List<Integer> dontcares = new ArrayList<>();
        public final List<Integer> errored = new ArrayList<>();
        @Nullable
        public KMapSolver.KMapResult solveRes = null;
        @Nullable
        private Value cachedVal = null;
        @Nullable
        public Value cachedMKNF = null;
        private boolean mknf = false;
        public final TerminalWriter writer = new TerminalWriter(
                () -> solveRes != null ? solveRes.expression : null, 2, 4, 3, 5
        );

        public KarnaughMapData() {}

        public void propogate(InstanceState state) {
            updateFromValue(state, state.getPort(0));
            if(state.getPort(1) != cachedMKNF) {
                cachedMKNF = state.getPort(1);
                if(cachedMKNF == Value.TRUE && !mknf) {
                    mknf = true;
                    tryCalculateIntervals(state);
                } else if(mknf) {
                    mknf = false;
                    tryCalculateIntervals(state);
                }
            }
            writer.propogate(state);
        }

        private void updateFromValue(InstanceState state, Value compVal) {
            if(compVal == cachedVal)
                return;
            cachedVal = compVal;

            minterms.clear();
            dontcares.clear();

            for (int i = 0; i < compVal.getAll().length; i++) {
                Value val = compVal.get(compVal.getWidth() - i - 1);

                if(val == Value.TRUE) {
                    minterms.add(i);
                } else if(val == Value.UNKNOWN) {
                    dontcares.add(i);
                } else if(val == Value.ERROR) {
                    errored.add(i);
                }
            }

            tryCalculateIntervals(state);
        }

        private void tryCalculateIntervals(InstanceState state) {
            solveRes = KMapSolver.solve(
                    Arrays.asList(
                            state.getAttributeValue(ATTR_VAR_1),
                            state.getAttributeValue(ATTR_VAR_2),
                            state.getAttributeValue(ATTR_VAR_3),
                            state.getAttributeValue(ATTR_VAR_4)
                    ),
                    minterms,
                    dontcares,
                    mknf
            );
        }

        @Override
        public KarnaughMapData clone() {
            return new KarnaughMapData();
        }
    }
}
