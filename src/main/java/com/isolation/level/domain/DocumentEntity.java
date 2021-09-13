package com.isolation.level.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "document")
public class DocumentEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

}
