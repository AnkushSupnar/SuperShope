package com.ankush.data.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseReportDTO {
    int sr;
    LocalDate date;
    String partyName;
    String invoiceNo;
    int itemCount;
    float netTotal;
    float discount;
    float grandTotal;
    float paid;
    float due;
}
