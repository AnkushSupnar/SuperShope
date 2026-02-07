package com.ankush.data.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillSalesReport {
    int sr;
    long billNo;
    LocalDate date;
    String customerName;
    boolean hasCustomer;
    int totalItems;
    float netTotal;
    float discount;
    float grandTotal;
    float paid;
}
