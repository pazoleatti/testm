package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Моделька для представления "списка пользователей"
 * @author aivanov
 */
public class TAUserView implements Serializable {
    private static final long serialVersionUID = 8435923534838847536L;

    private Integer id;
    private String name;
    private String login;
    private String email;
    private Boolean active;
    private String roles;
    private List<Long> taRoleIds;
    private String depName;
    private Integer depId;
    private String asnu;
    private List<Long> asnuIds;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public List<Long> getTaRoleIds() {
        return taRoleIds;
    }

    public void setTaRoleIds(List<Long> taRoleIds) {
        this.taRoleIds = taRoleIds;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public Integer getDepId() {
        return depId;
    }

    public void setDepId(Integer depId) {
        this.depId = depId;
    }

    public List<Long> getAsnuIds() {
        return asnuIds;
    }

    public void setAsnuIds(List<Long> asnuIds) {
        this.asnuIds = asnuIds;
    }

    public String getAsnu() {
        return asnu;
    }

    public void setAsnu(String asnu) {
        this.asnu = asnu;
    }
}
