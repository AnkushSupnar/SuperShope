package com.ankush.controller.dashboard;

import com.ankush.Main;
import com.ankush.data.entities.Bill;
import com.ankush.data.service.DashboardService;
import com.ankush.data.service.DashboardService.DayTotal;
import com.ankush.data.service.DashboardService.ItemGaps;
import com.ankush.data.service.DashboardService.ItemSale;
import com.ankush.data.service.DashboardService.Margin;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Business overview for the shop owner: how the selected day went against the same day last week,
 * money still to receive, the last 30 days, best sellers, the year by month, the day's bills and
 * item master problems worth fixing. All figures come from aggregate queries in {@link DashboardService}.
 */
@Component
public class Dashboard implements Initializable {
    // one blue for sales; the darker step of the same blue marks the selected day / month
    private static final String BAR = "#5598e7";
    private static final String BAR_SELECTED = "#184f95";
    private static final String LINE = "#2a78d6";
    private static final String TRACK = "#EAF1FC";
    private static final String KIRAN = "Kiran";

    private static final Locale INDIA = new Locale("en", "IN");
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("EEE d MMM");

    @Autowired private DashboardService dashboardService;
    @FXML private VBox content;

    // Built anew each time the page is loaded: this controller is a Spring singleton, so reusing the same grids
    // kept adding column constraints on every visit and squeezed the cards after switching menus.
    private DatePicker datePicker;
    private Label subtitle;
    private GridPane kpiRow;
    private GridPane chartRow;
    private GridPane bottomRow;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Font.loadFont(Main.class.getResource("/fxml/font/kiran.ttf").toExternalForm(), 10);
        datePicker = new DatePicker(LocalDate.now());
        subtitle = new Label();
        kpiRow = new GridPane();
        chartRow = new GridPane();
        bottomRow = new GridPane();
        for (GridPane row : new GridPane[]{kpiRow, chartRow, bottomRow}) {
            row.setHgap(16);
        }
        columns(kpiRow, 20, 20, 20, 20, 20);
        columns(chartRow, 62, 38);
        columns(bottomRow, 42, 30, 28);
        content.getChildren().setAll(header(), kpiRow, chartRow, bottomRow);
        datePicker.setOnAction(e -> refresh());
        refresh();
    }

    private void refresh() {
        LocalDate day = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        subtitle.setText("Showing " + day.format(LONG_DATE) + (day.equals(LocalDate.now()) ? "  ·  today" : ""));
        List<DayTotal> last30 = dashboardService.getDailyTotals(day.minusDays(29), day);

        kpiRow.getChildren().clear();
        kpiRow.addRow(0, salesTile(day, last30), billsTile(day), marginTile(day), unpaidTile(), monthTile(day));
        chartRow.getChildren().clear();
        chartRow.addRow(0, dailyChartCard(day, last30), topItemsCard(day));
        bottomRow.getChildren().clear();
        bottomRow.addRow(0, monthlyChartCard(day), billsCard(day), attentionCard());
    }

    // ---------------------------------------------------------------- header

    private Node header() {
        Label title = new Label("Business Overview");
        title.getStyleClass().add("dash-title");
        subtitle.getStyleClass().add("dash-subtitle");
        VBox titles = new VBox(2, title, subtitle);

        Button today = new Button("Today");
        today.getStyleClass().add("dash-button");
        today.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            refresh();
        });
        Button refresh = new Button("Refresh");
        refresh.getStyleClass().add("dash-button");
        refresh.setGraphic(icon("REFRESH", "#1A237E", "1em"));
        refresh.setOnAction(e -> refresh());
        datePicker.setPrefWidth(150);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(10, titles, spacer, datePicker, today, refresh);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ---------------------------------------------------------------- KPI tiles

    private Node salesTile(LocalDate day, List<DayTotal> last30) {
        double sale = last30.get(last30.size() - 1).sale;
        double lastWeek = dashboardService.getSale(day.minusDays(7), day.minusDays(7));
        String weekday = day.minusDays(7).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        VBox tile = tile("SALES", money(sale));
        tile.getChildren().add(delta(sale, lastWeek, "vs last " + weekday));
        tile.getChildren().add(sparkline(last30.subList(last30.size() - 14, last30.size())));
        tip(tile, "Total of all bills on " + day.format(SHORT_DATE) + ".\nSame day last week: " + money(lastWeek)
                + "\nLine: last 14 days");
        return tile;
    }

    private Node billsTile(LocalDate day) {
        long bills = dashboardService.getBillCount(day, day);
        long lastWeek = dashboardService.getBillCount(day.minusDays(7), day.minusDays(7));
        double sale = dashboardService.getSale(day, day);
        VBox tile = tile("BILLS", String.valueOf(bills));
        tile.getChildren().add(delta(bills, lastWeek, "vs last week"));
        tile.getChildren().add(detail(bills == 0 ? "No bills yet" : "Average bill " + money(sale / bills)));
        return tile;
    }

    private Node marginTile(LocalDate day) {
        Margin m = dashboardService.getMargin(day, day);
        VBox tile = tile("MARGIN", m.coveredSale <= 0 ? "—" : money(m.margin()));
        tile.getChildren().add(detail(m.coveredSale <= 0 ? "No purchase rates on this day's items"
                : String.format(INDIA, "%.1f%% on items with a purchase rate", m.marginPercent())));
        Label coverage = detail(String.format(INDIA, "Purchase rate known for %.0f%% of sales", m.coveragePercent()));
        if (m.sale > 0 && m.coveragePercent() < 60) {
            coverage.setGraphic(icon("EXCLAMATION_TRIANGLE", "#B26A00", "1em"));
        }
        tile.getChildren().add(coverage);
        tip(tile, "Margin = selling amount − purchase cost, counted only for bill lines whose item has a purchase rate.\n"
                + "Items without a purchase rate are left out, so the margin is not overstated.");
        return tile;
    }

    private Node unpaidTile() {
        double[] unpaid = dashboardService.getUnpaid();
        VBox tile = tile("TO RECEIVE", money(unpaid[0]));
        tile.getChildren().add(detail((long) unpaid[1] + " bills not fully paid"));
        tile.getChildren().add(detail("All dates"));
        tip(tile, "Bill amount minus amount paid, over every bill that is not fully paid.");
        return tile;
    }

    private Node monthTile(LocalDate day) {
        LocalDate start = day.withDayOfMonth(1);
        double month = dashboardService.getSale(start, day);
        LocalDate prevStart = start.minusMonths(1);
        LocalDate prevEnd = prevStart.plusDays(Math.min(day.getDayOfMonth(), prevStart.lengthOfMonth()) - 1);
        double previous = dashboardService.getSale(prevStart, prevEnd);
        String monthName = day.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String prevName = prevStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        VBox tile = tile(monthName.toUpperCase() + " SO FAR", money(month));
        tile.getChildren().add(delta(month, previous, "vs 1–" + prevEnd.getDayOfMonth() + " " + prevName));
        tile.getChildren().add(detail("Purchases " + money(dashboardService.getPurchaseTotal(start, day))));
        return tile;
    }

    private VBox tile(String caption, String value) {
        Label c = new Label(caption);
        c.getStyleClass().add("kpi-caption");
        Label v = new Label(value);
        v.getStyleClass().add("kpi-value");
        VBox box = new VBox(4, c, v);
        box.getStyleClass().add("dash-card");
        box.setMinHeight(150);
        return box;
    }

    /** Change against a comparison value, with an arrow and words so it never relies on colour alone. */
    private Label delta(double now, double before, String against) {
        Label label;
        if (before <= 0) {
            label = new Label(now > 0 ? "New  " + against : "No change  " + against);
            label.getStyleClass().add("delta-flat");
            return label;
        }
        double pct = (now - before) * 100 / before;
        if (Math.abs(pct) < 0.5) {
            label = new Label("No change  " + against);
            label.getStyleClass().add("delta-flat");
        } else if (pct > 0) {
            label = new Label(String.format(INDIA, "%.0f%% up  %s", pct, against));
            label.setGraphic(icon("ARROW_UP", "#1B7A3A", "0.9em"));
            label.getStyleClass().add("delta-up");
        } else {
            label = new Label(String.format(INDIA, "%.0f%% down  %s", -pct, against));
            label.setGraphic(icon("ARROW_DOWN", "#B3261E", "0.9em"));
            label.getStyleClass().add("delta-down");
        }
        return label;
    }

    /** 14-day trend line under the sales figure; the last point (the selected day) is marked. */
    private Node sparkline(List<DayTotal> days) {
        double width = 170, height = 34;
        double max = days.stream().mapToDouble(d -> d.sale).max().orElse(0);
        Polyline line = new Polyline();
        double lastX = 0, lastY = height;
        for (int i = 0; i < days.size(); i++) {
            double x = i * width / (days.size() - 1);
            double y = max <= 0 ? height - 1 : height - 2 - (days.get(i).sale / max) * (height - 6);
            line.getPoints().addAll(x, y);
            lastX = x;
            lastY = y;
        }
        line.setStroke(javafx.scene.paint.Color.web(LINE));
        line.setStrokeWidth(2);
        Circle dot = new Circle(lastX, lastY, 3.5, javafx.scene.paint.Color.web(LINE));
        dot.setStroke(javafx.scene.paint.Color.WHITE);
        dot.setStrokeWidth(1.5);
        Pane pane = new Pane(line, dot);
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        VBox.setMargin(pane, new Insets(6, 0, 0, 0));
        return pane;
    }

    // ---------------------------------------------------------------- charts

    private Node dailyChartCard(LocalDate day, List<DayTotal> days) {
        double max = days.stream().mapToDouble(d -> d.sale).max().orElse(0);
        BarChart<String, Number> chart = barChart(max);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < days.size(); i++) {
            // label every 3rd day counted back from the selected day, so it is always labelled and labels never
            // crowd; the other days get an invisible unique category (zero-width spaces), as the axis has no formatter
            boolean labelled = (days.size() - 1 - i) % 3 == 0;
            String category = labelled ? String.valueOf(days.get(i).date.getDayOfMonth()) : "​".repeat(i + 1);
            series.getData().add(new XYChart.Data<>(category, days.get(i).sale));
        }
        chart.getData().add(series);
        for (int i = 0; i < days.size(); i++) {
            DayTotal d = days.get(i);
            styleBar(series.getData().get(i), d.date.equals(day),
                    d.date.format(SHORT_DATE) + "\n" + money(d.sale) + "  ·  " + d.bills + (d.bills == 1 ? " bill" : " bills"));
        }
        double total = days.stream().mapToDouble(d -> d.sale).sum();
        long open = days.stream().filter(d -> d.bills > 0).count();
        String note = days.get(0).date.format(SHORT_DATE) + " – " + day.format(SHORT_DATE) + "  ·  total " + money(total)
                + (open > 0 ? "  ·  average " + money(total / open) + " per working day" : "");
        return card("Daily sales, last 30 days", note, chart);
    }

    private Node monthlyChartCard(LocalDate day) {
        double[] months = dashboardService.getMonthlySales(day.getYear());
        double max = 0;
        for (double v : months) max = Math.max(max, v);
        BarChart<String, Number> chart = barChart(max);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int m = 0; m < 12; m++) {
            series.getData().add(new XYChart.Data<>(java.time.Month.of(m + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH), months[m]));
        }
        chart.getData().add(series);
        for (int m = 0; m < 12; m++) {
            styleBar(series.getData().get(m), m == day.getMonthValue() - 1,
                    java.time.Month.of(m + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + day.getYear() + "\n" + money(months[m]));
        }
        double year = 0;
        for (double v : months) year += v;
        return card("Sales by month, " + day.getYear(), "Year so far " + money(year), chart);
    }

    private BarChart<String, Number> barChart(double max) {
        CategoryAxis x = new CategoryAxis();
        x.setTickLabelRotation(0);
        x.setTickLabelGap(4);
        // a rounded scale with about four steps, instead of a crowded automatic one
        double step = niceStep(max / 4);
        NumberAxis y = new NumberAxis(0, step * Math.max(1, Math.ceil(max / step)), step);
        y.setAutoRanging(false);
        y.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number n) { return compactMoney(n.doubleValue(), y.getUpperBound()); }
            @Override public Number fromString(String s) { return 0; }
        });
        y.setMinorTickVisible(false);
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.getStyleClass().add("dash-chart");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setBarGap(2);
        chart.setCategoryGap(6);
        chart.setPrefHeight(260);
        chart.setMinHeight(220);
        chart.setPadding(new Insets(4, 0, 0, 0));
        return chart;
    }

    private void styleBar(XYChart.Data<String, Number> data, boolean selected, String tooltip) {
        Node bar = data.getNode();
        if (bar == null) return;
        bar.setStyle("-fx-bar-fill: " + (selected ? BAR_SELECTED : BAR) + ";");
        Tooltip t = new Tooltip(tooltip);
        t.setShowDelay(Duration.millis(80));
        Tooltip.install(bar, t);
        bar.setOnMouseEntered(e -> bar.setOpacity(0.8));
        bar.setOnMouseExited(e -> bar.setOpacity(1));
    }

    // ---------------------------------------------------------------- lists

    private Node topItemsCard(LocalDate day) {
        List<ItemSale> items = dashboardService.getTopItems(day.minusDays(29), day, 10);
        VBox rows = new VBox(9);
        double max = items.isEmpty() ? 0 : items.get(0).amount;
        int rank = 1;
        for (ItemSale item : items) {
            Label number = new Label(String.valueOf(rank++));
            number.getStyleClass().add("row-muted");
            number.setMinWidth(18);
            Label name = new Label(item.name);
            name.setFont(Font.font(KIRAN, 17));
            name.setStyle("-fx-text-fill: #1F1F2E;");
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);
            Label value = new Label(money(item.amount));
            value.getStyleClass().add("row-text");
            value.setStyle("-fx-font-weight: bold;");
            HBox top = new HBox(8, number, name, value);
            top.setAlignment(Pos.CENTER_LEFT);

            Region fill = new Region();
            fill.setStyle("-fx-background-color: " + LINE + "; -fx-background-radius: 3;");
            StackPane track = new StackPane(fill);
            track.setAlignment(Pos.CENTER_LEFT);
            track.setStyle("-fx-background-color: " + TRACK + "; -fx-background-radius: 3;");
            track.setPrefHeight(6);
            track.setMaxHeight(6);
            double share = max <= 0 ? 0 : item.amount / max;
            fill.maxWidthProperty().bind(track.widthProperty().multiply(share));
            fill.prefWidthProperty().bind(track.widthProperty().multiply(share));
            VBox.setMargin(track, new Insets(0, 0, 0, 26));

            VBox row = new VBox(3, top, track);
            tip(row, quantity(item) + " sold for " + money(item.amount));
            rows.getChildren().add(row);
        }
        Node body = items.isEmpty() ? emptyState("No sales in the last 30 days") : rows;
        return card("Top 10 items, last 30 days", "By sale amount", body);
    }

    private Node billsCard(LocalDate day) {
        List<Bill> bills = dashboardService.getBills(day);
        VBox rows = new VBox(8);
        int shown = 0;
        for (Bill bill : bills) {
            if (shown++ == 8) break;
            double total = bill.getGrandtotal() == null ? 0 : bill.getGrandtotal();
            double paid = bill.getPaid() == null ? 0 : bill.getPaid();
            Label no = new Label("#" + bill.getBillno());
            no.getStyleClass().add("row-text");
            no.setStyle("-fx-font-weight: bold;");
            no.setMinWidth(54);
            String customer = bill.getCustomer() == null || bill.getCustomer().getName() == null ? "Walk-in" : bill.getCustomer().getName();
            int lines = bill.getTransactions() == null ? 0 : bill.getTransactions().size();
            Label who = new Label(customer + "  ·  " + lines + (lines == 1 ? " item" : " items"));
            who.getStyleClass().add("row-muted");
            who.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(who, Priority.ALWAYS);
            Label amount = new Label(money(total));
            amount.getStyleClass().add("row-text");
            boolean due = paid < total - 0.5;
            Label status = new Label(due ? "Due " + money(total - paid) : "Paid");
            status.getStyleClass().add(due ? "pill-due" : "pill-paid");
            // amount and status keep their full width; only the customer text shortens on small screens
            no.setMinWidth(Region.USE_PREF_SIZE);
            amount.setMinWidth(Region.USE_PREF_SIZE);
            status.setMinWidth(Region.USE_PREF_SIZE);
            HBox row = new HBox(10, no, who, amount, status);
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }
        if (bills.size() > 8) {
            Label more = new Label("and " + (bills.size() - 8) + " more bills");
            more.getStyleClass().add("row-muted");
            rows.getChildren().add(more);
        }
        Node body = bills.isEmpty() ? emptyState("No bills on this day yet") : rows;
        return card("Bills on " + day.format(SHORT_DATE), bills.size() + (bills.size() == 1 ? " bill, newest first" : " bills, newest first"), body);
    }

    private Node attentionCard() {
        ItemGaps gaps = dashboardService.getItemGaps();
        VBox rows = new VBox(12,
                attention(gaps.noSellingPrice, "items have no selling price", "Billing asks for a rate each time. Set it in Item Master or Set Selling Rate.",
                        "Every item has a selling price"),
                attention(gaps.unused180, "items not sold or bought in 180 days", "Hide them: Item Master → HIDE UNUSED.",
                        "No unused items"),
                attention(gaps.noMrp, "items have no MRP", "The bill cannot show the customer's saving. Enter MRP on purchase.",
                        "Every item has an MRP"),
                attention(gaps.noShortCode, "items have no short code", "Give codes for fast billing: Item Master → AUTO CODES.",
                        "Every item has a short code"));
        return card("Needs attention", gaps.activeItems + " active items", rows);
    }

    private Node attention(long count, String what, String hint, String okText) {
        boolean ok = count == 0;
        Node mark = ok ? icon("CHECK_CIRCLE", "#1B7A3A", "1.3em")
                : icon("EXCLAMATION_TRIANGLE", "#B26A00", "1.3em");
        String text1 = count == 1 ? what.replaceFirst("^items have", "item has").replaceFirst("^items ", "item ") : what;
        Label headline = new Label(ok ? okText : indianGrouping(count) + " " + text1);
        headline.getStyleClass().add("row-text");
        headline.setStyle("-fx-font-weight: bold;");
        headline.setWrapText(true);
        VBox text = new VBox(2, headline);
        if (!ok) {
            Label h = new Label(hint);
            h.getStyleClass().add("row-muted");
            h.setWrapText(true);
            text.getChildren().add(h);
        }
        HBox row = new HBox(10, mark, text);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    // ---------------------------------------------------------------- helpers

    private VBox card(String title, String note, Node body) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Label n = new Label(note);
        n.getStyleClass().add("card-note");
        VBox card = new VBox(4, t, n, body);
        VBox.setMargin(body, new Insets(10, 0, 0, 0));
        VBox.setVgrow(body, Priority.ALWAYS); // charts fill the card when the row is taller than they need
        card.getStyleClass().add("dash-card");
        card.setMinWidth(0);
        return card;
    }

    private Label detail(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("kpi-detail");
        label.setWrapText(true);
        return label;
    }

    private Node emptyState(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("row-muted");
        StackPane box = new StackPane(label);
        box.setMinHeight(120);
        return box;
    }

    private static FontAwesomeIcon icon(String glyph, String color, String size) {
        FontAwesomeIcon icon = new FontAwesomeIcon();
        icon.setGlyphName(glyph);
        icon.setFill(javafx.scene.paint.Color.web(color));
        icon.setSize(size);
        return icon;
    }

    private static void tip(Node node, String text) {
        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.millis(150));
        Tooltip.install(node, t);
    }

    private static void columns(GridPane grid, double... percents) {
        for (double p : percents) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(p);
            c.setHgrow(Priority.ALWAYS);
            c.setFillWidth(true);
            grid.getColumnConstraints().add(c);
        }
        RowConstraints r = new RowConstraints();
        r.setFillHeight(true);
        grid.getRowConstraints().add(r);
    }

    private static String money(double value) {
        return "₹" + indianGrouping(Math.round(value));
    }

    /** 2,38,385 style grouping (Java's en-IN number format groups in thousands). */
    private static String indianGrouping(long value) {
        String digits = String.valueOf(Math.abs(value));
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            sb.append(digits.charAt(i));
            int left = len - 1 - i;
            if (left > 0 && (left == 3 || (left > 3 && (left - 3) % 2 == 0))) sb.append(',');
        }
        return (value < 0 ? "-" : "") + sb;
    }

    /** 1, 2, 2.5 or 5 times a power of ten, at least the given raw step. */
    private static double niceStep(double raw) {
        if (raw <= 0) return 1000;
        double power = Math.pow(10, Math.floor(Math.log10(raw)));
        for (double m : new double[]{1, 2, 2.5, 5, 10}) {
            if (m * power >= raw) return m * power;
        }
        return 10 * power;
    }

    /** Axis labels in one unit per axis, picked from the axis top: ₹500 / ₹12k / ₹1.5L. */
    private static String compactMoney(double value, double axisTop) {
        if (value == 0) return "₹0";
        if (axisTop >= 100000) return trim(String.format(Locale.ROOT, "%.1f", value / 100000)) + "L";
        if (axisTop >= 1000) return trim(String.format(Locale.ROOT, "%.1f", value / 1000)) + "k";
        return String.format(Locale.ROOT, "₹%.0f", value);
    }

    private static String trim(String number) {
        return "₹" + (number.endsWith(".0") ? number.substring(0, number.length() - 2) : number);
    }

    private static String quantity(ItemSale item) {
        boolean kg = "ik.ga`^.".equals(item.unit);
        double q = item.quantity;
        String number = q == Math.rint(q) ? String.valueOf((long) q) : String.format(INDIA, "%.2f", q);
        return number + (kg ? " kg" : " nos");
    }
}
