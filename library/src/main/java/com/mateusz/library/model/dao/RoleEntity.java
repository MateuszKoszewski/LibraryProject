package com.mateusz.library.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.UserEntity;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="roles")
@AllArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Role roleEnum;

    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private List<UserEntity> userEntityList;

    public RoleEntity() {
        this.userEntityList = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRoleEnum() {
        return roleEnum;
    }

    public void setRoleEnum(Role roleEnum) {
        this.roleEnum = roleEnum;
        this.name = roleEnum.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserEntity> getUserEntityList() {
        return userEntityList;
    }

    public void setUserEntityList(List<UserEntity> userEntityList) {
        this.userEntityList = userEntityList;
    }
}
