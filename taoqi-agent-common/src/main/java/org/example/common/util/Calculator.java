package org.example.common.util;

public final class Calculator {

    private Calculator() {
    }

    public static int add(int a, int b) {
        return Math.addExact(a, b);
    }

    public static int divide(int dividend, int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("除数不能为 0");
        }
        return dividend / divisor;
    }
}
