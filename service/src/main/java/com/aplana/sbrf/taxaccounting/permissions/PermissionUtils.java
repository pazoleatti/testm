package com.aplana.sbrf.taxaccounting.permissions;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Вспомогательный класс для работы с правами
 */
public abstract class PermissionUtils {
    /**
     * Проверяет есть ли у пользователя одна из указанных ролей
     *
     * @param user  пользователь
     * @param roles список ролей
     * @return факт наличия роли
     */
    public static boolean hasRole(User user, String... roles) {
        boolean result = false;
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            for (String role : roles) {
                result |= grantedAuthority.getAuthority().equals(role);
            }
        }
        return result;
    }
}
