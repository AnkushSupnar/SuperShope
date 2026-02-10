package com.ankush.controller.report.salesreport;

import com.ankush.data.dto.DailySalesReport;
import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Transaction;
import com.ankush.data.service.BillService;
import com.ankush.data.service.ItemService;
import com.ankush.data.service.ItemStockService;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.view.AlertNotification;
import javafx.scene.text.Font;
import javafx.application.Platform;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class DailySalesReportController implements Initializable {
    @FXML private AnchorPane rootPane;
    @FXML private DatePicker date;
    @FXML private TextField txtItemName;
    @FXML private Button btnShow;
    @FXML private TableView<DailySalesReport> table;
    @FXML private TableColumn<DailySalesReport,Integer> colSr;
   // @FXML private TableColumn<DailySalesReport,LocalDate> colDate;
    @FXML private TableColumn<DailySalesReport,String> colItemName;
    @FXML private TableColumn<DailySalesReport,Float> colOpening;
    @FXML private TableColumn<DailySalesReport,Float> colSale;
    @FXML private TableColumn<DailySalesReport,Float> colPrice;
    @FXML private TableColumn<DailySalesReport,Float> colPriceTotal;
    @FXML private TableColumn<DailySalesReport,Float> colRate;
    @FXML private TableColumn<DailySalesReport,Float> colRateTotal;
    @FXML private TableColumn<DailySalesReport,Float> colMargine;
    //@FXML private ProgressBar progress;
    @FXML private TextField txtPurchase;
    @FXML private TextField txtSale;
    @FXML private TextField txtMargin;

    private AutoCompleteTextField autoCompleteItemName;
    @Autowired
    ItemService itemService;

    private ObservableList<DailySalesReport>saleList = FXCollections.observableArrayList();
    private List<Bill> billList  = new ArrayList<>();
    @Autowired
    BillService billService;
    @Autowired
    private ItemStockService stockService;
    @Autowired
    AlertNotification alert;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int i=0;
        date.setValue(LocalDate.now());
        colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
       // colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colOpening.setCellValueFactory(new PropertyValueFactory<>("openingstock"));
        colSale.setCellValueFactory(new PropertyValueFactory<>("totalsale"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPriceTotal.setCellValueFactory(new PropertyValueFactory<>("totalprice"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colRateTotal.setCellValueFactory(new PropertyValueFactory<>("totalrate"));
        colMargine.setCellValueFactory(new PropertyValueFactory<>("margine"));
        table.setItems(saleList);
        autoCompleteItemName = new AutoCompleteTextField(txtItemName, itemService.getAllItemNames(), Font.font("Kiran", 20));
        loadData(date.getValue());
        date.setOnAction(e->loadData(date.getValue()));
        btnShow.setOnAction(e->show());
    }
    private void loadData(LocalDate date)//travel billList
    {
        billList.clear();
        saleList.clear();
        table.refresh();
        txtMargin.setText(""+0.0f);
        txtSale.setText(""+0.0f);
        txtPurchase.setText(""+0.0f);

        billList.addAll(billService.getBillByDate(date));
        for(Bill bill:billList)
        {
            addInList(bill.getTransactions());
        }
    }
    void addInList(List<Transaction> trList)//travel tr List
    {
        for(Transaction tr:trList)
        {
        addInSaleList(tr);
        }
    }
    void addInSaleList(Transaction tr)
    {
        //addin SaleReport List
        if(saleList.size()==0)
        {
            saleList.add(
                    DailySalesReport.builder()
                            .id(saleList.size()+1)
                           // .date(tr.getBill().getDate())
                            .itemname(tr.getItemname())
                            .openingstock(stockService.getItemStockByItemname(tr.getItemname()).getStock()+tr.getQuantity())
                            .totalsale(tr.getQuantity())
                            .price(tr.getPrice())
                            .totalprice(tr.getQuantity()*tr.getPrice())
                            .rate(tr.getSailingprice())
                            .totalrate(tr.getQuantity()*tr.getSailingprice())
                            .margine((tr.getQuantity()*tr.getSailingprice())-(tr.getQuantity()*tr.getPrice()))
                            .build()
            );
        }
        else{
            int index=-1;
            for(DailySalesReport sale:saleList)
            {
                if(sale.getItemname().equals(tr.getItemname()) && sale.getRate()==tr.getSailingprice())
                {
                    index = saleList.indexOf(sale);
                    break;
                }
            }
            if(index==-1)
            {
                saleList.add(
                        DailySalesReport.builder()
                                .id(saleList.size()+1)
                               // .date(tr.getBill().getDate())
                                .itemname(tr.getItemname())
                                .openingstock(stockService.getItemStockByItemname(tr.getItemname()).getStock()+tr.getQuantity())
                                .totalsale(tr.getQuantity())
                                .price(tr.getPrice())
                                .totalprice(tr.getQuantity()*tr.getPrice())
                                .rate(tr.getSailingprice())
                                .totalrate(tr.getQuantity()*tr.getSailingprice())
                                .margine((tr.getQuantity()*tr.getSailingprice())-(tr.getQuantity()*tr.getPrice()))
                                .build()
                );

            }
            else
            {
                saleList.get(index).setTotalsale(saleList.get(index).getTotalsale()+tr.getQuantity());
                saleList.get(index).setTotalprice(
                        saleList.get(index).getTotalprice()+(tr.getQuantity()*tr.getPrice())
                );
                saleList.get(index).setTotalrate(
                        saleList.get(index).getTotalrate()+(tr.getQuantity()*tr.getSailingprice())
                );
                saleList.get(index).setMargine(
                        saleList.get(index).getTotalrate()-saleList.get(index).getTotalprice()
                );
                saleList.get(index).setOpeningstock(
                        saleList.get(index).getOpeningstock()+tr.getQuantity()
                );
            }
        }
        txtPurchase.setText(
                String.valueOf(Float.parseFloat(txtPurchase.getText())+(tr.getQuantity()*tr.getPrice()))
        );
        txtSale.setText(
                String.valueOf(Float.parseFloat(txtSale.getText())+(tr.getQuantity()*tr.getSailingprice()))
        );
        txtMargin.setText(
                String.valueOf(Float.parseFloat(txtSale.getText())-Float.parseFloat(txtPurchase.getText()))
        );
    }
    private void show()
    {
        if(txtItemName.getText().isEmpty()) {
            txtItemName.requestFocus();
            return;
        }
        loadData(date.getValue());
        DailySalesReport sale = null;
        for(DailySalesReport report:saleList)
        {
            if(report.getItemname().equals(txtItemName.getText()))
            {
                sale = report;
            }
        }
        if(sale!=null)
        {
            saleList.clear();
            saleList.add(sale);
            System.out.println("Remaining Stock ="+stockService.getItemStockByItemname(txtItemName.getText()));
        }

    }
}
