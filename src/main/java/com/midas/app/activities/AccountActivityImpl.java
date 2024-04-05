package com.midas.app.activities;

import static com.midas.app.workflows.CreateAccountWorkflow.QUEUE_NAME;

import com.midas.app.models.Account;
import com.midas.app.providers.external.stripe.StripePaymentProvider;
import com.midas.app.providers.payment.CreateAccount;
import com.midas.app.repositories.AccountRepository;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ActivityImpl(taskQueues = QUEUE_NAME)
public class AccountActivityImpl implements AccountActivity {

  private final AccountRepository accountRepository;
  private final StripePaymentProvider stripePaymentProvider;

  /**
   * Function to save the account in database
   *
   * @param account is the account to be saved
   * @return account saved in database
   */
  @Override
  public Account saveAccount(Account account) {
    // saving the account in account repository
    return accountRepository.save(account);
  }

  /**
   * Function to create payment account in stripe
   *
   * @param account is the account to be created
   * @return created user account
   */
  @Override
  public Account createPaymentAccount(Account account) {
    // calling the stripe provider to create a user in stripe
    return stripePaymentProvider.createAccount(
        CreateAccount.builder()
            .userId(account.getId().toString())
            .email(account.getEmail())
            .firstName(account.getFirstName())
            .lastName(account.getLastName())
            .build());
  }
}
