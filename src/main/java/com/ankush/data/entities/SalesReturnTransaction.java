package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "salesreturntransaction")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SalesReturnTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "itemname")
    private String itemname;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "unit")
    private String unit;

    @Column(name = "quantity")
    private Float quantity;

    @Column(name = "rate")
    private Float rate;

    @Column(name = "amount")
    private Float amount;

    @ManyToOne
    @JoinColumn(name = "salesreturn_id")
    private SalesReturn salesReturn;
}
