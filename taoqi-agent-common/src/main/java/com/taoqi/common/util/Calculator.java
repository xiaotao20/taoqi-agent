package com.taoqi.common.util;

/**
 * 简单计算工具类：提供加法与除法运算。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
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
