package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.entities.PurchaseParty;
import com.ankush.data.service.PurchasePartyService;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class AddPurchasePartyController implements Initializable {
    @Autowired @Lazy
    private StageManager stageManager;
    @FXML private AnchorPane pane;
    @FXML private AnchorPane mainPane;
    @FXML private TextField txtPartName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtGst;
    @FXML private TextField txtPan;
    @FXML private Button btnSave;
    @FXML private Button btnClear;
    @FXML private Button btnUpdate;
    @FXML private Button btnExit;
    @FXML private TableView<PurchaseParty> table;
    @FXML private TableColumn<PurchaseParty,Integer> colSrno;
    @FXML private TableColumn<PurchaseParty,String> colName;
    @FXML private TableColumn<PurchaseParty,String> colAddress;
    @FXML private TableColumn<PurchaseParty,String> colGst;
    @FXML private TableColumn<PurchaseParty,String> colPan;
    @Autowired
    private AlertNotification alert;
    @Autowired
    private PurchasePartyService service;
    @Autowired
    private SpringFXMLLoader loader;
    private ObservableList<PurchaseParty>list = FXCollections.observableArrayList();
    Integer id;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.id=0;
        colSrno.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colGst.setCellValueFactory(new PropertyValueFactory<>("gst"));
        colPan.setCellValueFactory(new PropertyValueFactory<>("pan"));
        list.addAll(service.getAllPurchaseParty());
        table.setItems(list);

        btnSave.setOnAction(e->save());
        btnUpdate.setOnAction(e->update());
        btnClear.setOnAction(e->clear());
        btnExit.setOnAction(e -> {
            BorderPane root = (BorderPane) pane.getParent();
            root.setCenter(loader.getPage("/fxml/create/CreateMenu.fxml"));
        });
    }

    private void clear() {
        txtPartName.setText("");
        txtAddress.setText("");
        txtPan.setText("");
        txtGst.setText("");
        id=0;
    }

    private void update() {
        try {
            if(table.getSelectionModel().getSelectedItem()==null)
            {
                return;
            }
            PurchaseParty party = table.getSelectionModel().getSelectedItem();
            if(party!=null)
            {
                txtPartName.setText(party.getName());
                txtPan.setText(party.getPan());
                txtAddress.setText(party.getAddress());
                txtGst.setText(party.getGst());
                id=party.getId();
            }
        }catch(Exception e)
        {
            alert.showError("Error in Updating Party "+e.getMessage());
        }
    }

    private void save()
    {
        try{
            if(!validate())
            {
                return;
            }
            PurchaseParty party = new PurchaseParty();
            if(id!=0) party.setId(id);
            party.setAddress(txtAddress.getText().trim());
            party.setGst(txtGst.getText().trim());
            party.setPan(txtPan.getText().trim());
            party.setName(txtPartName.getText().trim());

            if(service.saveParty(party)==1)
            {
                alert.showSuccess("Party Save Success");
            }
            else
            {
                alert.showSuccess("Party Update Success");
            }
            addInList(party);
            clear();

        }catch(Exception e)
        {
            alert.showError("Error in Saving Record "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void addInList(PurchaseParty party) {
        try{
            int index=-1;
            for(int i=0;i<list.size();i++)
            {
                if(list.get(i).getId()==party.getId())
                {
                    index=i;
                    break;
                }
            }
            if(index==-1)
            {
                list.add(party);
            }
            else
            {
                list.remove(index);
                list.add(index,party);
            }
        }catch(Exception e)
        {
            alert.showError("Error in Adding List "+e.getMessage());
        }
    }

    private boolean validate() {
        if(txtPartName.getText().isEmpty())
        {
            alert.showError("Enter Party Name");
            txtPartName.requestFocus();
            return false;
        }
        if(txtAddress.getText().isEmpty())
        {
            alert.showError("Enter Party Address");
            txtAddress.requestFocus();
            return false;
        }
        if(txtGst.getText().isEmpty())
        {
            txtGst.setText("-");
        }
        if(txtPan.getText().isEmpty())
        {
            txtPan.setText("-");
        }
        return true;
    }
}
