package com.ankush.controller.create;

import com.ankush.data.entities.Bank;
import com.ankush.data.service.BankService;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
@Component
public class AddBankController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private TextField txtName;
    @FXML private TextField txtIfsc;
    @FXML private TextField txtAccountNo;
    @FXML private TextField txtBalance;
    @FXML private TextField txtWoner;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private Button btnHome;
    @FXML private TableView<Bank> table;
    @FXML private TableColumn<Bank,Integer> colSr;
    @FXML private TableColumn<Bank,String> colName;
    @FXML private TableColumn<Bank,String> colIfsc;
    @FXML private TableColumn<Bank,String> colAccountNo;
    @FXML private TableColumn<Bank,Float> colBalance;
    @FXML private TableColumn<Bank,String> colWoner;
    @Autowired
    BankService service;
    @Autowired
    private AlertNotification alert;
    private Integer id;
    private ObservableList<Bank>list = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    this.id =0;
    colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("bankname"));
    colIfsc.setCellValueFactory(new PropertyValueFactory<>("ifsc"));
    colAccountNo.setCellValueFactory(new PropertyValueFactory<>("accountno"));
    colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
    colWoner.setCellValueFactory(new PropertyValueFactory<>("woner"));
    list.addAll(service.getAllBank());
    table.setItems(list);

        txtAccountNo.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtAccountNo.setText(oldValue);
                }
            }
        });
        txtBalance.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtBalance.setText(oldValue);
                }
            }
        });
        btnSave.setOnAction(e->save());
        btnUpdate.setOnAction(e->update());
        btnClear.setOnAction(e->clear());
    }

    private void clear() {
        txtName.setText("");
        txtIfsc.setText("");
        txtWoner.setText("");
        txtBalance.setText("");
        txtAccountNo.setText("");
        id=0;
    }

    private void update() {
        if(table.getSelectionModel().getSelectedItem()==null)
        {
            return;
        }
        Bank bank = table.getSelectionModel().getSelectedItem();
        id = bank.getId();
        txtName.setText(bank.getBankname());
        txtBalance.setText(""+bank.getBalance());
        txtWoner.setText(bank.getWoner());
        txtAccountNo.setText(bank.getAccountno());
        txtIfsc.setText(bank.getIfsc());
    }

    private void save() {
        try {
            if (!validate()) {
                return;
            }
            Bank bank = new Bank();
            if (id != 0) bank.setId(id);
            bank.setBankname(txtName.getText().trim());
            bank.setBalance(Float.parseFloat(txtBalance.getText().trim()));
            bank.setAccountno(txtAccountNo.getText().trim());
            bank.setIfsc(txtIfsc.getText().trim());
            bank.setWoner(txtWoner.getText().trim());
            int flag = service.saveBank(bank);
            if (flag == 1) {
                alert.showSuccess("Record Save Success");
                addInList(bank);
                clear();
            } else if (flag == 2) {
                alert.showSuccess("Record Update Success");
                addInList(bank);
                clear();
            } else {
                alert.showError("Error in saving Record");
            }
        }catch (Exception e)
        {
            alert.showError("Error in Saving Record "+e.getMessage());
        }

    }
    private void addInList(Bank bank) {
        int index=-1;
        for(int i = 0;i<list.size();i++)
        {
            if(list.get(i).getId()==bank.getId())
            {
                index=i;
                break;
            }
        }
        if(index==-1)
        {
            list.add(bank);
        }
        else{
            list.remove(index);
            list.add(index,bank);
        }
    }

    private boolean validate() {
        try {
            if(txtName.getText().isEmpty())
            {
                alert.showError("Enter Bank Name");
                txtName.requestFocus();
                return false;
            }
            if(txtIfsc.getText().isEmpty())
            {
                txtIfsc.setText("-");
            }
            if(txtAccountNo.getText().isEmpty())
            {
                alert.showError("Enter Bank Account Number");
                txtAccountNo.requestFocus();
                return false;
            }
            if(txtWoner.getText().isEmpty())
            {
                alert.showError("Enter Woner Name");
                txtWoner.requestFocus();
                return false;
            }
            if(txtBalance.getText().isEmpty())
            {
                txtBalance.setText(""+0);
            }
            return true;
        }catch(Exception e)
        {
            alert.showError("Error in Validating "+e.getMessage());
            return false;
        }
    }




  /*  txtRate.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                txtRate.setText(oldValue);
            }
        }
    });

   */
}
