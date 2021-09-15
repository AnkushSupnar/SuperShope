package com.ankush.view;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.Notifications;
import org.springframework.stereotype.Component;

@Component
public class AlertNotification {
    public void showSuccess(String msg)
    {
        try {
            Notifications message = Notifications.create().
                    title("Success Message").
                    text(msg).hideAfter(Duration.seconds(5)).
                    position(Pos.TOP_CENTER);
            message.showInformation();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void showError(String msg)
    {
        try {
            Notifications message = Notifications.create().
                    title("Success Message").
                    text(msg).hideAfter(Duration.seconds(5)).
                    position(Pos.TOP_CENTER);
            message.showError();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
