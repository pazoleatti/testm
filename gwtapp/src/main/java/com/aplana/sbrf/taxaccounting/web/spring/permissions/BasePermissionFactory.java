package com.aplana.sbrf.taxaccounting.web.spring.permissions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.DisposableBean;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Базовая реализация {@link PermissionFactory}.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
 */
public class BasePermissionFactory<T> implements PermissionFactory<T>, DisposableBean {
    private final Map<Long, Permission<T>> registeredPermissions = Maps.newHashMap();

    /**
     * Регистрирует права, объявленные в заданном классе.
     *
     * @param clazz класс, реализующий интерфейс {@link Permission}, содержащий требуемые права
     */
    public BasePermissionFactory(Class<? extends Permission> clazz) {
        registerPublicPermissions(clazz);
    }

    @Override
    public List<Permission<T>> buildFromMask(long mask) {
        if (registeredPermissions.containsKey(mask)) {
            // требуемая маска соответствует зарегистрированному праву
            return ImmutableList.of(registeredPermissions.get(mask));
        }

        List<Permission<T>> permissions = Lists.newArrayList();
        for (long i = 0; i < 64; i++) {
            long permissionToCheck = 1L << i;
            if ((mask & permissionToCheck) == permissionToCheck) {
                Permission<T> p = registeredPermissions.get(permissionToCheck);
                if (p == null) {
                    throw new IllegalStateException("Mask '" + permissionToCheck +
                            "' does not have a corresponding static permission");
                }
                permissions.add(p);
            }
        }

        return permissions;
    }

    @Override
    public List<Permission<T>> getRegisteredPermissions() {
        return ImmutableList.copyOf(Iterables.transform(registeredPermissions.entrySet(),
                new Function<Map.Entry<Long, Permission<T>>, Permission<T>>() {
                    @Override
                    public Permission<T> apply(Map.Entry<Long, Permission<T>> input) {
                        return input.getValue();
                    }
                }
        ));
    }

    /**
     * Регистрирует публичные статические поля типа {@link Permission} для заданного класса.
     *
     * @param clazz класс, реализующий интерфейс {@link Permission}, объявляющий статические права
     */
    @SuppressWarnings("unchecked")
    protected void registerPublicPermissions(Class<? extends Permission> clazz) {
        Validate.notNull(clazz);

        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(null);
                if (Permission.class.isAssignableFrom(fieldValue.getClass())) {
                    // найдено статическое поле Permission
                    Permission<T> permission = (Permission<T>) fieldValue;
                    registerPermission(permission);
                }
            } catch (Exception e) {
                // игронируем исключения
            }
        }
    }

    /**
     * Регистрирует право.
     *
     * @param permission право, требующее регистрации
     */
    protected void registerPermission(Permission<T> permission) {
        Validate.notNull(permission);

        // проверяем уникальность маски
        long mask = permission.getMask();
        Validate.isTrue(!registeredPermissions.containsKey(mask),
                "An existing permission already provides mask '%d'", mask);

        registeredPermissions.put(mask, permission);
    }

    @Override
    public void destroy() throws Exception {
        for(Permission<T> permission:registeredPermissions.values()){
            if(permission instanceof DisposableBean){
                ((DisposableBean) permission).destroy();
            }
        }
    }
}