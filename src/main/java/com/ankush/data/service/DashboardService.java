package com.ankush.data.service;

import com.ankush.data.entities.Bill;
import com.ankush.data.repository.BillRepository;
import com.ankush.data.repository.ItemRepository;
import com.ankush.data.repository.PurchaseInvoiceRepository;
import com.ankush.data.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Figures for the dashboard, from aggregate queries (no loading of every bill line). */
@Service
public class DashboardService {
    @Autowired private BillRepository billRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PurchaseInvoiceRepository purchaseInvoiceRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemService itemService;

    /** Sale and bill count of one day. */
    public static class DayTotal {
        public final LocalDate date;
        public final double sale;
        public final long bills;

        DayTotal(LocalDate date, double sale, long bills) {
            this.date = date;
            this.sale = sale;
            this.bills = bills;
        }
    }

    /** One day per date from start to end, days without bills included as zero. */
    public List<DayTotal> getDailyTotals(LocalDate start, LocalDate end) {
        Map<LocalDate, Object[]> rows = new HashMap<>();
        for (Object[] row : billRepository.getDailyTotals(start, end)) rows.put((LocalDate) row[0], row);
        List<DayTotal> days = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Object[] row = rows.get(d);
            days.add(row == null ? new DayTotal(d, 0, 0)
                    : new DayTotal(d, toDouble(row[1]), ((Number) row[2]).longValue()));
        }
        return days;
    }

    public double getSale(LocalDate start, LocalDate end) {
        return getDailyTotals(start, end).stream().mapToDouble(d -> d.sale).sum();
    }

    public long getBillCount(LocalDate start, LocalDate end) {
        return getDailyTotals(start, end).stream().mapToLong(d -> d.bills).sum();
    }

    /** Sale per month (index 0 = January) of the given year. */
    public double[] getMonthlySales(int year) {
        double[] months = new double[12];
        for (Object[] row : billRepository.getDailyTotals(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))) {
            months[((LocalDate) row[0]).getMonthValue() - 1] += toDouble(row[1]);
        }
        return months;
    }

    /** Margin of a period, only over bill lines that have a purchase rate. */
    public static class Margin {
        public final double sale;          // all bill lines
        public final double coveredSale;   // lines with a purchase rate
        public final double coveredCost;

        Margin(double sale, double coveredSale, double coveredCost) {
            this.sale = sale;
            this.coveredSale = coveredSale;
            this.coveredCost = coveredCost;
        }

        public double margin() { return coveredSale - coveredCost; }
        public double marginPercent() { return coveredSale <= 0 ? 0 : margin() * 100 / coveredSale; }
        /** Share of the sale whose purchase rate is known, so the margin can be judged. */
        public double coveragePercent() { return sale <= 0 ? 0 : coveredSale * 100 / sale; }
    }

    public Margin getMargin(LocalDate start, LocalDate end) {
        Object[] costed = transactionRepository.getCostedLineTotals(start, end).get(0);
        return new Margin(toDouble(transactionRepository.getLineTotal(start, end)), toDouble(costed[0]), toDouble(costed[1]));
    }

    /** [amount to receive, number of bills] for bills not fully paid. */
    public double[] getUnpaid() {
        Object[] row = billRepository.getUnpaidTotals().get(0);
        return new double[]{toDouble(row[0]), toDouble(row[1])};
    }

    public double getPurchaseTotal(LocalDate start, LocalDate end) {
        Double total = purchaseInvoiceRepository.getPurchaseTotal(start, end);
        return total == null ? 0 : total;
    }

    /** Best-selling item of a period by amount. */
    public static class ItemSale {
        public final String name;
        public final String unit;
        public final double amount;
        public final double quantity;

        ItemSale(String name, String unit, double amount, double quantity) {
            this.name = name;
            this.unit = unit;
            this.amount = amount;
            this.quantity = quantity;
        }
    }

    /** Top items by sale amount; sales under an old (renamed / merged) name count for the current item. */
    public List<ItemSale> getTopItems(LocalDate start, LocalDate end, int limit) {
        Map<String, String> oldNames = itemService.getOldNameMap();
        Map<String, double[]> totals = new HashMap<>();
        Map<String, String> units = new HashMap<>();
        for (Object[] row : transactionRepository.getItemSales(start, end)) {
            if (row[0] == null) continue;
            String name = oldNames.getOrDefault((String) row[0], (String) row[0]);
            double[] t = totals.computeIfAbsent(name, k -> new double[2]);
            t[0] += toDouble(row[2]);
            t[1] += toDouble(row[3]);
            units.putIfAbsent(name, (String) row[1]);
        }
        return totals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limit)
                .map(e -> new ItemSale(e.getKey(), units.get(e.getKey()), e.getValue()[0], e.getValue()[1]))
                .collect(Collectors.toList());
    }

    public List<Bill> getBills(LocalDate date) {
        List<Bill> bills = new ArrayList<>(billRepository.getByDate(date));
        bills.sort(Comparator.comparing(Bill::getBillno).reversed());
        return bills;
    }

    /** Item master problems worth fixing. */
    public static class ItemGaps {
        public long noSellingPrice, noMrp, noShortCode, activeItems, unused180;
    }

    public ItemGaps getItemGaps() {
        Object[] row = itemRepository.getActiveItemGaps().get(0);
        ItemGaps gaps = new ItemGaps();
        gaps.noSellingPrice = toLong(row[0]);
        gaps.noMrp = toLong(row[1]);
        gaps.noShortCode = toLong(row[2]);
        gaps.activeItems = toLong(row[3]);
        gaps.unused180 = itemService.findUnusedItems(180).size();
        return gaps;
    }

    private static double toDouble(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }

    private static long toLong(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }
}
