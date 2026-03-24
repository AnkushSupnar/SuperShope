package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;

@Table(name = "app_settings")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AppSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "setting_key", unique = true)
    private String settingKey;

    @Column(name = "setting_value")
    private String settingValue;
}
