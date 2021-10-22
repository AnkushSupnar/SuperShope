package com.ankush.controller.transaction;

import com.ankush.common.CommonData;
import com.ankush.controller.print.PrintBill;
import com.ankush.data.entities.*;
import com.ankush.data.service.*;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.beans.binding.Bindings;
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
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class BillingController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private AnchorPane leftPane;
    @FXML private AnchorPane rootPane;

    @FXML private DatePicker date;
    @FXML private TextField txtCustomerName;
    @FXML private TextField txtCustomerMobile;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private TextField txtQty;
    @FXML private TextField txtRate;
    @FXML private TextField txtAmount;
    @FXML private Button btnAdd;
    @FXML private Button btnRemove;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction,Long> colSrNo;
    @FXML private TableColumn<Transaction,String> colBarcode;
    @FXML private TableColumn<Transaction,String> colItemName;
    @FXML private TableColumn<Transaction,String> colUnit;
    @FXML private TableColumn<Transaction,Number> colQty;
    @FXML private TableColumn<Transaction,Number> colRate;
    @FXML private TableColumn<Transaction,Number> colMrp;
    @FXML private TableColumn<Transaction,Number> colAmount;
    @FXML private Button btnSave;
    @FXML private Button btnPrint;
    @FXML private Button btnHome;
    @FXML private Button btnUpdate2;
    @FXML private Button btnClear2;
    @FXML private TextField txtNetTotal;
    @FXML private TextField txtOther;
    @FXML private TextField txtGrandTotal;
    @FXML private TextField txtMrp;
    @FXML private TextField txtDiscount;
    @FXML private TextField txtPayble;
    @FXML private TextField txtRecived;
    @FXML private TextField txtRemaining;
    @FXML private TextField txtBank;
    private ListView<String> listItem;

    @FXML private TableView<Bill> tableOld;
    @FXML private TableColumn<Bill,Long> colBillNo;
    @FXML private TableColumn<Bill,LocalDate> colDate;
    @FXML private TableColumn<Bill,Float> colBillAmt;
    @FXML private TableColumn<Bill,Float> colPaid;
    @FXML private TableColumn<Bill,String> colCustomer;
    @FXML private TableColumn<Bill,String> colLogin;

    @FXML private DatePicker dateSearch;
    @FXML private Button btnWeek;
    @FXML private Button btnMonth;
    @FXML private Button btnAll;


    //for itemName search box
    private ListView listView;
    private ObservableList<String> itemNameSearch = FXCollections.observableArrayList();
    @Autowired ItemService itemService;
    private  Item item;
    ///////////////////
    @Autowired private ItemStockService stockService;
    @Autowired private AlertNotification alert;
    @Autowired private BankService bankService;
    @Autowired private CustomerService customerService;
    @Autowired private BillService billService;
    @Autowired private LoginService loginService;
    @Autowired private PrintBill printbill;
    @Autowired BankTransactionService bankTrService;

    private ObservableList<String>itemNameList = FXCollections.observableArrayList();
    private ObservableList<Transaction>trList = FXCollections.observableArrayList();
    private SuggestionProvider<String>customerNames;
    private ObservableList<Bill>billList =FXCollections.observableArrayList();
    private Long billno;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        createTable();
        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billno"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colBillAmt.setCellValueFactory(new PropertyValueFactory<>("grandtotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colCustomer.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getCustomer().getName()));
        colLogin.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getLogin().getUsername()));
        billList.addAll(billService.getBillByDate(LocalDate.now()));
        tableOld.setItems(billList);

        date.setValue(LocalDate.now());
        dateSearch.setValue(LocalDate.now());
        txtBank.setText(bankService.getBankById(1).getBankname());
        CommonData.customerNames.addAll(customerService.getAllCustomerNames());
        customerNames = SuggestionProvider.create(CommonData.customerNames);
        new AutoCompletionTextFieldBinding<>(txtCustomerName,customerNames);
        billno= null;
        cmbUnit.getItems().addAll("KG","NOS");
        TextFields.bindAutoCompletion(txtBank,bankService.getAllBankNames());
        createItemList();
        addItemNameSearch();
        txtBarcode.setOnAction(e->barcodeAction());
        txtBarcode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                item=null;
                if(!txtBarcode.getText().equals("")) {
                    item = itemService.getItemByBarcode(txtBarcode.getText());
                    if (item != null) {
                        setItem(item);
                    } else {
                        txtItemName.setText("");
                        txtRate.setText("");
                        txtAmount.setText("");
                    }
                }
            }
        });
        txtQty.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtQty.setText(oldValue);
                }
                else
                {
                    if(!txtRate.getText().isEmpty() ||
                            !txtRate.getText().equals(""+0.0f)||
                            !txtQty.getText().isEmpty()
                            )
                    {
                        if(isNumeric(txtQty.getText()))
                        txtAmount.setText(
                                String.valueOf(Float.parseFloat(txtRate.getText())*Float.parseFloat(txtQty.getText()))
                        );
                    }
                    if(txtQty.getText().isEmpty())
                       // 9075747714
                    {
                        txtAmount.setText(""+0.0f);
                    }
                }
            }
        });
        txtRate.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtRate.setText(oldValue);
                }
                else
                {
                    if(!txtRate.getText().isEmpty() ||
                            !txtRate.getText().equals(""+0.0f)||
                            !txtQty.getText().isEmpty()
                    )
                    {
                        if(isNumeric(txtQty.getText()))
                            txtAmount.setText(
                                    String.valueOf(Float.parseFloat(txtRate.getText())*Float.parseFloat(txtQty.getText()))
                            );
                    }
                    if(txtQty.getText().isEmpty())
                    // 9075747714
                    {
                        txtAmount.setText(""+0.0f);
                    }
                }
            }
        });
        txtOther.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtOther.setText(oldValue);
                }
                else
                {
                    if(!txtOther.getText().isEmpty() ||
                            !txtOther.getText().equals(""+0.0f)||
                            !txtOther.getText().isEmpty()&&
                                    isNumeric(txtOther.getText()))
                    {
                        if(isNumeric(txtOther.getText()))
                            calculateGrandTotal();
                    }
                }
            }
        });
        txtRecived.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtRecived.setText(oldValue);
                }
                else
                {
                    if(!txtRecived.getText().isEmpty() ||
                            !txtRecived.getText().equals(""+0.0f)||
                            !txtRecived.getText().isEmpty()&&
                                    isNumeric(txtRecived.getText()))
                    {
                        if(isNumeric(txtRecived.getText()))
                            txtRemaining.setText(
                                    String.valueOf(Float.parseFloat(txtRecived.getText())-Float.parseFloat(txtGrandTotal.getText()))
                            );
                    }
                }
            }
        });
        txtRate.setOnAction(e->{
            if(!txtRate.getText().isBlank() ||
                    !txtRate.getText().isBlank()||
                    !txtRate.getText().equals("") &&
                            isNumeric(txtRate.getText())&&
                            Float.parseFloat(txtRate.getText())>0.0f)
            {
                btnAdd.requestFocus();
            }
        });
        txtQty.setOnAction(e->{
            if(isNumeric(txtQty.getText())&& isNumeric(txtRate.getText()))
            {
                txtAmount.setText(
                        String.valueOf(Float.parseFloat(txtRate.getText())*Float.parseFloat(txtQty.getText()))
                );
                txtRate.requestFocus();
            }
        });
        btnAdd.setOnAction(e->add());
        btnRemove.setOnAction(e->remove());
        btnClear.setOnAction(e->clear());
        btnUpdate.setOnAction(e->update());
        dateSearch.setOnAction(e->{
            if(date.getValue()!=null)
            {
                billList.clear();
                billList.addAll(billService.getBillByDate(dateSearch.getValue()));
            }
        });
        btnWeek.setOnAction(e->{
            if(dateSearch.getValue()!=null) {
                billList.clear();
                billList.addAll(billService.findByDateBetween(dateSearch.getValue().with(previousOrSame(MONDAY)), date.getValue().with(nextOrSame(SUNDAY))));
            }
        });
        btnMonth.setOnAction(e->{
            if(dateSearch.getValue()!=null) {
                billList.clear();
                billList.addAll(billService.findByDateBetween(dateSearch.getValue().withDayOfMonth(1), dateSearch.getValue().withDayOfMonth(dateSearch.getValue().lengthOfMonth())));
            }
        });
        btnSave.setOnAction(e->save());
        btnClear2.setOnAction(e->clear2());
        btnUpdate2.setOnAction(e->update2());
    }

    private void update2() {
        if(tableOld.getSelectionModel().getSelectedItem()==null)
            return;
        Bill bill = tableOld.getSelectionModel().getSelectedItem();
        trList.clear();
        trList.addAll(bill.getTransactions());
        validateTrList();
        txtBank.setText(bill.getBank().getBankname());
        Customer cust = bill.getCustomer();
        if(cust.getId()!=1) {
            txtCustomerMobile.setText(bill.getCustomer().getContact());
            txtCustomerName.setText(bill.getCustomer().getName());
        }
        billno=bill.getBillno();

    }

    private void clear2() {
        trList.clear();
        date.setValue(LocalDate.now());
        txtNetTotal.setText(String.valueOf(0.0f));
        txtOther.setText(String.valueOf(0.0f));
        txtGrandTotal.setText(String.valueOf(0.0f));
        txtRecived.setText(String.valueOf(0.0f));
        txtCustomerName.setText("");
        txtMrp.setText(String.valueOf(0.0f));
        txtDiscount.setText(String.valueOf(0.0f));
        txtPayble.setText(String.valueOf(0.0f));
        txtRecived.setText(String.valueOf(0.0f));
        txtRemaining.setText(String.valueOf(0.0f));
        txtCustomerMobile.setText(String.valueOf(0.0f));
        txtBank.setText(bankService.getBankById(1).getBankname());
        billno=null;
    }

    private void save() {
        try {
//            printbill = new PrintBill();
//            printbill.setBill(billService.getBillByBillNo(42));
            // printbill.createDoc();
            if(!validateBill())return;
            Customer custom = null;
            custom =checkCustomer();
            if(custom==null)customerService.getCustomerByName(txtCustomerName.getText());
            float paid=0.0f,remain=0.0f;
            if(Float.parseFloat(txtRemaining.getText())<0.0f)
            {
                paid = Float.parseFloat(txtRecived.getText());
            }else paid = Float.parseFloat(txtPayble.getText());

            Bill bill = Bill.builder()
                    .bank(bankService.getBankByBankname(txtBank.getText()))
                    .customer(custom)
                    .date(date.getValue())
                    .discount(Float.parseFloat(txtDiscount.getText()))
                    .grandtotal(Float.parseFloat(txtGrandTotal.getText()))
                    .login(loginService.getLoginByUserName("Admin"))
                    .nettotal(Float.parseFloat(txtNetTotal.getText()))
                    .othercharges(Float.parseFloat(txtOther.getText()))
                    .paid(paid)
                    .transactions(new ArrayList<Transaction>())
                    .build();

            if(billno!=null) {
                bill.setBillno(billno);
                //update stock add Old Stock in item stock
                Bill oldBill = billService.getBillByBillNo(billno);
                addBankTransaction(BankTransaction.builder()
                        .bank(oldBill.getBank())
                        .transactionid(oldBill.getBillno())
                        .particulars("Edit Bill "+oldBill.getBillno())
                        .debit(oldBill.getPaid())
                        .credit(0.0f)
                        .date(LocalDate.now())
                        .build());
                ItemStock oldStock=null;
                for(Transaction tr:oldBill.getTransactions())
                {
                    oldStock = ItemStock.builder().stock(tr.getQuantity())
                            .barcode(tr.getBarcode())
                            .itemname(tr.getItemname())
                            .unit(tr.getUnit())
                            .build();
                    stockService.saveItemStock(oldStock);
                    System.out.println("Stock Added "+oldStock);
                    oldStock=null;
                }

            }
            for(Transaction tr:trList)
            {
                tr.setId(null);
                tr.setBill(bill);
                bill.getTransactions().add(tr);
            }
            int result = billService.saveBill(bill);
            if(result==1)
            {

                reduceStock(bill.getTransactions());
                //Add in BankTransaction
                addBankTransaction(BankTransaction.builder()
                        .bank(bill.getBank())
                        .credit(bill.getPaid())
                        .date(bill.getDate())
                        .debit(0.0f)
                        .particulars("Add Bill "+bill.getBillno())
                        .transactionid(bill.getBillno())
                        .build());
                //printbill = new PrintBill();
               // printbill.setBill(bill);
               // printbill.createDoc();
                alert.showSuccess("Bill Save Success");
                loadTodaysBills();
                clear2();

            }
            else if(result==2)
            {
                reduceStock(bill.getTransactions());
                addBankTransaction(BankTransaction.builder()
                        .bank(bill.getBank())
                        .credit(bill.getPaid())
                        .date(bill.getDate())
                        .debit(0.0f)
                        .particulars("Add Bill "+bill.getBillno())
                        .transactionid(bill.getBillno())
                        .build());
                alert.showSuccess("Bill  Updated Success");
                loadTodaysBills();
                clear2();
            }

        }catch(Exception e){
        alert.showError("Error in Saving Bill "+e.getMessage());
        e.printStackTrace();
        }
    }

    private Customer checkCustomer() {
        Customer customer =null;
        if(txtCustomerName.getText().isEmpty() || txtCustomerName.getText().isBlank()|| txtCustomerName.getText().equals("")||txtCustomerName.getText().equals(null))
        {
            return customerService.getById(1);
        }
        customer=customerService.getCustomerByName(txtCustomerName.getText());
        if(customer!=null)
        {
            return customer;
        }
        else if(alert.showConfirmation("Customer Not Found","Do You Want To Add?"))
        {
            customer = Customer.builder()
                    .email("-")
                    .contact(txtCustomerMobile.getText())
                    .name(txtCustomerName.getText())
                    .build();
            customerService.saveCustomer(customer);
            return customer;
        }
        return null;
    }

    void loadTodaysBills()
    {
        billList.clear();
        billList.addAll(billService.getBillByDate(LocalDate.now()));
    }
    void reduceStock(List<Transaction> list)
    {
        ItemStock stock = null;
        for(Transaction tr:list)
        {
            stock = ItemStock.builder()
                    .stock((tr.getQuantity()/(-1))).itemname(tr.getItemname())
                    .barcode(tr.getBarcode())
                    .unit(tr.getUnit())
                    .build();
            System.out.println("gon a reduce stock=> "+stock);
            stockService.saveItemStock(stock);
            stock=null;
        }
    }
    private boolean validateBill() {
        if (trList.size() == 0) {
            alert.showError("No Data to Save");
            printbill.setBill(billService.getBillByBillNo(31));
            printbill.createDoc();
            return false;
        }
        if(date.getValue()==null)
        {
            alert.showError("Select Date");
            date.requestFocus();
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
            alert.showError("Select Bank Again");
            txtBank.requestFocus();
            return false;
        }
        if(txtCustomerName.getText().isEmpty())
        {
            boolean result=true;
            if(txtRecived.getText().isEmpty()
                    || Float.parseFloat(txtRecived.getText())<=0.0f
                    ||Float.parseFloat(txtRemaining.getText())<0.0f)
            {
                alert.showError("Enter Received Amount or Select Customer Name to Credit Bill");
                result=false;
            }
            return result;
        }
        if(!txtCustomerName.getText().isEmpty() && txtCustomerMobile.getText().isEmpty())
        {
            alert.showError("Enter Customer Mobile Number");
            txtCustomerMobile.requestFocus();
            return false;
        }

        return true;

    }

    private void update() {
        if(table.getSelectionModel().getSelectedItem()==null)
            return;
        Transaction tr = table.getSelectionModel().getSelectedItem();
        txtBarcode.setText(tr.getBarcode());
        txtItemName.setText(tr.getItemname());
        txtRate.setText(String.valueOf(tr.getSailingprice()));
        txtQty.setText(String.valueOf(tr.getQuantity()));
        txtAmount.setText(String.valueOf(tr.getAmount()));
        if(tr.getUnit().equals("naga")) cmbUnit.getSelectionModel().select(1);
        else cmbUnit.getSelectionModel().select(0);
    }

    private void clear() {
        System.out.println("Clear");
        txtBarcode.setText("");
        txtItemName.setText("");
        cmbUnit.getSelectionModel().clearSelection();
        txtQty.setText("");
        txtRate.setText("");
        txtAmount.setText("");


    }

    private void remove() {
        if(table.getSelectionModel().getSelectedItem()==null)
            return;
        else {
            trList.remove(table.getSelectionModel().getSelectedItem());
            table.refresh();
            validateTrList();

        }
    }

    private void createTable() {
        colSrNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSrNo.setCellFactory(new Callback<TableColumn<Transaction,Long>, TableCell<Transaction,Long>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Long>()
                {
                    @Override
                    public void updateItem(Long item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colBarcode.setCellFactory(new Callback<TableColumn<Transaction,String>, TableCell<Transaction,String>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, String>()
                {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(item);
                        }
                    }
                };
            }
        });
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colItemName.setCellFactory(new Callback<TableColumn<Transaction,String>, TableCell<Transaction,String>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, String>()
                {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(item);
                        }
                    }
                };
            }
        });
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setCellFactory(new Callback<TableColumn<Transaction,String>, TableCell<Transaction,String>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, String>()
                {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(item);
                        }
                    }
                };
            }
        });
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setCellFactory(new Callback<TableColumn<Transaction,Number>, TableCell<Transaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colRate.setCellValueFactory(new PropertyValueFactory<>("sailingprice"));
        colRate.setCellFactory(new Callback<TableColumn<Transaction,Number>, TableCell<Transaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colMrp.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colMrp.setCellFactory(new Callback<TableColumn<Transaction,Number>, TableCell<Transaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(new Callback<TableColumn<Transaction,Number>, TableCell<Transaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setFont(Font.font ("kiran", 25));
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        table.setItems(trList);
    }

    private void add() {
        try {
            if(!validate())
            {
                return;
            }
                //checking stock availability;
            ItemStock stock = stockService.getItemStockByItemname(txtItemName.getText());
            if(stock.getStock()<Float.parseFloat(txtQty.getText()))
            {
                alert.showError("Less Quantity Available "+stock.getStock()+" "+cmbUnit.getValue());
                return;
            }
            Transaction tr = Transaction.builder()
                    .amount(Float.parseFloat(txtAmount.getText()))
                    .id((long) (trList.size()+1))
                    .barcode(txtBarcode.getText())
                    .itemname(item.getItemname())
                    .unit(item.getUnit())
                    .price(item.getPrice())
                    .quantity(Float.parseFloat(txtQty.getText()))
                    .rate(item.getRate())
                    .sailingprice(Float.parseFloat(txtRate.getText()))
                    .amount(Float.parseFloat(txtAmount.getText())).build();

            addInTrList(tr);
            validateTrList();
            txtBarcode.requestFocus();
            clear();
        }catch(Exception e)
        {
            alert.showError("Error in Adding Item");
            e.printStackTrace();
            return;
        }
    }

    private void addInTrList(Transaction tr) {
        try {
            int index=-1;
            for(Transaction t:trList)
            {

                System.out.println("Before index "+trList.indexOf(t));
                if(tr.getItemname().equals(t.getItemname())&&t.getSailingprice().compareTo(tr.getSailingprice())==0){
                    index = trList.indexOf(t);
                    System.out.println("Found At "+index);
                    break;
                }
            }
            if(index==-1)
            {
                trList.add(tr);
            }
            else
            {
                trList.get(index).setQuantity(trList.get(index).getQuantity()+tr.getQuantity());
                trList.get(index).setAmount(trList.get(index).getQuantity()*trList.get(index).getSailingprice());
                table.refresh();
            }
        }catch(Exception e)
        {
            alert.showError("Error in Adding List");
        }
    }

    private void validateTrList() {

        long id=0;
        Float net=0.0f;
        float mrp=0;
        for(Transaction trs:trList)
        {
            trList.get(trList.indexOf(trs)).setId(++id);
            net+=trs.getAmount();
            mrp+=(trs.getRate()*trs.getQuantity());
        }
        txtNetTotal.setText(String.valueOf(net));
        txtMrp.setText(String.valueOf(mrp));
        txtDiscount.setText(String.valueOf(mrp-net));
        calculateGrandTotal();
    }

    private boolean validate() {
        if(txtItemName.getText().isEmpty())
        {
            alert.showError("Enter Item Name");
            txtItemName.requestFocus();
            return false;
        }
        if(txtQty.getText().isEmpty()|| Float.parseFloat(txtQty.getText())<=0.0f)
        {
            alert.showError("Enter Item Quantity");
            txtQty.requestFocus();
            return false;
        }
        if(txtRate.getText().isEmpty() || Float.parseFloat(txtRate.getText())<=0.0f)
        {
            alert.showError("Enter Item Rate");
            txtRate.requestFocus();
            return false;
        }
        if(txtAmount.getText().isEmpty() || Float.parseFloat(txtAmount.getText())<=0.0f)
        {
            alert.showError("Select Item Again");
            txtBarcode.requestFocus();
            return false;
        }
        if(item==null)
        {
            alert.showError("Enter Item Name or Barcode");
            txtBarcode.requestFocus();
            return false;
        }
        return true;

    }

    private void barcodeAction() {
        if(txtBarcode.getText().isEmpty()) {
            txtItemName.requestFocus();
            return;
        }

        item = itemService.getItemByBarcode(txtBarcode.getText());
        if(item==null)
            return;
        else {
            setItem(item);
            txtQty.requestFocus();
        }
    }
    private void setItem(Item item)
    {
        if(item==null) return;
        txtItemName.setText(item.getItemname());
        txtRate.setText(""+item.getSailingprice());
        txtBarcode.setText(item.getBarcode());

        if(item.getUnit().equals("ik.ga`^."))
            cmbUnit.setValue("KG");
        else cmbUnit.setValue("NOS");
        if(item.getSailingprice()==0.0f)
        {
            alert.showError("Item Sailing Rate Not Set Enter Sailing Rate");
            txtRate.requestFocus();
            return;
        }

    }
    private void createItemList() {
        listItem = new ListView();
        listItem.setStyle("-fx-font:18pt \"Kiran\"");
        listItem.getItems().addAll(stockService.getAllItemNames());
        listItem.setLayoutX(2);
        listItem.setLayoutY(40);
        listItem.setPrefWidth(240);
        listItem.prefHeightProperty().bind(Bindings.divide(leftPane.heightProperty(),1.0));
        leftPane.getChildren().addAll(listItem);
        listItem.setOnMouseClicked(e->{
            if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2){
                System.out.println(listItem.getSelectionModel().getSelectedItem());
                System.out.println(itemService.getItemByName(listItem.getSelectionModel().getSelectedItem()));
                setItem(itemService.getItemByName(listItem.getSelectionModel().getSelectedItem()));
            }
        });
    }
    void addItemNameSearch()
    {
        itemNameSearch.addAll(itemService.getAllItemNames());
        listView = new ListView();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(400);
        listView.setLayoutY(120);
        rootPane.getChildren().addAll(listView);
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
                    txtQty.requestFocus();
                    setItem(item);
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
                setItem(itemService.getItemByName(txtItemName.getText()));
                txtQty.requestFocus();
                listView.setVisible(false);
            }
        });
        txtItemName.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if(t1)
                {
                    findItem(txtItemName.getText());
                    listView.setVisible(true);
                }
                else {
                    if(listView.isFocused())
                        return;
                    else
                        listView.setVisible(false);
                }
            }
        });
        listView.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if(t1) {
                    //in focus
                }
                else{
                    if(txtItemName.isFocused())
                        return;
                    else
                        listView.setVisible(false);
                }
            }
        });
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    void findItem(String find) {
        //cmodel.removeAllElements();
        listView.getItems().clear();
        if(find.equals("")|| find.equals(" "))
        {
            listView.getItems().clear();
            listView.getItems().addAll(itemNameSearch);
            return;
        }
        try {
            for (int i = 0; i < itemNameSearch.size(); i++) {
                if (itemNameSearch.get(i).startsWith(find)) {
                    listView.getItems().add(itemNameSearch.get(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in findItem " + e.getMessage());
            return;
        }
    }
    void calculateGrandTotal()
    {
        if(!isNumeric(txtRecived.getText()))
            txtRecived.setText(String.valueOf(0.0));
        txtGrandTotal.setText(
                String.valueOf(Float.parseFloat(txtNetTotal.getText())+Float.parseFloat(txtOther.getText()))
        );
        txtRemaining.setText(
                String.valueOf(Float.parseFloat(txtRecived.getText())-Float.parseFloat(txtGrandTotal.getText()))
        );
        txtPayble.setText(txtGrandTotal.getText());
    }
    private void addBankTransaction(BankTransaction bankTransaction)
    {
        try {

            bankTrService.saveBankTransaction(bankTransaction);
            System.out.println("Saved Bank Transaction "+bankTransaction);
            bankService.addBankBalance(bankTransaction.getCredit(),bankTransaction.getBank().getId());
            bankService.reduceBankBalance(bankTransaction.getDebit(),bankTransaction.getBank().getId());
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
