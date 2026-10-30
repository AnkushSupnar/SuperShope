package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "item")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "itemname")
    private String itemname;

    @Column(name = "rate")
    private float rate;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "unit")
    private String unit;

    @Column(name = "price")
    private float price;

    @Column(name = "sailingprice")
    private float sailingprice;

    // Short numeric code (PLU) typed at the billing counter instead of searching by name
    @Column(name = "shortcode", length = 10)
    private String shortCode;

    // English / alternate search text, so Marathi (Kiran) items can be found by typing e.g. "sugar"
    @Column(name = "searchname")
    private String searchName;

    @Column(name = "category", length = 60)
    private String category;

    // null is treated as active so existing rows stay visible
    @Column(name = "active")
    private Boolean active;

    // set when this item was merged into another: its barcode / short code then bring up that item
    @Column(name = "replaced_by")
    private Long replacedBy;

    public boolean isActiveItem() {
        return active == null || active;
    }
}