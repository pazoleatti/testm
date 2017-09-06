package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;

import java.util.Collection;

/**
 * Интерфейс для установки прав на конкретный объект.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public interface PermissionSetter<T extends SecuredEntity> {
    /**
     * Устанавливает права текущего пользователя на указанный объект.
     *
     * @param targetDomainObject объект, для которого нужно установить права
     * @param permissions        массив прав, которые должны быть определены. Если {@code null},
     *                           то определяются все зарегистрированные права
     * @see SecuredEntity#setPermissions(long)
     */
    void setPermissions(T targetDomainObject, Permission<T>... permissions);

    /**
     * Устанавливает права текущего пользователя на указанный объект.
     *
     * @param targetDomainObject объект, для которого нужно установить права
     * @param mask               битовая маска прав, которые должны быть определены
     * @see SecuredEntity#setPermissions(long)
     */
    void setPermissions(T targetDomainObject, long mask);

    /**
     * Устанавливает права текущего пользователя на указанные объекты.
     *
     * @param targetDomainObjects объекты, для которого нужно установить права
     * @param permissions         массив прав, которые должны быть определены. Если {@code null},
     *                            то определяются все зарегистрированные права
     */
    void setPermissions(Collection<T> targetDomainObjects, Permission<T>... permissions);

    /**
     * Устанавливает права текущего пользователя на указанные объекты.
     *
     * @param targetDomainObjects объекты, для которого нужно установить права
     * @param mask                битовая маска прав, которые должны быть определены
     */
    void setPermissions(Collection<T> targetDomainObjects, long mask);
}