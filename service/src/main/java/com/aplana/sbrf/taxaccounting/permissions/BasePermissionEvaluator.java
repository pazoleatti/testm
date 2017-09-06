package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.PermissionDao;
import com.aplana.sbrf.taxaccounting.dao.impl.PermissionDaoFactory;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Базовая реализация {@code PermissionEvaluator} для проверки доступа к методам с помощью аннотаций
 * <a href="http://projects.spring.io/spring-security/">Spring Security</a>.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public class BasePermissionEvaluator implements PermissionEvaluator {

    private static final Log LOG = LogFactory.getLog(BasePermissionEvaluator.class);

    @Autowired
    private PermissionDaoFactory permissionDaoFactory;

    private Map<Class<?>, PermissionFactory<?>> permissionFactoryRegistry;
    /**
     * Устанавливает используемый регистр {@link PermissionFactory}.
     */
    @Required
    public void setPermissionFactoryRegistry(Map<Class<?>, PermissionFactory<?>> permissionFactoryRegistry) {
        this.permissionFactoryRegistry = permissionFactoryRegistry;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object object, Object permission) {
        Class objectClass = object.getClass();

        if (Iterable.class.isAssignableFrom(objectClass)) {
            Iterable iterable = (Iterable) object;

            boolean result = true;
            for (Object targetDomainObject : iterable) {
                result &= hasPermissionSingle(authentication, targetDomainObject, permission);
            }

            return result;
        }
        if (objectClass.isArray()) {
            Object[] array = (Object[]) object;

            boolean result = true;
            for (Object targetDomainObject : array) {
                result &= hasPermissionSingle(authentication, targetDomainObject, permission);
            }

            return result;
        } else {
            return hasPermissionSingle(authentication, object, permission);
        }
    }

    public boolean hasPermissionSingle(Authentication authentication, Object targetDomainObject, Object permission) {
        return checkPermission(authentication, targetDomainObject, permission);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasPermission(Authentication authentication, Serializable target,
                                 String targetType, Object permission) {
        Class targetClass = target.getClass();

        if (Iterable.class.isAssignableFrom(targetClass)) {
            Iterable<Serializable> iterable = (Iterable<Serializable>) target;

            boolean result = true;
            for (Serializable targetId : iterable) {
                result &= hasPermissionSingleId(authentication, targetId, targetType, permission);
            }

            return result;
        }
        if (target instanceof long[]) {
            long[] array = (long[]) target;

            boolean result = true;
            for (long targetId : array) {
                result &= hasPermissionSingleId(authentication, targetId, targetType, permission);
            }

            return result;
        }
        if (target instanceof int[]) {
            int[] array = (int[]) target;

            boolean result = true;
            for (int targetId : array) {
                result &= hasPermissionSingleId(authentication, targetId, targetType, permission);
            }

            return result;
        }
        if (targetClass.isArray()) {
            Serializable[] array = (Serializable[]) target;

            boolean result = true;
            for (Serializable targetId : array) {
                result &= hasPermissionSingleId(authentication, targetId, targetType, permission);
            }

            return result;
        } else {
            return hasPermissionSingleId(authentication, target, targetType, permission);
        }
    }

    private boolean hasPermissionSingleId(Authentication authentication, Serializable targetId,
                                          String targetType, Object permission) {
        Class<SecuredEntity> targetClass;
        try {
            targetClass = (Class<SecuredEntity>) ClassUtils.getClass(targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class represented by '" + targetType + "' cannot be instantiated", e);
        }

        PermissionFactory<?> permissionFactory = getPermissionFactoryOrNull(targetClass);
        if (permissionFactory == null) {
            throw new IllegalStateException("No permission factory is registered for class '" + targetType + "'");
        }

        PermissionDao permissionDao = permissionDaoFactory.getPermissionDao(targetClass);
        SecuredEntity targetDomainObject = permissionDao.getSecuredEntity((Long) targetId);

        if (targetDomainObject == null) {
            throw new RuntimeException("Object of type '" + targetType +
                    "' with identity '" + targetId + "' not found");
        }
        return checkPermission(authentication, targetDomainObject, permission);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean checkPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        final boolean debug = LOG.isDebugEnabled();
        if (debug) {
            LOG.debug("Checking permission '" + permission + "' for object '" + targetDomainObject);
        }

        // определяем текущего пользователя
        if (authentication == null || !authentication.isAuthenticated()) {
            LOG.debug("No user is authenticated. Access is denied");
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new IllegalStateException("Principal is not a user");
        }

        PermissionFactory<T> permissionFactory = (PermissionFactory<T>) getPermissionFactoryOrNull(
                targetDomainObject.getClass());
        if (permissionFactory == null) {
            throw new IllegalStateException("No permission factory is registered for class '" +
                    targetDomainObject.getClass() + "'");
        }

        boolean granted = true;
        for (Permission<T> p : resolvePermission(permissionFactory, permission)) {
            if (!p.isGranted((User) principal, (T) targetDomainObject)) {
                granted = false;
                break;
            }
        }

        if (debug) {
            LOG.debug(granted ? "Access is granted" : "Access is denied");
        }
        return granted;
    }

    @SuppressWarnings("unchecked")
    private <T> List<Permission<T>> resolvePermission(PermissionFactory<T> permissionFactory, Object permission) {
        if (permission instanceof Integer || permission instanceof Long) {
            return permissionFactory.buildFromMask(((Number) permission).longValue());
        }

        if (permission instanceof Permission) {
            return ImmutableList.of((Permission<T>) permission);
        }

        if (permission instanceof Permission[]) {
            return ImmutableList.copyOf((Permission<T>[]) permission);
        }

        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }

    private PermissionFactory<?> getPermissionFactoryOrNull(Class<?> clazz) {
        if (clazz == Object.class || clazz == null) {
            return null;
        }

        PermissionFactory<?> result = permissionFactoryRegistry.get(clazz);
        if (result == null) {
            result = getPermissionFactoryOrNull(clazz.getSuperclass());
            if (result == null) {
                Class<?>[] interfaces = clazz.getInterfaces();
                for (int i = 0; result == null && i < interfaces.length; i++) {
                    result = getPermissionFactoryOrNull(interfaces[i]);
                }
            }
        }
        return result;
    }
}