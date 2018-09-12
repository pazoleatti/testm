package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;
import java.util.Set;

@Configurable
public abstract class DepartmentConfigPermission extends AbstractPermission<DepartmentConfig> {

    @Autowired
    protected TAUserService taUserService;
    @Autowired
    protected DepartmentDao departmentDao;

    /**
     * Право изменение записи настроек подразделений
     */
    public static final Permission<DepartmentConfig> CREATE = new CreatePermission(1 << 1);

    /**
     * Право изменение записи настроек подразделений
     */
    public static final Permission<DepartmentConfig> UPDATE = new UpdatePermission(1 << 2);

    /**
     * Право удаление записи настроек подразделений
     */
    public static final Permission<DepartmentConfig> DELETE = new DeletePermission(1 << 3);


    public DepartmentConfigPermission(long mask) {
        super(mask);
    }


    public static final class CreatePermission extends DepartmentConfigPermission {

        public CreatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentConfig targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS);
        }
    }


    public static final class UpdatePermission extends DepartmentConfigPermission {

        public UpdatePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentConfig targetDomainObject, Logger logger) {
            return super.isGrantedInternal(currentUser, targetDomainObject, logger);
        }
    }

    public static final class DeletePermission extends DepartmentConfigPermission {

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, DepartmentConfig targetDomainObject, Logger logger) {
            return super.isGrantedInternal(currentUser, targetDomainObject, logger);
        }
    }

    @Override
    protected boolean isGrantedInternal(User currentUser, DepartmentConfig targetDomainObject, Logger logger) {
        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
            return true;
        } else if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS)) {
            // ТерБанк периода
            Integer objectTBId = departmentDao.getParentTBId(targetDomainObject.getDepartment().getId());
            TAUser user = taUserService.getUser(currentUser.getUsername());
            Integer userTBId = departmentDao.getParentTBId(user.getDepartmentId());
            // ТерБанк пользователя и ТерБанки, куда назначено подразделение пользователя исполнителем
            Set<Integer> TBIds = new HashSet<>(departmentDao.fetchAllTBIdsByPerformer(user.getDepartmentId()));
            TBIds.add(userTBId);
            return TBIds.contains(objectTBId);
        }
        return false;
    }
}
