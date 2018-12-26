package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.PermissivePerson;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configurable
public abstract class PersonPermission extends AbstractPermission<PermissivePerson> {

    @Autowired
    TAUserService taUserService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    RefBookPersonDao refBookPersonDao;

    public static final Permission<PermissivePerson> VIEW = new ViewPermission(1 << 0);

    public static final Permission<PermissivePerson> VIEW_VIP_DATA = new ViewVipDataPermission(1 << 1);

    public static final Permission<PermissivePerson> EDIT = new EditPermission(1 << 2);

    public PersonPermission(long mask) {
        super(mask);
    }

    @Override
    protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
        return false;
    }

    public static final class ViewPermission extends PersonPermission {
        public ViewPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
            return PermissionUtils.hasRole(user, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER);
        }
    }

    public static final class ViewVipDataPermission extends PersonPermission {
        public ViewVipDataPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getCurrentUser();
            return VIEW.isGranted(user, targetDomainObject, logger) && (
                    taUser.hasRole(TARole.N_ROLE_CONTROL_UNP) ||
                            !targetDomainObject.isVip() ||
                            taUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER) &&
                                    isPersonTBsContainsAnyUserTBs(taUser, targetDomainObject)
            );
        }
    }

    public static final class EditPermission extends PersonPermission {
        public EditPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getCurrentUser();
            return VIEW.isGranted(user, targetDomainObject, logger) && (
                    taUser.hasRole(TARole.N_ROLE_CONTROL_UNP) ||
                            taUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER) &&
                                    taUser.hasRoles(TARole.N_ROLE_EDITOR_FL) &&
                                    isPersonTBsContainsAnyUserTBs(taUser, targetDomainObject)
            );
        }
    }

    /**
     * Хотя бы один из Тербанков пользователя находится в списке Тербанков ВерсииФЛ
     */
    protected boolean isPersonTBsContainsAnyUserTBs(TAUser taUser, PermissivePerson targetDomainObject) {
        List<Integer> allAvailableTBIds = departmentService.findAllAvailableTBIds(taUser);
        if (allAvailableTBIds.isEmpty()) {
            return false;
        } else {
            List<Integer> personTbIds = refBookPersonDao.getPersonTbIds(targetDomainObject.getId());
            Set<Integer> intersection = Sets.intersection(new HashSet<>(allAvailableTBIds), new HashSet<>(personTbIds));
            return !intersection.isEmpty();
        }
    }
}
