package com.ankush.common;

import com.ankush.data.entities.PurchaseTransaction;

import java.util.List;

/** Net / grand total arithmetic for the purchase invoice screen, kept free of JavaFX so it can be tested. */
public final class PurchaseTotals {
    private PurchaseTotals() {}

    /** Sum of line amounts, recomputed from the lines every time so it can never drift from the table. */
    public static float netTotal(List<PurchaseTransaction> lines) {
        double sum = 0;
        for (PurchaseTransaction tr : lines) {
            if (tr.getAmount() != null) sum += tr.getAmount();
        }
        return round2((float) sum);
    }

    public static float grandTotal(float net, float discount, float transporting, float wages, float other) {
        return round2(net - discount + transporting + wages + other);
    }

    /** Parses a total field; empty or invalid text counts as 0 so a half-typed field doesn't break the total. */
    public static float parse(String text) {
        if (text == null || text.isBlank()) return 0.0f;
        try {
            return Float.parseFloat(text.trim());
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public static float round2(float v) {
        return Math.round(v * 100f) / 100f;
    }
}
