package com.slava_110.logisimstuff.component.informatics;

import java.awt.Graphics;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.slava_110.logisimstuff.util.ComponentUtils;

public class CounterTable extends InstanceFactory {
    private static final Value[][] EMPTY_TABLE = new Value[4][16];

    public CounterTable() {
        super("counter-table", () -> "Counter Table");
        setOffsetBounds(Bounds.create(-180, -255, 180, 255));

        Port[] ports = new Port[7];
        ports[0] = new Port(-180, -240, Port.INPUT, 4);
        ports[0].setToolTip(() -> "Counter Direction");
        ports[1] = new Port(-180, -210, Port.INPUT, 4);
        ports[1].setToolTip(() -> "Counter Max");
        ports[2] = new Port(-180, -180, Port.INPUT, 4);
        ports[2].setToolTip(() -> "Counter Step");

        ports[3] = new Port(0, -240, Port.OUTPUT, 16);
        ports[3].setToolTip(() -> "Q3(t+1)");
        ports[4] = new Port(0, -210, Port.OUTPUT, 16);
        ports[4].setToolTip(() -> "Q2(t+1)");
        ports[5] = new Port(0, -180, Port.OUTPUT, 16);
        ports[5].setToolTip(() -> "Q1(t+1)");
        ports[6] = new Port(0, -150, Port.OUTPUT, 16);
        ports[6].setToolTip(() -> "Q0(t+1)");

        setPorts(ports);
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        painter.drawBounds();
        painter.drawPorts();

        Graphics g = painter.getGraphics();
        Bounds bounds = painter.getBounds();

        CounterData data = ComponentUtils.getData(painter);
        Value[][] cachedValues = data != null ? data.table : EMPTY_TABLE;

        ComponentUtils.drawVarTable(
                g,
                bounds,
                15,
                new String[] {"Q3", "Q2", "Q1", "Q0"},
                cachedValues
        );
    }

    @Override
    public void propagate(InstanceState state) {
        CounterData data = ComponentUtils.getOrCreateData(state, () -> new CounterData());
        data.propogate(state);
    }

    private static class CounterData implements InstanceData, Cloneable {
        private Value cachedDir = null;
        private boolean dir = false;
        private Value cachedMax = null;
        private int max = 0;
        private Value cachedStep = null;
        private int step = 0;
        public Value[][] table = new Value[4][16];

        public CounterData() {

        }

        public void propogate(InstanceState state) {
            boolean modified = false;
            if(cachedDir != state.getPort(0)) {
                cachedDir = state.getPort(0);
                dir = cachedDir.toIntValue() == 1 ? true : false;
                modified = true;
            }
            if(cachedMax != state.getPort(1)) {
                cachedMax = state.getPort(1);
                max = cachedMax.toIntValue();
                modified = true;
            }
            if(cachedStep != state.getPort(2)) {
                cachedStep = state.getPort(2);
                step = cachedStep.toIntValue();
                modified = true;
            }

            if(modified)
                createResults(state);
        }

        private void createResults(InstanceState state) {
            if(max < 0 || step < 0)
                return;
            int mod = dir ? -1 : 1;

            for (int y = 0; y <= max; y++) {
                Value[] row = new Value[4];
                int num = y + step * mod;
                if(num < 0)
                    num = num + max - 1 * mod;
                if(num > max)
                    num = num - max - 1 * mod;

                String binaryNum = String.format("%4s", Integer.toBinaryString(num)).replace(' ', '0');
                for (int x = 0; x < binaryNum.length(); x++) {
                    switch (binaryNum.charAt(binaryNum.length() - 1 - x)) {
                        case '0':
                            table[binaryNum.length() - 1 - x][15 - y] = Value.FALSE;
                            break;
                        case '1':
                            table[binaryNum.length() - 1 - x][15 - y] = Value.TRUE;
                    }
                }
            }
            if(max < 15) {
                for (int y = max + 1; y < 16; y++) {
                    for (int x = 0; x < 4; x++) {
                        table[3 - x][15 - y] = Value.UNKNOWN;
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                state.setPort(i + 3, Value.create(table[i]), 20);
            }
        }

        @Override
        public CounterData clone() {
            return new CounterData();
        }
    }
}
