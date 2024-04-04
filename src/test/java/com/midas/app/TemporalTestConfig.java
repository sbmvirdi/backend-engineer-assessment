package com.midas.app;

import io.temporal.testing.TestWorkflowEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalTestConfig {

  @Bean
  public TestWorkflowEnvironment testWorkflowEnvironment() {
    return TestWorkflowEnvironment.newInstance();
  }
}
