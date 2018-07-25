package com.aplana.sbrf.taxaccounting.permissions;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

@Configurable
public abstract class DepartmentReportPeriodPermission extends AbstractPermission<DepartmentReportPeriod>{

    /**
     * Право на открытие периодов
     */
    public static final Permission<DepartmentReportPeriod> OPEN = new OpenPermission(1 << 0);

    /**
     * Право на удаление периодов
     */
    public static final Permission<DepartmentReportPeriod> DELETE = new DeletePermission(1 << 1);

    /**
     * Право на закрытие периодов
     */
    public static final Permission<DepartmentReportPeriod> CLOSE = new ClosePermission(1 << 2);

    /**
     * Право на окткрытие корректирующего периода
     */
    public static final Permission<DepartmentReportPeriod> OPEN_CORRECT = new OpenCorrectPermission(1 << 3);

    /**
     * Право на назначение срока сдачи периода
     */
    public static final Permission<DepartmentReportPeriod> DEADLINE = new DeadlinePermission(1 << 4);

    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public DepartmentReportPeriodPermission(long mask) {
        super(mask);
    }

    /**
     * Право на открытие перида
     */
    public static final class OpenPermission extends DepartmentReportPeriodPermission {

        public OpenPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на удаление периода
     */
    public static final class DeletePermission extends DepartmentReportPeriodPermission {

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на закртие периода
     */
    public static final class ClosePermission extends DepartmentReportPeriodPermission {

        public ClosePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return targetDomainObject.isActive() && PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на открытие корректирующего периода
     */
    public static final class OpenCorrectPermission extends DepartmentReportPeriodPermission {

        public OpenCorrectPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return !targetDomainObject.isActive() && targetDomainObject.getCorrectionDate() == null && PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }

    /**
     * Право на назначение срока сдачи периода
     */
    public static final class DeadlinePermission extends DepartmentReportPeriodPermission {

        public DeadlinePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return targetDomainObject.isActive() && targetDomainObject.getCorrectionDate() == null && PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP);
        }
    }
}
