package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "purchasetransaction")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PurchaseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String barcode;
    String itemname;
    String unit;
    Float price;
    private Float qty;
    private Float amount;
    private Float mrp;


    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private PurchaseInvoice invoice;
}