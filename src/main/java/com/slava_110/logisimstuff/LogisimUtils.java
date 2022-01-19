package com.slava_110.logisimstuff;

import com.cburch.logisim.data.Value;

public final class LogisimUtils {

    public static Value createValueFromInts(int ...nums) {
        int width = nums.length;
        Value[] values = new Value[nums.length];

        for (int i = 0; i < nums.length; i++) {
            values[i] = createValueFromInt(nums[i]);
        }
        return Value.create(values);
    }

    public static Value createValueFromInt(int num) {
        switch (num) {
            case -2:
                return Value.UNKNOWN;
            case -1:
                return Value.ERROR;
            case 0:
                return Value.FALSE;
            case 1:
                return Value.TRUE;
            default:
                throw new IllegalArgumentException("Expected num in range [-2;1] but got " + num);
        }
    }

    public static int[] getIntsFromValue(Value val) {
        int width = val.getWidth();
        int[] ints = new int[width];

        for (int i = 0; i < width; i++) {
            ints[i] = getIntFromValue(val.get(i));
        }
        return ints;
    }

    public static int getIntFromValue(Value val) {
        if(val.getWidth() != 1)
            throw new IllegalArgumentException("Unable to convert value with width " + val.getWidth() + " to integer");
        if (Value.UNKNOWN.equals(val)) {
            return -2;
        } else if (Value.ERROR.equals(val)) {
            return -1;
        } else if (Value.FALSE.equals(val)) {
            return 0;
        } else if (Value.TRUE.equals(val)) {
            return 1;
        }
        throw new IllegalArgumentException("Expected non-nil value but got " + val.toDisplayString());
    }
}
