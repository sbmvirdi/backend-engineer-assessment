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

  @Override
  public Account saveAccount(Account account) {
    return accountRepository.save(account);
  }

  @Override
  public Account createPaymentAccount(Account account) {
    return stripePaymentProvider.createAccount(
        CreateAccount.builder()
            .userId(account.getId().toString())
            .email(account.getEmail())
            .firstName(account.getFirstName())
            .lastName(account.getLastName())
            .build());
  }
}
