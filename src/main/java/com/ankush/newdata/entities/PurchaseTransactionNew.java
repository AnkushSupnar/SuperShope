package com.ankush.newdata.entities;

import com.ankush.newdata.entities.PurchaseInvoice;
import com.ankush.newdata.entities.Product;
import lombok.*;

import javax.persistence.*;

@Table(name = "purchasetransaction")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PurchaseTransactionNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product; // Replaces barcode & itemname

    private String unit;
    private Float price;
    private Float qty;
    private Float amount;
    private Float mrp;

    @ManyToOne
    @JoinColumn(name = "invoiceId", nullable = false)
    private PurchaseInvoice invoice;
}
