package com.midas.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.generated.model.AccountDto;
import com.midas.generated.model.CreateAccountDto;
import io.temporal.testing.TestWorkflowEnvironment;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class TestMidasApplication {

  @Autowired private ObjectMapper objectMapper;

  @Autowired private TestRestTemplate testRestTemplate;

  public static final String CREATE_ACCOUNT_URL = "/accounts";
  public static final String INDIVIDUAL_ACCOUNT_URL = "/accounts/{accountId}";

  private static final Network network = Network.newNetwork();

  @Container
  private static final PostgresTestContainer postgreSqlContainer =
      PostgresTestContainer.getInstance();

  private static UUID accountId;
  private static String firstName;
  private static String lastName;
  private static String email;
  private static Integer dbPort;

  @DynamicPropertySource
  public static void overrideProps(DynamicPropertyRegistry registry) {
    dbPort = postgreSqlContainer.getMappedPort(5432);
    registry.add("spring.datasource.username", postgreSqlContainer::getUsername);
    registry.add("spring.datasource.password", postgreSqlContainer::getPassword);
    registry.add("spring.datasource.url", postgreSqlContainer::getJdbcUrl);
  }

  //  @Container
  //  public static final GenericContainer<?> elasticsearchContainer =
  //      new GenericContainer<>("elasticsearch:7.16.2")
  //          .withEnv("cluster.routing.allocation.disk.threshold_enabled", "true")
  //          .withEnv("cluster.routing.allocation.disk.watermark.low", "512mb")
  //          .withEnv("cluster.routing.allocation.disk.watermark.high", "256mb")
  //          .withEnv("cluster.routing.allocation.disk.watermark.flood_stage", "128mb")
  //          .withEnv("discovery.type", "single-node")
  //          .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
  //          .withEnv("xpack.security.enabled", "false")
  //          .withNetwork(network)
  //          .waitingFor(Wait.forHttp("/").forPort(9200));
  //
  //  @Container
  //  public static final GenericContainer<?> temporalContainer =
  //      new GenericContainer<>("temporalio/auto-setup:1.23.0")
  //          .withEnv("DB", "postgresql")
  //          .withEnv("DB_PORT", String.valueOf(dbPort))
  //          .withEnv("POSTGRES_USER", "midas-user")
  //          .withEnv("POSTGRES_PWD", "midas-app")
  //          .withEnv("POSTGRES_SEEDS", "postgresql")
  //          .withEnv(
  //              "DYNAMIC_CONFIG_FILE_PATH",
  // "/etc/temporal/config/dynamicconfig/development-sql.yaml")
  //          .withEnv("ENABLE_ES", "true")
  //          .withEnv("ES_SEEDS", "elasticsearch")
  //          .withEnv("ES_VERSION", "v7")
  //          .withNetwork(network)
  //          .withExposedPorts(7233);
  //
  //  @Container
  //  public static final GenericContainer<?> temporalAdminToolsContainer =
  //      new GenericContainer<>("temporalio/admin-tools:1.23.0")
  //          .withEnv("TEMPORAL_ADDRESS", "temporal:7233")
  //          .withNetwork(network)
  //          .withEnv("TEMPORAL_CLI_ADDRESS", "temporal:7233");
  //
  //  @Container
  //  public static final GenericContainer<?> temporalUiContainer =
  //      new GenericContainer<>("temporalio/ui:2.22.3")
  //          .withEnv("TEMPORAL_ADDRESS", "temporal:7233")
  //          .withEnv("TEMPORAL_CORS_ORIGINS", "http://localhost:3000")
  //          .withNetwork(network)
  //          .withExposedPorts(8080);

  @Autowired private TestWorkflowEnvironment testWorkflowEnvironment;

  @BeforeAll
  static void setUp() {
    // Start containers
    //    elasticsearchContainer.start();
    //    temporalContainer.start();
    //    temporalAdminToolsContainer.start();
    //    temporalUiContainer.start();
  }

  @AfterAll
  static void tearDown() {
    // Stop containers
    //    temporalUiContainer.stop();
    //    temporalAdminToolsContainer.stop();
    //    temporalContainer.stop();
    //    elasticsearchContainer.stop();
  }

  /**
   * function to populate the DTO from json
   *
   * @param fileName -- name of the json file
   * @param clazz -- class type of the DTO
   * @param <E> - type of the DTO
   * @return - object of DTO with data populated
   */
  private <E> E readRequest(String fileName, Class<E> clazz) {
    try {
      return objectMapper.readValue(
          this.getClass().getClassLoader().getResourceAsStream("requests/" + fileName + ".json"),
          clazz);
    } catch (IOException e) {
      return null;
    }
  }

  @Test
  @Order(1)
  void createAccount() {
    var headers = createHeader();
    var requestBody = readRequest("createAccount", CreateAccountDto.class);
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.POST,
            new HttpEntity<>(requestBody, headers),
            AccountDto.class);
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    accountId = response.getBody().getId();
    firstName = response.getBody().getFirstName();
    lastName = response.getBody().getLastName();
    email = response.getBody().getEmail();
  }

  @Test
  @Order(2)
  void getAccount() {
    var headers = createHeader();
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<List<AccountDto>>() {});
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertEquals(1, response.getBody().size());
    var account =
        response.getBody().stream().filter(a -> a.getId().equals(accountId)).findAny().orElse(null);
    Assertions.assertNotNull(account);
    Assertions.assertEquals(accountId, account.getId());
    Assertions.assertEquals(firstName, account.getFirstName());
    Assertions.assertEquals(lastName, account.getLastName());
    Assertions.assertEquals(email, account.getEmail());
  }

  @Test
  @Order(3)
  void patchAccount() {
    var headers = createHeader();
    var request = readRequest("updateAccount", CreateAccountDto.class);
    var response =
        testRestTemplate.exchange(
            INDIVIDUAL_ACCOUNT_URL.replace("{accountId}", accountId.toString()),
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            AccountDto.class);
    Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertEquals(accountId, response.getBody().getId());
    firstName = response.getBody().getFirstName();
    lastName = response.getBody().getLastName();
    email = response.getBody().getEmail();
  }

  @Test
  @Order(4)
  void verifyUpdateAccount() {
    var headers = createHeader();
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<List<AccountDto>>() {});
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    Assertions.assertEquals(1, response.getBody().size());
    var account =
        response.getBody().stream().filter(a -> a.getId().equals(accountId)).findAny().orElse(null);
    Assertions.assertNotNull(account);
    Assertions.assertEquals(accountId, account.getId());
    Assertions.assertEquals(firstName, account.getFirstName());
    Assertions.assertEquals(lastName, account.getLastName());
    Assertions.assertEquals(email, account.getEmail());
  }

  private HttpHeaders createHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
