package com.ysq.theTourGuide.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "administrator_type")
public class AdministratorType {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "authority_id")
    private Integer authorityId;

    public static final String ID = "id";

    public static final String TYPE_NAME = "typeName";

    public static final String AUTHORITY_ID = "authorityId";
}