package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Реализация сервиса для работы со справочником Подразделения
 */
@Service
public class RefBookDepartmentDataServiceImpl implements RefBookDepartmentDataService {
    private RefBookDepartmentDataDao refBookDepartmentDataDao;
    private DepartmentService departmentService;

    public RefBookDepartmentDataServiceImpl(RefBookDepartmentDataDao refBookDepartmentDataDao, DepartmentService departmentService) {
        this.refBookDepartmentDataDao = refBookDepartmentDataDao;
        this.departmentService = departmentService;
    }

    //TODO https://jira.aplana.com/browse/SBRFNDFL-2008

    /**
     * Получение всех доступных значений справочника
     *
     * @param user Пользователь
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookDepartment> fetchAllAvailableDepartments(TAUser user) {
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            List<Integer> taxFormDepartments = departmentService.getTaxFormDepartments(user, TaxType.NDFL, null, null);
            Set<Integer> departmentIds = departmentService.getRequiredForTreeDepartments(new HashSet<Integer>(taxFormDepartments)).keySet();
            return refBookDepartmentDataDao.fetchDepartments(departmentIds);
        } else {
            throw new AccessDeniedException("Недостаточно прав для поиска налоговых форм");
        }
    }

    /**
     * Получение доступных значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user         Пользователь
     * @param name         Наименование подразделения
     * @param pagingParams Параметры пейджинга
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public PagingResult<RefBookDepartment> fetchAvailableDepartments(TAUser user, String name, PagingParams pagingParams) {
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            List<Integer> declarationDepartments = departmentService.getTaxFormDepartments(user, TaxType.NDFL, null, null);
            Set<Integer> departmentIds = departmentService.getRequiredForTreeDepartments(new HashSet<Integer>(declarationDepartments)).keySet();
            return refBookDepartmentDataDao.fetchDepartments(departmentIds, name, pagingParams);
        } else {
            throw new AccessDeniedException("Недостаточно прав для поиска налоговых форм");
        }
    }

    /**
     * Получение доступных значений справочника, для которых открыт заданный период, с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user           Пользователь
     * @param name           Наименование подразделения
     * @param reportPeriodId ID отчетного периода, который должен быть открыт
     * @param pagingParams   Параметры пейджинга
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public PagingResult<RefBookDepartment> fetchDepartmentsWithOpenPeriod(TAUser user, String name, Integer reportPeriodId, PagingParams pagingParams) {
        if (user.hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            List<Integer> departmentsWithOpenPeriod = departmentService.getOpenPeriodDepartments(user, TaxType.NDFL, reportPeriodId);
            Set<Integer> departmentIds = departmentService.getRequiredForTreeDepartments(new HashSet<Integer>(departmentsWithOpenPeriod)).keySet();
            return refBookDepartmentDataDao.fetchDepartments(departmentIds, name, pagingParams);
        } else {
            throw new AccessDeniedException("Недостаточно прав для поиска налоговых форм");
        }
    }
}
