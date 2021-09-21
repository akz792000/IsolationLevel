package com.isolation.level.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "document")
public class DocumentEntity extends BaseEntity {

    @Column(name = "code")
    private Long code;

    @Column(name = "message")
    private String message;

}
