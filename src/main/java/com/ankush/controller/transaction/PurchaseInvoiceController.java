package com.ankush.controller.transaction;

import com.ankush.Main;
import com.ankush.common.CommonData;
import com.ankush.common.PurchaseTotals;
import com.ankush.controller.print.PurchaseInvoicePdf;
import com.ankush.data.entities.*;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.data.service.*;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlView;
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
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

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
    @FXML private TextField txtMrp;
    @FXML private TextField txtSellingRate;
    @FXML private Label lblSellingRate;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemove;
    @FXML private Button btnClear;
    @FXML private Button btnEditItem;
    @FXML private TableView<PurchaseTransaction> table;
    @FXML private TableColumn<PurchaseTransaction,Number> colSr;
    @FXML private TableColumn<PurchaseTransaction,String> colBarcode;
    @FXML private TableColumn<PurchaseTransaction,String> colItemName;
    @FXML private TableColumn<PurchaseTransaction,Number> colQty;
    @FXML private TableColumn<PurchaseTransaction,String> colUnit;
    @FXML private TableColumn<PurchaseTransaction,Number> colRate;
    @FXML private TableColumn<PurchaseTransaction,Number> colSellingRate;
    @FXML private TableColumn<PurchaseTransaction,Number> colAmount;
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
    @FXML private TableColumn<PurchaseInvoice,Number> colTotal;
    @FXML private TableColumn<PurchaseInvoice,Number> colPaid;

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
    @Autowired private ShopeeInfoService shopeeInfoService;
    private Long invoiceid;
    private AutoCompleteTextField autoCompleteItemName;
    private AutoCompleteTextField autoCompleteParty;
    private AutoCompleteTextField autoCompleteBank;
    private ObservableList<PurchaseTransaction>trList = FXCollections.observableArrayList();
    private Item item;
    private ObservableList<PurchaseInvoice>oldInvoiceList = FXCollections.observableArrayList();
    private final Map<String, Float> sellingRateMap = new HashMap<>();
    /** Row loaded with UPDATE; the next ADD replaces it. -1 when adding a new line. */
    private int editingIndex = -1;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Font.loadFont(Main.class.getResource("/fxml/font/kiran.ttf").toExternalForm(),10);
        this.invoiceid= Long.valueOf(0);
        stageManager.showFullScreen();
        colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSr.setCellFactory(new Callback<TableColumn<PurchaseTransaction,Number>, TableCell<PurchaseTransaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {

                        super.updateItem(item, empty);
                        setFont(Font.font ("kiran", 25));
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {

                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colBarcode.setCellFactory(new Callback<TableColumn<PurchaseTransaction,String>, TableCell<PurchaseTransaction,String>>() {
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
        colItemName.setCellFactory(new Callback<TableColumn<PurchaseTransaction,String>, TableCell<PurchaseTransaction,String>>() {
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
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colQty.setCellFactory(new Callback<TableColumn<PurchaseTransaction,Number>, TableCell<PurchaseTransaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {

                        super.updateItem(item, empty);
                        setFont(Font.font ("kiran", 25));
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {

                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setCellFactory(new Callback<TableColumn<PurchaseTransaction,String>, TableCell<PurchaseTransaction,String>>() {
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
        colRate.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRate.setCellFactory(new Callback<TableColumn<PurchaseTransaction,Number>, TableCell<PurchaseTransaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {

                        super.updateItem(item, empty);
                        setFont(Font.font ("kiran", 25));
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {

                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colSellingRate.setCellValueFactory(cellData -> {
            PurchaseTransaction t = cellData.getValue();
            Float r = sellingRateMap.get(sellingRateKey(t.getBarcode(), t.getItemname()));
            return new SimpleFloatProperty(r == null ? 0.0f : r);
        });
        colSellingRate.setCellFactory(new Callback<TableColumn<PurchaseTransaction,Number>, TableCell<PurchaseTransaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        setFont(Font.font ("kiran", 25));
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {
                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(new Callback<TableColumn<PurchaseTransaction,Number>, TableCell<PurchaseTransaction,Number>>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<PurchaseTransaction, Number>()
                {
                    @Override
                    public void updateItem(Number item, boolean empty) {

                        super.updateItem(item, empty);
                        setFont(Font.font ("kiran", 25));
                        if(isEmpty())
                        {
                            setText("");
                        }
                        else
                        {

                            setText(String.valueOf(item));
                        }
                    }
                };
            }
        });
        table.setItems(trList);
        item = null;
        cmbUnit.getItems().addAll("KG","NOS");
        autoCompleteParty = new AutoCompleteTextField(txtParty, partyService.getAllPurchasePartyNames(), Font.font("System Bold", 14), txtBarcode);
        autoCompleteParty.setUseContainsFilter(true);
        autoCompleteBank = new AutoCompleteTextField(txtBank, bankService.getAllBankNames(), Font.font("System Bold", 14), btnSave);
        autoCompleteBank.setUseContainsFilter(true);
        autoCompleteItemName = new AutoCompleteTextField(txtItemName, itemService.getActiveItemNames(), Font.font("Kiran", 20), txtQty);
        autoCompleteItemName.setUseContainsFilter(true);
        autoCompleteItemName.setSearchKeys(itemService.getSearchKeys());
        autoCompleteItemName.setOnSelectionCallback(selectedName -> {
            if(txtBarcode.getText().isEmpty()) {
                item = itemService.getItemByName(selectedName);
            }
            setItem();
        });

        colSrNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("partyinvoiceno"));
        colParty.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getPurchaseParty().getName()));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("grandtotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        date.setValue(LocalDate.now());
        dateSearch.setValue(LocalDate.now());
        oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(LocalDate.now()));
        tableold.setItems(oldInvoiceList);

        txtBarcode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                item=null;

                if(!txtBarcode.getText().equals(""))
               item = itemService.getItemByBarcode(txtBarcode.getText());
                if(item!=null){setItem();}
                else
                {
                    txtItemName.setText("");
                }
            }
        });
        txtBarcode.setOnMouseClicked(e->autoCompleteItemName.hidePopup());
        txtBarcode.setOnAction(e->txtItemName.requestFocus());
        txtQty.setOnMouseClicked(e->autoCompleteItemName.hidePopup());
        txtQty.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtQty.setText(oldValue);
                }
            }
        });
        txtMrp.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtMrp.setText(oldValue);
                }
            }
        });
        txtQty.setOnKeyReleased(e->{
            autoCompleteItemName.hidePopup();
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
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtRate.setText(oldValue);
                }
                autoFillSellingRateFromRate();
                refreshSellingRateLabel();
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
                    txtMrp.requestFocus();
                }
            }
        });
        txtMrp.setOnKeyReleased(e->{
            if(isNumber(txtMrp.getText()) && isNumber(txtMrp.getText()))
            {
                txtAmount.setText(
                        String.valueOf(Float.parseFloat(txtQty.getText())*Float.parseFloat(txtRate.getText()))
                );
                if(e.getCode()==KeyCode.ENTER)
                {
                    txtSellingRate.requestFocus();
                }
            }
        });
        txtSellingRate.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                txtSellingRate.setText(oldValue);
            }
            refreshSellingRateLabel();
        });
        txtSellingRate.setOnKeyReleased(e->{
            if(e.getCode()==KeyCode.ENTER)
            {
                btnAdd.requestFocus();
            }
        });
        txtTransporting.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtTransporting.setText(oldValue);
                }
            }
        });
        txtTransporting.setOnAction(e->{calculateGrandTotal();txtWages.requestFocus();});
        txtWages.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtWages.setText(oldValue);
                }
            }
        });
        txtWages.setOnAction(e->{calculateGrandTotal();txtOther.requestFocus();});
        txtOther.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                    txtOther.setText(oldValue);
                }
            }
        });
        txtOther.setOnAction(e->{calculateGrandTotal();txtDiscount.requestFocus();});
        txtDiscount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,6})?")) {
                txtDiscount.setText(oldValue);
            }
        });
        txtDiscount.setOnAction(e->{calculateGrandTotal();txtPaid.requestFocus();});
        txtPaid.setOnAction(e->{calculateGrandTotal();txtPaid.requestFocus();});
        // keep the grand total live while typing, not only on ENTER (tab / mouse used to leave it stale)
        for (TextField charge : List.of(txtTransporting, txtWages, txtOther, txtDiscount)) {
            charge.textProperty().addListener((observable, oldValue, newValue) -> refreshGrandTotal());
        }
        btnEditItem.setOnAction(e->editItem());
        btnEditItem.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F7), this::editItem);
        });
        btnAdd.setOnAction(e->add());
        btnUpdate.setOnAction(e->update());
        btnClear.setOnAction(e->clear());
        btnRemove.setOnAction(e->remove());
        btnSave.setOnAction(e->save());
        btnUpdate2.setOnAction(e->update2());
        btnClear2.setOnAction(e->clear2());
        btnPrint.setOnAction(e->printInvoice());
        btnExit.setOnAction(e->{
            stageManager.switchScene(FxmlView.HOME);
            stageManager.showFullScreen();
        });
        btnShow.setOnAction(e->{
            if(dateSearch.getValue()!=null) {
                oldInvoiceList.clear();
                tableold.refresh();
                oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(dateSearch.getValue()));
            }
        });
        btnMonth.setOnAction(e->{
            if(dateSearch.getValue()!=null)
            {
                oldInvoiceList.clear();
                tableold.refresh();
                oldInvoiceList.addAll(invoiceService.getDatePeriodWisePurchaseInvoice(dateSearch.getValue().withDayOfMonth(1),dateSearch.getValue().withDayOfMonth(dateSearch.getValue().lengthOfMonth())));
            }
            else{
                alert.showError("Select Date For Search");
                dateSearch.requestFocus();
            }
        });
        btnYear.setOnAction(e->{
            if(dateSearch.getValue()!=null)
            {
                oldInvoiceList.clear();
                oldInvoiceList.addAll(
                        invoiceService.getDatePeriodWisePurchaseInvoice(
                                dateSearch.getValue().with(firstDayOfYear()),
                                dateSearch.getValue().with(lastDayOfYear())));
            }
            else{
                alert.showError("Select Date For Search");
                dateSearch.requestFocus();
            }
        });
        btnAll.setOnAction(e->{
            oldInvoiceList.clear();
            tableold.refresh();
            oldInvoiceList.addAll(invoiceService.getAllPurchaseInvoice());
        });
    }

    /**
     * PRINT: the invoice selected in the list (or the one opened with UPDATE) as an A4 PDF, opened in the
     * PDF viewer to print. Prints what is saved, so unsaved changes on screen are not on it.
     */
    private void printInvoice() {
        PurchaseInvoice selected = tableold.getSelectionModel().getSelectedItem();
        Long id = selected != null ? selected.getId() : (invoiceid != null && invoiceid != 0 ? invoiceid : null);
        if (id == null) {
            alert.showError("Select an invoice in the list to print");
            return;
        }
        try {
            PurchaseInvoice invoice = invoiceService.getInvoiceById(id);
            List<ShopeeInfo> shops = shopeeInfoService.getAllShopeeInfo();
            java.io.File pdf = new PurchaseInvoicePdf().create(invoice, shops.isEmpty() ? null : shops.get(0),
                    PurchaseInvoicePdf.defaultFolder());
            alert.showSuccess("Invoice PDF saved: " + pdf.getAbsolutePath());
            // the viewer can take a moment to start; don't hold the screen meanwhile
            Thread opener = new Thread(() -> {
                try {
                    java.awt.Desktop.getDesktop().open(pdf);
                } catch (Exception ex) {
                    Platform.runLater(() -> alert.showError("PDF saved but could not be opened: " + pdf.getAbsolutePath()));
                }
            });
            opener.setDaemon(true);
            opener.start();
        } catch (Exception ex) {
            alert.showError("Error in printing invoice: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clear2() {
        date.setValue(LocalDate.now());
        autoCompleteParty.clear();
        txtInvoiceNo.setText("");
        clear();
        trList.clear();
        sellingRateMap.clear();
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
            sellingRateMap.clear();
            for(PurchaseTransaction tr : in.getTransactions()){
                // item renamed / merged since this invoice: edit and save the line under its current name, otherwise
                // saving re-creates the old item in Item Master and books the stock under the old name
                Item itm = itemService.findCurrentItem(tr.getItemname(), tr.getBarcode());
                if(itm!=null){
                    tr.setItemname(itm.getItemname());
                    tr.setBarcode(itm.getBarcode()==null ? "" : itm.getBarcode());
                    sellingRateMap.put(sellingRateKey(tr.getBarcode(), tr.getItemname()), itm.getSailingprice());
                }
            }
            trList.addAll(in.getTransactions());
            manageTransactionList();
            date.setValue(in.getDate());
            txtInvoiceNo.setText(in.getPartyinvoiceno());
            autoCompleteParty.setText(in.getPurchaseParty().getName());
            txtTransporting.setText(String.valueOf(in.getTransporting()));
            txtWages.setText(String.valueOf(in.getWages()));
            txtOther.setText(String.valueOf(in.getOther()));
            txtNetTotal.setText(String.valueOf(in.getNettotal()));
            txtGrandTotal.setText(String.valueOf(in.getGrandtotal()));
            txtDiscount.setText(String.valueOf(in.getDiscount()));
            txtPaid.setText(String.valueOf(in.getPaid()));
            autoCompleteBank.setText(in.getBank().getBankname());
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
        calculateGrandTotal();
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPartyinvoiceno(txtInvoiceNo.getText());
        invoice.setPurchaseParty(partyService.getPartyByName(autoCompleteParty.getText()));
        invoice.setBank(bankService.getBankByBankname(autoCompleteBank.getText()));
        invoice.setDate(date.getValue());
        invoice.setDiscount(Float.parseFloat(txtDiscount.getText()));
        invoice.setGrandtotal(Float.parseFloat(txtGrandTotal.getText()));
        invoice.setNettotal(Float.parseFloat(txtNetTotal.getText()));
        invoice.setOther(Float.parseFloat(txtOther.getText()));
        invoice.setTransporting(Float.parseFloat(txtTransporting.getText()));
        invoice.setPaid(Float.parseFloat(txtPaid.getText()));
        invoice.setWages(Float.parseFloat(txtWages.getText()));
        invoice.setLogin(CommonData.getLoginUser());
        // check before touching the lines, a refused save used to blank every Sr. No. on screen.
        // When editing, the old payment is refunded to its bank first, so it counts as available there.
        Float balance = bankService.getBalanceById(invoice.getBank().getId());
        float available = balance == null ? 0.0f : balance;
        if(invoiceid!=0)
        {
            PurchaseInvoice before = invoiceService.getInvoiceById(invoiceid);
            if(before.getBank()!=null && before.getBank().getId().equals(invoice.getBank().getId()))
                available += before.getPaid();
        }
        if(available<invoice.getPaid())
        {
            alert.showError("Not sufficient Balance in Bank choose another ");
            autoCompleteBank.requestFocus();
            return;
        }
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
        int flag=0;
        flag= invoiceService.savePurchaseInvoice(invoice);
        if(flag==1)
        {
            invoiceTrService.saveTransactions(invoice.getTransactions());

            addInStock(invoice.getTransactions());
            addInItemMaster(invoice.getTransactions());
            updateItemSellingRates(invoice.getTransactions());
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


            // reset totals too, otherwise the next invoice starts from this one's net total
            clear2();
            oldInvoiceList.clear();
            oldInvoiceList.addAll(invoiceService.getDateWisePurchaseInvoice(LocalDate.now()));
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
            updateItemSellingRates(invoice.getTransactions());
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
            // stock is looked up through renames / merges (the old name+barcode lookup found nothing after a
            // rename, which was reported as "already sold"); lines of the same item are added together
            Map<String, Float> bought = new HashMap<>();
            Map<String, Float> inStock = new HashMap<>();
            for(PurchaseTransaction tr:transactions)
            {
                ItemStock s = itemStockService.getCurrentStock(tr.getItemname(), tr.getBarcode());
                String key = s==null ? tr.getItemname() : s.getItemname();
                bought.merge(key, tr.getQty(), Float::sum);
                inStock.put(key, s==null || s.getStock()==null ? 0.0f : s.getStock());
            }
            for(Map.Entry<String, Float> e : bought.entrySet())
            {
                if(inStock.get(e.getKey()) < e.getValue()) return 0;
            }
            return 1;
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
                // through renames / merges and NULL barcodes: an exact name+barcode lookup missed those items
                // and re-created them as duplicates
                item = itemService.findCurrentItem(tr.getItemname(),tr.getBarcode());
                // New items are created (with confirmation) when the line is added; this only
                // covers old invoices being edited whose item no longer exists.
                if(item==null)
                {
                    item = new Item();
                    item.setItemname(tr.getItemname());
                    item.setUnit(tr.getUnit());
                    item.setPrice(tr.getPrice());
                    item.setBarcode(tr.getBarcode());
                    item.setRate(tr.getMrp());
                    itemService.saveItem(item);
                }
                else
                {
                    item.setPrice(tr.getPrice());
                    // buying stock again brings a deactivated item back into billing search
                    if(!item.isActiveItem()) item.setActive(true);
                    itemService.saveItem(item);
                }

            }
        }catch (Exception e)
        {
            alert.showError("Error in Addin In ItemMaster");
            e.printStackTrace();
        }
    }
    private void updateItemSellingRates(List<PurchaseTransaction> list){
        try{
            for(PurchaseTransaction tr : list){
                Float newRate = sellingRateMap.get(sellingRateKey(tr.getBarcode(), tr.getItemname()));
                if(newRate==null || newRate<=0) continue;
                Item itm = itemService.findCurrentItem(tr.getItemname(), tr.getBarcode());
                if(itm!=null && itm.getSailingprice()!=newRate){
                    itm.setSailingprice(newRate);
                    itemService.saveItem(itm);
                }
            }
        }catch(Exception e){
            alert.showError("Error in Updating Item Selling Rate");
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
                    stock.setUnit(tr.getUnit());
                    itemStockService.saveItemStock(stock);


                }
        }catch(Exception e)
        {
            alert.showError("Error in Adding Stock");
            e.printStackTrace();
        }
    }
    private boolean validateData() {
        if(autoCompleteParty.getText().isEmpty())
        {
            alert.showError("Select Purchase Party Name");
            autoCompleteParty.requestFocus();
            return false;
        }
        if(partyService.getPartyByName(autoCompleteParty.getText())==null)
        {
            alert.showError("Party Not Found");
            autoCompleteParty.requestFocus();
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
        if(autoCompleteBank.getText().isEmpty())
        {
            alert.showError("Select Bank Name");
            autoCompleteBank.requestFocus();
            return false;
        }
        if(bankService.getBankByBankname(autoCompleteBank.getText())==null)
        {
            alert.showError("Bank Not Found");
            autoCompleteBank.requestFocus();
            return false;
        }
        if(txtMrp.getText().isEmpty()||txtMrp.getText().isBlank()||txtMrp.getText().equals("") && !isNumber(txtMrp.getText()))
        {
            txtMrp.setText(String.valueOf(0.0));
        }
        if(!txtPaid.getText().isBlank() && !isNumber(txtPaid.getText()))
        {
            alert.showError("Enter Paid Amount in digits");
            txtPaid.requestFocus();
            return false;
        }
        return true;
    }

    private void remove() {
        if(table.getSelectionModel().getSelectedItem()==null)
            return;
        trList.remove(table.getSelectionModel().getSelectedIndex());
        table.getSelectionModel().clearSelection();
        editingIndex=-1;
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
        txtMrp.setText(""+0.0f);
        txtSellingRate.setText(""+0.0f);
        if(lblSellingRate!=null) lblSellingRate.setStyle("");
        cmbUnit.getSelectionModel().clearSelection();
        item=null;
        editingIndex=-1;
        txtQty.requestFocus();
    }
    private void calculateGrandTotal()
    {
        if(txtTransporting.getText().isEmpty()) txtTransporting.setText(""+0.0);
        if(txtWages.getText().isEmpty()) txtWages.setText(""+0.0);
        if(txtOther.getText().isEmpty()) txtOther.setText(""+0.0);
        if(txtPaid.getText().isEmpty()) txtPaid.setText(""+0.0);
        if(txtDiscount.getText().isEmpty()) txtDiscount.setText(""+0.0);
        refreshGrandTotal();
    }
    /** Net total is always rebuilt from the table lines; grand total = net - discount + charges. */
    private void refreshGrandTotal()
    {
        float net = PurchaseTotals.netTotal(trList);
        txtNetTotal.setText(String.valueOf(net));
        txtGrandTotal.setText(String.valueOf(PurchaseTotals.grandTotal(net,
                PurchaseTotals.parse(txtDiscount.getText()),
                PurchaseTotals.parse(txtTransporting.getText()),
                PurchaseTotals.parse(txtWages.getText()),
                PurchaseTotals.parse(txtOther.getText()))));
    }
    private void update() {
        if(table.getSelectionModel().getSelectedItem()==null)
        {
            return;
        }
        PurchaseTransaction tr = table.getSelectionModel().getSelectedItem();
        if(tr==null) return;
        else{
            int index = table.getSelectionModel().getSelectedIndex();
            // barcode first: its listener fills (or clears) the name from Item Master, the line's own name wins
            txtBarcode.setText(tr.getBarcode()==null ? "" : tr.getBarcode());
            txtItemName.setText(tr.getItemname());
            txtQty.setText(String.valueOf(tr.getQty()));
            txtRate.setText(String.valueOf(tr.getPrice()));
            txtAmount.setText(String.valueOf(tr.getAmount()));
            txtMrp.setText(String.valueOf(tr.getMrp()));
            Float sr = sellingRateMap.get(sellingRateKey(tr.getBarcode(), tr.getItemname()));
            txtSellingRate.setText(String.valueOf(sr==null?0.0f:sr));
            if(tr.getUnit().equals("ik.ga`^."))
                cmbUnit.getSelectionModel().select(0);
            else
                cmbUnit.getSelectionModel().select(1);
            // set last: the field listeners above reset item state, ADD must then replace this line
            editingIndex = index;
        }
    }
    private void add() {
       if(!validateItem())
       {
           return;
       }
        PurchaseTransaction tr = createTransaction();
        captureSellingRate(tr);
        if(editingIndex>=0 && editingIndex<trList.size()
                && trList.get(editingIndex).getItemname().trim().equals(tr.getItemname().trim()))
        {
            // edited line: replace it instead of merging, which used to add the qty on top of the old qty.
            // Only for the same item: another item added after UPDATE is a new line, not a replacement.
            tr.setId(trList.get(editingIndex).getId());
            trList.set(editingIndex, tr);
            calculateGrandTotal();
            clear();
            return;
        }
        editingIndex=-1;
        addInTrList(tr);
    }
    private void captureSellingRate(PurchaseTransaction tr){
        if(txtSellingRate.getText()!=null && !txtSellingRate.getText().isEmpty() && isNumber(txtSellingRate.getText())){
            float v = Float.parseFloat(txtSellingRate.getText());
            if(v>0){
                sellingRateMap.put(sellingRateKey(tr.getBarcode(), tr.getItemname()), v);
            }
        }
        table.refresh();
    }
    private void addInTrList(PurchaseTransaction tr) {
        int index=-1;
        for(int i=0;i<trList.size();i++)
        {
            if(trList.get(i).getItemname().trim().equals(tr.getItemname().trim()) &&
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
            calculateGrandTotal();
            clear();
        }
        else
        {
            trList.get(index).setQty(trList.get(index).getQty()+tr.getQty());
            trList.get(index).setAmount(trList.get(index).getAmount()+tr.getAmount());
            table.refresh();
            // was computed before the net total was restored, dropping this whole line from the grand total
            calculateGrandTotal();
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
        if(txtQty.getText().isEmpty() || !isNumber(txtQty.getText()) || Float.parseFloat(txtQty.getText())<=0)
        {
            alert.showError("Enter Quantity");
            txtQty.requestFocus();
            return false;
        }
        // the line amount is qty x rate, so the rate must be a number (a stale Amount field used to let an
        // empty rate through and ADD then failed silently)
        if(!isNumber(txtRate.getText()) || Float.parseFloat(txtRate.getText())<0)
        {
            alert.showError("Enter Purchase Rate");
            txtRate.requestFocus();
            return false;
        }
        if(cmbUnit.getSelectionModel().getSelectedItem()==null)
        {
            alert.showError("Select Item Unit");
            cmbUnit.requestFocus();
            return false;

        }
        if(!txtBarcode.getText().isEmpty()){
        Item item = itemService.getItemByBarcode(txtBarcode.getText());
        if(item!=null)
        {
          //  System.out.println(item);
            if(!item.getItemname().trim().equals(txtItemName.getText().trim()))
            {
                // same product, new name (e.g. the company changed "10 gram" to "9 gram"): rename, don't add an item
                if(confirmRename(item, "This barcode belongs to the item below. Rename it to the name you typed?"))
                    return true;
                txtItemName.requestFocus();
                return false;
            }
            else
                return true;
        }
        }
        item=null;
        item=itemService.getItemByName(txtItemName.getText().trim());
        if(item!=null)
        {
            // old items can have no barcode (NULL): that used to throw and ADD did nothing
            String itemBarcode = item.getBarcode()==null ? "" : item.getBarcode().trim();
            if(!itemBarcode.equals(txtBarcode.getText().trim()))
            {
                // the product came with a new barcode: update the existing item instead of creating another
                if(confirmRename(item, "This item has a different barcode (" + itemBarcode + "). Change it to the new barcode?"))
                    return true;
                txtItemName.requestFocus();
                return false;
            }
            else return true;
        }
        // Not in item master: never create silently, the user must confirm (avoids duplicate items)
        return confirmNewItem();
    }

    /**
     * Shows similar existing items and lets the user pick one of them or explicitly create a new item.
     * @return true if a new item was created and the line can be added
     */
    private boolean confirmNewItem() {
        String name = txtItemName.getText().trim();
        List<String> similar = itemService.findSimilarNames(name);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("New Item");
        dialog.setHeaderText("This item is not in Item Master");
        Label newName = new Label(name);
        newName.setFont(Font.font("Kiran", 22));
        VBox content = new VBox(8, newName);
        ListView<String> similarList = new ListView<>();
        if (!similar.isEmpty()) {
            Label hint = new Label("Similar items already exist. Select one and click 'Use Selected' to buy it as it is, or "
                    + "'Rename Selected' when the company changed it (weight / size / price) - the old item gets the new name.");
            hint.setWrapText(true);
            similarList.getItems().addAll(similar);
            similarList.setStyle("-fx-font:18pt \"Kiran\"");
            similarList.setPrefHeight(Math.min(similar.size(), 6) * 38 + 4);
            similarList.getSelectionModel().select(0);
            content.getChildren().addAll(hint, similarList);
        }
        dialog.getDialogPane().setContent(content);
        ButtonType useSelected = new ButtonType("Use Selected", ButtonBar.ButtonData.OTHER);
        ButtonType renameSelected = new ButtonType("Rename Selected", ButtonBar.ButtonData.OTHER);
        ButtonType createNew = new ButtonType("Create New Item", ButtonBar.ButtonData.YES);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        if (similar.isEmpty()) dialog.getButtonTypes().setAll(createNew, cancel);
        else dialog.getButtonTypes().setAll(useSelected, renameSelected, createNew, cancel);

        ButtonType choice = dialog.showAndWait().orElse(cancel);
        if (choice == renameSelected) {
            String picked = similarList.getSelectionModel().getSelectedItem();
            Item old = picked == null ? null : itemService.getItemByName(picked);
            if (old != null && renameFromFields(old)) return true;
            txtItemName.requestFocus();
            return false;
        }
        if (choice == useSelected) {
            String picked = similarList.getSelectionModel().getSelectedItem();
            if (picked != null) {
                item = itemService.getItemByName(picked);
                setItem();
                txtQty.requestFocus();
            }
            return false;
        }
        if (choice != createNew) {
            txtItemName.requestFocus();
            return false;
        }
        Item newItem = new Item();
        newItem.setItemname(name);
        newItem.setBarcode(txtBarcode.getText().trim());
        newItem.setUnit(cmbUnit.getSelectionModel().getSelectedIndex() == 0 ? "ik.ga`^." : "naga");
        newItem.setPrice(isNumber(txtRate.getText()) ? Float.parseFloat(txtRate.getText()) : 0.0f);
        newItem.setRate(isNumber(txtMrp.getText()) ? Float.parseFloat(txtMrp.getText()) : 0.0f);
        newItem.setSailingprice(isNumber(txtSellingRate.getText()) ? Float.parseFloat(txtSellingRate.getText()) : 0.0f);
        newItem.setActive(true);
        itemService.saveItem(newItem);
        autoCompleteItemName.addSuggestions(List.of(name));
        alert.showSuccess("New item created. Add short code / category later in Item Master.");
        return true;
    }
    /** Shows the existing item and the typed name/barcode; on Yes renames the item from the entry fields. */
    private boolean confirmRename(Item existing, String question) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Change Existing Item");
        dialog.setHeaderText(question);
        Label oldName = new Label(existing.getItemname() + "   (" + existing.getBarcode() + ")");
        oldName.setFont(Font.font("Kiran", 20));
        Label arrow = new Label("will become");
        Label newName = new Label(txtItemName.getText().trim() + "   (" + txtBarcode.getText().trim() + ")");
        newName.setFont(Font.font("Kiran", 20));
        dialog.getDialogPane().setContent(new VBox(6, oldName, arrow, newName,
                new Label("Old bills keep the old name. No new item is created.")));
        ButtonType yes = new ButtonType("Yes, change it", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(yes, no);
        return dialog.showAndWait().orElse(no) == yes && renameFromFields(existing);
    }

    /** Renames / updates {@code existing} with the values in the entry fields. */
    private boolean renameFromFields(Item existing) {
        String unit = cmbUnit.getSelectionModel().getSelectedIndex() == 0 ? "ik.ga`^." : "naga";
        float price = isNumber(txtRate.getText()) ? Float.parseFloat(txtRate.getText()) : existing.getPrice();
        float mrp = isNumber(txtMrp.getText()) && Float.parseFloat(txtMrp.getText()) > 0 ? Float.parseFloat(txtMrp.getText()) : existing.getRate();
        float selling = isNumber(txtSellingRate.getText()) && Float.parseFloat(txtSellingRate.getText()) > 0
                ? Float.parseFloat(txtSellingRate.getText()) : existing.getSailingprice();
        return applyRename(existing, txtItemName.getText(), txtBarcode.getText(), unit, price, mrp, selling);
    }

    private boolean applyRename(Item existing, String name, String barcode, String unit, float price, float mrp, float selling) {
        String oldName = existing.getItemname();
        String oldBarcode = existing.getBarcode();
        try {
            item = itemService.renameItem(existing, name, barcode, unit, price, mrp, selling);
        } catch (IllegalArgumentException ex) {
            alert.showError(ex.getMessage());
            return false;
        }
        // lines of this invoice already entered under the old name follow the item
        for (PurchaseTransaction tr : trList) {
            if (oldName.equals(tr.getItemname()) && (oldBarcode == null || oldBarcode.equals(tr.getBarcode()))) {
                Float rate = sellingRateMap.remove(sellingRateKey(tr.getBarcode(), tr.getItemname()));
                tr.setItemname(item.getItemname());
                tr.setBarcode(item.getBarcode());
                if (rate != null) sellingRateMap.put(sellingRateKey(tr.getBarcode(), tr.getItemname()), rate);
            }
        }
        table.refresh();
        autoCompleteItemName.setSuggestions(itemService.getActiveItemNames());
        autoCompleteItemName.setSearchKeys(itemService.getSearchKeys());
        alert.showSuccess("Item changed. Old bills keep the old name.");
        return true;
    }

    /** EDIT ITEM / F7: change the selected item's name, barcode, unit or rates (company changed the product). */
    private void editItem() {
        Item current = item != null ? item : itemService.getItemByName(txtItemName.getText().trim());
        if (current == null) {
            alert.showError("Select an existing item first");
            txtItemName.requestFocus();
            return;
        }
        TextField name = new TextField(current.getItemname());
        name.setFont(Font.font("Kiran", 20));
        TextField barcode = new TextField(current.getBarcode());
        ComboBox<String> unit = new ComboBox<>();
        unit.getItems().addAll("KG", "NOS");
        unit.getSelectionModel().select("ik.ga`^.".equals(current.getUnit()) ? 0 : 1);
        TextField price = new TextField(String.valueOf(current.getPrice()));
        TextField mrp = new TextField(String.valueOf(current.getRate()));
        TextField selling = new TextField(String.valueOf(current.getSailingprice()));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Item Name"), name);
        grid.addRow(1, new Label("Barcode"), barcode);
        grid.addRow(2, new Label("Unit"), unit);
        grid.addRow(3, new Label("Purchase Rate"), price);
        grid.addRow(4, new Label("MRP"), mrp);
        grid.addRow(5, new Label("Selling Rate"), selling);
        Label note = new Label("Use this when the company changes weight / size / price of the same product.\n"
                + "The item is changed in place: no new item, old bills keep the old name.");
        note.setWrapText(true);
        grid.add(note, 0, 6, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Change existing item");
        dialog.getDialogPane().setContent(grid);
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        Platform.runLater(name::requestFocus);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != save) return;
        if (!isNumber(price.getText()) || !isNumber(mrp.getText()) || !isNumber(selling.getText())) {
            alert.showError("Enter rates in digits");
            return;
        }
        if (applyRename(current, name.getText(), barcode.getText(),
                unit.getSelectionModel().getSelectedIndex() == 0 ? "ik.ga`^." : "naga",
                Float.parseFloat(price.getText()), Float.parseFloat(mrp.getText()), Float.parseFloat(selling.getText()))) {
            setItem();
            txtQty.requestFocus();
        }
    }

    void setItem(){
        if(item!=null)
        {
            txtBarcode.setText(item.getBarcode()==null ? "" : item.getBarcode());
            txtItemName.setText(item.getItemname());
            txtRate.setText(String.valueOf(item.getPrice()));
            float saved = item.getSailingprice();
            if(saved>0){
                txtSellingRate.setText(String.valueOf(saved));
            } else {
                txtSellingRate.setText(String.valueOf(round2(item.getPrice()*1.10f)));
            }
            if(item.getUnit().equals("ik.ga`^."))
               cmbUnit.getSelectionModel().select(0);
            else
                cmbUnit.getSelectionModel().select(1);
            refreshSellingRateLabel();
        }
    }
    private String sellingRateKey(String barcode, String itemname){
        return (barcode==null?"":barcode.trim())+"|"+(itemname==null?"":itemname.trim());
    }
    private float round2(float v){
        return Math.round(v*100f)/100f;
    }
    private void autoFillSellingRateFromRate(){
        if(!isNumber(txtRate.getText())) return;
        float rate = Float.parseFloat(txtRate.getText());
        if(rate<=0) return;
        boolean savedSellingRateExists = item!=null && item.getSailingprice()>0;
        Float capturedSelling = sellingRateMap.get(sellingRateKey(txtBarcode.getText(), txtItemName.getText()));
        boolean capturedSellingExists = capturedSelling!=null && capturedSelling>0;
        if(savedSellingRateExists || capturedSellingExists) return;
        txtSellingRate.setText(String.valueOf(round2(rate*1.10f)));
    }
    private void refreshSellingRateLabel(){
        if(lblSellingRate==null) return;
        if(!isNumber(txtRate.getText()) || !isNumber(txtSellingRate.getText())){
            lblSellingRate.setStyle("");
            return;
        }
        float purchase = Float.parseFloat(txtRate.getText());
        float sell = Float.parseFloat(txtSellingRate.getText());
        if(purchase>0 && sell>0 && sell < purchase*1.10f){
            lblSellingRate.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
        } else {
            lblSellingRate.setStyle("");
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
        // MRP is optional: empty / not a number counts as 0 (it used to throw and ADD did nothing)
        tr.setMrp(isNumber(txtMrp.getText()) ? Float.valueOf(txtMrp.getText()) : 0.0f);
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
