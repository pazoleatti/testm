package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для подразделений
 */
@Configurable
public abstract class DepartmentPermission extends AbstractPermission<Department>{
    /**
     * Право на редактирование подразделений
     */
    public static final Permission<Department> EDIT = new EditPermission(1 << 0);
    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public DepartmentPermission(long mask) {
        super(mask);
    }

    /**
     * Право на редактирование подразделений
     */
    public static final class EditPermission extends DepartmentPermission {

        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, Department targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }
}
