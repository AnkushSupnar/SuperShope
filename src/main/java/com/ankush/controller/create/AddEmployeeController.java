package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.service.EmployeeService;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlController;
import com.ankush.data.entities.Employee;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
@Component
public class AddEmployeeController implements FxmlController {
    private static final Logger LOG = LoggerFactory.getLogger(AddUserController.class);
    private final StageManager stageManager;
    @FXML private AnchorPane mainPane;
    @FXML private TextField txtName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnSave;
    @FXML private Button btnUpadate;
    @FXML private Button btnExit;
    @FXML private Hyperlink linkCreateUser;
    @FXML private TableView<Employee> table;
    @FXML private TableColumn<Employee,Integer> colSrno;
    @FXML private TableColumn<Employee,String> colName;
    @FXML private TableColumn<Employee,String> colAddress;
    @FXML private TableColumn<Employee,String> colContact;
    @FXML private TableColumn<Employee,String> colEmail;
    @FXML private TableColumn<Employee,String> colRole;
    @Autowired
    private AlertNotification alert;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SpringFXMLLoader loader;
    private ObservableList<Employee>empList = FXCollections.observableArrayList();
    private Integer id;
    @Autowired @Lazy
    public AddEmployeeController(StageManager stageManager) {
        this.stageManager = stageManager;

    }
    @Override
    public void initialize() {
        id=0;
        colSrno.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        empList.addAll(employeeService.getAllEmployee());
        table.setItems(empList);
        cmbRole.getItems().addAll("Admin","Salesman");
        btnSave.setOnAction(e->save());
        btnUpadate.setOnAction(e->update());
        linkCreateUser.setOnAction(e->stageManager.switchScene(FxmlView.ADDUSER));
        btnExit.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            root.setCenter(loader.getPage("/fxml/create/CreateMenu.fxml"));
        });
    }

    private void update() {
        try{
            if(table.getSelectionModel().getSelectedItem()==null)
            return;
            Employee employee = employeeService.getEmployeeById(table.getSelectionModel().getSelectedItem().getId());
            if(employee!=null)
            {
                System.out.println(employee);
                txtName.setText(employee.getName());
                txtAddress.setText(employee.getAddress());
                txtContact.setText(employee.getContact());
                txtEmail.setText(employee.getEmail());
                cmbRole.setValue(employee.getRole());
                id=employee.getId();
            }
        }catch(Exception e)
        {
            alert.showError("Error in Updating "+e.getMessage());
            e.printStackTrace();
        }

    }

    private void save() {
        try {
            if(!validate())
            {
                return;
            }
            Employee emp= new Employee(
                    txtName.getText(),
                    txtAddress.getText(),
                    txtContact.getText(),
                    txtEmail.getText(),
                    cmbRole.getSelectionModel().getSelectedItem());
            if(id!=0)emp.setId(id);
            System.out.println(emp);
            if(employeeService.saveEmployee(emp)==1)
            {
                alert.showSuccess("Employee Saved Success");
                addInList(emp);
                clear();
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            alert.showError("Error in Saving Record "+e.getMessage());

        }
    }

    private void clear() {
        txtName.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtContact.setText("");
        id=0;
    }

    private void addInList(Employee emp)
    {
        if(empList.isEmpty())
        {
            empList.add(emp);
        }
        else{
            int index=-1;
            for(int i=0;i< empList.size();i++)
            {
                if(empList.get(i).getId()==emp.getId())
                {
                    index=i;
                }
            }
            if(index==-1)
            {
                empList.add(emp);
            }
            else
            {
                empList.remove(index);
                empList.add(index,emp);
            }
        }
    }

    private boolean validate() {
        try {

            if(txtName.getText().isEmpty())
            {
                alert.showError("Enter Employee Full Name");
                txtName.requestFocus();
                return false;
            }
            if(txtAddress.getText().isEmpty())
            {
                alert.showError("Enter Employee Address");
                txtAddress.requestFocus();
                return false;
            }
            if(txtContact.getText().isEmpty())
            {
                alert.showError("Enter Employee Mobile no");
                txtContact.requestFocus();
                return false;
            }
            if(txtEmail.getText().isEmpty())
            {
                txtEmail.setText("-");
            }
            if(cmbRole.getSelectionModel().getSelectedItem()==null)
            {
                alert.showError("Select Employee Role");
                cmbRole.requestFocus();
                return false;
            }
            return true;
        }catch(Exception e)
        {
            alert.showError("Error In Validation");
            return false;
        }
    }
}
