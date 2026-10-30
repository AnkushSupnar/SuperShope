package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

/**
 * A billing counter: one bill can be in progress on each counter at the same time.
 * The customer details of that in-progress bill are kept here, its lines in {@link TempTransaction}.
 */
@Table(name = "counter_master")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class BillingCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "counterno", nullable = false, unique = true)
    private Integer counterno;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private Boolean active;

    // draft of the bill in progress on this counter
    @Column(name = "customername")
    private String customername;

    @Column(name = "customermobile")
    private String customermobile;

    @Column(name = "othercharges")
    private Float othercharges;

    public boolean isActiveCounter() {
        return active == null || active;
    }
}
