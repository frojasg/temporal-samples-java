package io.temporal.samples.order;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface NotificationActivity {

  @ActivityMethod(name = "sendNotification")
  void sendForPickupNotification(Long orderId);
}
