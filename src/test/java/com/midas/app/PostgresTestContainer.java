package com.midas.app;

import org.testcontainers.containers.PostgreSQLContainer;

class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

  private static final String POSTGRES_IMAGE = "postgres:latest";
  private static final String DB_NAME = "test-container";
  private static final String DB_USER_NAME = "test";
  private static final String DB_PASSWORD = "test";

  private static PostgresTestContainer container;

  private PostgresTestContainer() {
    super(POSTGRES_IMAGE);
  }

  static PostgresTestContainer getInstance() {
    if (container == null) {
      container =
          new PostgresTestContainer()
              .withDatabaseName(DB_NAME)
              .withUsername(DB_USER_NAME)
              .withPassword(DB_PASSWORD);
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("TEST_DB_URL", container.getJdbcUrl());
    System.setProperty("TEST_DB_USERNAME", container.getUsername());
    System.setProperty("TEST_DB_PASSWORD", container.getPassword());
    logger().info("Test Postgres URL:" + container.getJdbcUrl());
  }

  @Override
  public void stop() {
    // do nothing, JVM handles shut down
  }
}
