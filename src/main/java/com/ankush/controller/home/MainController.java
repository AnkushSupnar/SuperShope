package com.ankush.controller.home;

import com.ankush.view.FxmlController;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MainController implements FxmlController {

    @FXML
    private Button btnGo;

    private final StageManager stageManager;
    @Autowired @Lazy
    public MainController(StageManager stageManager)
    {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize() {
    btnGo.setOnAction(e->{
        stageManager.switchScene(FxmlView.DASHBOARD);
    });
    }
}
