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
  private final AccountActivity accountActivity =
      Workflow.newActivityStub(
          AccountActivity.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

  @Override
  public Account createAccount(Account details) {
    var account = accountActivity.saveAccount(details);
    account = accountActivity.createPaymentAccount(account);
    return accountActivity.saveAccount(account);
  }
}
