package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

/** A line of a bill still in progress on a counter; moved to {@link Transaction} when the bill is saved. */
@Table(name = "temptransaction", indexes = @Index(name = "idx_temptransaction_counterno", columnList = "counterno"))
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TempTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "counterno", nullable = false)
    private Integer counterno;

    @Column(name = "lineno")
    private Integer lineno;

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
}
