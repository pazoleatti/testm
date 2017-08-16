package com.aplana.sbrf.taxaccounting.permissions;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.core.userdetails.User;

/**
 * Абстракный суперкласс для реализации {@link Permission}.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
@JsonSerialize
public abstract class AbstractPermission<T> implements Permission<T> {
    protected long mask;

    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public AbstractPermission(long mask) {
        this.mask = mask;
    }

    @Override
    public long getMask() {
        return mask;
    }

    @Override
    /**
     * Если пользователь имеет роль Суперпользователь - он имеет права на любые операции. Для всех других - выполняется проверка
     * {@code isGrantedInternal}
     */
    public boolean isGranted(User user, T targetDomainObject) {
        return isGrantedInternal(user, targetDomainObject);
    }

    /**
     * Внутренняя проверка права заданного пользователю на объект.
     *
     * @param user               пользователь, для которого проверяется право
     * @param targetDomainObject объект, для которого проверяется право
     * @return {@code true}, если у пользователя есть право на объект; {@code false} в противном случае
     */
    protected abstract boolean isGrantedInternal(User user, T targetDomainObject);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPermission<?> that = (AbstractPermission<?>) o;

        return mask == that.mask;
    }

    @Override
    public int hashCode() {
        return (int) (mask ^ (mask >>> 32));
    }

    @Override
    public String toString() {
        return "AbstractPermission{" +
                "mask=" + mask +
                '}';
    }
}