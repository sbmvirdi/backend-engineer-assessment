package com.midas.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.generated.model.AccountDto;
import com.midas.generated.model.CreateAccountDto;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
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
      PostgresTestContainer.getInstance().withNetwork(network);

  private static UUID accountId;
  private static String firstName;
  private static String lastName;
  private static String email;

  @DynamicPropertySource
  public static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.username", postgreSqlContainer::getUsername);
    registry.add("spring.datasource.password", postgreSqlContainer::getPassword);
    registry.add("spring.datasource.url", postgreSqlContainer::getJdbcUrl);
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

  /**
   * Test Case: To test the Create an account flow of a user which creates user in service and
   * stripe Expected Output: User Created Expected Status Code: 201
   */
  @Test
  @Order(1)
  void createAccount() {
    // creating headers
    var headers = createHeader();

    // populating the request body from json file
    var requestBody = readRequest("createAccount", CreateAccountDto.class);

    // initiating the api call
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.POST,
            new HttpEntity<>(requestBody, headers),
            AccountDto.class);

    // verifying the status of the api call
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    // verifying the data received in the response
    Assertions.assertNotNull(response.getBody());

    // verifying the data payload and response
    accountId = response.getBody().getId();
    firstName = response.getBody().getFirstName();
    lastName = response.getBody().getLastName();
    email = response.getBody().getEmail();
  }

  /**
   * Test Case: To test Get a account of a user which will list the created user from above request
   * Expected Output: User in List Response Expected Status Code: 200
   */
  @Test
  @Order(2)
  void getAccount() {
    // creating headers
    var headers = createHeader();

    // initiating the api call
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<List<AccountDto>>() {});

    // verifying the status of the api call
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    // verifying the data received in the response
    Assertions.assertNotNull(response.getBody());
    Assertions.assertEquals(1, response.getBody().size());
    // fetching the account from the response payload
    var account =
        response.getBody().stream().filter(a -> a.getId().equals(accountId)).findAny().orElse(null);
    Assertions.assertNotNull(account);

    // verifying the data payload and response
    Assertions.assertEquals(accountId, account.getId());
    Assertions.assertEquals(firstName, account.getFirstName());
    Assertions.assertEquals(lastName, account.getLastName());
    Assertions.assertEquals(email, account.getEmail());
  }

  /**
   * Test Case: To patch an account of a user and verify if the details are updated properly
   * Expected Output: User Updated Expected Status Code: 202
   */
  @Test
  @Order(3)
  void patchAccount() {
    // creating headers
    var headers = createHeader();

    // populating the request body from json file
    var request = readRequest("updateAccount", CreateAccountDto.class);

    // initiating the api call
    var response =
        testRestTemplate.exchange(
            INDIVIDUAL_ACCOUNT_URL.replace("{accountId}", accountId.toString()),
            HttpMethod.PATCH,
            new HttpEntity<>(request, headers),
            AccountDto.class);

    // verifying the status of the api call
    Assertions.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    // verifying the data received in the response
    Assertions.assertNotNull(response.getBody());
    // verifying the accountId of the user in response
    Assertions.assertEquals(accountId, response.getBody().getId());

    // verifying the data payload and response
    firstName = response.getBody().getFirstName();
    lastName = response.getBody().getLastName();
    email = response.getBody().getEmail();
  }

  /**
   * Test Case: To test Get a account of a user which will list the created user from above request
   * and verify the information is patched Expected Output: User in List Response Expected Status
   * Code: 200
   */
  @Test
  @Order(4)
  void verifyUpdateAccount() {
    // creating headers
    var headers = createHeader();

    // initiating the api call
    var response =
        testRestTemplate.exchange(
            CREATE_ACCOUNT_URL,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<List<AccountDto>>() {});

    // verifying the status of the api call
    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    // verifying the data received in the response
    Assertions.assertNotNull(response.getBody());
    Assertions.assertEquals(1, response.getBody().size());
    // fetching the account from the response payload
    var account =
        response.getBody().stream().filter(a -> a.getId().equals(accountId)).findAny().orElse(null);
    Assertions.assertNotNull(account);

    // verifying the data payload and response
    Assertions.assertEquals(accountId, account.getId());
    Assertions.assertEquals(firstName, account.getFirstName());
    Assertions.assertEquals(lastName, account.getLastName());
    Assertions.assertEquals(email, account.getEmail());
  }

  /**
   * Function to create headers for requests
   *
   * @return HttpHeaders for requests
   */
  private HttpHeaders createHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
