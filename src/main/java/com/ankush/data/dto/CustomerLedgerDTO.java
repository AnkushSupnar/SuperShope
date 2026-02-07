package com.ankush.data.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerLedgerDTO {
    int sr;
    LocalDate date;
    long billNo;
    int items;
    float grandTotal;
    float paid;
    float due;
}
