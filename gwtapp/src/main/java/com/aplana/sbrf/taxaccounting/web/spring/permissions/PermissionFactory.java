package com.aplana.sbrf.taxaccounting.web.spring.permissions;

import java.util.List;

/**
 * Фабрика для работы с правами.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public interface PermissionFactory<T> {
    /**
     * Создает набор прав, соответствующий заданной битовой маске.
     *
     * @param mask битовая маска
     * @return {@link Permission}, представляющее заданную маску
     */
    List<Permission<T>> buildFromMask(long mask);

    /**
     * Возвращает доступные зарегистрированные права.
     */
    List<Permission<T>> getRegisteredPermissions();
}