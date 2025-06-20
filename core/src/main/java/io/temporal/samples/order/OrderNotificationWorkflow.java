package io.temporal.samples.order;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;

@WorkflowInterface
public interface OrderNotificationWorkflow {

  /**
   * Schedule a notification to be ready for pickup in 15 mins or when the merchant notifies it's
   * ready
   *
   * @param orderId you know the id of the thing we want to notify
   */
  @WorkflowMethod
  void scheduleNotification(Long orderId);

  @SignalMethod
  void readForPick();

  class OrderNotificationWorkflowImpl implements OrderNotificationWorkflow {

    boolean readyForPickUp = false;

    /**
     * Define the NotificationActivity stub. Activity stubs are proxies for activity invocations that
     * are executed outside of the workflow thread on the activity worker, that can be on a
     * different host. Temporal is going to dispatch the activity results back to the workflow and
     * unblock the stub as soon as activity is completed on the activity worker.
     *
     * <p>In the {@link ActivityOptions} definition the "setStartToCloseTimeout" option sets the
     * overall timeout that our workflow is willing to wait for activity to complete. For this
     *  example, it is set to 2 seconds.
     */
    private final NotificationActivity notificationActivity =
        Workflow.newActivityStub(
            NotificationActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public void scheduleNotification(Long orderId) {
      // we send the notification on the estimated time or when merchant tell us the thing is ready.
      Workflow.await(Duration.ofMinutes(15), () -> readyForPickUp);

      // send the notification
      notificationActivity.sendForPickupNotification(orderId);
    }

    @Override
    public void readForPick() {
      readyForPickUp = true;
    }
  }
}
