package com.ankush.controller.create;

import com.ankush.data.entities.Login;
import com.ankush.data.service.EmployeeService;
import com.ankush.data.service.LoginService;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlController;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddUserController implements FxmlController {
    private static final Logger LOG = LoggerFactory.getLogger(AddUserController.class);
    private final StageManager stageManager;

    @FXML private AnchorPane mainPane;
    @FXML private ComboBox<String> cmbEmployee;
    @FXML private TextField txtUserName;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtRepassword;
    @FXML private Button btnSave;
    @FXML private Button btnClear;
    @FXML private Button btnExit;

    @Autowired
    private EmployeeService empService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AlertNotification alert;
    private List<String>empNameList = new ArrayList<>();
    @Autowired @Lazy
    public AddUserController(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize() {
        empNameList.addAll(empService.getAllEmployeeNames());
        cmbEmployee.getItems().addAll(empNameList);
        btnSave.setOnAction(e->save());
        btnClear.setOnAction(e->clear());
    }

    private void save() {
        try {
            if(!validate())
            {
                return;
            }
            Login login = new Login(
                    txtUserName.getText().trim(),
                    txtPassword.getText().trim(),
                    empService.getByName(cmbEmployee.getValue())
            );
            if(loginService.saveLogin(login)==1)
            {
                alert.showSuccess("Login Saved Success");
                clear();
            }
        }catch(Exception e)
        {
            alert.showError("Error in Saving Record "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void clear() {
        cmbEmployee.getSelectionModel().clearSelection();
        txtUserName.setText("");
        txtPassword.setText("");
        txtRepassword.setText("");
    }

    private boolean validate() {
        if(cmbEmployee.getSelectionModel().getSelectedItem()==null)
        {
            alert.showError("Select Employee Name");
            cmbEmployee.requestFocus();
            return false;
        }
        if(txtUserName.getText().isEmpty())
        {
            alert.showError("Enter User Name for Employee Login");
            txtUserName.requestFocus();
            return false;
        }
        if(txtPassword.getText().isEmpty())
        {
            alert.showError("Enter Password");
            txtPassword.requestFocus();
            return false;
        }
        if(txtRepassword.getText().isEmpty())
        {
            alert.showError("Enter ReEnter Password");
            txtRepassword.requestFocus();
            return false;
        }
        if(!txtPassword.getText().equals(txtRepassword.getText()))
        {
            alert.showError("Both Password Must Be match");
            txtRepassword.requestFocus();
            return false;
        }
        return true;
    }
}
