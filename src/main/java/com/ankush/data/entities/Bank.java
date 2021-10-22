package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "bank")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "bankname")
    private String bankname;

    @Column(name = "ifsc")
    private String ifsc;

    @Column(name = "accountno")
    private String accountno;

    @Column(name = "woner")
    private String woner;

    @Column(name = "balance")
    private Float balance;


}