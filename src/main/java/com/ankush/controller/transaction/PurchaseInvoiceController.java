package com.ankush.controller.transaction;

import com.ankush.data.entities.*;
import com.ankush.data.service.*;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;

import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


import java.net.URL;

import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class PurchaseInvoiceController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private SplitPane mainPane;
    @FXML private AnchorPane billingPane;
    @FXML private DatePicker date;
    @FXML private TextField txtInvoiceNo;
    @FXML private TextField txtParty;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private TextField txtQty;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private TextField txtRate;
    @FXML private TextField txtAmount;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemove;
    @FXML private Button btnClear;
    @FXML private TableView<PurchaseTransaction> table;
    @FXML private TableColumn<PurchaseTransaction,Long> colSr;
    @FXML private TableColumn<PurchaseTransaction,String> colBarcode;
    @FXML private TableColumn<PurchaseTransaction,String> colItemName;
    @FXML private TableColumn<PurchaseTransaction,Float> colQty;
    @FXML private TableColumn<PurchaseTransaction,String> colUnit;
    @FXML private TableColumn<PurchaseTransaction,Number> colRate;
    @FXML private TableColumn<PurchaseTransaction,Float> colAmount;
    @FXML private TextField txtTransporting;
    @FXML private TextField txtWages;
    @FXML private TextField txtOther;
    @FXML private TextField txtNetTotal;
    @FXML private TextField txtGrandTotal;
    @FXML private TextField txtDiscount;
    @FXML private TextField txtPaid;
    @FXML private TextField txtBank;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate2;
    @FXML private Button btnClear2;
    @FXML private Button btnPrint;
    @FXML private Button btnExit;
    @FXML private TableView<PurchaseInvoice> tableold;
    @FXML private TableColumn<PurchaseInvoice,Long> colSrNo;
    @FXML private TableColumn<PurchaseInvoice, LocalDate> colDate;
    @FXML private TableColumn<PurchaseInvoice,String> colInvoiceNo;
    @FXML private TableColumn<PurchaseInvoice,String> colParty;
    @FXML private TableColumn<PurchaseInvoice,Float> colTotal;
    @FXML private TableColumn<PurchaseInvoice,Float> colPaid;

    @FXML private DatePicker dateSearch;
    @FXML private Button btnShow;
    @FXML private Button btnMonth;
    @FXML private Button btnYear;
    @FXML private Button btnAll;
    @Autowired private AlertNotification alert;
    @Autowired PurchaseInvoiceService invoiceService;
    @Autowired PurchasePartyService partyService;
    @Autowired ItemService itemService;
    @Autowired BankService bankService;
    @Autowired private PurchaseTransactionService invoiceTrService;
    @Autowired private BankTransactionService bankTransactionService;
    @Autowired private ItemStockService itemStockService;
    private Long invoiceid;
    private ListView listView;
    private ObservableList<String> itemNameList = FXCollections.observableArrayList();
    private ObservableList<PurchaseTransaction>trList = FXCollections.observableArrayList();
    private Item item;
    private ObservableList<PurchaseInvoice>oldInvoiceList = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.invoiceid= Long.valueOf(0);
        colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        table.setItems(trList);
        item = null;
        cmbUnit.getItems().addAll("KG","NOS");
        TextFields.bindAutoCompletion(txtParty,partyService.getAllPurchasePartyNames());
        TextFields.bindAutoCompletion(txtBank,bankService.getAllBankNames());
        addItemNameSearch();

        colSrNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("partyinvoiceno"));
        colParty.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getPurchaseParty().getName()));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("grandtotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(LocalDate.now()));
        tableold.setItems(oldInvoiceList);

        txtBarcode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                item=null;
               item = itemService.getItemByBarcode(txtBarcode.getText());
                if(item!=null){setItem();}
                else
                {
                    txtItemName.setText("");
                }
            }
        });
        txtBarcode.setOnMouseClicked(e->listView.setVisible(false));
        txtQty.setOnMouseClicked(e->listView.setVisible(false));
        txtQty.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtQty.setText(oldValue);
                }
            }
        });
        txtQty.setOnKeyReleased(e->{
            listView.setVisible(false);
            if(isNumber(txtQty.getText()) && !txtRate.getText().isEmpty() && isNumber(txtRate.getText()))
            {
                txtAmount.setText(
                        String.valueOf(Float.parseFloat(txtQty.getText())*Float.parseFloat(txtRate.getText()))
                );
                if(e.getCode()==KeyCode.ENTER)
                {
                    txtRate.requestFocus();
                }
            }
            if(!isNumber(txtQty.getText()))
            {
               txtAmount.setText("");
            }

        });
        txtRate.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtRate.setText(oldValue);
                }
            }
        });
        txtRate.setOnKeyReleased(e->{
            if(txtItemName.getText().isEmpty())
            {
                alert.showError("Select Item Name First");
                txtItemName.requestFocus();
                return;
            }
            if(txtQty.getText().isEmpty())
            {
                alert.showError("Enter Quantity First");
                txtQty.requestFocus();
                return;
            }
            if(isNumber(txtQty.getText()) && isNumber(txtRate.getText()))
            {
                txtAmount.setText(
                        String.valueOf(Float.parseFloat(txtQty.getText())*Float.parseFloat(txtRate.getText()))
                );
                if(e.getCode()==KeyCode.ENTER)
                {
                    btnAdd.requestFocus();
                }
            }
        });
        txtTransporting.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtTransporting.setText(oldValue);
                }
            }
        });
        txtTransporting.setOnAction(e->{calculateGrandTotal();txtWages.requestFocus();});
        txtWages.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtWages.setText(oldValue);
                }
            }
        });
        txtWages.setOnAction(e->{calculateGrandTotal();txtOther.requestFocus();});
        txtOther.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtOther.setText(oldValue);
                }
            }
        });
        txtOther.setOnAction(e->{calculateGrandTotal();txtDiscount.requestFocus();});
        txtDiscount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                txtDiscount.setText(oldValue);
            }
        });
        txtDiscount.setOnAction(e->{calculateGrandTotal();txtPaid.requestFocus();});
        btnAdd.setOnAction(e->add());
        btnUpdate.setOnAction(e->update());
        btnClear.setOnAction(e->clear());
        btnRemove.setOnAction(e->remove());
        btnSave.setOnAction(e->save());
        btnUpdate2.setOnAction(e->update2());
        btnClear2.setOnAction(e->clear2());
    }

    private void clear2() {
        date.setValue(LocalDate.now());
        txtParty.setText("");
        txtInvoiceNo.setText("");
        clear();
        trList.clear();
        txtTransporting.setText(""+0.0);
        txtWages.setText(""+0.0);
        txtOther.setText(""+0.0);
        txtNetTotal.setText(""+0.0);
        txtGrandTotal.setText(""+0.0);
        txtDiscount.setText(""+0.0);
        txtPaid.setText(""+0.0);
        invoiceid= Long.valueOf(0);

    }

    private void update2() {
        try {
            if(tableold.getSelectionModel().getSelectedItem()==null){return;}
            PurchaseInvoice in = invoiceService.getInvoiceById(tableold.getSelectionModel().getSelectedItem().getId());

            if(checkItemStock(in.getTransactions())!=1)
            {
                alert.showError("Yuo can not update this bill Items already Sold from Stock");
                return;
            }
            System.out.println("loading data");
            trList.clear();
            trList.addAll(in.getTransactions());
            manageTransactionList();
            date.setValue(in.getDate());
            txtInvoiceNo.setText(in.getPartyinvoiceno());
            txtParty.setText(in.getPurchaseParty().getName());
            txtTransporting.setText(String.valueOf(in.getTransporting()));
            txtWages.setText(String.valueOf(in.getWages()));
            txtOther.setText(String.valueOf(in.getWages()));
            txtNetTotal.setText(String.valueOf(in.getNettotal()));
            txtGrandTotal.setText(String.valueOf(in.getGrandtotal()));
            txtDiscount.setText(String.valueOf(in.getDiscount()));
            txtPaid.setText(String.valueOf(in.getPaid()));
            txtBank.setText(in.getBank().getBankname());
            calculateGrandTotal();
            invoiceid=in.getId();
        }catch(Exception e)
        {
            alert.showError("Error in Updating Invoice");
            e.printStackTrace();
        }
    }

    private void save() {
        if(!validateData()) return;
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPartyinvoiceno(txtInvoiceNo.getText());
        invoice.setPurchaseParty(partyService.getPartyByName(txtParty.getText()));
        invoice.setBank(bankService.getBankByBankname(txtBank.getText()));
        invoice.setDate(date.getValue());
        invoice.setDiscount(Float.parseFloat(txtDiscount.getText()));
        invoice.setGrandtotal(Float.parseFloat(txtGrandTotal.getText()));
        invoice.setNettotal(Float.parseFloat(txtNetTotal.getText()));
        invoice.setOther(Float.parseFloat(txtOther.getText()));
        invoice.setTransporting(Float.parseFloat(txtTransporting.getText()));
        invoice.setPaid(Float.parseFloat(txtPaid.getText()));
        invoice.setWages(Float.parseFloat(txtWages.getText()));
        for(int i=0;i<trList.size();i++)
        {
            trList.get(i).setInvoice(invoice);
            trList.get(i).setId(null);
        }
        invoice.setTransactions(trList);
       // List<PurchaseTransaction> oldtr = null;
        PurchaseInvoice oldInvoice = null;
        float oldPaid=0.0f;
        if(invoiceid!=0)
        {
            invoice.setId(invoiceid);
            //getting old Transaction for updating InvoiceBill
           // oldtr =  invoiceService.getInvoiceById(invoiceid).getTransactions();
            oldInvoice = invoiceService.getInvoiceById(invoiceid);
            oldPaid=oldInvoice.getPaid();
            //System.out.println("Old invoice Paid"+ oldInvoice.getPaid());
        }
        if(bankService.getBalanceById(invoice.getBank().getId())<invoice.getPaid())
        {
            alert.showError("Not sufficient Balance in Bank choose another ");
            txtBank.requestFocus();
            return;
        }
        int flag=0;
        flag= invoiceService.savePurchaseInvoice(invoice);
        if(flag==1)
        {
            invoiceTrService.saveTransactions(invoice.getTransactions());

            addInStock(invoice.getTransactions());
            addInItemMaster(invoice.getTransactions());
            if(invoice.getPaid()>0 && bankService.reduceBankBalance(invoice.getPaid(),invoice.getBank().getId())==1)
            {
                BankTransaction bankTransaction = new BankTransaction();
                bankTransaction.setBank(invoice.getBank());
                bankTransaction.setTransactionid(invoice.getId());
                bankTransaction.setDate(invoice.getDate());
                bankTransaction.setDebit(invoice.getPaid());
                bankTransaction.setCredit(0.0f);
                bankTransaction.setParticulars("Reduce Purchase Invoice "+invoice.getId());
                bankTransactionService.saveBankTransaction(bankTransaction);
            }


            oldInvoiceList.clear();
            oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(LocalDate.now()));
            tableold.refresh();

            trList.clear();
            tableold.refresh();
            alert.showSuccess("Invoice Saved Success "+invoice.getId());
        }
        else if(flag==2)
        {
            invoiceTrService.deleteTransactions(oldInvoice.getTransactions());//delete old transactions
            reduceStock(oldInvoice.getTransactions());//reduceing old Stock
            if(oldInvoice.getPaid()>0 && bankService.addBankBalance(oldInvoice.getPaid(),oldInvoice.getBank().getId())==1)
            {
                BankTransaction bankTransaction= new BankTransaction();
                bankTransaction.setBank(oldInvoice.getBank());
                bankTransaction.setTransactionid(oldInvoice.getId());
                bankTransaction.setDate(LocalDate.now());
                System.out.println("To credited "+oldInvoice.getPaid());
                bankTransaction.setCredit(oldPaid);
                bankTransaction.setDebit(0.0f);
                bankTransaction.setParticulars("Add Purchase Invoice Edit "+oldInvoice.getId());
                bankTransactionService.saveBankTransaction(bankTransaction);

            }
            if(invoice.getPaid()>0 && bankService.reduceBankBalance(invoice.getPaid(),invoice.getBank().getId())==1)
            {
                BankTransaction bankTransaction = new BankTransaction();
                bankTransaction.setBank(invoice.getBank());
                bankTransaction.setTransactionid(invoice.getId());
                bankTransaction.setDate(invoice.getDate());
                bankTransaction.setDebit(invoice.getPaid());
                bankTransaction.setCredit(0.0f);
                bankTransaction.setParticulars("Reduce Purchase Invoice "+invoice.getId());
                bankTransactionService.saveBankTransaction(bankTransaction);
            }
            invoiceTrService.saveTransactions(invoice.getTransactions());
            addInStock(invoice.getTransactions());
            addInItemMaster(invoice.getTransactions());
            clear2();
            oldInvoiceList.clear();
            oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(LocalDate.now()));
            tableold.refresh();
            alert.showSuccess("Invoice Update Success");

        }

        //save Item in Item Stock And Item
    }

    private void reduceStock(List<PurchaseTransaction> oldtr) {
        ItemStock stock=null;
        for(PurchaseTransaction tr:oldtr)
        {
            stock = new ItemStock();
            stock.setBarcode(tr.getBarcode());
            stock.setStock((tr.getQty()*-1));
            stock.setItemname(tr.getItemname());
            System.out.println("For Reducing stock ="+stock);
            itemStockService.saveItemStock(stock);

        }
    }

    private int checkItemStock(List<PurchaseTransaction> transactions) {
        try {
            System.out.println("checking stock");
            int flag=1;
           for(PurchaseTransaction tr:transactions)
           {
               if(itemStockService.getItemStockByNameAndBarcode(tr.getItemname(),tr.getBarcode()).getStock()<tr.getQty())
               {
                   flag=0;
               }
           }
           return flag;
        }catch(Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    private void addInItemMaster(List<PurchaseTransaction> list){
        try {
            Item item=null;
            for(PurchaseTransaction tr:list)
            {
                item = itemService.getItemByItemnameAndBarcode(tr.getItemname(),tr.getBarcode());
                if(item==null)
                {
                    item = new Item();
                    item.setItemname(tr.getItemname());
                    item.setUnit(tr.getUnit());
                    item.setPrice(tr.getPrice());
                    item.setBarcode(tr.getBarcode());
                    item.setRate(0.0f);
                    itemService.saveItem(item);
                }
                else
                {
                    item.setPrice(tr.getPrice());
                    itemService.saveItem(item);
                }

            }
        }catch (Exception e)
        {
            alert.showError("Error in Addin In ItemMaster");
            e.printStackTrace();
        }
    }
    private void addInStock(List<PurchaseTransaction> list)
    {
        try {
            ItemStock stock=null;
                for(PurchaseTransaction tr:list)
                {
                     stock = new ItemStock();
                    stock.setStock(tr.getQty());
                    stock.setItemname(tr.getItemname());
                    stock.setBarcode(tr.getBarcode());
                    itemStockService.saveItemStock(stock);

                }
        }catch(Exception e)
        {
            alert.showError("Error in Adding Stock");
            e.printStackTrace();
        }
    }
    private boolean validateData() {
        if(txtParty.getText().isEmpty())
        {
            alert.showError("Select Purchase Party Name");
            txtParty.requestFocus();
            return false;
        }
        if(partyService.getPartyByName(txtParty.getText())==null)
        {
            alert.showError("Party Not Found");
            txtParty.requestFocus();
            return false;
        }
        if(txtInvoiceNo.getText().isEmpty())
        {
            alert.showError("Enter Invoice Number");
            txtInvoiceNo.requestFocus();
            return false;
        }
        if(date.getValue()==null)
        {
            alert.showError("Select Invoice Date");
            date.requestFocus();
            return false;
        }
        if(trList.isEmpty())
        {
            alert.showError("No Data to Save");
            txtBarcode.requestFocus();
            return false;
        }
        if(txtBank.getText().isEmpty())
        {
            alert.showError("Select Bank Name");
            txtBank.requestFocus();
            return false;
        }
        if(bankService.getBankByBankname(txtBank.getText())==null)
        {
            alert.showError("Bank Not Found");
            txtBank.requestFocus();
            return false;
        }
        return true;
    }

    private void remove() {
        if(table.getSelectionModel().getSelectedItem()==null)
            return;
        PurchaseTransaction tr = table.getSelectionModel().getSelectedItem();
        txtNetTotal.setText(
                String.valueOf(Float.parseFloat(txtNetTotal.getText())-tr.getAmount())
        );
        trList.remove(table.getSelectionModel().getSelectedIndex());
        table.getSelectionModel().clearSelection();
        calculateGrandTotal();
        Long sr= Long.valueOf(0);
        for(int i=0;i<trList.size();i++)
        {
            trList.get(i).setId(++sr);
        }
    }
    private void clear() {
        txtBarcode.setText("");
        txtItemName.setText("");
        txtQty.setText("");
        txtRate.setText("");
        txtAmount.setText("");
        cmbUnit.getSelectionModel().clearSelection();
        item=null;
        txtQty.requestFocus();
    }
    private void calculateGrandTotal()
    {
        if(txtTransporting.getText().isEmpty()) txtTransporting.setText(""+0.0);
        if(txtWages.getText().isEmpty()) txtWages.setText(""+0.0);
        if(txtOther.getText().isEmpty()) txtOther.setText(""+0.0);
        if(txtPaid.getText().isEmpty()) txtPaid.setText(""+0.0);
        if(txtDiscount.getText().isEmpty()) txtDiscount.setText(""+0.0);
        txtGrandTotal.setText(
                String.valueOf((Float.parseFloat(txtNetTotal.getText())-Float.parseFloat(txtDiscount.getText()))+
                        Float.parseFloat(txtTransporting.getText())+
                        Float.parseFloat(txtWages.getText())+
                        Float.parseFloat(txtOther.getText()))
        );

    }
    private void update() {
        if(table.getSelectionModel().getSelectedItem()==null)
        {
            return;
        }
        PurchaseTransaction tr = table.getSelectionModel().getSelectedItem();
        if(tr==null) return;
        else{
            txtItemName.setText(tr.getItemname());
            txtBarcode.setText(tr.getBarcode());
            txtQty.setText(String.valueOf(tr.getQty()));
            txtRate.setText(String.valueOf(tr.getPrice()));
            txtAmount.setText(String.valueOf(tr.getAmount()));
            if(tr.getUnit().equals("ik.ga`^."))
                cmbUnit.getSelectionModel().select(0);
            else
                cmbUnit.getSelectionModel().select(1);
        }
    }
    private void add() {
       if(!validateItem())
       {
           return;
       }
        PurchaseTransaction tr = createTransaction();
        addInTrList(tr);
    }
    private void addInTrList(PurchaseTransaction tr) {
        int index=-1;
        for(int i=0;i<trList.size();i++)
        {
            if(trList.get(i).getItemname().trim().equals(tr.getItemname()) &&
                    trList.get(i).getBarcode().equals(tr.getBarcode())){
                System.out.println("Found Same Item");
                index=i;
                break;
            }
        }
        if(index==-1)
        {
            tr.setId((long) (trList.size()+1));
            trList.add(tr);
            txtNetTotal.setText(String.valueOf(Float.parseFloat(txtNetTotal.getText())+tr.getAmount()));
            calculateGrandTotal();
            clear();
        }
        else
        {
            txtNetTotal.setText(
                    String.valueOf(Float.parseFloat(txtNetTotal.getText())-
                            trList.get(index).getAmount())
            );
            trList.get(index).setQty(trList.get(index).getQty()+tr.getQty());
            trList.get(index).setAmount(trList.get(index).getAmount()+tr.getAmount());
            table.refresh();
            calculateGrandTotal();
           // trList.remove(index);
           // tr.setId((long) (index+1));
            //trList.add(index,tr);
            txtNetTotal.setText(
                    String.valueOf(Float.parseFloat(txtNetTotal.getText())+
                            tr.getAmount())
            );
            clear();

        }
    }
    private boolean validateItem() {
        if(txtItemName.getText().isEmpty())
        {
            alert.showError("Enter Item Name");
            txtItemName.requestFocus();
            return false;
        }
        if(txtQty.getText().isEmpty())
        {
            alert.showError("Enter Quantity");
            txtQty.requestFocus();
            return false;
        }
        if(txtAmount.getText().isEmpty())
        {
            alert.showError("Enter Item Again");
            txtItemName.requestFocus();
            return false;
        }
        if(cmbUnit.getSelectionModel().getSelectedItem()==null)
        {
            alert.showError("Select Item Unit");
            cmbUnit.requestFocus();
            return false;

        }
        Item item = itemService.getItemByBarcode(txtBarcode.getText());
        if(item!=null)
        {
            if(!item.getItemname().equals(txtItemName.getText().trim()))
            {
                alert.showError("This Barcode is already registered with different Item Name Please Change Item Name");
                txtItemName.requestFocus();
                return false;
            }
            else
                return true;
        }
        item=null;
        item=itemService.getItemByName(txtItemName.getText().trim());
        if(item!=null)
        {
            if(!item.getBarcode().equals(txtBarcode.getText().trim()))
            {
                alert.showError("This Item name is already register with different barcode Please Select another Item Name");
                txtItemName.requestFocus();
                return false;
            }
            else return true;
        }
        return true;
    }
    void setItem(){
        if(item!=null)
        {
            txtBarcode.setText(item.getBarcode());
            txtItemName.setText(item.getItemname());
            txtRate.setText(String.valueOf(item.getPrice()));
            if(item.getUnit().equals("ik.ga`^."))
               cmbUnit.getSelectionModel().select(0);
            else
                cmbUnit.getSelectionModel().select(1);
        }
    }
    private PurchaseTransaction createTransaction()
    {
        PurchaseTransaction tr = new PurchaseTransaction();
        tr.setItemname(txtItemName.getText());
        tr.setBarcode(txtBarcode.getText());
        tr.setPrice(Float.valueOf(txtRate.getText()));
        tr.setQty(Float.parseFloat(txtQty.getText()));
        tr.setAmount(tr.getQty()*Float.parseFloat(txtRate.getText()));

        if(cmbUnit.getSelectionModel().getSelectedIndex()==0)
           tr.setUnit("ik.ga`^.");
        else
           tr.setUnit("naga");
        return tr;
    }
    private void manageTransactionList()
    {
        Long sr= Long.valueOf(0);
        for(int i=0;i<trList.size();i++)
        {
            trList.get(i).setId(++sr);
        }
    }
    void addItemNameSearch()
    {
        itemNameList.addAll(itemService.getAllItemNames());
        listView = new ListView();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(140);
        listView.setLayoutY(142);
        billingPane.getChildren().addAll(listView);
        listView.setVisible(false);
        txtItemName.setOnKeyReleased(e->{
            findItem(txtItemName.getText());
            if(listView.getItems().size()>0)
            {
                listView.getSelectionModel().select(0);
                listView.setVisible(true);
            }
            if(e.getCode()==KeyCode.ENTER){
                if(listView.getItems().size()>0)
                {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
                if(txtItemName.getText().equals(listView.getSelectionModel().getSelectedItem()))
                {
                    if(txtBarcode.getText().isEmpty())
                    {
                        item = itemService.getItemByName(txtItemName.getText());
                    }
                    listView.setVisible(false);
                   setItem();
                    txtQty.requestFocus();

                }
            }
            if(e.getCode()==KeyCode.DOWN)
            {
                if(listView.getItems().size()>0)
                {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
            }

        });
        txtItemName.setOnMouseClicked(e->{
            findItem(txtItemName.getText());
            listView.setVisible(true);
        });
        listView.setOnKeyReleased(e->{
            String item = String.valueOf(listView.getSelectionModel().getSelectedItems());
            if(e.getCode()== KeyCode.ENTER)
            {
                txtItemName.setText(item.substring(1,item.length()-1));
                listView.setVisible(false);
                txtItemName.requestFocus();
            }
        });
        listView.setOnMouseClicked(e->{
            if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2)
            {
                String itemName = String.valueOf(listView.getSelectionModel().getSelectedItems());
                txtItemName.setText(itemName.substring(1,itemName.length()-1));
                //item = itemService.getItemByName(txtItemName.getText());
               // System.out.println(item);
               // setItem();
                listView.setVisible(false);
            }
        });
    }
    void findItem(String find) {
        //cmodel.removeAllElements();
        listView.getItems().clear();
        if(find.equals("")|| find.equals(" "))
        {
            listView.getItems().clear();
            listView.getItems().addAll(itemNameList);
            return;
        }
        try {
            for (int i = 0; i < itemNameList.size(); i++) {
                if (itemNameList.get(i).startsWith(find)) {
                    listView.getItems().add(itemNameList.get(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in findItem " + e.getMessage());
            return;
        }
    }
    private boolean isNumber(String num) {
        if (num == null) {
            return false;
        }
        try {
            Float.parseFloat(num);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
