package com.example.demo.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import lombok.Data;

@Data
@Embeddable
public class UserRoleId implements Serializable {
    private Integer userId;
    private Integer roleId;

}