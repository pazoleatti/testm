package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Информация о пользователе, его ролях и принадлежности к подразделению
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TAUser implements SecuredEntity, Serializable {
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
	private long permissions;

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

    public boolean hasRole(TaxType taxType, String roleAlias) {
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

	/**
	 * Проверяет есть ли у пользователя единственная роль
	 * @param role роль пользователя
	 * @return true - если у пользователя есть только такая роль, false - в противном случае
	 */
	public boolean hasSingleRole(String role) {
		return roles != null && roles.size() == 1 && roles.get(0).getAlias().equals(role);
	}

    public boolean hasRoles(TaxType taxType, String... roleAlias) {
        boolean hashRole = false;
        if (roleAlias.length >= 0) {
            for (int i = 0; i < roleAlias.length; i++) {
                hashRole |= hasRole(taxType, roleAlias[i]);
            }
        }
        return hashRole;
    }

    public boolean hasRoles(String... roleAlias) {
        boolean hashRole = false;
        if (roleAlias.length >= 0) {
            for (int i = 0; i < roleAlias.length; i++) {
                hashRole |= hasRole(roleAlias[i]);
            }
        }
        return hashRole;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TAUser taUser = (TAUser) o;

		return id == taUser.id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
