package com.taoqi.common.result;

import java.util.List;

/**
 * 通用接口返回结果，统一接口的成功/失败结构与业务状态码。
 *
 * <p>字段固定为 {@code code}、{@code message}、{@code data}、{@code success}；
 * {@code data} 为泛型，可直接返回单个对象、列表或分页数据，例如：
 *
 * <pre>{@code
 * R<User> user = R.data(user);
 * R<List<User>> users = R.list(userList);
 * R<Void> failed = R.fail("参数错误");
 * }</pre>
 *
 * @param code    业务状态码，成功默认 200
 * @param message 提示信息
 * @param data    业务数据
 * @param success 是否成功
 * @param <T>     业务数据类型
 * @author xiaotao
 * @date 2026-11-03
 */
public record R<T>(int code, String message, T data, boolean success) {

    /**
     * 成功状态码。
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码。
     */
    public static final int FAIL_CODE = 500;

    /**
     * 成功时的默认提示信息。
     */
    public static final String SUCCESS_MESSAGE = "success";

    /**
     * 成功并携带数据。
     */
    public static <T> R<T> data(T data) {
        return new R<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, true);
    }

    /**
     * 成功并携带列表数据，等价于 {@code R.data(list)}，用于明确表达列表语义。
     */
    public static <T> R<List<T>> list(List<T> data) {
        return R.data(data);
    }

    /**
     * 成功且没有数据。
     */
    public static R<Void> ok() {
        return new R<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, true);
    }

    /**
     * 成功并自定义提示信息。
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(SUCCESS_CODE, message, data, true);
    }

    /**
     * 失败，使用默认失败码 500。
     */
    public static <T> R<T> fail(String message) {
        return new R<>(FAIL_CODE, message, null, false);
    }

    /**
     * 失败，指定业务状态码。
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, false);
    }

    /**
     * 失败，指定业务状态码并携带补充数据。
     */
    public static <T> R<T> fail(int code, String message, T data) {
        return new R<>(code, message, data, false);
    }

    /**
     * 完全自定义返回结果。
     */
    public static <T> R<T> of(int code, String message, T data, boolean success) {
        return new R<>(code, message, data, success);
    }
}
