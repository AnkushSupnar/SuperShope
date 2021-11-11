package com.ankush.controller.dashboard;

import com.ankush.view.StageManager;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
@Component
public class Dashboard implements Initializable {
    @Autowired
    @Lazy
    StageManager stageManager;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
