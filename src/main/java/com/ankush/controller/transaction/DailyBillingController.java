package com.ankush.controller.transaction;

import com.ankush.controller.print.PrintBill;
import com.ankush.controller.print.PrintExample;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.data.entities.*;
import com.ankush.data.service.*;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Component
public class DailyBillingController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPaneFavorite;

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
    @FXML private Button btnSalesReturn;
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
    @FXML private TableColumn<Bill,Float> colBillAmt;
    @FXML private TableColumn<Bill,Float> colPaid;
    @FXML private TableColumn<Bill,Float> colDue;
    @FXML private TableColumn<Bill,String> colStatus;
    @FXML private TableColumn<Bill,String> colCustomer;

    @FXML private DatePicker dateSearch;
    @FXML private Button btnWeek;
    @FXML private Button btnMonth;
    @FXML private Button btnAll;

    //for itemName search box
    private AutoCompleteTextField autoCompleteItemName;
    private AutoCompleteTextField autoCompleteCustomerName;
    private AutoCompleteTextField autoCompleteBank;
    @Autowired ItemService itemService;
    private  Item item;
    ///////////////////
    @Autowired private ItemStockService stockService;
    @Autowired private AppSettingsService appSettingsService;
    @Autowired private AlertNotification alert;
    @Autowired private BankService bankService;
    @Autowired private CustomerService customerService;
    @Autowired private BillService billService;
    @Autowired private LoginService loginService;
    //@Autowired private PrintBill printbill;
    @Autowired BankTransactionService bankTrService;
    @Autowired ShopeeInfoService shopeeInfoService;
    @Autowired FavoriteCategoryService favoriteCategoryService;
    @Autowired BackupService backupService;
    @Autowired @Lazy SalesReturnController salesReturnController;

    private ObservableList<String>itemNameList = FXCollections.observableArrayList();
    private ObservableList<Transaction>trList = FXCollections.observableArrayList();
    private ObservableList<Bill>billList =FXCollections.observableArrayList();
    private Long billno;
    private PrintBill printbill;

    private ComboBox<String> cmbCategory;
    private static final String ALL_CATEGORIES = "All Categories";
    private static final int SALES_RANK_DAYS = 90;
    // set while code updates txtBarcode / txtQty so their listeners don't undo the change
    private boolean suppressBarcodeListener = false;
    private boolean updatingFromAmount = false;
    /** Row loaded with UPDATE; ADD of the same item replaces it. -1 when adding a new line. */
    private int editingIndex = -1;
    /** Customer id 1 is the default walk-in (cash) customer. */
    private static final int WALK_IN_CUSTOMER_ID = 1;

    // Counters: each one holds a bill in progress, saved line by line in temptransaction.
    // Editing a saved bill is a separate mode that never touches the counters.
    @FXML private HBox counterBar;
    @FXML private Label lblEditBanner;
    @Autowired private BillingCounterService counterService;
    /** counter whose bill is on screen; null only while no counter exists */
    private Integer currentCounter;
    /** true while a saved bill (billno) is open with UPDATE; the counters are locked meanwhile */
    private boolean editMode = false;
    /** counter to go back to when the edit is saved or cancelled */
    private Integer counterBeforeEdit;
    /** set while the screen is filled from the database, so nothing is written back */
    private boolean loadingCounter = false;
    /** item name -> quantity the bill being edited took from stock (given back when it is saved) */
    private final Map<String, Float> editedBillQty = new java.util.HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        trList.clear();
        billList.clear();
        billno = null;
        createTable();
        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billno"));
        colBillAmt.setCellValueFactory(new PropertyValueFactory<>("grandtotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colDue.setCellValueFactory(cellData -> {
            Bill b = cellData.getValue();
            float due = (b.getGrandtotal() != null ? b.getGrandtotal() : 0) - (b.getPaid() != null ? b.getPaid() : 0);
            return new javafx.beans.property.SimpleObjectProperty<>(due > 0 ? due : 0f);
        });
        colStatus.setCellValueFactory(cellData -> {
            Bill b = cellData.getValue();
            float grand = b.getGrandtotal() != null ? b.getGrandtotal() : 0;
            float paid = b.getPaid() != null ? b.getPaid() : 0;
            if (paid >= grand) return new SimpleStringProperty("PAID");
            else if (paid > 0) return new SimpleStringProperty("PARTIAL");
            else return new SimpleStringProperty("UNPAID");
        });
        colCustomer.setCellValueFactory(cellData -> {
            Bill b = cellData.getValue();
            float grand = b.getGrandtotal() != null ? b.getGrandtotal() : 0;
            float paid = b.getPaid() != null ? b.getPaid() : 0;
            if (paid < grand && b.getCustomer() != null) {
                return new SimpleStringProperty(b.getCustomer().getName());
            }
            return new SimpleStringProperty("");
        });
        // Color rows based on payment status
        tableOld.setStyle("-fx-font-size: 14;");
        tableOld.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Bill bill, boolean empty) {
                super.updateItem(bill, empty);
                if (empty || bill == null) {
                    setStyle("");
                } else {
                    float grand = bill.getGrandtotal() != null ? bill.getGrandtotal() : 0;
                    float paid = bill.getPaid() != null ? bill.getPaid() : 0;
                    if (paid >= grand) {
                        setStyle("-fx-background-color: #E8F5E9;"); // green - paid
                    } else if (paid > 0) {
                        setStyle("-fx-background-color: #FFF8E1;"); // yellow - partial
                    } else {
                        setStyle("-fx-background-color: #FFEBEE;"); // red - unpaid
                    }
                }
            }
        });
        //billList.addAll(billService.getBillByDate(LocalDate.now()));
       // billList.addAll(billService.getAllBills());
        tableOld.setItems(billList);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                billList.addAll(billService.getBillByDate(LocalDate.now()));
                return null;
            }
        };
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(task);
        service.shutdown();
        printbill = new PrintBill();
        List<ShopeeInfo> shopeeInfoList = shopeeInfoService.getAllShopeeInfo();
        if (!shopeeInfoList.isEmpty()) {
            printbill.setShopeeInfo(shopeeInfoList.get(0));
        }
        date.setValue(LocalDate.now());
        dateSearch.setValue(LocalDate.now());
        Optional<Bank> defaultBank = bankService.getBankByIdOptional(1);
        if(defaultBank.isEmpty()){
            alert.showError("Please add a Bank before using Billing. Go to Create > Add Bank.");
            Platform.runLater(()->stageManager.switchScene(FxmlView.HOME));
            return;
        }
        autoCompleteBank = new AutoCompleteTextField(txtBank, bankService.getAllBankNames(), Font.font("System Bold", 14), btnSave);
        autoCompleteBank.setUseContainsFilter(true);
        autoCompleteBank.setText(defaultBank.get().getBankname());
        billno= null;
        cmbUnit.getItems().addAll("KG","NOS");
        autoCompleteCustomerName = new AutoCompleteTextField(txtCustomerName, customerService.getAllCustomerNames(), Font.font("System Bold", 14), txtCustomerMobile);
        autoCompleteCustomerName.setUseContainsFilter(true);
        autoCompleteCustomerName.setOnSelectionCallback(selectedName -> {
            Customer c = customerService.getCustomerByName(selectedName);
            if (c != null && c.getContact() != null) {
                txtCustomerMobile.setText(c.getContact());
            }
        });
        autoCompleteItemName = new AutoCompleteTextField(txtItemName, new ArrayList<>(), Font.font("Kiran", 20), txtQty);
        // match anywhere in the name, plus English search name / short code, popular items first
        autoCompleteItemName.setUseContainsFilter(true);
        autoCompleteItemName.setOnSelectionCallback(selectedName -> {
            Item selected = itemService.getItemByName(selectedName);
            if(selected != null) item = selected;
            setItem(item);
        });
        createItemList();
        refreshItemSearch();
        txtBarcode.setOnAction(e->barcodeAction());
        txtBarcode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(suppressBarcodeListener) return;
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
                else if(!updatingFromAmount)
                {
                    if(!txtRate.getText().isEmpty() &&
                            !txtRate.getText().equals(""+0.0f)&&
                            !txtQty.getText().isEmpty()
                            )
                    {
                        if(isNumeric(txtQty.getText()) && isNumeric(txtRate.getText()))
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
                    if(!txtRate.getText().isEmpty() &&
                            !txtRate.getText().equals(""+0.0f)&&
                            !txtQty.getText().isEmpty()
                    )
                    {
                        if(isNumeric(txtQty.getText()) && isNumeric(txtRate.getText()))
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
                    // also when cleared: empty counts as 0 (used to keep the old charge in the grand total)
                    calculateGrandTotal();
                }
            }
        });
        txtRecived.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if(isFocused) Platform.runLater(()->txtRecived.selectAll());
        });
        txtRecived.setOnAction(e->{
            btnSave.requestFocus();
            btnSave.fire();
        });
        txtRecived.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtRecived.setText(oldValue);
                }
                else
                {
                    if(!txtRecived.getText().isEmpty() &&
                            !txtRecived.getText().equals(""+0.0f)&&
                                    isNumeric(txtRecived.getText()))
                    {
                        if(isNumeric(txtRecived.getText()) && isNumeric(txtGrandTotal.getText()))
                            txtRemaining.setText(
                                    String.valueOf(Float.parseFloat(txtRecived.getText())-Float.parseFloat(txtGrandTotal.getText()))
                            );
                    }
                }
            }
        });
        txtRate.setOnAction(e->{
            if(!txtRate.getText().isBlank() &&
                    isNumeric(txtRate.getText())&&
                    Float.parseFloat(txtRate.getText())>0.0f)
            {
                add();
            }
        });
        txtQty.setOnAction(e->{
            if(isNumeric(txtQty.getText())&& isNumeric(txtRate.getText()))
            {
                txtAmount.setText(
                        String.valueOf(Float.parseFloat(txtRate.getText())*Float.parseFloat(txtQty.getText()))
                );
                // quick add: rate already known from item master, so add the line straight away
                if(appSettingsService.isQuickAddEnabled() && Float.parseFloat(txtRate.getText())>0.0f) {
                    add();
                } else {
                    txtRate.requestFocus();
                }
            }
        });
        // Loose items: type the amount (e.g. 50) and the quantity is worked out from the rate
        txtAmount.textProperty().addListener((obs, oldValue, newValue) -> {
            // qty x rate is written here as a float string, so allow any number of decimals
            if (!newValue.matches("\\d{0,100}([\\.]\\d*)?")) {
                txtAmount.setText(oldValue);
            }
        });
        txtAmount.setOnAction(e -> amountAction());
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) remove();
        });
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
        btnAdd.setOnAction(e->add());
        btnRemove.setOnAction(e->remove());
        btnClear.setOnAction(e->clear());
        btnUpdate.setOnAction(e->update());
        dateSearch.setOnAction(e->{
            if(dateSearch.getValue()!=null)
            {
                billList.clear();
                billList.addAll(billService.getBillByDate(dateSearch.getValue()));
            }
        });
        btnWeek.setOnAction(e->{
            if(dateSearch.getValue()!=null) {
                billList.clear();
                billList.addAll(billService.findByDateBetween(dateSearch.getValue().with(previousOrSame(MONDAY)), dateSearch.getValue().with(nextOrSame(SUNDAY))));
            }
        });
        btnMonth.setOnAction(e->{
            if(dateSearch.getValue()!=null) {
                billList.clear();
                billList.addAll(billService.findByDateBetween(dateSearch.getValue().withDayOfMonth(1), dateSearch.getValue().withDayOfMonth(dateSearch.getValue().lengthOfMonth())));
            }
        });
        btnAll.setOnAction(e->{
            billList.clear();
            billList.addAll(billService.getAllBills());
        });
        btnSave.setOnAction(e->save());
        btnClear2.setOnAction(e->newBill());
        btnUpdate2.setOnAction(e->update2());
        btnHome.setOnAction(e->{
            javafx.stage.Stage win = (javafx.stage.Stage) btnHome.getScene().getWindow();
            if(win == stageManager.getPrimaryStage()) {
                stageManager.switchScene(FxmlView.HOME);
            } else {
                win.close();
            }
        });
        if(btnSalesReturn != null) {
            btnSalesReturn.setOnAction(e -> openSalesReturnForSelectedBill());
        }
        btnPrint.setOnAction(e->{
            if(tableOld.getSelectionModel().getSelectedItem()==null) {

                printbill.setBill(billService.getBillByBillNo(billService.getLastBill()));
                new PrintExample("D:\\Shopee\\bill.pdf");
                return;
            }
            //printbill = new PrintBill();
            printbill.setBill(billService.getBillByBillNo(tableOld.getSelectionModel().getSelectedItem().getBillno()));
            new PrintExample("D:\\Shopee\\bill.pdf");
        });
        itemSearch();
        initCounters();
    }

    // ---------------------------------------------------------------- counters

    private void initCounters() {
        editMode = false;
        editedBillQty.clear();
        counterBeforeEdit = null;
        currentCounter = null;
        showEditBanner(false);
        // customer details typed on a counter are kept with it (lines are kept on every change)
        for (TextField f : List.of(txtCustomerName, txtCustomerMobile, txtOther)) {
            f.focusedProperty().addListener((obs, was, is) -> { if (!is) saveDraftHeader(); });
        }
        rootPane.sceneProperty().addListener((obs, o, scene) -> {
            if (scene == null) return;
            scene.windowProperty().addListener((o2, w0, w) -> {
                // counters may have been added or changed in the master meanwhile
                if (w != null) w.focusedProperty().addListener((o3, was, is) -> { if (is) refreshCounterBar(); });
            });
        });
        counterService.ensureDefaultCounter();
        List<BillingCounter> counters = counterService.getActiveCounters();
        if (counters.isEmpty()) {
            alert.showError("No active billing counter. Add one in Create > Billing Counters.");
            refreshCounterBar();
            return;
        }
        Map<Integer, double[]> pending = counterService.getPendingSummary();
        Integer first = counters.stream().map(BillingCounter::getCounterno)
                .filter(pending::containsKey).findFirst().orElse(counters.get(0).getCounterno());
        loadCounter(first);
        showPendingBillsPopup(pending);
    }

    /** On opening billing: lists the bills left in progress on the counters (also after a restart). */
    private void showPendingBillsPopup(Map<Integer, double[]> pending) {
        if (pending.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        for (BillingCounter c : counterService.getAllCounters()) {
            double[] p = pending.get(c.getCounterno());
            if (p == null) continue;
            sb.append(String.format(java.util.Locale.ROOT, "%s (C%d):  %d items,  Rs. %.2f", c.getName(), c.getCounterno(), (int) p[0], p[1]));
            if (c.getCustomername() != null && !c.getCustomername().isBlank()) sb.append("  -  ").append(c.getCustomername());
            sb.append('\n');
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Pending Bills");
        info.setHeaderText("Bills not finished yet are waiting on these counters");
        info.setContentText(sb + "\nClick a counter at the top (or F5 / F6, Ctrl+1..9) to continue it.");
        Platform.runLater(info::show);
    }

    /** Puts the counter's bill in progress on screen. Writes nothing. */
    private void loadCounter(Integer counterno) {
        BillingCounter counter = counterService.getByCounterno(counterno);
        loadingCounter = true;
        try {
            clear();
            clear2();
            currentCounter = counterno;
            for (TempTransaction t : counterService.getLines(counterno)) {
                trList.add(Transaction.builder()
                        .itemname(t.getItemname()).barcode(t.getBarcode()).unit(t.getUnit())
                        .quantity(t.getQuantity()).rate(t.getRate()).price(t.getPrice())
                        .amount(t.getAmount()).sailingprice(t.getSailingprice()).build());
            }
            if (counter != null) {
                autoCompleteCustomerName.setText(counter.getCustomername() == null ? "" : counter.getCustomername());
                txtCustomerMobile.setText(counter.getCustomermobile() == null ? "" : counter.getCustomermobile());
                txtOther.setText(String.valueOf(counter.getOthercharges() == null ? 0.0f : counter.getOthercharges()));
            }
            validateTrList();
        } finally {
            loadingCounter = false;
        }
        refreshCounterBar();
        Platform.runLater(() -> txtBarcode.requestFocus());
    }

    private void switchCounter(Integer counterno) {
        if (editMode) {
            alert.showError("Finish editing Bill " + billno + " first: SAVE it, or CLEAR / F8 to cancel");
            return;
        }
        if (counterno == null || counterno.equals(currentCounter)) return;
        saveDraftHeader();
        loadCounter(counterno);
    }

    /** F5 / F6: previous / next active counter. */
    private void stepCounter(int step) {
        List<BillingCounter> counters = counterService.getActiveCounters();
        if (counters.isEmpty()) return;
        int pos = 0;
        for (int i = 0; i < counters.size(); i++)
            if (counters.get(i).getCounterno().equals(currentCounter)) pos = i;
        pos = Math.floorMod(pos + step, counters.size());
        switchCounter(counters.get(pos).getCounterno());
    }

    /** Writes the bill on screen to its counter (lines + customer). Never during an edit of a saved bill. */
    private void persistCounter() {
        if (editMode || loadingCounter || currentCounter == null) return;
        List<TempTransaction> lines = new ArrayList<>();
        for (Transaction tr : trList) {
            lines.add(TempTransaction.builder()
                    .itemname(tr.getItemname()).barcode(tr.getBarcode()).unit(tr.getUnit())
                    .quantity(tr.getQuantity()).rate(tr.getRate()).price(tr.getPrice())
                    .amount(tr.getAmount()).sailingprice(tr.getSailingprice()).build());
        }
        try {
            counterService.replaceLines(currentCounter, lines);
            saveDraftHeader();
        } catch (Exception e) {
            alert.showError("Could not keep the bill on counter " + currentCounter + ": " + e.getMessage());
            e.printStackTrace();
        }
        refreshCounterBar();
    }

    private void saveDraftHeader() {
        if (editMode || loadingCounter || currentCounter == null) return;
        try {
            counterService.saveDraft(currentCounter, autoCompleteCustomerName.getText(), txtCustomerMobile.getText(),
                    isNumeric(txtOther.getText()) ? Float.parseFloat(txtOther.getText()) : 0.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** One button per active counter: number, item count and total; the current one highlighted. */
    private void refreshCounterBar() {
        if (counterBar == null) return;
        List<BillingCounter> counters = counterService.getActiveCounters();
        Map<Integer, double[]> pending = counterService.getPendingSummary();
        counterBar.getChildren().clear();
        int index = 0;
        for (BillingCounter c : counters) {
            index++;
            Integer no = c.getCounterno();
            double[] p = pending.get(no);
            Button b = new Button(p == null ? "C" + no : String.format(java.util.Locale.ROOT, "C%d  %d | %.0f", no, (int) p[0], p[1]));
            b.setUserData(no);
            b.setMnemonicParsing(false);
            String bg = no.equals(currentCounter) ? "#0D47A1" : (p != null ? "#EF6C00" : "#78909C");
            b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;"
                    + " -fx-background-radius: 5; -fx-background-insets: 0;"
                    + (no.equals(currentCounter) ? " -fx-border-color: #FFD600; -fx-border-width: 2; -fx-border-radius: 5;" : ""));
            b.setTooltip(new Tooltip(c.getName() + (index <= 9 ? "  (Ctrl+" + index + ")" : "")));
            b.setDisable(editMode);
            b.setOnAction(e -> switchCounter(no));
            counterBar.getChildren().add(b);
        }
    }

    private void showEditBanner(boolean show) {
        if (lblEditBanner == null) return;
        lblEditBanner.setVisible(show);
        lblEditBanner.setManaged(show);
        if (show) lblEditBanner.setText("EDITING BILL " + billno + "  -  SAVE or CLEAR (F8) to cancel");
    }

    /** Leaves the edit of a saved bill and goes back to the counter that was open. */
    private void exitEditMode() {
        editMode = false;
        billno = null;
        editedBillQty.clear();
        showEditBanner(false);
        Integer back = counterBeforeEdit;
        counterBeforeEdit = null;
        if (back == null || counterService.getByCounterno(back) == null) {
            List<BillingCounter> counters = counterService.getActiveCounters();
            back = counters.isEmpty() ? null : counters.get(0).getCounterno();
        }
        if (back != null) loadCounter(back);
        else { clear(); clear2(); refreshCounterBar(); }
    }

    /** CLEAR / F8: cancel the edit of a saved bill, or discard the bill on the current counter. */
    private void newBill() {
        if (editMode) {
            if (alert.showConfirmation("Cancel Edit", "Cancel editing Bill " + billno + "? Changes are not saved.")) {
                exitEditMode();
            }
            return;
        }
        if (!trList.isEmpty() && !alert.showConfirmation("New Bill",
                "Discard the bill on counter " + currentCounter + " and start a new one?")) {
            return;
        }
        if (currentCounter != null) counterService.clearCounter(currentCounter);
        clear();
        clear2();
        refreshCounterBar();
        txtBarcode.requestFocus();
    }

    private void itemSearch() {
    }

    private void update2() {
        if(tableOld.getSelectionModel().getSelectedItem()==null)
            return;
        if(editMode) {
            alert.showError("Already editing Bill " + billno + ": SAVE it, or CLEAR / F8 to cancel");
            return;
        }
        Bill bill = billService.getBillByBillNo(tableOld.getSelectionModel().getSelectedItem().getBillno());
        // the counter's bill stays saved in temptransaction; edit mode never writes there
        saveDraftHeader();
        counterBeforeEdit = currentCounter;
        editMode = true;
        clear();
        clear2();
        trList.addAll(bill.getTransactions());
        editedBillQty.clear();
        for (Transaction t : bill.getTransactions()) {
            Item current = itemService.findCurrentItem(t.getItemname(), t.getBarcode());
            editedBillQty.merge(current != null ? current.getItemname() : t.getItemname(), t.getQuantity(), Float::sum);
        }
        // load the whole bill, not only its lines: saving used to turn a credit bill into a fully paid
        // walk-in bill (customer, paid and other charges were lost) and move it to today's date
        if(bill.getDate()!=null) date.setValue(bill.getDate());
        txtOther.setText(String.valueOf(bill.getOthercharges()==null ? 0.0f : bill.getOthercharges()));
        Customer cust = bill.getCustomer();
        if(cust!=null && cust.getId()!=WALK_IN_CUSTOMER_ID) {
            autoCompleteCustomerName.setText(cust.getName());
            txtCustomerMobile.setText(cust.getContact()==null ? "" : cust.getContact());
            // walk-in bills keep received empty so they are saved fully paid again
            txtRecived.setText(String.valueOf(bill.getPaid()==null ? 0.0f : bill.getPaid()));
        }
        validateTrList();
      //  txtBank.setText(bill.getBank().getBankname());
//        Customer cust = bill.getCustomer();
//        if(cust.getId()!=1) {
//            txtCustomerMobile.setText(bill.getCustomer().getContact());
//            txtCustomerName.setText(bill.getCustomer().getName());
//        }
        billno=bill.getBillno();
        showEditBanner(true);
        refreshCounterBar();
    }

    private void clear2() {
        trList.clear();
        date.setValue(LocalDate.now());
        txtNetTotal.setText(String.valueOf(0.0f));
        txtOther.setText(String.valueOf(0.0f));
        txtGrandTotal.setText(String.valueOf(0.0f));
        txtRecived.setText(String.valueOf(0.0f));
        autoCompleteCustomerName.clear();
        txtMrp.setText(String.valueOf(0.0f));
        txtDiscount.setText(String.valueOf(0.0f));
        txtPayble.setText(String.valueOf(0.0f));
        txtRecived.setText(String.valueOf(0.0f));
        txtRemaining.setText(String.valueOf(0.0f));
        // empty, not "0.0": a new customer added on the next bill would be saved with mobile "0.0"
        txtCustomerMobile.clear();
        bankService.getBankByIdOptional(1).ifPresent(bank -> autoCompleteBank.setText(bank.getBankname()));
        billno=null;
        editingIndex=-1;
    }

    private void save() {
        try {
            if(!validateBill())return;
            Customer custom = checkCustomer();
            if(custom==null) {
                // "Customer Not Found - add?" answered No: a credit bill must not be saved without its customer
                alert.showError("Select an existing customer, or add the new one, before saving");
                autoCompleteCustomerName.requestFocus();
                return;
            }
            float paid=0.0f,remain=0.0f;
            float remainVal = isNumeric(txtRemaining.getText()) ? Float.parseFloat(txtRemaining.getText()) : 0.0f;
            if(remainVal<0.0f)
            {
                paid = isNumeric(txtRecived.getText()) ? Float.parseFloat(txtRecived.getText()) : 0.0f;
            }else paid = isNumeric(txtPayble.getText()) ? Float.parseFloat(txtPayble.getText()) : 0.0f;

            Bill bill = Bill.builder()
                    //.bank(bankService.getBankByBankname(txtBank.getText()))
                    .customer(custom)
                    .date(date.getValue())
                    .discount(Float.parseFloat(txtDiscount.getText()))
                    .grandtotal(Float.parseFloat(txtGrandTotal.getText()))
                    .login(loginService.getLoginByUserName("Admin"))
                    .nettotal(Float.parseFloat(txtNetTotal.getText()))
                    .othercharges(isNumeric(txtOther.getText()) ? Float.parseFloat(txtOther.getText()) : 0.0f)
                    .paid(paid)
                    .transactions(new ArrayList<Transaction>())
                    .build();

            if(editMode) {
                if(billno==null) throw new IllegalStateException("Edited bill has no bill number");
                bill.setBillno(billno);
            }
            for(Transaction tr:trList)
            {
                tr.setId(null);
                tr.setBill(bill);
                bill.getTransactions().add(tr);
            }
            if(!editMode)
            {
                // new bill from the current counter: bill, stock and clearing the counter in one DB transaction
                billService.saveCounterBill(bill, currentCounter);
                //Add in BankTransaction
//                addBankTransaction(BankTransaction.builder()
//                        //.bank(bill.getBank())
//                        .credit(bill.getPaid())
//                        .date(bill.getDate())
//                        .debit(0.0f)
//                        .particulars("Add Bill "+bill.getBillno())
//                        .transactionid(bill.getBillno())
//                        .build());
                clear();
                clear2();
                refreshCounterBar();
                alert.showSuccess("Bill Save Success");
                printbill.setBill(billService.getBillByBillNo(bill.getBillno()));
                if(shouldPrintAfterSave()) {
                    new PrintExample("D:\\Shopee\\bill.pdf");
                }
                loadTodaysBills();
                backupService.performAutoBackup();

            }
            else
            {
                // old lines' stock back, bill saved, new lines' stock out: one DB transaction
                billService.saveEditedBill(bill);
//                addBankTransaction(BankTransaction.builder()
//                        .. .bank(bill.getBank())
//                        .credit(bill.getPaid())
//                        .date(bill.getDate())
//                        .debit(0.0f)
//                        .particulars("Add Bill "+bill.getBillno())
//                        .transactionid(bill.getBillno())
//                        .build());
                alert.showSuccess("Bill  Updated Success");
                printbill.setBill(billService.getBillByBillNo(bill.getBillno()));
                // back to the counter that was open before the edit, its bill untouched
                exitEditMode();
                if(shouldPrintAfterSave()) {
                    new PrintExample("D:\\Shopee\\bill.pdf");
                }
                loadTodaysBills();
                backupService.performAutoBackup();
            }

        }catch(Exception e){
        alert.showError("Error in Saving Bill "+e.getMessage());
        e.printStackTrace();
        // nothing was saved (one DB transaction): give the lines their Sr. No. back, they stay on the counter
        validateTrList();
        }
    }

    private Customer checkCustomer() {
        Customer customer =null;
        if(autoCompleteCustomerName.getText().isEmpty() || autoCompleteCustomerName.getText().isBlank()|| autoCompleteCustomerName.getText().equals("")||autoCompleteCustomerName.getText().equals(null))
        {
            return customerService.getById(1);
        }
        customer=customerService.getCustomerByName(autoCompleteCustomerName.getText());
        if(customer!=null)
        {
            return customer;
        }
        else if(alert.showConfirmation("Customer Not Found","Do You Want To Add?"))
        {
            customer = Customer.builder()
                    .email("-")
                    .contact(txtCustomerMobile.getText())
                    .name(autoCompleteCustomerName.getText())
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
    private boolean validateBill() {
        if (trList.size() == 0) {
            alert.showError("No Data to Save");
           // printbill.setBill(billService.getBillByBillNo(31));
            //printbill.createDoc();
            return false;
        }
        if(date.getValue()==null)
        {
            alert.showError("Select Date");
            date.requestFocus();
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
            alert.showError("Select Bank Again");
            autoCompleteBank.requestFocus();
            return false;
        }
        if(autoCompleteCustomerName.getText().isEmpty())
        {
            float recivedAmt = isNumeric(txtRecived.getText()) ? Float.parseFloat(txtRecived.getText()) : 0.0f;
            if(recivedAmt<=0.0f)
            {
                // walk-in cash customer with no amount typed: treat the bill as fully paid
                txtRecived.setText(txtGrandTotal.getText());
                return true;
            }
            float remainingAmt = isNumeric(txtRemaining.getText()) ? Float.parseFloat(txtRemaining.getText()) : 0.0f;
            if(remainingAmt<0.0f)
            {
                alert.showError("Received amount is less than bill total. Select Customer Name to give credit");
                txtRecived.requestFocus();
                return false;
            }
            return true;
        }
        if(!autoCompleteCustomerName.getText().isEmpty() && txtCustomerMobile.getText().isEmpty())
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
        int index = table.getSelectionModel().getSelectedIndex();
        txtBarcode.setText(tr.getBarcode());
        txtItemName.setText(tr.getItemname());
        txtRate.setText(String.valueOf(tr.getSailingprice()));
        txtQty.setText(String.valueOf(tr.getQuantity()));
        txtAmount.setText(String.valueOf(tr.getAmount()));
        if(tr.getUnit().equals("naga")) cmbUnit.getSelectionModel().select(1);
        else cmbUnit.getSelectionModel().select(0);
        // the barcode listener doesn't find lines without a barcode (or of a renamed item): ADD needs the item
        item = itemService.findCurrentItem(tr.getItemname(), tr.getBarcode());
        editingIndex = index;
    }

    private void clear() {
        System.out.println("Clear");
        txtBarcode.setText("");
        txtItemName.setText("");
        cmbUnit.getSelectionModel().clearSelection();
        txtQty.setText("");
        txtRate.setText("");
        txtAmount.setText("");
        editingIndex=-1;
    }

    private void remove() {
        if(table.getSelectionModel().getSelectedItem()==null)
            return;
        else {
            trList.remove(table.getSelectionModel().getSelectedItem());
            editingIndex=-1;
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
            // an inactive item reached by barcode / code: it is back in the shop, so ask and make it active again
            if(item != null && !item.isActiveItem()) {
                if(!alert.showConfirmation("Inactive Item",
                        "This item is marked inactive (not sold any more).\nBill it and make it active again?")) {
                    return;
                }
                item.setActive(true);
                itemService.saveItem(item);
                createItemList();
                refreshItemSearch();
            }
                //checking stock availability;
            if(appSettingsService.isStockCheckEnabled()) {
                ItemStock stock = stockService.getItemStockByItemname(item.getItemname());
                if (stock == null) {
                    Alert stockAlert = new Alert(Alert.AlertType.ERROR);
                    stockAlert.setTitle("Stock Not Available");
                    stockAlert.setHeaderText("Item is not available in stock");
                    Label itemLabel = new Label(txtItemName.getText());
                    itemLabel.setFont(Font.font("Kiran", 20));
                    stockAlert.getDialogPane().setContent(itemLabel);
                    stockAlert.showAndWait();
                    return;
                }
                // count what this bill already has of the item (except the line being edited), and when editing
                // a saved bill, the quantity it took from stock that saving gives back first
                float onBill = 0.0f;
                for (int i = 0; i < trList.size(); i++) {
                    if (i != editingIndex && item.getItemname().equals(trList.get(i).getItemname()))
                        onBill += trList.get(i).getQuantity();
                }
                float available = (stock.getStock() == null ? 0.0f : stock.getStock())
                        + (editMode ? editedBillQty.getOrDefault(item.getItemname(), 0.0f) : 0.0f);
                if (available < onBill + Float.parseFloat(txtQty.getText())) {
                    alert.showError("Less Quantity Available " + (available - onBill) + " " + cmbUnit.getValue());
                    return;
                }
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
                    .amount(round2(Float.parseFloat(txtAmount.getText()))).build();

            if(editingIndex>=0 && editingIndex<trList.size()
                    && trList.get(editingIndex).getItemname().equals(tr.getItemname())) {
                // line loaded with UPDATE: replace it, merging used to add the new qty on top of the old one
                trList.set(editingIndex, tr);
            } else {
                addInTrList(tr);
            }
            editingIndex=-1;
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
                trList.get(index).setAmount(round2(trList.get(index).getQuantity()*trList.get(index).getSailingprice()));
                table.refresh();
            }
        }catch(Exception e)
        {
            alert.showError("Error in Adding List");
        }
    }

    private void validateTrList() {

        long id=0;
        double sum=0;
        float mrp=0;
        for(Transaction trs:trList)
        {
            trList.get(trList.indexOf(trs)).setId(++id);
            sum+=trs.getAmount();
            // many client items have MRP (rate) 0 or below the selling price; count such lines at selling price
            mrp+=(Math.max(trs.getRate(), trs.getSailingprice())*trs.getQuantity());
        }
        // rounded: the float sum showed e.g. 3213.1401 on screen and on the saved bill
        float net = round2((float) sum);
        txtNetTotal.setText(String.valueOf(net));
        // two decimals: float subtraction showed e.g. 15.399414 (Locale.ROOT keeps '.' for Float.parseFloat on save)
        txtMrp.setText(String.format(java.util.Locale.ROOT, "%.2f", mrp));
        txtDiscount.setText(String.format(java.util.Locale.ROOT, "%.2f", mrp - net));
        calculateGrandTotal();
        // every change of the lines (add, merge, edit, remove) is kept on the counter right away
        persistCounter();
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

    /**
     * Enter in the Barcode/Code box. Accepts a scanned barcode, an item short code ("101"),
     * or quantity*code ("3*101" / "1.5*101") which adds the line in one step.
     */
    private void barcodeAction() {
        String text = txtBarcode.getText() == null ? "" : txtBarcode.getText().trim();
        if(text.isEmpty()) {
            txtItemName.requestFocus();
            return;
        }
        String code = text;
        Float qty = null;
        int star = text.indexOf('*');
        if(star > 0) {
            String qtyText = text.substring(0, star).trim();
            code = text.substring(star + 1).trim();
            if(!isNumeric(qtyText) || Float.parseFloat(qtyText) <= 0.0f) {
                alert.showError("Wrong quantity: " + qtyText);
                txtBarcode.selectAll();
                return;
            }
            qty = Float.parseFloat(qtyText);
        }
        Item found = findItemByCode(code);
        if(found == null) {
            alert.showError("No item found for code " + code);
            txtBarcode.selectAll();
            return;
        }
        setItem(found);
        if(qty != null && found.getSailingprice() > 0.0f) {
            txtQty.setText(String.valueOf(qty));
            add();
        } else {
            if(qty != null) txtQty.setText(String.valueOf(qty));
            Platform.runLater(() -> {
                txtQty.requestFocus();
                txtQty.selectAll();
            });
        }
    }

    private Item findItemByCode(String code) {
        try {
            Item found = itemService.getItemByBarcode(code);
            if(found != null) return found;
        } catch(Exception e) {
            // same barcode on several items; fall through to short code
            e.printStackTrace();
        }
        return itemService.getItemByShortCode(code);
    }

    private void setItem(Item item)
    {
        if(item==null) return;
        this.item = item;
        txtItemName.setText(item.getItemname());
        txtRate.setText(""+item.getSailingprice());
        suppressBarcodeListener = true;
        try {
            txtBarcode.setText(item.getBarcode() == null ? "" : item.getBarcode());
        } finally {
            suppressBarcodeListener = false;
        }

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
        tabPaneFavorite.getTabs().clear();

        // "Top sellers" tab: filled automatically from recent sales, double-click to bill
        Tab topTab = new Tab("TOP");
        topTab.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
        ListView<String> topList = new ListView<>();
        topList.setStyle("-fx-font:18pt \"Kiran\"");
        topList.getItems().addAll(itemService.getTopSellingNames(30, 40));
        topList.setPlaceholder(new Label("No sales in the last 30 days"));
        attachPickHandler(topList);
        topTab.setContent(anchorFill(topList));
        topTab.setClosable(false);
        tabPaneFavorite.getTabs().add(topTab);

        // "All Items" tab with category filter
        Tab allTab = new Tab("sava-");
        allTab.setStyle("-fx-font:14pt \"Kiran\"");
        listItem = new ListView<>();
        listItem.setStyle("-fx-font:18pt \"Kiran\"");
        attachPickHandler(listItem);
        cmbCategory = new ComboBox<>();
        cmbCategory.getItems().add(ALL_CATEGORIES);
        cmbCategory.getItems().addAll(itemService.getCategories());
        cmbCategory.setValue(ALL_CATEGORIES);
        cmbCategory.setMaxWidth(Double.MAX_VALUE);
        cmbCategory.setStyle("-fx-font-size: 12pt;");
        cmbCategory.valueProperty().addListener((obs, o, n) -> refreshItemSearch());
        VBox allBox = new VBox(4, cmbCategory, listItem);
        VBox.setVgrow(listItem, Priority.ALWAYS);
        allTab.setContent(anchorFill(allBox));
        allTab.setClosable(false);
        tabPaneFavorite.getTabs().add(allTab);
        tabPaneFavorite.getSelectionModel().select(allTab);

        // Dynamic favorite category tabs
        List<FavoriteCategory> categories = favoriteCategoryService.getAllCategories();
        for(FavoriteCategory category : categories) {
            Tab tab = new Tab(category.getName());
            tab.setStyle("-fx-font:14pt \"Kiran\"");
            AnchorPane pane = new AnchorPane();
            ListView<String> catListView = new ListView<>();
            catListView.setStyle("-fx-font:18pt \"Kiran\"");

            Set<String> activeNames = new HashSet<>(itemService.getActiveItemNames());
            List<String> itemNames = favoriteCategoryService.getItemNamesByCategory(category.getId());
            // inactive items (no longer sold) are hidden here too
            itemNames.stream().filter(activeNames::contains).forEach(catListView.getItems()::add);
            attachHideMenu(catListView);

            AnchorPane.setTopAnchor(catListView, 0.0);
            AnchorPane.setBottomAnchor(catListView, 0.0);
            AnchorPane.setLeftAnchor(catListView, 0.0);
            AnchorPane.setRightAnchor(catListView, 0.0);
            pane.getChildren().add(catListView);
            catListView.setOnMouseClicked(e->{
                if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2){
                    String selectedName = catListView.getSelectionModel().getSelectedItem();
                    if(selectedName!=null) {
                        setItem(itemService.getItemByName(selectedName));
                        autoCompleteItemName.hidePopup();
                        Platform.runLater(()->txtQty.requestFocus());
                    }
                }
            });
            tab.setContent(pane);
            tab.setClosable(false);
            tabPaneFavorite.getTabs().add(tab);
        }
    }
    private AnchorPane anchorFill(javafx.scene.Node node) {
        AnchorPane pane = new AnchorPane(node);
        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        return pane;
    }

    private void attachPickHandler(ListView<String> list) {
        list.setOnMouseClicked(e->{
            if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2){
                String selectedName = list.getSelectionModel().getSelectedItem();
                if(selectedName==null) return;
                setItem(itemService.getItemByName(selectedName));
                autoCompleteItemName.hidePopup();
                Platform.runLater(()->txtQty.requestFocus());
            }
        });
        attachHideMenu(list);
    }

    /** Right-click on an item: hide it from billing and purchase because the shop no longer sells it. */
    private void attachHideMenu(ListView<String> list) {
        MenuItem hide = new MenuItem("Mark inactive (no longer sold)");
        hide.setOnAction(e -> {
            String name = list.getSelectionModel().getSelectedItem();
            Item selected = name == null ? null : itemService.getItemByName(name);
            if (selected == null) return;
            if (!alert.showConfirmation("Mark Inactive",
                    "Hide this item from billing and purchase search?\n(Nothing is deleted. Purchase it again or tick 'Active' in Item Master to bring it back.)")) {
                return;
            }
            itemService.deactivate(List.of(selected));
            createItemList();
            refreshItemSearch();
            alert.showSuccess("Item hidden from billing");
        });
        list.setContextMenu(new ContextMenu(hide));
    }

    /**
     * Reloads item suggestions: only active items, filtered by the selected category,
     * searchable by English name / short code and ranked by recent sales.
     */
    private void refreshItemSearch() {
        String category = cmbCategory == null ? null : cmbCategory.getValue();
        boolean allCategories = category == null || ALL_CATEGORIES.equals(category);
        List<String> names = itemService.getActiveItems().stream()
                .filter(i -> allCategories || category.equals(i.getCategory()))
                .map(Item::getItemname)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .collect(Collectors.toList());
        autoCompleteItemName.setSuggestions(names);
        autoCompleteItemName.setSearchKeys(itemService.getSearchKeys());
        autoCompleteItemName.setRankScores(itemService.getSaleCounts(SALES_RANK_DAYS));

        if(listItem != null) {
            List<String> listNames = names;
            if(appSettingsService.isStockCheckEnabled()) {
                Set<String> allowed = new HashSet<>(names);
                listNames = stockService.getItemNameByBarcode().stream()
                        .filter(allowed::contains)
                        .distinct()
                        .collect(Collectors.toList());
            }
            listItem.getItems().setAll(listNames);
        }
    }

    /** Enter in Amount: work out quantity from amount / rate (loose items sold by rupee value). */
    private void amountAction() {
        if(item == null || !isNumeric(txtAmount.getText()) || !isNumeric(txtRate.getText())) return;
        float amount = Float.parseFloat(txtAmount.getText());
        float rate = Float.parseFloat(txtRate.getText());
        if(amount <= 0.0f || rate <= 0.0f) return;
        float qty = Math.round((amount / rate) * 1000f) / 1000f;
        updatingFromAmount = true;
        try {
            txtQty.setText(String.valueOf(qty));
            txtAmount.setText(String.valueOf(amount));
        } finally {
            updatingFromAmount = false;
        }
        add();
    }

    private void handleShortcut(KeyEvent e) {
        KeyCode code = e.getCode();
        if(code == KeyCode.F2) {
            txtBarcode.requestFocus();
            txtBarcode.selectAll();
        } else if(code == KeyCode.F3) {
            txtItemName.requestFocus();
        } else if(code == KeyCode.F4) {
            txtRecived.requestFocus();
        } else if(code == KeyCode.F5) {
            stepCounter(-1);
        } else if(code == KeyCode.F6) {
            stepCounter(1);
        } else if(e.isControlDown() && code.isDigitKey() && code != KeyCode.DIGIT0 && code != KeyCode.NUMPAD0) {
            // Ctrl+1..9: n-th counter in the bar
            int n = Integer.parseInt(code.getName().replaceAll("\\D", ""));
            List<BillingCounter> counters = counterService.getActiveCounters();
            if(n <= counters.size()) switchCounter(counters.get(n - 1).getCounterno());
        } else if(code == KeyCode.F8) {
            newBill();
        } else if(code == KeyCode.F9 || (code == KeyCode.S && e.isControlDown())) {
            save();
        } else if(code == KeyCode.F10) {
            btnPrint.fire();
        } else {
            return;
        }
        e.consume();
    }

    private boolean shouldPrintAfterSave() {
        String mode = appSettingsService.getPrintMode();
        if(AppSettingsService.PRINT_ALWAYS.equals(mode)) return true;
        if(AppSettingsService.PRINT_NEVER.equals(mode)) return false;
        return alert.showConfirmation("Print Bill", "Do you want to print the bill?");
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
    void calculateGrandTotal()
    {
        if(!isNumeric(txtRecived.getText()))
            txtRecived.setText(String.valueOf(0.0));
        float netTotal = isNumeric(txtNetTotal.getText()) ? Float.parseFloat(txtNetTotal.getText()) : 0.0f;
        float other = isNumeric(txtOther.getText()) ? Float.parseFloat(txtOther.getText()) : 0.0f;
        txtGrandTotal.setText(String.valueOf(round2(netTotal + other)));
        float recived = isNumeric(txtRecived.getText()) ? Float.parseFloat(txtRecived.getText()) : 0.0f;
        float grandTotal = isNumeric(txtGrandTotal.getText()) ? Float.parseFloat(txtGrandTotal.getText()) : 0.0f;
        txtRemaining.setText(String.valueOf(round2(recived - grandTotal)));
        txtPayble.setText(txtGrandTotal.getText());
    }
    private static float round2(float v) {
        return Math.round(v * 100f) / 100f;
    }
    private void openSalesReturnForSelectedBill() {
        Bill selected = tableOld.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert.showError("Select a bill from the list to return");
            return;
        }
        Bill fullBill = billService.getBillByBillNo(selected.getBillno());
        salesReturnController.preselectBill(fullBill);
        stageManager.openInNewWindow(FxmlView.SALESRETURN);
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
