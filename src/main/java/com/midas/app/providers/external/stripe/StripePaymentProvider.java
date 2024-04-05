package com.midas.app.providers.external.stripe;

import com.midas.app.enums.ProviderType;
import com.midas.app.exceptions.ApiException;
import com.midas.app.models.Account;
import com.midas.app.providers.payment.CreateAccount;
import com.midas.app.providers.payment.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class StripePaymentProvider implements PaymentProvider {
  private final Logger logger = LoggerFactory.getLogger(StripePaymentProvider.class);

  private final StripeConfiguration configuration;

  /** providerName is the name of the payment provider */
  @Override
  public String providerName() {
    return "stripe";
  }

  /**
   * createAccount creates a new account in the payment provider.
   *
   * @param details is the details of the account to be created.
   * @return Account
   */
  @Override
  public Account createAccount(CreateAccount details) {
    try {
      // attaching the api key to the stripe sdk
      Stripe.apiKey = configuration.getApiKey();

      // creating customer in stripe
      Customer customer =
          Customer.create(
              CustomerCreateParams.builder()
                  .setEmail(details.getEmail())
                  .setName(String.join(" ", details.getFirstName(), details.getLastName()))
                  .build());

      // returning the user account with providerType and providerId generated through stripe
      return Account.builder()
          .id(UUID.fromString(details.getUserId()))
          .firstName(details.getFirstName())
          .providerId(customer.getId())
          .providerType(ProviderType.getProviderType(providerName()))
          .lastName(details.getLastName())
          .email(details.getEmail())
          .build();
    } catch (StripeException e) {
      // if stripe integration is failed throwing error
      throw new ApiException(HttpStatus.BAD_REQUEST, "failed to create customer");
    }
  }
}
