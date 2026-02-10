package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "favorite_category_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "item_id"})
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FavoriteCategoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private FavoriteCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Item item;
}
