package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

@Configurable
public abstract class ConfigurationPermission extends AbstractPermission<Configuration> {

    /**
     * Право на просмотр конфигураций
     */
    public static final Permission<Configuration> VIEW = new ViewPermission(1 << 0);

    /**
     * Право на редактирование конфигураций
     */
    public static final Permission<Configuration> EDIT = new EditPermission(1 << 1);

    /**
     * Право на удаление конфигураций
     */
    public static final Permission<Configuration> REMOVE = new RemovePermission(1 << 2);

    /**
     * Право на восстановление параметров конфигураций по умолчанию
     */
    public static final Permission<Configuration> DEFAULT = new DefaultPermission(1 << 3);

    /**
     * Право на создание конфигураций
     */
    public static final Permission<Configuration> CREATE = new CreatePermission(1 << 4);

    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public ConfigurationPermission(long mask) {
        super(mask);
    }

    /**
     * Право на просмотр конфигураций
     */
    public static final class ViewPermission extends ConfigurationPermission {

        public ViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            if (targetDomainObject != null) {
                if (targetDomainObject.getId() != null) {
                    return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
                }
                ConfigurationParam param = ConfigurationParam.valueOf(targetDomainObject.getCode());
                if (param != null) {
                    switch (param.getGroup()) {
                        case COMMON: {
                            return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
                        }
                        case COMMON_PARAM: {
                            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
                        }
                        default:
                            return false;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Право на редактирование конфигураций
     */
    public static final class EditPermission extends ConfigurationPermission {

        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            if (targetDomainObject != null) {
                if (targetDomainObject.getId() != null) {
                    return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
                }
                ConfigurationParam param = ConfigurationParam.valueOf(targetDomainObject.getCode());
                switch (param.getGroup()) {
                    case COMMON: {
                        return PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
                    }
                    case COMMON_PARAM: {
                        return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP);
                    }
                    default:
                        return false;
                }
            }
            return false;
        }
    }

    /**
     * Право на удаление конфигураций
     */
    public static final class RemovePermission extends ConfigurationPermission {

        public RemovePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            return targetDomainObject != null && PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
        }
    }

    /**
     * Право на восстановление параметров конфигураций по умолчанию
     */
    public static final class DefaultPermission extends ConfigurationPermission {

        public DefaultPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            return targetDomainObject != null && PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на создание конфигураций
     */
    public static final class CreatePermission extends ConfigurationPermission {

        public CreatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            return targetDomainObject != null && PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
        }
    }
}
