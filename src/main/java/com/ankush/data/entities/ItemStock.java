package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "itemstock")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ItemStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
    private Long id;

  //  @Column(name = "itemname")
    private String itemname;

    //@Column(name = "barcode")
    private String barcode;

    //@Column(name = "stock")
    private Float stock;

    //@Column(name = "unit", length = 10)
    private String unit;

}