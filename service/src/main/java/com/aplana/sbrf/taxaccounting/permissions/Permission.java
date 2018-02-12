package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.security.core.userdetails.User;

/**
 * Право, выданное какой-либо сущности.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public interface Permission<T> {
    /**
     * Возвращает битовую маску, представляющую право.
     */
    long getMask();

    /**
     * Определяет, дано ли право заданному пользователю на заданный объект.
     *
     * @param user               пользователь, для которого проверяется право
     * @param targetDomainObject объект, для которого проверяется право
     * @param logger             объект {@link com.aplana.sbrf.taxaccounting.model.log.Logger} для записи результата,
     *                           может быть {@code null} если не требуется логгирование
     * @return {@code true}, если у пользователя есть данное право на объект; {@code false} в противном случае
     */
    boolean isGranted(User user, T targetDomainObject, Logger logger);
}