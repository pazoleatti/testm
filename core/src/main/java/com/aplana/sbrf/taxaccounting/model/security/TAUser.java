package com.aplana.sbrf.taxaccounting.model.security;

import java.io.Serializable;
import java.util.List;

public class TAUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String login;
	private String name;
	private List<TARole> roles;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<TARole> getRoles() {
		return roles;
	}
	public void setRoles(List<TARole> roles) {
		this.roles = roles;
	}
}
