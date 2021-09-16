package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "purchase_transaction")
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

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    private Float qty;
    private Float amount;

    @ManyToOne
    @JoinColumn(name="purchaseinvoice_id")
    private PurchaseInvoice invoice;
}