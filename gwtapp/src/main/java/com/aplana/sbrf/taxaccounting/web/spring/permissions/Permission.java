package com.aplana.sbrf.taxaccounting.web.spring.permissions;

import com.aplana.sbrf.taxaccounting.model.TAUser;
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
     * @return {@code true}, если у пользователя есть данное право на объект; {@code false} в противном случае
     */
    boolean isGranted(User user, T targetDomainObject);
}