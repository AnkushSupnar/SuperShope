package com.ankush.common;

import com.ankush.data.entities.PurchaseTransaction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseTotalsTest {

    private static PurchaseTransaction line(float qty, float rate) {
        PurchaseTransaction tr = new PurchaseTransaction();
        tr.setQty(qty);
        tr.setPrice(rate);
        tr.setAmount(qty * rate);
        return tr;
    }

    @Test
    void grandTotalIsNetMinusDiscountPlusCharges() {
        List<PurchaseTransaction> lines = List.of(line(10, 25), line(2, 100));
        float net = PurchaseTotals.netTotal(lines);
        assertEquals(450.0f, net);
        assertEquals(480.0f, PurchaseTotals.grandTotal(net, 20, 30, 15, 5));
    }

    @Test
    void mergingSameItemKeepsTheWholeLineInTheTotal() {
        // old code: grand total computed after removing the line's old amount and before adding the new one back,
        // so adding 5 more of an item already at 10 x 20 showed 0 + charges instead of 300 + charges
        List<PurchaseTransaction> lines = new ArrayList<>(List.of(line(10, 20)));
        PurchaseTransaction again = line(5, 20);
        lines.get(0).setQty(lines.get(0).getQty() + again.getQty());
        lines.get(0).setAmount(lines.get(0).getAmount() + again.getAmount());
        float net = PurchaseTotals.netTotal(lines);
        assertEquals(300.0f, net);
        assertEquals(350.0f, PurchaseTotals.grandTotal(net, 0, 50, 0, 0));
    }

    @Test
    void manyFractionalLinesDoNotDrift() {
        List<PurchaseTransaction> lines = new ArrayList<>();
        for (int i = 0; i < 37; i++) lines.add(line(3, 33.33f));
        assertEquals(3699.63f, PurchaseTotals.netTotal(lines));
    }

    @Test
    void emptyOrHalfTypedChargeCountsAsZero() {
        assertEquals(0.0f, PurchaseTotals.parse(""));
        assertEquals(0.0f, PurchaseTotals.parse("  "));
        assertEquals(0.0f, PurchaseTotals.parse(null));
        assertEquals(0.0f, PurchaseTotals.parse("."));
        assertEquals(12.5f, PurchaseTotals.parse("12.5"));
    }
}
