package com.ankush.data.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyPurchaseDTO {
    int sr;
    String month;
    int invoiceCount;
    float netTotal;
    float otherCharges;
    float discount;
    float grandTotal;
    float paid;
    float due;
}
