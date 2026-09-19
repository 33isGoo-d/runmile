package com.runmile.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DemoControllerConditionTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DemoController.class);

    @Test
    void 기본_설정에서는_데모_초기화_API를_등록하지_않는다() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DemoController.class));
    }
}
