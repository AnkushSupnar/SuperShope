package com.ankush.controller.dashboard;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Transaction;
import com.ankush.data.service.BillService;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
    @FXML private Label lblTodayAmount;
    @FXML private Label lblTodaysBill;
    @FXML private Label lblTodayPurchase;
    @FXML private LineChart lineChartMonth;
    @FXML private Label lblTodayMargin;


    @Autowired
    BillService billService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
            dayReport();
            addMonthLineChart();
    }

    private void addMonthLineChart()  {
        LocalDate date = LocalDate.now();
        XYChart.Series series = new XYChart.Series();

        for (date = date.withDayOfMonth(1); date.isBefore(date.withDayOfMonth(date.lengthOfMonth())); date = date.plusDays(1)) {
            series.getData().add(new XYChart.Data(""+date,billService.getDateTotalSale(date)));
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
}
