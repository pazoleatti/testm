package com.aplana.sbrf.taxaccounting.model.security;

import java.io.Serializable;
import java.util.List;

/**
 * Информация о пользователе, его ролях и принадлежности к подразделению
 */
public class TAUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String login;
	private String name;
	private List<TARole> roles;
	private int departmentId;
	
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
	public int getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	public List<TARole> getRoles() {
		return roles;
	}
	public void setRoles(List<TARole> roles) {
		this.roles = roles;
	}
	/**
	 * Проверяет, что у пользователя есть роль с заданным {@link TARole#getAlias() алиасом}
	 * @param roleAlias алиас роли
	 * @return true - если у пользователя есть такая роль, false - в противном случае
	 */
	public boolean hasRole(String roleAlias) {
		if (roles == null) {
			throw new IllegalStateException("Roles list is not initialized properly!");
		}
		
		if (roleAlias == null) {
			throw new IllegalArgumentException("roleAlias cannot be null");
		}
		
		for (TARole role: roles) {
			if (roleAlias.equals(role.getAlias())) {
				return true;
			}
		}
		return false;
	}
}
