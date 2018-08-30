package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Реализация сервиса для работы со справочником Подразделения
 */
@Service
public class RefBookDepartmentDataServiceImpl implements RefBookDepartmentDataService {
    private RefBookDepartmentDataDao refBookDepartmentDataDao;
    private DepartmentDao departmentDao;
    private DepartmentService departmentService;

    public RefBookDepartmentDataServiceImpl(RefBookDepartmentDataDao refBookDepartmentDataDao, DepartmentService departmentService, DepartmentDao departmentDao) {
        this.refBookDepartmentDataDao = refBookDepartmentDataDao;
        this.departmentService = departmentService;
        this.departmentDao = departmentDao;
    }

    @Override
    public RefBookDepartment fetch(Integer id) {
        return refBookDepartmentDataDao.fetchDepartmentById(id);
    }

    /**
     * Получение подразделения пользователя
     *
     * @param user Пользователь
     * @return Подразделение
     */
    @Override
    @Transactional(readOnly = true)
    public RefBookDepartment fetchUserDepartment(TAUser user) {
        return refBookDepartmentDataDao.fetchDepartmentById(user.getDepartmentId());
    }

    /**
     * Получение доступных (согласно правам доступа пользователя)  значений справочника
     *
     * @param user Пользователь
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public List<RefBookDepartment> fetchAllAvailableDepartments(TAUser user) {
        List<Integer> declarationDepartments = departmentService.getTaxFormDepartments(user);
        return refBookDepartmentDataDao.fetchDepartments(declarationDepartments);
    }

    /**
     * Получение доступных (согласно правам доступа пользователя) значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> fetchAvailableDepartments(TAUser user, String name, PagingParams pagingParams) {
        List<Integer> declarationDepartments = departmentService.getTaxFormDepartments(user);
        return refBookDepartmentDataDao.fetchDepartments(declarationDepartments, name, pagingParams);
    }

    /**
     * Получение доступных (согласно правам доступа пользователя) для бизнес-администрирования подразделений с фильтрацией по наименованию и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> fetchAvailableBADepartments(TAUser user, String name, PagingParams pagingParams) {
        List<Integer> declarationDepartments = departmentService.getBADepartmentIds(user);
        return refBookDepartmentDataDao.fetchDepartments(declarationDepartments, name, pagingParams);
    }

    /**
     * Получение подразделений, доступных (согласно правам доступа пользователя) для назначения исполнителями, с фильтрацией по наименованию и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
    public PagingResult<RefBookDepartment> fetchAvailableDestinationDepartments(TAUser user, String name, PagingParams pagingParams) {
        List<Integer> declarationDepartments = departmentService.getDestinationDepartmentIds(user);
        return refBookDepartmentDataDao.fetchDepartments(declarationDepartments, name, pagingParams);
    }

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений справочника, для которых открыт заданный период,
     * с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user           Пользователь
     * @param name           Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                       наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param reportPeriodId ID отчетного периода, который должен быть открыт
     * @param pagingParams   Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'N_ROLE_OPER')")
    public PagingResult<RefBookDepartment> fetchActiveDepartmentsWithOpenPeriod(TAUser user, String name, Integer reportPeriodId, PagingParams pagingParams) {
        List<Integer> departmentsWithOpenPeriod = departmentService.getOpenPeriodDepartments(user, TaxType.NDFL, reportPeriodId);
        return refBookDepartmentDataDao.fetchActiveDepartments(departmentsWithOpenPeriod, name, pagingParams);
    }

    @Override
    public List<RefBookDepartment> fetchActiveAvailableTB(TAUser user) {
        if (user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return refBookDepartmentDataDao.fetchAllActiveByType(DepartmentType.TERR_BANK);
        } else if (user.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER)) {
            Integer userTBId = departmentDao.getParentTBId(user.getDepartmentId());
            // Все ТБ, для которых подразделение пользователя назначено исполнителем.
            Set<Integer> TBIds = new HashSet<>(departmentDao.fetchAllTBIdsByPerformer(user.getDepartmentId()));
            TBIds.add(userTBId);
            return refBookDepartmentDataDao.fetchDepartments(TBIds);
        }
        throw new AccessDeniedException("Недостаточно прав");
    }
}