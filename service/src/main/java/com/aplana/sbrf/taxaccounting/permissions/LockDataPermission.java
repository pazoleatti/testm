package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Реализация прав для блокировки
 */
@Configurable
public abstract class LockDataPermission extends AbstractPermission<LockData>{
    @Autowired
    protected TAUserService taUserService;
    /**
     * Право на просмотр блокировки
     */
    public static final Permission<LockData> VIEW = new ViewPermission(1 << 0);
    /**
     * Право на удаление блокировки
     */
    public static final Permission<LockData> DELETE = new DeletePermission(1 << 1);
    /**
     * Создает новое право по заданной битовой маске.
     *
     * @param mask битовая маска
     */
    public LockDataPermission(long mask) {
        super(mask);
    }

    /**
     * Право на просмотр блокировки
     */
    public static final class ViewPermission extends LockDataPermission {

        public ViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, LockData targetDomainObject) {
            TAUser taUser = taUserService.getUser(currentUser.getUsername());
            return PermissionUtils.hasRole(currentUser, TARole.N_ROLE_ADMIN) || (targetDomainObject.getUserId() == taUser.getId());
        }
    }

    /**
     * Право на удаление блокировки
     */
    public static final class DeletePermission extends LockDataPermission {

        public DeletePermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User currentUser, LockData targetDomainObject) {
            return VIEW.isGranted(currentUser, targetDomainObject);
        }
    }

}
