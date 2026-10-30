package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * A name an item used to have: it was renamed (e.g. the company changed 10 gram to 9 gram) or another item
 * was merged into it. Old bills keep the old name, so the old name is kept here to still find the item
 * by it and to count its old sales for the best-seller ranking.
 */
@Table(name = "item_name_history")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemNameHistory {
    public static final String RENAMED = "RENAMED";
    public static final String MERGED = "MERGED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "old_name")
    private String oldName;

    @Column(name = "reason", length = 20)
    private String reason;

    @Column(name = "changed_on")
    private LocalDate changedOn;
}
