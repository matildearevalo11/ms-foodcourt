package com.pragma.powerup.infrastructure.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(name = "url_logo", nullable = false)
    private String urlLogo;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
}
