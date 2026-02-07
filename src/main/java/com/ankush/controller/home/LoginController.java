package com.ankush.controller.home;

import com.ankush.common.CommonData;
import com.ankush.controller.print.PrintBill;
import com.ankush.data.entities.Employee;
import com.ankush.data.entities.Login;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.service.BillService;
import com.ankush.data.service.LoginService;
import com.ankush.data.service.ShopeeInfoService;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlController;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class LoginController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);
    @Autowired @Lazy
    StageManager stageManager;
    @Autowired
    private ShopeeInfoService shopeeInfoService;
    @FXML private AnchorPane mainPane;
    @FXML private ComboBox<String> cmbUserName;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkAddEmployee;
    @FXML private Text txtQuestion;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AlertNotification alert;
    @Autowired
    private CommonData commonData;
    @Autowired BillService billService;
    private Employee employee;
    private ObservableList<String>userNameList= FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        txtQuestion.setVisible(false);
        linkAddEmployee.setVisible(false);
        userNameList.addAll(loginService.getAllUserNames());
        try {
            List<ShopeeInfo> shopeeInfoList = shopeeInfoService.getAllShopeeInfo();
            if (shopeeInfoList.isEmpty()) {
                System.out.println("No shopee info found");
                txtQuestion.setText("First Time?");
                txtQuestion.setVisible(true);
                linkAddEmployee.setText("Register Shop");
                linkAddEmployee.setVisible(true);
                linkAddEmployee.setOnAction(e->{
                    stageManager.switchScene(FxmlView.CREATE);
                });
            }
            else if (userNameList.isEmpty()) {
                System.out.println("No login found");
                txtQuestion.setVisible(true);
                linkAddEmployee.setVisible(true);
                linkAddEmployee.setOnAction(e->{
                    stageManager.switchScene(FxmlView.EMPLOYEE);
                    stageManager.showFullScreen();
                });
            }
            else{
                cmbUserName.setItems(userNameList);
                linkAddEmployee.setOnAction(e->{
                    stageManager.switchScene(FxmlView.EMPLOYEE);
                    stageManager.showFullScreen();
                });
                btnLogin.setOnAction(e->login());
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private void login() {
        try {
            if(cmbUserName.getValue()==null)
            {
                alert.showError("Select Your Name");
                cmbUserName.requestFocus();
                return;
            }
            if(txtPassword.getText().isEmpty())
            {
                alert.showError("Enter Your Password");
                txtPassword.requestFocus();
                return;
            }
            if(loginService.validate(cmbUserName.getValue(),txtPassword.getText())==1)
            {
                alert.showSuccess("Wel come "+cmbUserName.getValue());
                CommonData.setLoginUser(loginService.getLoginByUserName(cmbUserName.getValue()));
                ShopeeInfo shopeeInfo = shopeeInfoService.getShopeeInfoById(1L).orElse(null);
                if(shopeeInfo!=null)CommonData.setShopeeInfo(shopeeInfo);

                stageManager.switchScene(FxmlView.HOME);
                stageManager.showFullScreen();
            }
            else
            {
                alert.showError("Wrong Password!");
            }
        }catch(Exception e)
        {
            alert.showError("Error in Login");
            e.printStackTrace();
        }
    }
}
