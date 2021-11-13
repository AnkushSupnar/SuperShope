package com.ankush.controller.dashboard;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Transaction;
import com.ankush.data.service.BillService;
import com.ankush.view.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
@Component
public class Dashboard implements Initializable {
    @Autowired
    @Lazy
    StageManager stageManager;
    @FXML private DatePicker date;
    @FXML private DatePicker dateMonth;

    @FXML
    private Tab tabMonthly;
    @FXML private Label lblTodayAmount;
    @FXML private Label lblTodaysBill;
    @FXML private Label lblTodayPurchase;
    @FXML private LineChart lineChartMonth;
    @FXML private Label lblTodayMargin;


    @FXML private Label lblMonthlyAmount;
    @FXML private Label lblMonthlyBills;
    @FXML private Label lblMonthlyMargin;
    @FXML private Label lblMonthlyPurchase;

    @FXML private LineChart lineChartYear;
    @FXML private Tab tabYearly;
    @FXML private Label lblYearAmount;
    @FXML private Label lblYearBills;
    @FXML private Label lblYearMargin;
    @FXML private PieChart pichartYear;
    ObservableList<PieChart.Data>yearPichartData = FXCollections.observableArrayList();


    @FXML
    private Label lblYearPurchase;

    @Autowired
    BillService billService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        date.setValue(LocalDate.now());
        dateMonth.setValue(LocalDate.now());
        tabMonthly.setOnSelectionChanged(e->{
            lineChartMonth.getData().clear();

            addMonthLineChart();
            lineChartMonth.setVisible(false);
            lineChartMonth.setVisible(true);
        });
        dateMonth.setOnAction(e->{
            lineChartMonth.getData().clear();

            addMonthLineChart();
            lineChartMonth.setVisible(false);
            lineChartMonth.setVisible(true);
        });
        tabYearly.setOnSelectionChanged(e->{
            lineChartYear.getData().clear();
            yearlyReport();
        });
        date.setOnAction(e->{
            lineChartYear.getData().clear();
            yearlyReport();
        });
            dayReport();

    }

    private void addMonthLineChart()  {
        LocalDate date = dateMonth.getValue();
        lblMonthlyAmount.setText(""+0.0f);
        lblMonthlyBills.setText(""+0);
        lblMonthlyMargin.setText(""+0.0f);
        lblMonthlyPurchase.setText(""+0.0f);
        XYChart.Series series = new XYChart.Series();

        for (date = date.withDayOfMonth(1); date.isBefore(date.withDayOfMonth(date.lengthOfMonth())); date = date.plusDays(1)) {
            series.getData().add(new XYChart.Data(""+date,billService.getDateTotalSale(date)));
            lblMonthlyBills.setText(
                    String.valueOf(Integer.parseInt(lblMonthlyBills.getText())+billService.getDateTotalBill(date))
            );
            lblMonthlyAmount.setText(
                    String.valueOf(Float.parseFloat(lblMonthlyAmount.getText())+billService.getDateTotalSale(date))
            );
        }
        lineChartMonth.getData().add(series);
    }
    private void dayReport()
    {
        lblTodaysBill.setText(""+billService.getDateTotalBill(LocalDate.now()));
        lblTodayAmount.setText(""+billService.getDateTotalSale(LocalDate.now()));
        lblTodayPurchase.setText(""+0.0f);
        for(Bill bill:billService.getBillByDate(LocalDate.now()))
        {
            for(Transaction tr:bill.getTransactions())
            {
                lblTodayPurchase.setText(String.valueOf(Float.parseFloat(lblTodayPurchase.getText())+(tr.getPrice()*tr.getQuantity())));
            }
        }
        lblTodayMargin.setText(
                String.valueOf(Float.parseFloat(lblTodayAmount.getText())-Float.parseFloat(lblTodayPurchase.getText()))
        );

    }
    private void yearlyReport() {
        lineChartYear.getData().clear();
        pichartYear.getData().clear();
        yearPichartData.clear();
        lblYearBills.setText(String.valueOf(billService.getPeriodBillCount(LocalDate.of(date.getValue().getYear(),01,01),LocalDate.of(date.getValue().getYear(),12,31))));
        lblYearAmount.setText(String.valueOf(billService.getPeriodBillAmount(LocalDate.of(date.getValue().getYear(),01,01),LocalDate.of(date.getValue().getYear(),12,31))));

        XYChart.Series series = new XYChart.Series();
        series.getData().add(new XYChart.Data("January",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),01,01),LocalDate.of(date.getValue().getYear(),01,31))));
        yearPichartData.add(new PieChart.Data("January",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),01,01),LocalDate.of(date.getValue().getYear(),01,31))));

        series.getData().add(new XYChart.Data("February",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),02,01),LocalDate.of(date.getValue().getYear(),02,28))));
        yearPichartData.add(new PieChart.Data("February",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),02,01),LocalDate.of(date.getValue().getYear(),02,28))));

        series.getData().add(new XYChart.Data("March",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),03,01),LocalDate.of(date.getValue().getYear(),03,31))));
        yearPichartData.add(new PieChart.Data("March",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),03,01),LocalDate.of(date.getValue().getYear(),03,31))));

        series.getData().add(new XYChart.Data("April",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),04,01),LocalDate.of(date.getValue().getYear(),04,30))));
        yearPichartData.add(new PieChart.Data("April",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),04,01),LocalDate.of(date.getValue().getYear(),04,30))));

        series.getData().add(new XYChart.Data("May",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),05,01),LocalDate.of(date.getValue().getYear(),05,31))));
        yearPichartData.add(new PieChart.Data("May",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),05,01),LocalDate.of(date.getValue().getYear(),05,31))));

        series.getData().add(new XYChart.Data("June",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),06,01),LocalDate.of(date.getValue().getYear(),06,30))));
        yearPichartData.add(new PieChart.Data("June",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),06,01),LocalDate.of(date.getValue().getYear(),06,30))));

        series.getData().add(new XYChart.Data("July",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),07,01),LocalDate.of(date.getValue().getYear(),07,31))));
        yearPichartData.add(new PieChart.Data("July",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),07,01),LocalDate.of(date.getValue().getYear(),07,31))));

        series.getData().add(new XYChart.Data("August",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),8,01),LocalDate.of(date.getValue().getYear(),8,31))));
        yearPichartData.add(new PieChart.Data("August",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),8,01),LocalDate.of(date.getValue().getYear(),8,31))));

        series.getData().add(new XYChart.Data("September",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),9,01),LocalDate.of(date.getValue().getYear(),9,30))));
        yearPichartData.add(new PieChart.Data("September",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),9,01),LocalDate.of(date.getValue().getYear(),9,30))));

        series.getData().add(new XYChart.Data("October",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),10,01),LocalDate.of(date.getValue().getYear(),10,31))));
        yearPichartData.add(new PieChart.Data("October",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),10,01),LocalDate.of(date.getValue().getYear(),10,31))));

        series.getData().add(new XYChart.Data("November",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),11,01),LocalDate.of(date.getValue().getYear(),11,30))));
        yearPichartData.add(new PieChart.Data("November",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),11,01),LocalDate.of(date.getValue().getYear(),11,30))));

        series.getData().add(new XYChart.Data("December",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),12,01),LocalDate.of(date.getValue().getYear(),12,31))));
        yearPichartData.add(new PieChart.Data("December",billService.getMonthlyBillAmount(LocalDate.of(date.getValue().getYear(),12,01),LocalDate.of(date.getValue().getYear(),12,31))));

        lineChartYear.getData().add(series);
        pichartYear.getData().addAll(yearPichartData);
        List<Bill>billList = billService.findByDateBetween(LocalDate.of(date.getValue().getYear(),01,01),LocalDate.of(date.getValue().getYear(),12,31));
    }
}
