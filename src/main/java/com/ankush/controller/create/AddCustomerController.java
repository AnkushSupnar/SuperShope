package com.ankush.controller.create;

import com.ankush.common.CommonData;
import com.ankush.data.entities.Customer;
import com.ankush.data.service.CustomerService;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
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
public class AddCustomerController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private TextField txtName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private Button btnHome;
    @FXML private TableView<Customer> table;
    @FXML private TableColumn<Customer,Long> colSrNo;
    @FXML private TableColumn<Customer,String> colName;
    @FXML private TableColumn<Customer,String> colAdddress;
    @FXML private TableColumn<Customer,String> colContact;
    @FXML private TableColumn<Customer,String> colEmail;
    @FXML private Button btnShowAll;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;

    @Autowired
    CustomerService customerService;
    @Autowired
    private AlertNotification alert;
    private ObservableList<Customer>list = FXCollections.observableArrayList();
    private int id;
    private SuggestionProvider<String>customerName;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        id=0;
        CommonData.customerNames.addAll(customerService.getAllCustomerNames());
        customerName = SuggestionProvider.create(CommonData.customerNames);
        new AutoCompletionTextFieldBinding<>(txtSearch,customerName);
        colSrNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAdddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        table.setItems(list);
        txtContact.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,12}?")) {
                    txtContact.setText(oldValue);
                }
            }
        });
        btnSave.setOnAction(e->save());
        btnClear.setOnAction(e->clear());
        btnUpdate.setOnAction(e->update());
        btnShowAll.setOnAction(e->showAll());
        btnSearch.setOnAction(e->search());

    }

    private void search() {
        if(txtSearch.getText().isEmpty())
            txtSearch.requestFocus();
        Customer c =customerService.getCustomerByName(txtSearch.getText());
        if(c==null)
            return;
        list.clear();
        list.add(c);
    }

    private void showAll() {
        list.clear();
        list.addAll(customerService.getAllCustomer());
        table.refresh();

    }

    private void update() {
        if(table.getSelectionModel().getSelectedItem()==null) return;
        Customer customer = table.getSelectionModel().getSelectedItem();
        txtName.setText(customer.getName());
        txtAddress.setText(customer.getAddress());
        txtContact.setText(customer.getContact());
        txtEmail.setText(customer.getEmail());
        id=customer.getId();
    }


    private void save() {
        if(!validate())
        {
            return;
        }


        Customer customer = Customer.builder()
                .address(txtAddress.getText().trim())
                .contact(txtContact.getText())
                .name(txtName.getText().trim())
                .email(txtEmail.getText().trim())
                .build();
      if(id!=0)customer.setId(id);

       int flag=customerService.saveCustomer(customer);
       if(flag==1)
       {
           alert.showSuccess("Customer Save Success");
       }
       else
       {
           alert.showSuccess("Customer Update Success");
       }
        addInList(customer);
        customerName.clearSuggestions();
        CommonData.customerNames.clear();
        CommonData.customerNames.addAll(customerService.getAllCustomerNames());
        customerName.addPossibleSuggestions(CommonData.customerNames);
        clear();
    }

    private void addInList(Customer customer) {
        int index=-1;
        for(Customer cust:list)
        {
            if(cust.getId()==customer.getId())
            {
                index = list.indexOf(cust);
                break;
            }
        }
        if(index==-1)
        {
            list.add(customer);
        }
        else
        {
            list.remove(index);
            list.add(index,customer);
            table.refresh();
        }
        table.refresh();
    }

    private boolean validate() {
        if(txtName.getText().isEmpty())
        {
            alert.showError("Enter Customer Name");
            txtName.requestFocus();
            return false;
        }
        if(id==0 && customerService.getCustomerByName(txtName.getText())!=null)
        {
            alert.showError("Customer Name already exist choose another ");
            txtName.requestFocus();
            return false;
        }
        if(txtAddress.getText().isEmpty())
        {
            alert.showError("Enter Address");
            txtAddress.requestFocus();
            return false;
        }
        if(txtContact.getText().isEmpty())
        {
            alert.showError("Enter Customer Contact Number");
            txtContact.requestFocus();
            return false;
        }
        if(id==0 && customerService.getCustomerByContact(txtContact.getText())!=null)
        {
            alert.showError("This Phone Number Already Exist Choose another one");
            txtContact.requestFocus();
            return false;
        }
        if(txtEmail.getText().isEmpty())
        {
            txtEmail.setText("-");
        }
        return true;
    }

    private void clear() {
        txtName.setText("");
        txtAddress.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        id=0;
    }
}
