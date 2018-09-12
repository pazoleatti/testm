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
public abstract class PersonVipDataPermission extends AbstractPermission<PermissivePerson> {

    @Autowired
    TAUserService taUserService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    RefBookPersonDao refBookPersonDao;

    public static final Permission<PermissivePerson> VIEW_VIP_DATA = new ViewVipDataPermission(1 << 0);

    public PersonVipDataPermission(long mask) {
        super(mask);
    }

    @Override
    protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
        return false;
    }

    public static final class ViewVipDataPermission extends PersonVipDataPermission {
        public ViewVipDataPermission(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, PermissivePerson targetDomainObject, Logger logger) {
            TAUser taUser = taUserService.getCurrentUser();
            if (taUser.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
                return true;
            }

            List<Integer> departmentsAvailableToUser = departmentService.getBADepartmentIds(taUser);

            if (departmentsAvailableToUser.isEmpty()) {
                return false;
            } else {
                Set<Integer> permittedDepartmentsSet = new HashSet<>(departmentsAvailableToUser);
                if (targetDomainObject.getVip()) {
                    List<Integer> vipDepartments = refBookPersonDao.getPersonTbIds(targetDomainObject.getId());
                    Set<Integer> vipDepartmentsSet = new HashSet<>(vipDepartments);
                    Set<Integer> intersection = Sets.intersection(permittedDepartmentsSet, vipDepartmentsSet);
                    if (!intersection.isEmpty()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
    }
}
