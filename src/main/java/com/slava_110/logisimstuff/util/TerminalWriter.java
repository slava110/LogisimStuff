package com.slava_110.logisimstuff.util;

import java.util.function.Supplier;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceState;

public class TerminalWriter {
    private final Supplier<String> textSupplier;
    private final int outputPort;
    private final int statusPort;
    private final int nextPort;
    private final int resetPort;
    private char[] chars;
    private int index = 0;
    private Value cachedNext = Value.NIL;
    private Value cachedReset = Value.NIL;

    public TerminalWriter(Supplier<String> textSupplier, int outputPort, int statusPort, int nextPort, int resetPort) {
        this.textSupplier = textSupplier;
        this.outputPort = outputPort;
        this.statusPort = statusPort;
        this.nextPort = nextPort;
        this.resetPort = resetPort;
    }

    public void propogate(InstanceState state) {
        Value act = state.getPort(nextPort);
        if(cachedNext != act) {
            cachedNext = act;
            if(act == Value.TRUE) {
                if(chars == null) {
                    startWriting(state);
                } else if(index < chars.length) {
                    nextChar(state);
                }
            }
        }
        Value reset = state.getPort(resetPort);
        if(cachedReset != reset) {
            cachedReset = reset;
            if(reset == Value.TRUE) {
                reset(state);
            }
        }
    }

    public void reset(InstanceState state) {
        chars = null;
        index = 0;
        if(state.getPort(statusPort) != Value.FALSE)
            state.setPort(statusPort, Value.FALSE, 20);
    }

    private void startWriting(InstanceState state) {
        String textToWrite = textSupplier.get();
        if(textToWrite != null && !textToWrite.isEmpty()) {
            chars = textToWrite.toCharArray();
            nextChar(state);
        }
    }

    private void nextChar(InstanceState state) {
        char c = chars[index++];
        state.setPort(outputPort, encodeChar(c), 20);
        if(index == chars.length && state.getPort(statusPort) != Value.TRUE)
            state.setPort(statusPort, Value.TRUE, 20);
    }

    private Value encodeChar(char c) {
        Value[] values = new Value[7];
        String rawBinary = new StringBuilder(Integer.toBinaryString(c)).reverse().toString();
        String binaryChar = String.format("%-7s", rawBinary).replace(' ', '0');

        char[] chars = binaryChar.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '0':
                    values[i] = Value.FALSE;
                    break;
                case '1':
                    values[i] = Value.TRUE;
                    break;
                default:
                    throw new IllegalArgumentException("Expected binary string!");
            }
        }
        return Value.create(values);
    }
}
