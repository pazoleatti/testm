package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.filter.DepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.result.RefBookDepartmentDTO;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Реализация сервиса для работы со справочником Подразделения
 */
@Service("refBookDepartmentService")
public class RefBookDepartmentServiceImpl implements RefBookDepartmentService {
    private RefBookDepartmentDao refBookDepartmentDao;
    private DepartmentDao departmentDao;
    private DepartmentService departmentService;

    public RefBookDepartmentServiceImpl(RefBookDepartmentDao refBookDepartmentDao, DepartmentService departmentService, DepartmentDao departmentDao) {
        this.refBookDepartmentDao = refBookDepartmentDao;
        this.departmentService = departmentService;
        this.departmentDao = departmentDao;
    }

    @Override
    public RefBookDepartment fetch(Integer id) {
        return refBookDepartmentDao.fetchDepartmentById(id);
    }

    @Override
    public RefBookDepartment findParentTBById(int id) {
        return refBookDepartmentDao.findParentTBById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RefBookDepartment fetchUserDepartment(TAUser user) {
        return refBookDepartmentDao.fetchDepartmentById(user.getDepartmentId());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> findDepartments(String name, PagingParams pagingParams) {
        return refBookDepartmentDao.findDepartments(name, pagingParams);
    }

    @Override
    public List<RefBookDepartmentDTO> findAllTBWithChildren(String searchPattern, boolean exactSearch) {
        List<RefBookDepartment> departments = refBookDepartmentDao.findAllByNameAsTree(searchPattern, exactSearch);
        List<RefBookDepartmentDTO> dtoList = new ArrayList<>(departments.size());
        for (RefBookDepartment department : departments) {
            dtoList.add(new RefBookDepartmentDTO(department));
        }
        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> fetchAvailableBADepartments(TAUser user, String name, PagingParams pagingParams) {
        List<Integer> declarationDepartments = departmentService.getBADepartmentIds(user);
        return refBookDepartmentDao.fetchDepartments(declarationDepartments, name, pagingParams);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
    public PagingResult<RefBookDepartment> fetchAvailableDestinationDepartments(TAUser user, String name, PagingParams pagingParams) {
        List<Integer> declarationDepartments = departmentService.getDestinationDepartmentIds(user);
        return refBookDepartmentDao.fetchDepartments(declarationDepartments, name, pagingParams);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> findAllByFilter(DepartmentFilter filter, PagingParams pagingParams, TAUser user) {
        List<Integer> availableForUserDepartmentIds = departmentService.findAllAvailableIds(user);
        filter.setIds(availableForUserDepartmentIds);
        return refBookDepartmentDao.findAllByFilter(filter, pagingParams);
    }

    @Override
    public List<RefBookDepartment> fetchActiveAvailableTB(TAUser user) {
        if (user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return refBookDepartmentDao.fetchAllActiveByType(DepartmentType.TERR_BANK);
        } else {
            Integer userTBId = departmentDao.getParentTBId(user.getDepartmentId());
            // Все ТБ, для которых подразделение пользователя назначено исполнителем.
            Set<Integer> TBIds = new HashSet<>(departmentDao.findAllTBIdsByPerformerId(user.getDepartmentId()));
            TBIds.add(userTBId);
            return refBookDepartmentDao.findAllActiveByIds(TBIds);
        }
    }

    @Override
    public List<RefBookDepartment> findTbExcludingPresented(List<Integer> presentedTbidList) {
        return refBookDepartmentDao.findActiveByTypeExcludingPresented(DepartmentType.TERR_BANK, presentedTbidList);
    }
}