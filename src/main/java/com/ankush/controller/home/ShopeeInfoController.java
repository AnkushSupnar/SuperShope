package com.ankush.controller.home;

import com.ankush.Main;
import com.ankush.data.entities.Employee;
import com.ankush.data.entities.Login;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.service.EmployeeService;
import com.ankush.data.service.LoginService;
import com.ankush.data.service.ShopeeInfoService;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ShopeeInfoController implements Initializable {
    @Autowired
    private AlertNotification alert;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    private ShopeeInfoService shopeeInfoService;
    @Autowired
    private LoginService loginService;
    @Autowired @Lazy
    StageManager stageManager;
    @FXML
    private Button btnLogin;

    @FXML
    private TextField txtAddress;

    @FXML
    private PasswordField txtAdminPassword;

    @FXML
    private PasswordField txtAdminPassword1;

    @FXML
    private TextField txtAdminUserName;

    @FXML
    private TextField txtContact;

    @FXML
    private TextField txtShopeeName;

    @FXML
    private TextField txtWonerName;
    @FXML
    void createDetails(ActionEvent event) {
        if (areFieldsEmpty()) {
            alert.showError("Please fill in all fields before proceeding.");
            return;
        }
        if(!txtAdminPassword.getText().equals(txtAdminPassword1.getText()))
        {
            alert.showError("Admin Password not matched");
            return;
        }

        ShopeeInfo shopeeInfo = new ShopeeInfo();
        shopeeInfo.setShopeeContact(txtContact.getText());
        shopeeInfo.setShopeeName(txtShopeeName.getText());
        shopeeInfo.setShopeeOwnerName(txtWonerName.getText());

        Employee employee = new Employee();
        employee.setAddress(txtAddress.getText());
        employee.setContact(txtContact.getText());
        employee.setName(txtWonerName.getText());
        employee.setRole("Admin");

        int saveEmp = employeeService.saveEmployee(employee);
        if(saveEmp==1){
            Login login = new Login(
                    txtAdminUserName .getText().trim(),
                    txtAdminPassword .getText().trim(),
                    employeeService.getEmployeeById(1)
            );
           int loginSaved =  loginService.saveLogin(login);
           if(loginSaved==1){
               ShopeeInfo savedInfo =  shopeeInfoService.saveShopeeInfo(shopeeInfo);
               if(savedInfo!=null){
                   alert.showSuccess("Welcome!");
                   stageManager.switchScene(FxmlView.LOGIN);
               }
               else {
                   alert.showError("Shopee Information not saved");
               }


           }
           else{
               alert.showError("Login Not saved");
           }
        }
        else{
            alert.showError("Employee Not saved");
        }



        }

    private boolean areFieldsEmpty() {
        return txtAdminUserName.getText().trim().isEmpty()
                || txtAdminPassword.getText().trim().isEmpty()
                || txtAdminPassword1.getText().trim().isEmpty()
                || txtContact.getText().trim().isEmpty()
                || txtShopeeName.getText().trim().isEmpty()
                || txtWonerName.getText().trim().isEmpty()
                ||txtAddress.getText().trim().isEmpty();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Font.loadFont(Main.class.getResourceAsStream("/fxml/font/kiran.ttf"), 20);
        String kiranStyle = "-fx-font-family: 'Kiran'; -fx-font-size: 20px;";
        txtShopeeName.setStyle(kiranStyle);
        txtWonerName.setStyle(kiranStyle);
        txtAddress.setStyle(kiranStyle);
    }
}
