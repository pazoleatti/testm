package com.aplana.sbrf.taxaccounting.web.spring.permissions;

import com.aplana.sbrf.taxaccounting.model.AuthorisableEntity;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * Базовая абстрактная реализация {@link PermissionSetter}.
 * В классах-наследниках необходимо перегрузить метод {@code setEditableFields} для задания прав на редактирование
 * отдельных полей сущности
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public abstract class AbstractPermissionSetter<T extends AuthorisableEntity> implements PermissionSetter<T> {
    private PermissionFactory<T> permissionFactory;
    private PermissionEvaluator permissionEvaluator;

    /**
     * Устанавливает используемую {@link PermissionFactory}.
     */
    @Required
    public void setPermissionFactory(PermissionFactory<T> permissionFactory) {
        this.permissionFactory = permissionFactory;
    }

    /**
     * Устанавливает используемый {@link PermissionEvaluator}.
     */
    @Required
    public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPermissions(final T targetDomainObject, Permission<T>... permissions) {
        /*if (targetDomainObject == null) {
            throw new CbrError("common.error.message.not.enough.rights");
        }
        Validate.notNull(targetDomainObject);*/

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Permission<T>> retrievedPermissions = permissions == null ?
                permissionFactory.getRegisteredPermissions() : ImmutableSet.copyOf(permissions);
        Iterable<Permission<T>> grantedPermissions = Iterables.filter(retrievedPermissions, new Predicate<Permission<T>>() {
            @Override
            public boolean apply(Permission<T> input) {
                return permissionEvaluator.hasPermission(authentication, targetDomainObject, input);
            }
        });
        long permissionMask = 0L;
        for (Permission<T> permission : grantedPermissions) {
            permissionMask |= permission.getMask();
        }
        targetDomainObject.setPermissions(permissionMask);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPermissions(final T targetDomainObject, long mask) {
        setPermissions(targetDomainObject, Iterables.toArray(permissionFactory.buildFromMask(mask), Permission.class));
    }

    @Override
    public void setPermissions(Collection<T> targetDomainObjects, Permission<T>... permissions) {
        Validate.notEmpty(targetDomainObjects);

        for (T targetDomainObject : targetDomainObjects) {
            setPermissions(targetDomainObject, permissions);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPermissions(Collection<T> targetDomainObjects, long mask) {
        setPermissions(targetDomainObjects, Iterables.toArray(permissionFactory.buildFromMask(mask), Permission.class));
    }
}