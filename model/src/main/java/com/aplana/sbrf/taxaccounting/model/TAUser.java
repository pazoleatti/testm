package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Информация о пользователе, его ролях и принадлежности к подразделению
 */
public class TAUser implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Код учетной записи для пользователя "Система" */
	public static final int SYSTEM_USER_ID = 0;

	private int id;
	private String login;
	private String name;
	private List<TARole> roles;
	private List<Long> asnuIds;
	private int departmentId;
	private boolean active;
	private String email;

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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
    public List<Long> getAsnuIds() {
        return asnuIds;
    }
    public void setAsnuIds(List<Long> asnuIds) {
        this.asnuIds = asnuIds;
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

    @Override
    public String toString() {
        return "TAUser{" +
                "id: " + id +
                ",login: " + login +
                ",name: " + name +
                ",roles: " + roles.toString() +
                ",departmentId: " + departmentId +
                ",active: " + active +
                ",email: " + email +
                '}';
    }
}
