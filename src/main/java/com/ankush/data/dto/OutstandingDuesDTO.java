package com.ankush.data.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutstandingDuesDTO {
    int sr;
    String customerName;
    String contact;
    int totalBills;
    float totalAmount;
    float totalPaid;
    float dueAmount;
}
