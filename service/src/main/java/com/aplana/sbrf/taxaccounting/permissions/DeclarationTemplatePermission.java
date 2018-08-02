package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для макетов декларации.
 */
@Configurable
public abstract class DeclarationTemplatePermission extends AbstractPermission<DeclarationTemplate> {

    /**
     * Право на просмотр макета декларации
     */
    public static final Permission<DeclarationTemplate> VIEW = new ViewPermission(1 << 1);

    /**
     * Право на изменение макета декларации
     */
    public static final Permission<DeclarationTemplate> UPDATE = new UpdatePermission(1 << 2);


    public DeclarationTemplatePermission(long mask) {
        super(mask);
    }

    /**
     * Право на просмотр макета декларации
     */
    public static final class ViewPermission extends DeclarationTemplatePermission {

        public ViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationTemplate targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONF);
        }
    }

    /**
     * Право на изменение макета декларации
     */
    public static final class UpdatePermission extends DeclarationTemplatePermission {

        public UpdatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DeclarationTemplate targetDomainObject, Logger logger) {
            return VIEW.isGranted(currentUser, targetDomainObject, logger);
        }
    }
}
