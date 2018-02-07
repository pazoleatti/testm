package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

@Configurable
public abstract class ConfigurationPermission extends AbstractPermission<Configuration>{

    /**
     * Право на редактирование конфигурации
     */
    public static final Permission<Configuration> EDIT = new ConfigurationPermission.EditPermission(1 << 0);

    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public ConfigurationPermission(long mask) {
        super(mask);
    }

    /**
     * Право на редактирование конфигурации
     */
    public static final class EditPermission extends ConfigurationPermission {

        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Configuration targetDomainObject, Logger logger) {
            return targetDomainObject != null && PermissionUtils.hasRole(user, TARole.ROLE_ADMIN);
        }
    }
}
