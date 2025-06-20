package io.temporal.samples.hello;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.samples.order.NotificationActivity;
import io.temporal.samples.order.OrderNotificationWorkflow;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowRule;
import java.time.Duration;
import org.junit.Rule;
import org.junit.Test;

public class OrderNotificationTest {

  public NotificationActivity notificationService =
      mock(NotificationActivity.class, withSettings().withoutAnnotations());

  @Rule
  public TestWorkflowRule testWorkflowRule =
      TestWorkflowRule.newBuilder()
          .setActivityImplementations(notificationService)
          .setWorkflowTypes(OrderNotificationWorkflow.OrderNotificationWorkflowImpl.class)
          .build();

  @Test(timeout = 5000)
  public void sendNotificationAfterTimeoutTest() {
    // Get a workflow stub using the same task queue the worker uses.

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build();
    OrderNotificationWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(OrderNotificationWorkflow.class, workflowOptions);

    // Start workflow asynchronously to not use another thread to query.
    WorkflowClient.start(workflow::scheduleNotification, 1L);

    testWorkflowRule.getTestEnvironment().sleep(Duration.ofMinutes(16));
    verify(notificationService).sendForPickupNotification(1L);
  }

  @Test(timeout = 5000)
  public void sendNotificationAfterSignalInsteadOfTimer() {
    // Get a workflow stub using the same task queue the worker uses.

    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build();
    OrderNotificationWorkflow workflow =
        testWorkflowRule
            .getWorkflowClient()
            .newWorkflowStub(OrderNotificationWorkflow.class, workflowOptions);

    // Start workflow asynchronously to not use another thread to query.
    WorkflowClient.start(workflow::scheduleNotification, 1L);

    TestWorkflowEnvironment testEnvironment = testWorkflowRule.getTestEnvironment();
    testEnvironment.sleep(Duration.ofMinutes(5));

    workflow.readForPick();

    testEnvironment.sleep(Duration.ofMinutes(1));

    verify(notificationService).sendForPickupNotification(1L);
    reset(notificationService);
    testEnvironment.sleep(Duration.ofMinutes(18));

    verify(notificationService, never()).sendForPickupNotification(any());
  }
}
