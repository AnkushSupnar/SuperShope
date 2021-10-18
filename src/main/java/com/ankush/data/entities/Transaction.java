package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "transaction")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Transaction {
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

    @Column(name = "price")
    private Float price;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "sailingprice")
    private Float sailingprice;

    @ManyToOne
    @JoinColumn(name = "billno")
    private Bill bill;

}