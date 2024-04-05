package com.midas.app.workflows;

import static com.midas.app.workflows.CreateAccountWorkflow.QUEUE_NAME;

import com.midas.app.activities.AccountActivity;
import com.midas.app.models.Account;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@WorkflowImpl(taskQueues = QUEUE_NAME)
public class CreateAccountWorkflowImpl implements CreateAccountWorkflow {
  /** Class to implement the CreateAccountWorkflow */

  // creating the activity stub to utilize in the workflow
  private final AccountActivity accountActivity =
      Workflow.newActivityStub(
          AccountActivity.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

  /**
   * Workflow Method to execute activity steps to be performed
   *
   * @param details is the details of the account to be created.
   * @return account created of user
   */
  @Override
  public Account createAccount(Account details) {
    // saving the basic information of the user
    var account = accountActivity.saveAccount(details);
    // creating customer for the user in the stripe
    account = accountActivity.createPaymentAccount(account);
    // saving the user with providerType and providerId from stripe
    return accountActivity.saveAccount(account);
  }
}
