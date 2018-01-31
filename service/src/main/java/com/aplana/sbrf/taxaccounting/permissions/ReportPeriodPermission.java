package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для ведения периодов
 */
@Configurable
public abstract class ReportPeriodPermission extends AbstractPermission<ReportPeriod>{
    /**
     * Право на редактирование периодов
     */
    public static final Permission<ReportPeriod> EDIT = new EditPermission(1 << 0);
    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public ReportPeriodPermission(long mask) {
        super(mask);
    }

    /**
     * Право на редактирование периодов
     */
    public static final class EditPermission extends ReportPeriodPermission {

        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, ReportPeriod targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }
}
