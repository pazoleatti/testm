package com.aplana.sbrf.taxaccounting.permissions;


import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;
import java.util.Set;

@Configurable
public abstract class DepartmentReportPeriodPermission extends AbstractPermission<DepartmentReportPeriod> {

    @Autowired
    protected TAUserService taUserService;
    @Autowired
    protected DepartmentDao departmentDao;

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
    public static final Permission<DepartmentReportPeriod> REOPEN = new ReopenPermission(1 << 4);

    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public DepartmentReportPeriodPermission(long mask) {
        super(mask);
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
            return super.isGrantedInternal(currentUser, targetDomainObject, logger);
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
            return super.isGrantedInternal(currentUser, targetDomainObject, logger);
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
            return !targetDomainObject.isActive() && targetDomainObject.getCorrectionDate() == null &&
                    super.isGrantedInternal(currentUser, targetDomainObject, logger);
        }
    }

    /**
     * Право на назначение срока сдачи периода
     */
    public static final class ReopenPermission extends DepartmentReportPeriodPermission {

        public ReopenPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
            return super.isGrantedInternal(currentUser, targetDomainObject, logger);
        }
    }

    @Override
    protected boolean isGrantedInternal(User currentUser, DepartmentReportPeriod targetDomainObject, Logger logger) {
        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
            return true;
        } else if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
            // ТерБанк периода
            Integer reportPeriodTBId = departmentDao.getParentTBId(targetDomainObject.getDepartmentId());
            TAUser user = taUserService.getUser(currentUser.getUsername());
            Integer userTBId = departmentDao.getParentTBId(user.getDepartmentId());
            // ТерБанк пользователя и ТерБанки, куда назначено подразделение пользователя исполнителем
            Set<Integer> TBIds = new HashSet<>(departmentDao.fetchAllTBIdsByPerformer(user.getDepartmentId()));
            TBIds.add(userTBId);
            return TBIds.contains(reportPeriodTBId);
        }
        return false;
    }
}
