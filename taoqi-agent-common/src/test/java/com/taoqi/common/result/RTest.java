package com.taoqi.common.result;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code R} 的单元测试：验证成功、列表、失败及自定义构造。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
class RTest {

    @Test
    void dataWrapsSingleObject() {
        R<String> result = R.data("hello");

        assertThat(result.code()).isEqualTo(R.SUCCESS_CODE);
        assertThat(result.message()).isEqualTo(R.SUCCESS_MESSAGE);
        assertThat(result.data()).isEqualTo("hello");
        assertThat(result.success()).isTrue();
    }

    @Test
    void listWrapsListData() {
        R<List<String>> result = R.list(List.of("a", "b"));

        assertThat(result.success()).isTrue();
        assertThat(result.data()).containsExactly("a", "b");
    }

    @Test
    void okHasNoData() {
        R<Void> result = R.ok();

        assertThat(result.success()).isTrue();
        assertThat(result.data()).isNull();
    }

    @Test
    void failUsesDefaultCode() {
        R<Void> result = R.fail("参数错误");

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(R.FAIL_CODE);
        assertThat(result.message()).isEqualTo("参数错误");
        assertThat(result.data()).isNull();
    }

    @Test
    void failSupportsCustomCodeAndData() {
        R<List<String>> result = R.fail(400, "部分失败", List.of("a"));

        assertThat(result.success()).isFalse();
        assertThat(result.code()).isEqualTo(400);
        assertThat(result.data()).containsExactly("a");
    }

    @Test
    void ofBuildsCustomResult() {
        R<Integer> result = R.of(201, "created", 1, true);

        assertThat(result.code()).isEqualTo(201);
        assertThat(result.message()).isEqualTo("created");
        assertThat(result.data()).isEqualTo(1);
        assertThat(result.success()).isTrue();
    }
}
